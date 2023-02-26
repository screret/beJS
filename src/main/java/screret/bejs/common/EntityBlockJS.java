package screret.bejs.common;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.custom.BasicBlockJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import screret.bejs.kubejs.BlockEntityTypeBuilder;
import screret.bejs.misc.IMultipleItemHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EntityBlockJS extends BasicBlockJS {
    protected final BlockEntityTypeBuilder builder;
    public EntityBlockJS(EntityBlockJS.Builder p) {
        super(p);
        builder = p.blockEntityTypeBuilder;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return builder.get().create(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return builder.tickCallback != null ? BlockEntityJS::tick : null;
    }

    /*@Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if(builder.hasGui) {
            if(pLevel.getBlockEntity(pPos) instanceof BlockEntityJS blockEntityJS) {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, blockEntityJS, pPos);
                //pPlayer.openMenu(blockEntityJS);
                return InteractionResult.CONSUME;
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return pLevel.getBlockEntity(pPos) instanceof BlockEntityJS blockEntityJS ? blockEntityJS : null;
    }*/

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof BlockEntityJS blockEntityJS && !pLevel.isClientSide) {
                Set<IItemHandler> oldHandlers = new HashSet<>();

                for (Direction dir : Direction.values()) {
                    IItemHandler itemHandler = blockEntityJS.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).orElse(null);
                    if(itemHandler != null && !oldHandlers.contains(itemHandler)) {
                        oldHandlers.add(itemHandler);
                        if(itemHandler instanceof IMultipleItemHandler multipleItemHandler) {
                            for (var container : multipleItemHandler.getAllContainers()) {
                                for (int i = 0; i < container.getSlots(); ++i) {
                                    Containers.dropItemStack(pLevel, pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, container.getStackInSlot(i));
                                }
                            }
                        } else {
                            for (int i = 0; i < itemHandler.getSlots(); ++i) {
                                Containers.dropItemStack(pLevel, pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, itemHandler.getStackInSlot(i));
                            }
                        }
                    }
                }

                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    public static class Builder extends BlockBuilder {
        public transient BlockEntityJS.Builder blockEntityTypeBuilder;
        public transient boolean doCreateBlockEntity;

        public Builder(ResourceLocation i) {
            super(i);
            blockEntityTypeBuilder = getOrCreateBlockEntityTypeBuilder();
            doCreateBlockEntity = true;
        }

        @Override
        public void createAdditionalObjects() {
            super.createAdditionalObjects();
            if (blockEntityTypeBuilder != null && doCreateBlockEntity) {
                RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.addBuilder(blockEntityTypeBuilder);
            }
        }

        @HideFromJS
        protected BlockEntityJS.Builder getOrCreateBlockEntityTypeBuilder() {
            return blockEntityTypeBuilder == null ? (blockEntityTypeBuilder = new BlockEntityJS.Builder(id)) : blockEntityTypeBuilder;
        }

        @Override
        public BuilderBase<Block> displayName(String name) {
            if (blockEntityTypeBuilder != null) {
                blockEntityTypeBuilder.displayName(name);
            }
            return super.displayName(name);
        }

        public Builder entity(@Nullable Consumer<BlockEntityJS.Builder> i) {
            if (i == null) {
                blockEntityTypeBuilder = null;
                lootTable = null;
                doCreateBlockEntity = false;
            } else {
                i.accept(getOrCreateBlockEntityTypeBuilder());
            }

            return this;
        }

        public Builder entityById(ResourceLocation blockEntityId) {
            this.blockEntityTypeBuilder = (BlockEntityJS.Builder) RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.objects.get(blockEntityId);
            doCreateBlockEntity = false;
            return this;
        }

        @Override
        public Block createObject() {
            EntityBlockJS block = new EntityBlockJS(this);
            if(this.blockEntityTypeBuilder != null) blockEntityTypeBuilder.addValidBlock(block);
            return block;
        }
    }
}
