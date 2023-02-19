package screret.bejs.kubejs;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import screret.bejs.BeJS;

import javax.annotation.Nullable;

public class BlockEntityJS extends BlockEntity implements Nameable {
    public final Builder builder;
    private final ResourceLocation id;

    @Nullable
    private Component name;

    public BlockEntityJS(Builder builder, BlockPos pos, BlockState state) {
        super(builder.get(), pos, state);
        if(builder.defaultValues != null)
            builder.defaultValues.accept(this.getPersistentData());
        this.builder = builder;
        this.id = builder.id;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if(t instanceof BlockEntityJS blockEntity) {
            try {
                blockEntity.builder.tickCallback.tick(level, pos, state, blockEntity);
            } catch (Exception exception) {
                BeJS.LOGGER.error("beJS tick error!:", exception);
            }
        } else {
            throw new IllegalStateException("T was not an instance of BlockEntityJS");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

        if (this.name != null) {
            pTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);

        if (pTag.contains("CustomName", Tag.TAG_STRING)) {
            this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
        }

    }

    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : Component.translatable(builder.translationKey);
    }

    @Override
    public boolean hasCustomName() {
        return Nameable.super.hasCustomName();
    }

    @Override
    public Component getDisplayName() {
        return Nameable.super.getDisplayName();
    }

    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    public void setCustomName(Component pName) {
        this.name = pName;
    }


    public static class Builder extends BlockEntityTypeBuilder {
        public Builder(ResourceLocation i) {
            super(i);
        }

        @Override
        public BlockEntityType<BlockEntityJS> createObject() {
            return BlockEntityType.Builder.of((pPos, pState) -> new BlockEntityJS(this, pPos, pState), this.validBlocks.toArray(Block[]::new)).build(null);
        }
    }
}
