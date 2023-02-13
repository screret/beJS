package screret.bejs.kubejs;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.custom.BasicBlockJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EntityBlockJS extends BasicBlockJS {
    private final BlockEntityTypeBuilder builder;
    public EntityBlockJS(EntityBlockJS.Builder p) {
        super(p);
        builder = (BlockEntityTypeBuilder) RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.objects.get(p.id);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return builder.get().create(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : BlockEntityJS::serverTick;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide) {
            if(builder.hasGui) {
                if(pLevel.getBlockEntity(pPos) instanceof BlockEntityJS blockEntityJS) {
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, blockEntityJS, pPos);
                    //pPlayer.openMenu(blockEntityJS);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return pLevel.getBlockEntity(pPos) instanceof BlockEntityJS blockEntityJS ? blockEntityJS : null;
    }

    public static class Builder extends BlockBuilder {
        public transient BlockEntityTypeBuilder blockEntityTypeBuilder;

        public Builder(ResourceLocation i) {
            super(i);
        }

        @Override
        public void createAdditionalObjects() {
            if (blockEntityTypeBuilder != null) {
                RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.addBuilder(blockEntityTypeBuilder);
            }
        }

        @HideFromJS
        protected BlockEntityTypeBuilder getOrCreateBlockEntityTypeBuilder() {
            return blockEntityTypeBuilder == null ? (blockEntityTypeBuilder = new BlockEntityJS.Builder(id)) : blockEntityTypeBuilder;
        }

        @Override
        public BuilderBase<Block> displayName(String name) {
            if (blockEntityTypeBuilder != null) {
                blockEntityTypeBuilder.displayName(name);
            }
            return super.displayName(name);
        }

        public BlockBuilder entity(@Nullable Consumer<BlockEntityTypeBuilder> i) {
            if (i == null) {
                blockEntityTypeBuilder = null;
                lootTable = null;
            } else {
                i.accept(getOrCreateBlockEntityTypeBuilder());
            }

            return this;
        }

        @Override
        public Block createObject() {
            EntityBlockJS block = new EntityBlockJS(this);
            blockEntityTypeBuilder.addValidBlock(block);
            return block;
        }
    }
}
