package screret.bejs.common;

import com.google.common.cache.LoadingCache;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import screret.bejs.BeJS;
import screret.bejs.kubejs.MultiBlockBuilder;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

public class MultiBlockControllerBlockEntity extends BlockEntityJS {
    public MultiBlockBuilder builder;
    public BlockPattern pattern;
    public BlockPattern.BlockPatternMatch match;


    public boolean valid;
    public IItemHandler inputItem, outputItem;
    public IEnergyStorage inputEnergy, outputEnergy;
    public IFluidHandler inputFluid, outputFluid;

    public MultiBlockControllerBlockEntity(MultiBlockBuilder builder, BlockPos pPos, BlockState pBlockState) {
        super(builder, pPos, pBlockState);
        this.builder = builder;
        this.pattern = builder.pattern == null ? null : builder.pattern.get().build();
        this.match = null;

        this.inputEnergy = this.outputEnergy = null;
        this.inputItem = this.outputItem = null;
        this.inputFluid = this.outputFluid = null;
    }

    public boolean checkStructurePattern() {
        if (pattern == null) return false;
        LoadingCache<BlockPos, BlockInWorld> worldCache = BlockPattern.createLevelCache(this.level, false);

        Direction stateDirection = this.getBlockState().getValue(MultiBlockControllerBlock.FACING).getOpposite();
        BlockPattern.BlockPatternMatch checked = this.find(this.worldPosition, stateDirection, worldCache) /*this.matches(pattern, start, stateDirection, Direction.UP, worldCache)*/;
        if (checked != null) {
            valid = true;
            this.match = checked;
        } else {
            valid = false;
        }

        this.level.setBlock(this.worldPosition, this.getBlockState().setValue(MultiBlockControllerBlock.VALID, valid), 0b1111);
        return valid;
    }

    public void scanPatternForIO() {
        BlockPos start = match.getBlock(0, 0,0).getPos();
        BlockPos end = match.getBlock(match.getWidth(), match.getHeight(), match.getDepth()).getPos();
        for(BlockPos blockpos : BlockPos.betweenClosed(start, end)) {
            BlockEntity entity = this.level.getBlockEntity(blockpos);

            if (entity != null) {
                LazyOptional<IItemHandler> itemCap = entity.getCapability(ForgeCapabilities.ITEM_HANDLER);
                if(itemCap.isPresent()) {
                    IItemHandler item = itemCap.orElse(null);
                    if(item.isItemValid(0, new ItemStack(Items.AIR, 1))) {
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
                    BeJS.LOGGER.error("beJS multiblock tick error!:", exception);
                }
            }
        }
    }

    @HideFromJS
    @Nullable
    public BlockPattern.BlockPatternMatch find(BlockPos pPos, Direction finger, LoadingCache<BlockPos, BlockInWorld> loadingCache) {
        for(BlockPos blockpos : BlockPos.betweenClosed(translateAndRotate(pPos, finger, -pattern.getWidth() + 1, -pattern.getHeight() + 1, -pattern.getDepth() + 1), translateAndRotate(pPos, finger, pattern.getWidth() - 1, pattern.getHeight() - 1, pattern.getDepth() - 1))) {
            BlockPattern.BlockPatternMatch match = this.matches(pattern, blockpos, finger, loadingCache);
            if (match != null) {
                return match;
            }
        }

        return null;
    }

    @HideFromJS
    @Nullable
    private BlockPattern.BlockPatternMatch matches(BlockPattern pattern, BlockPos pPos, Direction pFinger, LoadingCache<BlockPos, BlockInWorld> pCache) {
        var patternBlocks = pattern.getPattern();
        for(int x = 0; x < pattern.getWidth(); ++x) {
            for(int y = 0; y < pattern.getHeight(); ++y) {
                for(int z = 0; z < pattern.getDepth(); ++z) {
                    var blockMatch = pCache.getUnchecked(translateAndRotate(pPos, pFinger, x, y, z));
                    if (!patternBlocks[z][y][x].test(blockMatch)) {
                        //BeJS.LOGGER.error("failed scan at pos: " + new BlockPos(x, y, z) + "; real position: " + blockMatch.getPos() + "; start: " + pPos + "; Reason: " + blockMatch.getState() + " is not valid for the matcher.");
                        return null;
                    }
                }
            }
        }

        //BeJS.LOGGER.debug("found match!: [ finger: " + pFinger + "; thumb: " + Direction.UP + "; controller pos: " + pPos + " ]");
        return new BlockPattern.BlockPatternMatch(pPos, pFinger, Direction.UP, pCache, pattern.getWidth(), pattern.getHeight(), pattern.getDepth());
    }

    @HideFromJS
    protected static BlockPos translateAndRotate(BlockPos pPos, Direction pFinger, int pPalmOffset, int pThumbOffset, int pFingerOffset) {
        Vec3i fingerNormalClone = new Vec3i(pFinger.getStepX(), pFinger.getStepY(), pFinger.getStepZ());
        Vec3i crossProduct = fingerNormalClone.cross(new Vec3i(0, 1, 0));
        return pPos.offset(
                crossProduct.getX() * pPalmOffset + fingerNormalClone.getX() * pFingerOffset,
                -pThumbOffset + crossProduct.getY() * pPalmOffset + fingerNormalClone.getY() * pFingerOffset,
                crossProduct.getZ() * pPalmOffset + fingerNormalClone.getZ() * pFingerOffset
        );
    }

}
