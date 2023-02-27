package screret.bejs.misc;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import screret.bejs.kubejs.BlockEntityTypeBuilder;
import screret.bejs.util.OutputItemStackHandler;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.List;

public class MultipleItemStackHandler implements IMultipleItemHandler, INBTSerializable<CompoundTag> {
    private static final ItemStackHandler EMPTY = new ItemStackHandler(1);

    private final NonNullList<IItemHandlerModifiable> containers;

    public MultipleItemStackHandler() {
        this(1, 1);
    }

    public MultipleItemStackHandler(int count, int size) {
        containers = NonNullList.withSize(count, new ItemStackHandler(size));
    }

    public MultipleItemStackHandler(NonNullList<NonNullList<ItemStack>> stacks) {
        containers = stacks.stream().map(ItemStackHandler::new).collect(NonNullList::create, NonNullList::add, NonNullList::addAll);
    }

    public MultipleItemStackHandler(List<BlockEntityTypeBuilder.ItemHandler> handlers) {
        containers = NonNullList.withSize(handlers.size(), EMPTY);
        for (int i = 0; i < handlers.size(); ++i) {
            var handler = handlers.get(i);
            if(handler.canInput()) {
                containers.set(i, new ItemStackHandler(handler.capacity()));
            } else {
                containers.set(i, new OutputItemStackHandler(handler.capacity()));
            }
        }
    }

    public void addHandler(IItemHandlerModifiable handler) {
        containers.add(handler);
    }

    @Override
    public List<IItemHandlerModifiable> getAllContainers() {
        return containers;
    }

    @Override
    public int getContainers() {
        return containers.size();
    }

    @Override
    public IItemHandler getContainer(int index) {
        return containers.get(index);
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int container, int slot) {
        return containers.get(container).getStackInSlot(slot);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int container, int slot, @NotNull ItemStack stack, boolean simulate) {
        return containers.get(container).insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int container, int slot, int amount, boolean simulate) {
        return containers.get(container).extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int container, int slot) {
        return containers.get(container).getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int container, int slot, @NotNull ItemStack stack) {
        return containers.get(container).isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int container, int slot, @NotNull ItemStack stack) {
        containers.get(container).setStackInSlot(slot, stack);
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag tag = new ListTag();
        for (var container : containers) {
            if (container instanceof INBTSerializable<?> serializable) {
                tag.add(serializable.serializeNBT());
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("containers", tag);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag tag = nbt.getList("containers", Tag.TAG_COMPOUND);
        for (int i = 0; i < tag.size() && i < this.containers.size(); ++i) {
            var cont = this.containers.get(i);
            if(cont instanceof INBTSerializable serializable) {
                serializable.deserializeNBT(tag.getCompound(i));
            }
        }
    }
}
