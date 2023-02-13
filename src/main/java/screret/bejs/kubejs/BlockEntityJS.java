package screret.bejs.kubejs;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockEntityJS extends BlockEntity implements Nameable {
    private final Builder builder;
    private final ResourceLocation id;
    private final MenuProvider menu;

    @Nullable
    private Component name;

    /*
     * allowed value types: int, long, short, boolean, string, float
     * allowed keys: "progress":int, "totalProgress":int, "isProcessing":boolean, "fuelDuration":int, "remainingFuel":int
     */
    private final Map<String, Object> values;

    public BlockEntityJS(Builder builder, BlockPos pos, BlockState state) {
        super(ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(builder.id), pos, state);
        this.builder = builder;
        this.id = builder.id;
        this.menu = builder.hasGui ? new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable(builder.translationKey);
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                //return new BlockEntity;
                if(ModList.get().isLoaded("screenjs")) {
                    return builder.menuTypeBuilder.builder.get().create(pContainerId, pPlayerInventory);
                }
                return null;
            }
        } : null;
        this.values = new HashMap<>();
        for(var kvp : builder.defaultValues.entrySet()) {
            if(kvp.getValue() instanceof String str) this.values.put(kvp.getKey(), str);
            else if(kvp.getValue() instanceof Integer _int) this.values.put(kvp.getKey(), _int);
            else if(kvp.getValue() instanceof Long lng) this.values.put(kvp.getKey(), lng);
            else if(kvp.getValue() instanceof Short shrt) this.values.put(kvp.getKey(), shrt);
            else if(kvp.getValue() instanceof Boolean bool) this.values.put(kvp.getKey(), bool);
            else if(kvp.getValue() instanceof Float flt) this.values.put(kvp.getKey(), flt);
        }
    }

    public MenuProvider getMenuProvider() {
        return menu;
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T t) {
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

        CompoundTag tag = new CompoundTag();
        for (var keyVal : getValues().entrySet()) {
            if(keyVal.getValue() instanceof Integer ntgr) tag.putInt(keyVal.getKey(), ntgr);
            else if(keyVal.getValue() instanceof String str) tag.putString(keyVal.getKey(), str);
            else if(keyVal.getValue() instanceof Long lng) tag.putLong(keyVal.getKey(), lng);
            else if(keyVal.getValue() instanceof Short shrt) tag.putShort(keyVal.getKey(), shrt);
            else if(keyVal.getValue() instanceof Boolean bool) tag.putBoolean(keyVal.getKey(), bool);
            else if(keyVal.getValue() instanceof Float flt) tag.putFloat(keyVal.getKey(), flt);
        }
        pTag.put("values", tag);


        if (this.name != null) {
            pTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        builder.loadCallback.load(this.level, this.worldPosition, this, pTag);

        var values = pTag.getCompound("values");
        for (var key : values.getAllKeys()) {
            var value = values.get(key);
            if(value.getId() == Tag.TAG_BYTE) this.values.put(key, values.getBoolean(key));
            else if(value.getId() == Tag.TAG_SHORT) this.values.put(key, ((ShortTag)value).getAsShort());
            else if(value.getId() == Tag.TAG_INT) this.values.put(key, ((IntTag)value).getAsInt());
            else if(value.getId() == Tag.TAG_LONG) this.values.put(key, ((LongTag)value).getAsLong());
            else if(value.getId() == Tag.TAG_STRING) this.values.put(key, value.getAsString());
            else if(value.getId() == Tag.TAG_FLOAT) this.values.put(key, ((FloatTag)value).getAsFloat());
        }

        if (pTag.contains("CustomName", Tag.TAG_STRING)) {
            this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
        }
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValue(String key, Object value) {
        values.put(key, value);
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : Component.translatable(builder.translationKey);
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
        public BlockEntityType<?> createObject() {
            return BlockEntityType.Builder.of((pPos, pState) -> new BlockEntityJS(Builder.this, pPos, pState), this.validBlocks.toArray(Block[]::new)).build(null);
        }
    }
}
