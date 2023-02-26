package screret.bejs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public class BlockInWorldExtended extends BlockInWorld {

    public BlockInWorldExtended(LevelReader pLevel, BlockPos pPos, boolean pLoadChunks) {
        super(pLevel, pPos, pLoadChunks);
    }

    public static Predicate<BlockInWorld> hasState(Predicate<BlockState> pState) {
        return block -> block != null && pState.test(block.getState());
    }

    public static Predicate<BlockInWorld> hasBlockEntity(Predicate<BlockEntity> pState) {
        return block -> block != null && block.getEntity() != null && pState.test(block.getEntity());
    }

    public static Predicate<BlockInWorld> or(Predicate<BlockInWorld> first, Predicate<BlockInWorld> second) {
        return block -> first.test(block) || second.test(block);
    }

    public static Predicate<BlockInWorld> and(Predicate<BlockInWorld> first, Predicate<BlockInWorld> second) {
        return block -> first.test(block) && second.test(block);
    }
}
