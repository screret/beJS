package screret.bejs.kubejs;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.BlockItemBuilder;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import screret.screenjs.kubejs.BlockEntityMenuType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BlockEntityTypeBuilder extends BuilderBase<BlockEntityType<?>> {
    @FunctionalInterface
    public interface TickCallback {
        void tick(Level pLevel, BlockPos pPos, BlockState pState, BlockEntityJS pBlockEntity);
    }
    @FunctionalInterface
    public interface SaveCallback {
        void saveAdditional(Level pLevel, BlockPos pPos, BlockEntityJS pBlockEntity, CompoundTag tag);
    }
    @FunctionalInterface
    public interface LoadCallback {
        void load(Level pLevel, BlockPos pPos, BlockEntityJS pBlockEntity, CompoundTag tag);
    }


    public transient EntityBlockJS.Builder blockBuilder;
    public transient MenuTypeBuilderWrapper menuTypeBuilder;
    //public transient EntityBlockJS.Builder guiBuilder;


    public transient List<Block> validBlocks;
    public transient TickCallback tickCallback;
    public transient SaveCallback saveCallback;
    public transient LoadCallback loadCallback;
    public transient boolean hasGui;
    public transient RecipeType<?> recipeType;

    public transient Map<String, Object> defaultValues;


    public BlockEntityTypeBuilder(ResourceLocation i) {
        super(i);
        validBlocks = new ArrayList<>();
        blockBuilder = getOrCreateBlockBuilder();
        menuTypeBuilder = getOrCreateMenuBuilder();
        tickCallback = null;
        saveCallback = null;
        loadCallback = null;
        hasGui = false;
        recipeType = null;
        defaultValues = new HashMap<>();
    }

    @HideFromJS
    protected EntityBlockJS.Builder getOrCreateBlockBuilder() {
        return blockBuilder == null ? (blockBuilder = new EntityBlockJS.Builder(id).blockEntity(this)) : blockBuilder;
    }

    @HideFromJS
    protected MenuTypeBuilderWrapper getOrCreateMenuBuilder() {
        if(ModList.get().isLoaded("screenjs")) {
            return menuTypeBuilder == null ? (menuTypeBuilder = new MenuTypeBuilderWrapper(new BlockEntityMenuType.Builder(id))) : menuTypeBuilder;
        }
        return null;
    }

    @Override
    public final RegistryObjectBuilderTypes<BlockEntityType<?>> getRegistryType() {
        return RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE;
    }

    @Override
    public void createAdditionalObjects() {
        if (blockBuilder != null) {
            RegistryObjectBuilderTypes.BLOCK.addBuilder(blockBuilder);
        }
    }

    @Override
    public BuilderBase<BlockEntityType<?>> displayName(String name) {
        if (blockBuilder != null) {
            blockBuilder.displayName(name);
        }
        return super.displayName(name);
    }

    public BlockEntityTypeBuilder addValidBlock(Block block) {
        this.validBlocks.add(block);
        return this;
    }

    public BlockEntityTypeBuilder ticker(TickCallback ticker) {
        this.tickCallback = ticker;
        return this;
    }

    public BlockEntityTypeBuilder loadCallback(LoadCallback ticker) {
        this.loadCallback = ticker;
        return this;
    }

    public BlockEntityTypeBuilder saveCallback(SaveCallback ticker) {
        this.saveCallback = ticker;
        return this;
    }

    public BlockEntityTypeBuilder recipeType(RecipeType<?> type) {
        this.recipeType = type;
        return this;
    }

    public BlockEntityTypeBuilder hasGui() {
        if(!ModList.get().isLoaded("screenjs")) throw new IllegalStateException("Having a gui requires ScreenJS.");
        hasGui = true;
        return this;
    }

    public BlockEntityTypeBuilder addDefaultValue(String key, Object value) {
        defaultValues.put(key, value);
        return this;
    }

    public BlockEntityTypeBuilder block(@Nullable Consumer<EntityBlockJS.Builder> i) {
        if (i == null) {
            blockBuilder = null;
        } else {
            i.accept(getOrCreateBlockBuilder());
        }

        return this;
    }

    public static class MenuTypeBuilderWrapper {
        public transient BlockEntityMenuType.Builder builder;

        public MenuTypeBuilderWrapper(BlockEntityMenuType.Builder builder) {
            this.builder = builder;
        }
    }
}
