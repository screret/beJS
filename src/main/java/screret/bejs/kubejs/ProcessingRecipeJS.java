package screret.bejs.kubejs;

import com.google.gson.JsonArray;
import dev.latvian.mods.kubejs.recipe.*;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.core.NonNullList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

import static screret.bejs.recipe.ProcessingRecipe.Serializer.*;

@SuppressWarnings("unused")
public class ProcessingRecipeJS extends RecipeJS {
    public final List<Ingredient> inputItems = new ArrayList<>(1);
    public final List<ItemStack> outputItems = new ArrayList<>(1);

    public String inKey = "ingredients";
    public String outKey = "results";

    @Override
    public void create(RecipeArguments args) {
        for (Object o : ListJS.orSelf(args.get(0))) {
            outputItems.add(parseItemOutput(o));
        }

        for (Object o : ListJS.orSelf(args.get(1))) {
            inputItems.add(parseItemInput(o));
        }
    }

    public ProcessingRecipeJS energy(int e) {
        json.addProperty("energy", e);
        save();
        return this;
    }

    public ProcessingRecipeJS time(int t) {
        json.addProperty("processingTime", t);
        save();
        return this;
    }

    @Override
    public void deserialize() {
        if (json.has(inKey)) {
            NonNullList<Ingredient> ingredients = ingredientsFromJson(GsonHelper.getAsJsonArray(json, inKey));
            inputItems.addAll(ingredients);
        }

        if (json.has(outKey)) {
            NonNullList<ItemStack> results = resultsFromJson(GsonHelper.getAsJsonArray(json, outKey));
            outputItems.addAll(results);
        }
    }

    @Override
    public void serialize() {
        if (serializeOutputs && !outKey.isEmpty()) {
            JsonArray out = new JsonArray();

            for (var stack : outputItems) {
                out.add(itemToJson(stack));
            }

            json.add(outKey, out);
        }

        if (serializeInputs && !inKey.isEmpty()) {
            JsonArray in = new JsonArray();

            for (var ingredient : inputItems) {
                in.add(ingredient.toJson());
            }

            json.add(inKey, in);
        }
    }


    @Override
    public boolean hasInput(IngredientMatch match) {
        for (var in : inputItems) {
            if (match.contains(in)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean replaceInput(IngredientMatch match, Ingredient with, ItemInputTransformer transformer) {
        boolean changed = false;

        for (int i = 0; i < inputItems.size(); ++i) {
            var in = inputItems.get(i);

            if (match.contains(in)) {
                inputItems.set(i, transformer.transform(this, match, in, with));
                changed = true;
            }
        }

        return changed;
    }

    @Override
    public boolean hasOutput(IngredientMatch match) {
        for (var out : outputItems) {
            if (match.contains(out)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean replaceOutput(IngredientMatch match, ItemStack with, ItemOutputTransformer transformer) {
        boolean changed = false;

        for (int i = 0; i < outputItems.size(); ++i) {
            var out = outputItems.get(i);

            if (match.contains(out)) {
                outputItems.set(i, transformer.transform(this, match, out, with));
                changed = true;
            }
        }

        return changed;
    }

}
