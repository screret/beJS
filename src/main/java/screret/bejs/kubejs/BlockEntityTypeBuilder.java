package screret.bejs.kubejs;

import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;
import java.util.function.Predicate;

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

    public transient List<Block> validBlocks;
    public transient TickCallback tickCallback;
    public transient SaveCallback saveCallback;
    public transient LoadCallback loadCallback;
    public transient boolean hasGui;
    public transient ItemHandler itemHandler;
    public transient EnergyHandler energyHandler;
    public transient FluidHandler fluidHandler;

    public transient Map<String, Object> defaultValues;


    public BlockEntityTypeBuilder(ResourceLocation i) {
        super(i);
        validBlocks = new ArrayList<>();
        //blockBuilder = getOrCreateBlockBuilder();
        //menuTypeBuilder = getOrCreateMenuBuilder();
        tickCallback = null;
        saveCallback = null;
        loadCallback = null;
        hasGui = false;
        itemHandler = null;
        energyHandler = null;
        fluidHandler = null;
        defaultValues = new HashMap<>();
    }

    @Override
    public final RegistryObjectBuilderTypes<BlockEntityType<?>> getRegistryType() {
        return RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE;
    }

    /*
    @HideFromJS
    protected EntityBlockJS.Builder getOrCreateBlockBuilder() {
        return blockBuilder == null ? (blockBuilder = new EntityBlockJS.Builder(id).blockEntity(this)) : blockBuilder;
    }

    @HideFromJS
    protected MenuTypeBuilderWrapper getOrCreateMenuBuilder() {
        if(Platform.isModLoaded("screenjs")) {
            return menuTypeBuilder == null ? (menuTypeBuilder = new MenuTypeBuilderWrapper(new BlockEntityMenuType.Builder(id))) : menuTypeBuilder;
        }
        return null;
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
    */

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

    public BlockEntityTypeBuilder hasGui() {
        if(!Platform.isModLoaded("screenjs")) throw new IllegalStateException("Having a gui requires ScreenJS.");
        hasGui = true;
        return this;
    }

    public BlockEntityTypeBuilder defaultValue(String key, Object value) {
        defaultValues.put(key, value);
        KubeJS.LOGGER.debug(defaultValues.toString());
        return this;
    }

    public BlockEntityTypeBuilder itemHandler(int capacity) {
        this.itemHandler = new ItemHandler(capacity);
        return this;
    }

    public BlockEntityTypeBuilder energyHandler(int capacity, int maxReceive, int maxExtract) {
        this.energyHandler = new EnergyHandler(capacity, maxReceive, maxExtract);
        return this;
    }

    public BlockEntityTypeBuilder fluidHandler(int capacity, Predicate<FluidStack> validator) {
        this.fluidHandler = new FluidHandler(capacity, validator);
        return this;
    }

    /*
    public BlockEntityTypeBuilder block(@Nullable Consumer<EntityBlockJS.Builder> i) {
        if (i == null) {
            blockBuilder = null;
        } else {
            i.accept(getOrCreateBlockBuilder());
        }

        return this;
    }
    */

    public record EnergyHandler(int capacity, int maxReceive, int maxExtract) {}
    public record FluidHandler(int capacity, Predicate<FluidStack> validator) {}
    public record ItemHandler(int capacity) {}

}
