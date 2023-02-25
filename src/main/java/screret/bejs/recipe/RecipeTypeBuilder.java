package screret.bejs.recipe;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import screret.bejs.BEJSPlugin;

import java.util.function.Function;

public class RecipeTypeBuilder extends BuilderBase<RecipeType<CustomRecipeJS>> {
    public transient Function<CustomRecipeJS, ItemStack> assemblerFunction;


    public RecipeTypeBuilder(ResourceLocation i) {
        super(i);
        assemblerFunction = null;
    }

    public RecipeTypeBuilder assembler(Function<CustomRecipeJS, ItemStack> assembler) {
        this.assemblerFunction = assembler;
        return this;
    }

    @Override
    public RegistryObjectBuilderTypes<? super RecipeType<?>> getRegistryType() {
        return BEJSPlugin.RECIPE_TYPE;
    }

    @Override
    public RecipeType<CustomRecipeJS> createObject() {
        return RecipeType.simple(id);
    }
}
