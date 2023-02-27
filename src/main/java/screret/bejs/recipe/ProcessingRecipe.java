package screret.bejs.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.KubeJSRegistries;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.fluids.FluidStack;
import screret.bejs.BEJSPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ProcessingRecipe implements Recipe<Container> {
    final RecipeTypeBuilder builder;
    final RecipeType<?> type;
    final ResourceLocation id;
    final NonNullList<Ingredient> itemIngredients;
    final NonNullList<FluidIngredient> fluidIngredients;
    final NonNullList<ItemStack> itemResults;
    final NonNullList<FluidStack> fluidResults;
    final int processingTime;
    final int energy;

    public ProcessingRecipe(RecipeTypeBuilder builder, ResourceLocation id, NonNullList<ItemStack> itemResults, NonNullList<FluidStack> fluidResults, NonNullList<Ingredient> itemIngredients, NonNullList<FluidIngredient> fluidIngredients, int processingTime, int energy) {
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
            }
        }

        return RecipeMatcher.findMatches(inputs, itemIngredients) != null;
    }

    @Nonnull
    @Override
    public ItemStack assemble(Container pContainer) {
        return this.builder.assemblerFunction != null ? this.builder.assemblerFunction.assemble(this, pContainer) : this.itemResults.get(0).copy();
    }

    public NonNullList<ItemStack> getItemResults() {
        NonNullList<ItemStack> resultsCopy = NonNullList.withSize(itemResults.size(), ItemStack.EMPTY);
        for (int i = 0; i < resultsCopy.size(); ++i) {
            resultsCopy.set(i, itemResults.get(i).copy());
        }
        return resultsCopy;
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

    public static class Serializer implements RecipeSerializer<ProcessingRecipe> {
        public static final String INPUT_ITEM_KEY = "inputItems", OUTPUT_ITEM_KEY = "resultItems", INPUT_FLUID_KEY = "inputFluids", OUTPUT_FLUID_KEY = "resultFluids", ENERGY_KEY = "energy", TIME_KEY = "processingTime";

        public ProcessingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            String typeID = GsonHelper.getAsString(pJson, "type", "");
            NonNullList<Ingredient> itemInput = ingredientsFromJson(GsonHelper.getAsJsonArray(pJson, INPUT_ITEM_KEY));
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

        public static NonNullList<Ingredient> ingredientsFromJson(JsonArray pIngredientArray) {
            NonNullList<Ingredient> items = NonNullList.create();

            for(int i = 0; i < pIngredientArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(pIngredientArray.get(i));
                items.add(ingredient);
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
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(pBuffer));

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
