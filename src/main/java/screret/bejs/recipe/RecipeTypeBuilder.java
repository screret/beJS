package screret.bejs.recipe;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.KubeJSRegistries;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import screret.bejs.BEJSPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RecipeTypeBuilder extends BuilderBase<RecipeType<ProcessingRecipe>> {
    public static final Map<ResourceLocation, RecipeType<ProcessingRecipe>> ALL_RECIPES = new Object2ObjectOpenHashMap<>();

    public transient Assembler assemblerFunction;
    public transient int inputSlotCount, outputSlotCount;
    public transient ItemStack toastSymbol;

    public RecipeTypeBuilder(ResourceLocation i) {
        super(i);
        assemblerFunction = null;
        inputSlotCount = 0;
        outputSlotCount = 0;
        toastSymbol = new ItemStack(Blocks.CRAFTING_TABLE);
    }

    public RecipeTypeBuilder assembler(Assembler assembler) {
        this.assemblerFunction = assembler;
        return this;
    }

    public RecipeTypeBuilder maxInputs(int count) {
        this.inputSlotCount = count;
        return this;
    }

    public RecipeTypeBuilder maxOutputs(int count) {
        this.outputSlotCount = count;
        return this;
    }

    public RecipeTypeBuilder toastSymbol(ItemStack toastSymbol) {
        this.toastSymbol = toastSymbol;
        return this;
    }

    @Override
    public RegistryObjectBuilderTypes<? super RecipeType<?>> getRegistryType() {
        return BEJSPlugin.RECIPE_TYPE;
    }

    @Override
    public RecipeType<ProcessingRecipe> createObject() {
        RecipeType<ProcessingRecipe> type = RecipeType.simple(id);
        ALL_RECIPES.put(id, type);
        KubeJSRegistries.recipeSerializers().register(id, ProcessingRecipe.Serializer::new);
        return type;
    }

    @FunctionalInterface
    public interface Assembler {
        ItemStack assemble(ProcessingRecipe recipe, Container container);
    }
}
