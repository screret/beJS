package screret.bejs.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

public class NBTSerializableFluidTank extends FluidTank implements INBTSerializable<CompoundTag> {
    public NBTSerializableFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    @Override
    public CompoundTag serializeNBT() {
        return super.writeToNBT(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
    }
}
