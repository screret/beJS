package screret.bejs.common;

import dev.latvian.mods.kubejs.KubeJSRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import screret.bejs.BeJS;
import screret.bejs.kubejs.MultiBlockBuilder;

public class MultiBlockControllerBlockEntity extends BlockEntityJS {
    public MultiBlockBuilder builder;
    public BlockPattern pattern;

    public boolean valid;
    public IItemHandler inputItem, outputItem;
    public IEnergyStorage inputEnergy, outputEnergy;
    public IFluidHandler inputFluid, outputFluid;


    public MultiBlockControllerBlockEntity(MultiBlockBuilder builder, BlockPos pPos, BlockState pBlockState) {
        super(builder, pPos, pBlockState);
        this.builder = builder;
        BlockPatternBuilder blockPatternBuilder = BlockPatternBuilder.start();
        builder.pattern.accept(blockPatternBuilder);
        this.pattern = blockPatternBuilder.build();
    }

    public boolean checkStructurePattern() {
        if (builder.pattern == null || pattern == null) return false;
        var checked = this.pattern.find(this.level, this.worldPosition);
        valid = checked != null;
        this.level.setBlock(this.worldPosition, this.getBlockState().setValue(MultiBlockControllerBlock.VALID, valid), 3);
        return valid;
    }

    public void scanPatternForIO() {
        var pattern = this.pattern;
        int maxOffset = Math.max(Math.max(pattern.getWidth(), pattern.getHeight()), pattern.getDepth());
        BlockPos pos = this.getBlockPos();
        for(BlockPos blockpos : BlockPos.betweenClosed(pos, pos.offset(maxOffset - 1, maxOffset - 1, maxOffset - 1))) {
            BlockEntity entity = this.level.getBlockEntity(blockpos);

            if (entity != null) {
                LazyOptional<IItemHandler> itemCap = entity.getCapability(ForgeCapabilities.ITEM_HANDLER);
                if(itemCap.isPresent()) {
                    IItemHandler item = itemCap.orElse(null);
                    if(item.isItemValid(0, new ItemStack(Items.GLASS, 1))) {
                        inputItem = item;
                    } else {
                        outputItem = item;
                    }
                }

                LazyOptional<IEnergyStorage> energyCap = entity.getCapability(ForgeCapabilities.ENERGY);
                if (energyCap.isPresent()) {
                    IEnergyStorage energy = energyCap.orElse(null);
                    if(energy.canReceive()) {
                        inputEnergy = energy;
                    } else if (energy.canExtract()) {
                        outputEnergy = energy;
                    }
                }

                LazyOptional<IFluidHandler> fluidCap = entity.getCapability(ForgeCapabilities.FLUID_HANDLER);
                if (fluidCap.isPresent()) {
                    IFluidHandler fluid = fluidCap.orElse(null);
                    if(fluid.isFluidValid(0, FluidStack.EMPTY)) {
                        inputFluid = fluid;
                    } else {
                        outputFluid = fluid;
                    }
                }
            }
        }
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if(t instanceof MultiBlockControllerBlockEntity blockEntity) {
            if(level.getGameTime() % 10 == 0) {
                if (blockEntity.checkStructurePattern()) {
                    blockEntity.scanPatternForIO();
                }
            }

            if(blockEntity.valid && blockEntity.builder.tickCallback != null) {
                try {
                    blockEntity.builder.tickCallback.tick(level, pos, state, blockEntity);
                } catch (Exception exception) {
                    BeJS.LOGGER.error("beJS tick error!:", exception);
                }
            }
        }
    }

}
