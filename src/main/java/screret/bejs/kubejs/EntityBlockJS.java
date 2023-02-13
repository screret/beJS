package screret.bejs.kubejs;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.custom.BasicBlockJS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

public class EntityBlockJS extends BasicBlockJS {
    private BlockEntityTypeBuilder builder;
    public EntityBlockJS(EntityBlockJS.Builder p) {
        super(p);
        builder = p.beTypeBuilder;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return builder.get().create(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : BlockEntityJS::serverTick;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide) {
            if(builder.hasGui) {
                if(pLevel.getBlockEntity(pPos) instanceof BlockEntityJS blockEntityJS) {
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, blockEntityJS.getMenuProvider(), pPos);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    public static class Builder extends BlockBuilder {
        public transient BlockEntityTypeBuilder beTypeBuilder;

        public Builder(ResourceLocation i) {
            super(i);
            beTypeBuilder = null;
        }

        public Builder blockEntity(BlockEntityTypeBuilder builder) {
            this.beTypeBuilder = builder;
            return this;
        }

        @Override
        public Block createObject() {
            return new EntityBlockJS(this);
        }
    }
}
