package screret.bejs.recipe;

import com.google.common.cache.AbstractLoadingCache;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.KubeJSRegistries;
import dev.latvian.mods.kubejs.platform.forge.ingredient.IngredientStackImpl;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import screret.bejs.BEJSPlugin;
import screret.bejs.BeJS;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class ProcessingRecipe implements Recipe<Container> {
    final RecipeTypeBuilder builder;
    final RecipeType<?> type;
    final ResourceLocation id;
    final NonNullList<IngredientStackImpl> itemIngredients;
    final NonNullList<FluidIngredient> fluidIngredients;
    final NonNullList<ItemStack> itemResults;
    final NonNullList<FluidStack> fluidResults;
    final int processingTime;
    final int energy;

    public ProcessingRecipe(RecipeTypeBuilder builder, ResourceLocation id, NonNullList<ItemStack> itemResults, NonNullList<FluidStack> fluidResults, NonNullList<IngredientStackImpl> itemIngredients, NonNullList<FluidIngredient> fluidIngredients, int processingTime, int energy) {
        this.builder = builder;
        this.type = builder.get();
        this.id = id;
        this.itemResults = itemResults;
        this.fluidResults = fluidResults;
        this.itemIngredients = itemIngredients;
        this.fluidIngredients = fluidIngredients;
        this.processingTime = processingTime;
        this.energy = energy;
    }

    @Override
    public boolean matches(Container container, Level pLevel) {
        List<ItemStack> inputs = new ArrayList<>();

        for(int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemstack = container.getItem(i);
            if (!itemstack.isEmpty()) {
                inputs.add(itemstack);
                //inputs.put(stackCache.getUnchecked(itemstack), inputs.getOrDefault(itemstack, 0) + itemstack.getCount());
            }
            //BeJS.LOGGER.debug(inputs);
        }

        return inputsMatch(this.itemIngredients, inputs);
    }

    @Nonnull
    @Override
    public ItemStack assemble(Container pContainer) {
        return this.builder.assemblerFunction != null ? this.builder.assemblerFunction.assemble(this, pContainer) : this.itemResults.get(0).copy();
    }

    public NonNullList<IngredientStackImpl> getItemInputs() {
        return itemIngredients;
    }

    public NonNullList<ItemStack> getItemResults() {
        NonNullList<ItemStack> resultsCopy = NonNullList.withSize(itemResults.size(), ItemStack.EMPTY);
        for (int i = 0; i < resultsCopy.size(); ++i) {
            resultsCopy.set(i, itemResults.get(i).copy());
        }

        return resultsCopy;
    }

    public NonNullList<FluidIngredient> getFluidInputs() {
        return fluidIngredients;
    }

    public NonNullList<FluidStack> getFluidResults() {
        NonNullList<FluidStack> resultsCopy = NonNullList.withSize(fluidResults.size(), FluidStack.EMPTY);
        for (int i = 0; i < resultsCopy.size(); ++i) {
            resultsCopy.set(i, fluidResults.get(i).copy());
        }
        return resultsCopy;
    }

    public int getEnergy() {
        return this.energy;
    }

    public int getProcessingTime() {
        return this.processingTime;
    }

    @Nonnull
    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight <= this.builder.inputSlotCount;
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return builder.toastSymbol == null ? Recipe.super.getToastSymbol() : builder.toastSymbol;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return this.itemResults.get(0);
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return KubeJSRegistries.recipeSerializers().get(builder.id);
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    private static boolean inputsMatch(List<IngredientStackImpl> ingredients, List<ItemStack> inputs) {
        if(inputs == null || inputs.isEmpty()) {
            return false;
        }
        if (inputs.size() < ingredients.size()) {
            return false;
        }
        Set<IngredientStackImpl> ingredientStacks = new HashSet<>(ingredients);

        for(var element : inputs) {
            if(ingredientStacks.isEmpty()) return true;

            AtomicReference<IngredientStackImpl> current = new AtomicReference<>();
            boolean valid = ingredientStacks.stream().anyMatch(ingredient -> {
                if (ingredient.test(element) && element.getCount() >= ingredient.getCount()) {
                    current.set(ingredient);
                    return true;
                }
                return false;
            });
            if (valid) {
                ingredientStacks.remove(current.get());
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean outputsFit(List<ItemStack> current, List<ItemStack> outputs) {
        if(current == null || current.isEmpty()) {
            return true;
        }
        Set<ItemStack> currentStacks = new HashSet<>(current);
        for(ItemStack element : outputs) {
            AtomicReference<ItemStack> currentOutput = new AtomicReference<>();
            boolean valid = currentStacks.stream().anyMatch(stack -> {
                if ((stack.isEmpty() || element.isEmpty()) || (stack.is(element.getItem()) && element.getCount() + stack.getCount() < stack.getMaxStackSize())) {
                    currentOutput.set(stack);
                    BeJS.LOGGER.debug("valid: true; input: " + stack + "; to add: " + element);
                    return true;
                }
                BeJS.LOGGER.debug("valid: false; input: " + stack + "; to add: " + element);
                return false;
            });
            if (valid) {
                currentStacks.remove(currentOutput.get());
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public static Map<IngredientStackImpl, Integer> getInputStackIndexMap(IItemHandler current, List<IngredientStackImpl> inputs) {
        Map<IngredientStackImpl, Integer> map = new HashMap<>();

        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < current.getSlots(); ++i) {
            stacks.add(current.getStackInSlot(i));
        }

        Set<ItemStack> currentStacks = new HashSet<>(stacks);
        for (IngredientStackImpl stack : inputs) {
            var possible = currentStacks.stream().filter(stack).findFirst();
            if (possible.isPresent()) {
                ItemStack result = possible.get();
                int index = stacks.indexOf(result);
                currentStacks.remove(result);
                map.put(stack, index);
            }
        }

        return map;
    }

    public static Map<ItemStack, Integer> getOutputStackIndexMap(IItemHandler current, List<ItemStack> inputs) {
        Map<ItemStack, Integer> map = new HashMap<>();

        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < current.getSlots(); ++i) {
            stacks.add(current.getStackInSlot(i));
        }

        Set<ItemStack> currentStacks = new HashSet<>(stacks);
        for (ItemStack stack : inputs) {
            var possible = currentStacks.stream().filter(itemStack -> stack.is(itemStack.getItem())).findFirst();
            if (possible.isPresent()) {
                ItemStack result = possible.get();
                int index = stacks.indexOf(result);
                BeJS.LOGGER.debug("result: " + result + "; index: " + index);
                currentStacks.remove(result);
                map.put(stack, index);
            }
        }

        return map;
    }


    public static class Serializer implements RecipeSerializer<ProcessingRecipe> {
        public static final String INPUT_ITEM_KEY = "inputItems", OUTPUT_ITEM_KEY = "resultItems", INPUT_FLUID_KEY = "inputFluids", OUTPUT_FLUID_KEY = "resultFluids", ENERGY_KEY = "energy", TIME_KEY = "processingTime";

        public static final IngredientStackImpl EMPTY = new IngredientStackImpl(Ingredient.EMPTY, 0);

        public ProcessingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            String typeID = GsonHelper.getAsString(pJson, "type", "");
            NonNullList<IngredientStackImpl> itemInput = ingredientsFromJson(GsonHelper.getAsJsonArray(pJson, INPUT_ITEM_KEY));
            NonNullList<FluidIngredient> fluidInput = fluidIngredientsFromJson(GsonHelper.getAsJsonArray(pJson, INPUT_FLUID_KEY));
            int time = GsonHelper.getAsInt(pJson, TIME_KEY, 200);
            int energy = GsonHelper.getAsInt(pJson, ENERGY_KEY, 0);
            if (itemInput.isEmpty()) {
                throw new JsonParseException("No ingredients for beJS custom recipe");
            } else {
                NonNullList<ItemStack> itemResult = resultsFromJson(GsonHelper.getAsJsonArray(pJson, OUTPUT_ITEM_KEY));
                NonNullList<FluidStack> fluidResult = fluidResultsFromJson(GsonHelper.getAsJsonArray(pJson, OUTPUT_FLUID_KEY));
                return new ProcessingRecipe((RecipeTypeBuilder) BEJSPlugin.RECIPE_TYPE.objects.get(new ResourceLocation(typeID)), pRecipeId, itemResult, fluidResult, itemInput, fluidInput, time, energy);
            }
        }

        public static NonNullList<FluidIngredient> fluidIngredientsFromJson(JsonArray pIngredientArray) {
            NonNullList<FluidIngredient> items = NonNullList.create();

            for(int i = 0; i < pIngredientArray.size(); ++i) {
                FluidIngredient ingredient = FluidIngredient.fromJson(pIngredientArray.get(i));
                items.add(ingredient);
            }

            return items;
        }

        public static NonNullList<IngredientStackImpl> ingredientsFromJson(JsonArray pIngredientArray) {
            NonNullList<IngredientStackImpl> items = NonNullList.create();

            for(int i = 0; i < pIngredientArray.size(); ++i) {
                JsonElement element = pIngredientArray.get(i);
                items.add(IngredientStackImpl.SERIALIZER.fromJson().apply(element.getAsJsonObject()));
            }

            return items;
        }

        public static NonNullList<FluidStack> fluidResultsFromJson(JsonArray pIngredientArray) {
            NonNullList<FluidStack> results = NonNullList.create();

            for(int i = 0; i < pIngredientArray.size(); ++i) {
                FluidStack stack = FluidStack.CODEC.parse(JsonOps.INSTANCE, pIngredientArray.get(i).getAsJsonObject()).result().orElse(FluidStack.EMPTY);
                if (!stack.isEmpty()) {
                    results.add(stack);
                }
            }

            return results;
        }

        public static NonNullList<ItemStack> resultsFromJson(JsonArray pIngredientArray) {
            NonNullList<ItemStack> results = NonNullList.create();

            for(int i = 0; i < pIngredientArray.size(); ++i) {
                ItemStack stack = CraftingHelper.getItemStack(pIngredientArray.get(i).getAsJsonObject(), true, true);
                if (!stack.isEmpty()) {
                    results.add(stack);
                }
            }

            return results;
        }

        public ProcessingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ResourceLocation typeID = pBuffer.readResourceLocation();
            int size = pBuffer.readVarInt();
            NonNullList<IngredientStackImpl> ingredients = NonNullList.withSize(size, EMPTY);
            ingredients.replaceAll(ignored -> IngredientStackImpl.SERIALIZER.fromNet().apply(pBuffer));

            size = pBuffer.readVarInt();
            NonNullList<ItemStack> results = NonNullList.withSize(size, ItemStack.EMPTY);
            results.replaceAll(ignored -> pBuffer.readItem());

            size = pBuffer.readVarInt();
            NonNullList<FluidIngredient> fluidIngredients = NonNullList.withSize(size, FluidIngredient.EMPTY);
            fluidIngredients.replaceAll(ignored -> FluidIngredient.fromNetwork(pBuffer));

            size = pBuffer.readVarInt();
            NonNullList<FluidStack> fluidResults = NonNullList.withSize(size, FluidStack.EMPTY);
            fluidResults.replaceAll(ignored -> pBuffer.readFluidStack());

            int time = pBuffer.readVarInt();
            int energy = pBuffer.readVarInt();
            return new ProcessingRecipe((RecipeTypeBuilder) BEJSPlugin.RECIPE_TYPE.objects.get(typeID), pRecipeId, results, fluidResults, ingredients, fluidIngredients, time, energy);
        }

        public void toNetwork(FriendlyByteBuf pBuffer, ProcessingRecipe pRecipe) {
            pBuffer.writeResourceLocation(pRecipe.builder.id);

            pBuffer.writeVarInt(pRecipe.itemIngredients.size());
            for(Ingredient ingredient : pRecipe.itemIngredients) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeVarInt(pRecipe.itemResults.size());
            for (ItemStack result : pRecipe.itemResults) {
                pBuffer.writeItem(result);
            }

            pBuffer.writeVarInt(pRecipe.fluidIngredients.size());
            for(FluidIngredient ingredient : pRecipe.fluidIngredients) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeVarInt(pRecipe.fluidResults.size());
            for (FluidStack result : pRecipe.fluidResults) {
                pBuffer.writeFluidStack(result);
            }

            pBuffer.writeVarInt(pRecipe.processingTime);
            pBuffer.writeVarInt(pRecipe.energy);
        }
    }
}
