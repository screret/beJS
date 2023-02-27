package screret.bejs.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.recipe.*;
import dev.latvian.mods.kubejs.util.ListJS;
import net.minecraft.core.NonNullList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import screret.bejs.recipe.FluidIngredient;

import java.util.ArrayList;
import java.util.List;

import static screret.bejs.recipe.ProcessingRecipe.Serializer.*;

@SuppressWarnings("unused")
public class ProcessingRecipeJS extends RecipeJS {
    public final List<Ingredient> inputItems = new ArrayList<>(1);
    public final List<FluidIngredient> inputFluids = new ArrayList<>(0);
    public final List<ItemStack> outputItems = new ArrayList<>(1);
    public final List<FluidStack> outputFluids = new ArrayList<>(0);

    @Override
    public void create(RecipeArguments args) {
        for (Object o : ListJS.orSelf(args.get(0))) {
            outputItems.add(parseItemOutput(o));
        }

        for (Object o : ListJS.orSelf(args.get(1))) {
            inputItems.add(parseItemInput(o));
        }

        for (Object o : ListJS.orSelf(args.get(2))) {
            outputFluids.add(fluidOutputFrom(o));
        }

        for (Object o : ListJS.orSelf(args.get(3))) {
            inputFluids.add(fluidInputFrom(o));

        }
    }

    public ProcessingRecipeJS energy(int e) {
        json.addProperty(ENERGY_KEY, e);
        save();
        return this;
    }

    public ProcessingRecipeJS time(int t) {
        json.addProperty(TIME_KEY, t);
        save();
        return this;
    }

    public JsonElement fluidToJson(FluidStack fluidStack) {
        return FluidStack.CODEC.encode(fluidStack, JsonOps.INSTANCE, new JsonObject()).result().orElse(new JsonObject()).getAsJsonObject();
    }

    public FluidIngredient fluidInputFrom(Object o) {
        return FluidIngredient.of(fluidOutputFrom(o));
    }

    public FluidStack fluidOutputFrom(Object o) {
        FluidStackJS fs = FluidStackJS.of(o);
        return new FluidStack(fs.getFluid(), (int) fs.getAmount(), fs.getNbt());
    }

    @Override
    public void deserialize() {
        if (json.has(INPUT_ITEM_KEY)) {
            NonNullList<Ingredient> ingredients = ingredientsFromJson(GsonHelper.getAsJsonArray(json, INPUT_ITEM_KEY));
            inputItems.addAll(ingredients);
        }

        if (json.has(INPUT_FLUID_KEY)) {
            NonNullList<FluidIngredient> ingredients = fluidIngredientsFromJson(GsonHelper.getAsJsonArray(json, INPUT_FLUID_KEY));
            inputFluids.addAll(ingredients);
        }

        if (json.has(OUTPUT_ITEM_KEY)) {
            NonNullList<ItemStack> results = resultsFromJson(GsonHelper.getAsJsonArray(json, OUTPUT_ITEM_KEY));
            outputItems.addAll(results);
        }

        if (json.has(OUTPUT_FLUID_KEY)) {
            NonNullList<FluidStack> results = fluidResultsFromJson(GsonHelper.getAsJsonArray(json, OUTPUT_FLUID_KEY));
            outputFluids.addAll(results);
        }
    }

    @Override
    public void serialize() {
        if (serializeOutputs) {
            JsonArray out = new JsonArray();
            for (var stack : outputItems) {
                out.add(itemToJson(stack));
            }
            json.add(OUTPUT_ITEM_KEY, out);

            out = new JsonArray();
            for (var stack : outputFluids) {
                out.add(fluidToJson(stack));
            }
            json.add(OUTPUT_FLUID_KEY, out);
        }

        if (serializeInputs) {
            JsonArray in = new JsonArray();
            for (var ingredient : inputItems) {
                in.add(ingredient.toJson());
            }
            json.add(INPUT_ITEM_KEY, in);

            in = new JsonArray();
            for (var fluid : inputFluids) {
                in.add(fluid.toJson());
            }
            json.add(INPUT_FLUID_KEY, in);
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
