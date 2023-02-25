package screret.bejs.kubejs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;

public class MultiBlockBuilder extends BlockEntityTypeBuilder {
    public transient BlockPattern pattern;


    public MultiBlockBuilder(ResourceLocation i) {
        super(i);
        pattern = null;
    }

    public MultiBlockBuilder pattern(BlockPatternBuilder pattern) {
        this.pattern = pattern.build();
        return this;
    }

    @Override
    public BlockEntityType<?> createObject() {
        return BlockEntityType.Builder.of((pPos, pState) -> new MultiBlockControllerBlockEntity(this, pPos, pState), this.validBlocks.toArray(Block[]::new)).build(null);
    }
}
