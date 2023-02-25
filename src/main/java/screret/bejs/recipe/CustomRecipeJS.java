package screret.bejs.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import screret.bejs.BEJSPlugin;
import screret.bejs.BeJS;

import java.util.ArrayList;
import java.util.List;

public class CustomRecipeJS implements Recipe<Container> {
    final RecipeTypeBuilder builder;
    final RecipeType<?> type;
    final ResourceLocation id;
    final NonNullList<Ingredient> ingredients;
    final ItemStack result;
    final int processingTime;

    public CustomRecipeJS(RecipeTypeBuilder builder, ResourceLocation id, ItemStack pResult, NonNullList<Ingredient> pIngredients, int processingTime) {
        this.builder = builder;
        this.type = builder.get();
        this.id = id;
        this.result = pResult;
        this.ingredients = pIngredients;
        this.processingTime = processingTime;
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
        return this.builder.assemblerFunction != null ? this.builder.assemblerFunction.apply(this) : this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return this.result;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BeJS.CUSTOM_JS_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    public static class Serializer implements RecipeSerializer<CustomRecipeJS> {
        public CustomRecipeJS fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            String builderID = GsonHelper.getAsString(pJson, "builder", "");
            NonNullList<Ingredient> items = itemsFromJson(GsonHelper.getAsJsonArray(pJson, "ingredients"));
            int time = GsonHelper.getAsInt(pJson, "processingTime", 200);
            if (items.isEmpty()) {
                throw new JsonParseException("No ingredients for beJS custom recipe");
            } else {
                ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
                return new CustomRecipeJS((RecipeTypeBuilder) BEJSPlugin.RECIPE_TYPE.objects.get(new ResourceLocation(builderID)), pRecipeId, result, items, time);
            }
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray pIngredientArray) {
            NonNullList<Ingredient> items = NonNullList.create();

            for(int i = 0; i < pIngredientArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(pIngredientArray.get(i));
                items.add(ingredient);
            }

            return items;
        }

        public CustomRecipeJS fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ResourceLocation builderID = pBuffer.readResourceLocation();
            int i = pBuffer.readVarInt();
            NonNullList<Ingredient> items = NonNullList.withSize(i, Ingredient.EMPTY);

            items.replaceAll(ignored -> Ingredient.fromNetwork(pBuffer));

            ItemStack result = pBuffer.readItem();

            int time = pBuffer.readVarInt();
            return new CustomRecipeJS((RecipeTypeBuilder) BEJSPlugin.RECIPE_TYPE.objects.get(builderID), pRecipeId, result, items, time);
        }

        public void toNetwork(FriendlyByteBuf pBuffer, CustomRecipeJS pRecipe) {
            pBuffer.writeResourceLocation(pRecipe.builder.id);

            pBuffer.writeVarInt(pRecipe.ingredients.size());

            for(Ingredient ingredient : pRecipe.ingredients) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeItem(pRecipe.result);

            pBuffer.writeVarInt(pRecipe.processingTime);
        }
    }
}
