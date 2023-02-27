package screret.bejs.misc;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IMultipleFluidHandler extends IFluidHandler {

    int fill(int tank, FluidStack resource, FluidAction action);

    FluidStack drain(int tank, FluidStack resource, FluidAction action);

    FluidStack drain(int tank, int maxDrain, FluidAction action);

    void setFluid(int tank, FluidStack stack);
}
