package screret.bejs.kubejs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MultiBlockControllerBlockEntity extends BlockEntity {
    public MultiBlockBuilder builder;

    public boolean valid;

    public MultiBlockControllerBlockEntity(MultiBlockBuilder builder, BlockPos pPos, BlockState pBlockState) {
        super(builder.get(), pPos, pBlockState);
        this.builder = builder;
    }

    public boolean checkStructurePattern() {
        if (builder.pattern == null) return false;
        var checked = this.builder.pattern.find(this.level, this.worldPosition);
        if (checked != null) {
            valid = true;
        }
        return valid;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if(t instanceof MultiBlockControllerBlockEntity blockEntity) {
            if(level.getGameTime() % 10 == 0) {
                blockEntity.checkStructurePattern();
            }

            if(blockEntity.valid) {

            }
        }
    }

}
