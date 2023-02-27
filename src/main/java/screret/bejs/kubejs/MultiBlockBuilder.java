package screret.bejs.kubejs;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import screret.bejs.common.MultiBlockControllerBlockEntity;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MultiBlockBuilder extends BlockEntityTypeBuilder {
    public transient Supplier<BlockPatternBuilder> pattern;

    public MultiBlockBuilder(ResourceLocation i) {
        super(i);
        pattern = null;
    }

    public MultiBlockBuilder pattern(Supplier<BlockPatternBuilder> pattern) {
        this.pattern = pattern;
        return this;
    }

    @Override
    public BlockEntityType<?> createObject() {
        return BlockEntityType.Builder.of((pPos, pState) -> new MultiBlockControllerBlockEntity(this, pPos, pState), this.validBlocks.toArray(Block[]::new)).build(null);
    }

}
