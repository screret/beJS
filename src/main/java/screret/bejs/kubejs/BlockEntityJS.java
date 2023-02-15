package screret.bejs.kubejs;

import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.KubeJSRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import screret.screenjs.ScreenJSPlugin;
import screret.screenjs.block.BlockEntityContainerMenu;
import screret.screenjs.kubejs.BlockEntityMenuType;

import javax.annotation.Nullable;

public class BlockEntityJS extends BlockEntity implements Nameable, MenuProvider {
    public final Builder builder;
    private final ResourceLocation id;

    @Nullable
    private Component name;

    public BlockEntityJS(Builder builder, BlockPos pos, BlockState state) {
        super(KubeJSRegistries.blockEntities().get(builder.id), pos, state);
        builder.defaultValues.accept(this.getPersistentData());
        this.builder = builder;
        this.id = builder.id;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if(t instanceof BlockEntityJS blockEntity) {
            blockEntity.builder.tickCallback.tick(level, pos, state, blockEntity);
        } else {
            throw new IllegalStateException("T was not an instance of BlockEntityJS");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        builder.saveCallback.saveAdditional(this.level, this.worldPosition, this, pTag);


        if (this.name != null) {
            pTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        builder.loadCallback.load(this.level, this.worldPosition, this, pTag);

        if (pTag.contains("CustomName", Tag.TAG_STRING)) {
            this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
        }
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player pPlayer) {
        if(Platform.isModLoaded("screenjs")) {
            BlockEntityMenuType.Builder menu = (BlockEntityMenuType.Builder) ScreenJSPlugin.MENU_TYPE.objects.get(this.id);
            return new BlockEntityContainerMenu(menu, windowId, playerInv, this);
        }
        return null;
    }

    public static class Builder extends BlockEntityTypeBuilder {
        public Builder(ResourceLocation i) {
            super(i);
        }

        @Override
        public BlockEntityTypeBuilder ticker(TickCallback ticker) {
            return super.ticker(ticker);
        }

        @Override
        public BlockEntityType<BlockEntityJS> createObject() {
            return BlockEntityType.Builder.of((pPos, pState) -> new BlockEntityJS(this, pPos, pState), this.validBlocks.toArray(Block[]::new)).build(null);
        }
    }
}
