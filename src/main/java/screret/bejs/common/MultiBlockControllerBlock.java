package screret.bejs.common;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.block.custom.BasicBlockJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;
import screret.bejs.kubejs.MultiBlockBuilder;

import java.util.function.Consumer;

public class MultiBlockControllerBlock extends BasicBlockJS {
    public static final BooleanProperty VALID = BooleanProperty.create("valid");

    protected final MultiBlockBuilder builder;

    public MultiBlockControllerBlock(Builder p) {
        super(p);
        builder = p.multiBlockBuilder;
        registerDefaultState(this.getStateDefinition().any().setValue(VALID, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VALID);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return builder.get().create(blockPos, blockState);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState originalState = super.getStateForPlacement(context);
        return originalState.setValue(VALID, false);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return builder.tickCallback != null ? MultiBlockControllerBlockEntity::tick : null;
    }

    public static class Builder extends BlockBuilder {
        public transient MultiBlockBuilder multiBlockBuilder;


        public Builder(ResourceLocation i) {
            super(i);
            multiBlockBuilder = getOrCreateBlockEntityTypeBuilder();
        }

        @Override
        public void createAdditionalObjects() {
            super.createAdditionalObjects();
            if (multiBlockBuilder != null) {
                RegistryObjectBuilderTypes.BLOCK_ENTITY_TYPE.addBuilder(multiBlockBuilder);
            }
        }

        @HideFromJS
        protected MultiBlockBuilder getOrCreateBlockEntityTypeBuilder() {
            return multiBlockBuilder == null ? (multiBlockBuilder = new MultiBlockBuilder(id)) : multiBlockBuilder;
        }

        @Override
        public BuilderBase<Block> displayName(String name) {
            if (multiBlockBuilder != null) {
                multiBlockBuilder.displayName(name);
            }
            return super.displayName(name);
        }

        public Builder entity(@Nullable Consumer<MultiBlockBuilder> i) {
            if (i == null) {
                multiBlockBuilder = null;
                lootTable = null;
            } else {
                i.accept(getOrCreateBlockEntityTypeBuilder());
            }

            return this;
        }

        @Override
        public Block createObject() {
            MultiBlockControllerBlock block = new MultiBlockControllerBlock(this);
            multiBlockBuilder.addValidBlock(block);
            return block;
        }
    }
}
