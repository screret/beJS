package screret.bejs.misc;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

@AutoRegisterCapability
public interface IMultipleItemHandler extends IItemHandlerModifiable {

    /**
     * @return All containers
     **/
    List<IItemHandlerModifiable> getAllContainers();

    /**
     * @return The number of containers available
     **/
    int getContainers();

    /**
     * @return The container at the given index
     **/
    IItemHandler getContainer(int index);

    /**
     * Returns the ItemStack in a given slot.
     *
     * The result's stack size may be greater than the itemstack's max size.
     *
     * If the result is empty, then the slot is empty.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This ItemStack <em>MUST NOT</em> be modified. This method is not for
     * altering an inventory's contents. Any implementers who are able to detect
     * modification through this method should throw an exception.
     * </p>
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED ITEMSTACK</em></strong>
     * </p>
     *
     * @param slot Slot to query
     * @return ItemStack in given slot. Empty Itemstack if the slot is empty.
     **/
    @Nonnull
    ItemStack getStackInSlot(int container, int slot);

    /**
     * <p>
     * Inserts an ItemStack into the given slot and return the remainder.
     * The ItemStack <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param slot     Slot to insert into.
     * @param stack    ItemStack to insert. This must not be modified by the item handler.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
     *         May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
     *         The returned ItemStack can be safely modified after.
     **/
    @Nonnull
    ItemStack insertItem(int container, int slot, @Nonnull ItemStack stack, boolean simulate);

    /**
     * Extracts an ItemStack from the given slot.
     * <p>
     * The returned value must be empty if nothing is extracted,
     * otherwise its stack size must be less than or equal to {@code amount} and {@link ItemStack#getMaxStackSize()}.
     * </p>
     *
     * @param container Container to extract from.
     * @param slot      Slot to extract from.
     * @param amount    Amount to extract (may be greater than the current stack's max limit)
     * @param simulate  If true, the extraction is only simulated
     * @return ItemStack extracted from the slot, must be empty if nothing can be extracted.
     *         The returned ItemStack can be safely modified after, so item handlers should return a new or copied stack.
     **/
    @Nonnull
    ItemStack extractItem(int container, int slot, int amount, boolean simulate);

    /**
     * Retrieves the maximum stack size allowed to exist in the given slot, in the given container.
     *
     * @param container Container to query.
     * @param slot Slot to query.
     * @return     The maximum stack size allowed in the slot.
     */
    int getSlotLimit(int container, int slot);

    /**
     * <p>
     * This function re-implements the vanilla function {@link Container#canPlaceItem(int, ItemStack)}.
     * It should be used instead of simulated insertions in cases where the contents and state of the inventory are
     * irrelevant, mainly for the purpose of automation and logic (for instance, testing if a minecart can wait
     * to deposit its items into a full inventory, or if the items in the minecart can never be placed into the
     * inventory and should move on).
     * </p>
     * <ul>
     * <li>isItemValid is false when insertion of the item is never valid.</li>
     * <li>When isItemValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual items in the inventory, its fullness, or any other state are <strong>not</strong> considered by isItemValid.</li>
     * </ul>
     * @param container Container to query for validity
     * @param slot      Slot to query for validity
     * @param stack     Stack to test with for validity
     *
     * @return true if the slot can insert the ItemStack, not considering the current state of the inventory.
     *         false if the slot can never insert the ItemStack in any situation.
     */
    boolean isItemValid(int container, int slot, @Nonnull ItemStack stack);

    /**
     * Overrides the stack in the given slot, in the given container. This method is used by the
     * standard Forge helper methods and classes. It is not intended for
     * general use by other mods, and the handler may throw an error if it
     * is called unexpectedly.
     *
     * @param slot  Slot to modify
     * @param stack ItemStack to set slot to (may be empty).
     * @throws RuntimeException if the handler is called in a way that the handler
     * was not expecting.
     **/
    void setStackInSlot(int container, int slot, @NotNull ItemStack stack);


    @Nonnull
    @Override
    default ItemStack getStackInSlot(int slot) {
        return getStackInSlot(0, slot);
    }

    @Nonnull
    @Override
    default ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return insertItem(0, slot, stack, simulate);
    }

    @Nonnull
    @Override
    default ItemStack extractItem(int slot, int amount, boolean simulate) {
        return extractItem(0, slot, amount, simulate);
    }

    @Override
    default int getSlotLimit(int slot) {
        return getSlotLimit(0, slot);
    }

    @Override
    default boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return isItemValid(0, slot, stack);
    }

    @Override
    default void setStackInSlot(int slot, @NotNull ItemStack stack) {
        setStackInSlot(0, slot, stack);
    }

    @Override
    default int getSlots() {
        return getContainer(0).getSlots();
    }

}
