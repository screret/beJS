package screret.bejs.misc;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import screret.bejs.kubejs.BlockEntityTypeBuilder;

import java.util.List;
import java.util.function.Predicate;

public class MultipleFluidTank implements IMultipleFluidHandler, INBTSerializable<CompoundTag> {
    private static final FluidTank EMPTY = new FluidTank(1);

    private final NonNullList<IFluidTank> tanks;


    public MultipleFluidTank() {
        this(1, 1);
    }

    public MultipleFluidTank(int count, int capacity) {
        this(count, capacity, e -> true);
    }

    public MultipleFluidTank(int count, int capacity, Predicate<FluidStack> validator) {
        this.tanks = NonNullList.withSize(count, new NBTSerializableFluidTank(capacity, validator));
    }

    public MultipleFluidTank(List<BlockEntityTypeBuilder.FluidHandler> handlers) {
        this.tanks = NonNullList.withSize(handlers.size(), EMPTY);
        for (int i = 0; i < handlers.size(); ++i) {
            var tank = handlers.get(i);
            tanks.set(i, new NBTSerializableFluidTank(tank.capacity(), tank.validator()));
        }
    }

    @Override
    public int getTanks() {
        return tanks.size();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return tanks.get(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tanks.get(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tanks.get(tank).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return fill(0, resource, action);
    }

    public int fill(int tank, FluidStack resource, FluidAction action) {
        return tanks.get(tank).fill(resource, action);
    }


    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return drain(0, resource, action);
    }

    public FluidStack drain(int tank, FluidStack resource, FluidAction action) {
        return this.tanks.get(tank).drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return drain(0, maxDrain, action);
    }

    public FluidStack drain(int tank, int maxDrain, FluidAction action) {
        return tanks.get(tank).drain(maxDrain, action);
    }

    @Override
    public void setFluid(int tank, FluidStack stack) {
        if(tanks.get(tank) instanceof FluidTank fluidTank) {
            fluidTank.setFluid(stack);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag tag = new ListTag();
        for (int i = 0; i < this.tanks.size(); ++i) {
            var tank = this.tanks.get(i);
            if (tank instanceof INBTSerializable<?> serializable) {
                tag.add(serializable.serializeNBT());
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("tanks", tag);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag tag = nbt.getList("tanks", Tag.TAG_COMPOUND);
        for (int i = 0; i < tag.size() && i < tanks.size(); ++i) {
            if(this.tanks.get(i) instanceof INBTSerializable serializable) {
                serializable.deserializeNBT(tag.getCompound(i));
            }
        }
    }
}
