package screret.bejs.kubejs;

import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class BlockEntityTypeBuilder extends BuilderBase<BlockEntityType<?>> {
    @FunctionalInterface
    public interface TickCallback {
        void tick(Level pLevel, BlockPos pPos, BlockState pState, BlockEntityJS pBlockEntity);
    }

    public transient List<Block> validBlocks;
    public transient TickCallback tickCallback;
    //public transient boolean hasGui;
    public transient ItemHandler itemHandler;
    public transient EnergyHandler energyHandler;
    public transient FluidHandler fluidHandler;

    //public transient List<CapabilityBuilder<BlockEntity, ?, ?>> capabilityBuilders;
    /**
     * allowed keys: "progress":int, "totalProgress":int, "isProcessing":boolean, "fuelDuration":int, "remainingFuel":int
     * other keys are also allowed, but not used by default
     */
    public transient Consumer<CompoundTag> defaultValues;


    public BlockEntityTypeBuilder(ResourceLocation i) {
        super(i);
        validBlocks = new ArrayList<>();
        tickCallback = null;
        //hasGui = false;
        itemHandler = null;
        energyHandler = null;
        fluidHandler = null;

        defaultValues = null;
    }

    @Override
    public final RegistryObjectBuilderTypes<BlockEntityType<?>> getRegistryType() {
        return RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE;
    }

    public BlockEntityTypeBuilder addValidBlock(Block block) {
        this.validBlocks.add(block);
        return this;
    }

    public BlockEntityTypeBuilder ticker(TickCallback ticker) {
        this.tickCallback = ticker;
        return this;
    }

    /*public BlockEntityTypeBuilder hasGui() {
        if(!Platform.isModLoaded("screenjs")) throw new IllegalStateException("Having a gui requires ScreenJS.");
        hasGui = true;
        return this;
    }*/


    /**
     * allowed (used) keys: "progress":int, "totalProgress":int, "isProcessing":boolean, "fuelDuration":int, "remainingFuel":int
     * other keys are also allowed, but not used by default
     */
    public BlockEntityTypeBuilder defaultValues(Consumer<CompoundTag> consumer) {
        defaultValues = consumer;
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
    public BlockEntityTypeBuilder attachCapability(CapabilityBuilder<BlockEntity, ?, ?> capabilityBuilder) {
        this.capabilityBuilders.add(capabilityBuilder);
        return this;
    }
     */

    public record EnergyHandler(int capacity, int maxReceive, int maxExtract) {}
    public record FluidHandler(int capacity, Predicate<FluidStack> validator) {}
    public record ItemHandler(int capacity) {}

}
