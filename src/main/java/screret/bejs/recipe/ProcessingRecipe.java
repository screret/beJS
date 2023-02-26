package screret.bejs.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import screret.bejs.BEJSPlugin;
import screret.bejs.BeJS;

import java.util.ArrayList;
import java.util.List;

public class ProcessingRecipe implements Recipe<Container> {
    final RecipeTypeBuilder builder;
    final RecipeType<?> type;
    final ResourceLocation id;
    final NonNullList<Ingredient> ingredients;
    final NonNullList<ItemStack> results;
    final int processingTime;
    final int energy;

    public ProcessingRecipe(RecipeTypeBuilder builder, ResourceLocation id, NonNullList<ItemStack> pResults, NonNullList<Ingredient> pIngredients, int processingTime, int energy) {
        this.builder = builder;
        this.type = builder.get();
        this.id = id;
        this.results = pResults;
        this.ingredients = pIngredients;
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

        return RecipeMatcher.findMatches(inputs, ingredients) != null;
    }

    @Override
    public ItemStack assemble(Container pContainer) {
        return this.builder.assemblerFunction != null ? this.builder.assemblerFunction.assemble(this, pContainer) : this.results.get(0).copy();
    }

    public NonNullList<ItemStack> getResults() {
        NonNullList<ItemStack> resultsCopy = NonNullList.withSize(results.size(), ItemStack.EMPTY);
        for (int i = 0; i < resultsCopy.size(); ++i) {
            resultsCopy.set(i, results.get(i).copy());
        }
        return resultsCopy;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight <= this.builder.inputSlotCount;
    }

    @Override
    public ItemStack getToastSymbol() {
        return builder.toastSymbol == null ? Recipe.super.getToastSymbol() : builder.toastSymbol;
    }

    @Override
    public ItemStack getResultItem() {
        return this.results.get(0);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return KubeJSRegistries.recipeSerializers().get(builder.id);
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    public static class Serializer implements RecipeSerializer<ProcessingRecipe> {
        public ProcessingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            String typeID = GsonHelper.getAsString(pJson, "type", "");
            NonNullList<Ingredient> items = ingredientsFromJson(GsonHelper.getAsJsonArray(pJson, "ingredients"));
            int time = GsonHelper.getAsInt(pJson, "processingTime", 200);
            int energy = GsonHelper.getAsInt(pJson, "energy", 0);
            if (items.isEmpty()) {
                throw new JsonParseException("No ingredients for beJS custom recipe");
            } else {
                NonNullList<ItemStack> result = resultsFromJson(GsonHelper.getAsJsonArray(pJson, "results"));
                return new ProcessingRecipe((RecipeTypeBuilder) BEJSPlugin.RECIPE_TYPE.objects.get(new ResourceLocation(typeID)), pRecipeId, result, items, time, energy);
            }
        }

        public static NonNullList<Ingredient> ingredientsFromJson(JsonArray pIngredientArray) {
            NonNullList<Ingredient> items = NonNullList.create();

            for(int i = 0; i < pIngredientArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(pIngredientArray.get(i));
                items.add(ingredient);
            }

            return items;
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

            int time = pBuffer.readVarInt();
            int energy = pBuffer.readVarInt();
            return new ProcessingRecipe((RecipeTypeBuilder) BEJSPlugin.RECIPE_TYPE.objects.get(typeID), pRecipeId, results, ingredients, time, energy);
        }

        public void toNetwork(FriendlyByteBuf pBuffer, ProcessingRecipe pRecipe) {
            pBuffer.writeResourceLocation(pRecipe.builder.id);

            pBuffer.writeVarInt(pRecipe.ingredients.size());
            for(Ingredient ingredient : pRecipe.ingredients) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeVarInt(pRecipe.results.size());
            for (ItemStack result : pRecipe.results) {
                pBuffer.writeItem(result);
            }

            pBuffer.writeVarInt(pRecipe.processingTime);
            pBuffer.writeVarInt(pRecipe.energy);
        }
    }
}
