package com.cleanroommc.modularui.common.internal.wrapper;

import com.cleanroommc.modularui.api.screen.ModularUIContext;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import invtweaks.api.container.ChestContainer;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.*;

@ChestContainer
public class ModularUIContainer extends Container {

    private final ModularUIContext context;
    private boolean initialisedContainer = false;
    private final List<BaseSlot> sortedShiftClickSlots = new ArrayList<>();

    // invtweaks compat
    private boolean reservedSortingSlots = false;
    private boolean verticalInvTweaks = false;
    private int slotsPerRow = 0;
    private final Map<ContainerSection, List<Slot>> invTweaksSectionSlots = new HashMap<>();

    public ModularUIContainer(ModularUIContext context, ModularWindow mainWindow) {
        this.context = context;
        this.context.initialize(this, mainWindow);
        checkSlotIds();
        sortSlots();
        initialisedContainer = true;
    }

    public void setSorted(int slotsPerRow, boolean verticalButtons) {
        if (this.reservedSortingSlots) {
            throw new IllegalStateException("Sorting is already reserved for a different inventory");
        }
        this.slotsPerRow = slotsPerRow;
        this.verticalInvTweaks = verticalButtons;
        this.reservedSortingSlots = true;
    }

    public void sortSlots() {
        this.sortedShiftClickSlots.sort(Comparator.comparingInt(BaseSlot::getShiftClickPriority));
    }

    public ModularUIContext getContext() {
        return context;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.isEntityAlive();
    }

    private void checkSlotIds() {
        for (int i = 0; i < inventorySlots.size(); i++) {
            inventorySlots.get(i).slotNumber = i;
        }
    }

    @Override
    public Slot addSlotToContainer(Slot slotIn) {
        if (slotIn instanceof BaseSlot && ((BaseSlot) slotIn).getShiftClickPriority() > Integer.MIN_VALUE) {
            sortedShiftClickSlots.add((BaseSlot) slotIn);
        }
        slotIn.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(slotIn);

        if (initialisedContainer) {
            sortSlots();
        }
        return slotIn;
    }

    public void removeSlot(Slot slot) {
        if (slot != inventorySlots.get(slot.slotNumber)) {
            throw new IllegalStateException("Could not find slot in container!");
        }
        inventorySlots.remove(slot.slotNumber);

        if (slot instanceof BaseSlot && sortedShiftClickSlots.remove(slot) && initialisedContainer) {
            sortSlots();
        }
        checkSlotIds();
        for (List<Slot> slots : invTweaksSectionSlots.values()) {
            slots.removeIf(slot1 -> slot1 == slot);
        }
    }

    public void setSlotSortable(BaseSlot slot, ContainerSection section) {
        if (slot != inventorySlots.get(slot.slotNumber)) {
            throw new IllegalArgumentException("Slot is not at the expected index!");
        }
        if (section == ContainerSection.INVENTORY_HOTBAR || section == ContainerSection.INVENTORY_NOT_HOTBAR) {
            setSlotSortable(slot, ContainerSection.INVENTORY);
        }
        this.invTweaksSectionSlots.computeIfAbsent(section, section1 -> new ArrayList<>()).add(slot);
    }

    @Override
    public void detectAndSendChanges() {
        for (ModularWindow window : this.context.getOpenWindows()) {
            if (window.isInitialized()) {
                // do not allow syncing before the client is initialized
                window.serverUpdate();
            }
        }
    }

    public void sendSlotChange(ItemStack stack, int index) {
        for (IContainerListener listener : this.listeners) {
            listener.sendSlotContents(this, index, stack);
        }
    }

    public void sendHeldItemUpdate() {
        for (IContainerListener listener : listeners) {
            if (listener instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) listener;
                player.connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = this.inventorySlots.get(index);
        if (slot instanceof BaseSlot && !((BaseSlot) slot).isPhantom()) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                ItemStack remainder = transferItem((BaseSlot) slot, stack.copy());
                stack.setCount(remainder.getCount());
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack transferItem(BaseSlot fromSlot, ItemStack stack) {
        for (BaseSlot slot : this.sortedShiftClickSlots) {
            if (fromSlot.getShiftClickPriority() != slot.getShiftClickPriority() && slot.isEnabled() && slot.isItemValidPhantom(stack)) {
                ItemStack itemstack = slot.getStack();
                if (slot.isPhantom()) {
                    if (itemstack.isEmpty() || (ItemHandlerHelper.canItemStacksStackRelaxed(stack, itemstack) && itemstack.getCount() < slot.getItemStackLimit(itemstack))) {
                        slot.putStack(stack.copy());
                        return stack;
                    }
                } else {
                    if (ItemHandlerHelper.canItemStacksStackRelaxed(stack, itemstack)) {
                        int j = itemstack.getCount() + stack.getCount();
                        int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

                        if (j <= maxSize) {
                            stack.setCount(0);
                            itemstack.setCount(j);
                            slot.onSlotChanged();
                        } else if (itemstack.getCount() < maxSize) {
                            stack.shrink(maxSize - itemstack.getCount());
                            itemstack.setCount(maxSize);
                            slot.onSlotChanged();
                        }

                        if (stack.isEmpty()) {
                            return stack;
                        }
                    }
                }
            }
        }
        for (Slot slot1 : this.sortedShiftClickSlots) {
            if (!(slot1 instanceof BaseSlot)) {
                continue;
            }
            BaseSlot slot = (BaseSlot) slot1;
            ItemStack itemstack = slot.getStack();
            if (fromSlot.getItemHandler() != slot.getItemHandler() && slot.isEnabled() && itemstack.isEmpty() && slot.isItemValid(stack)) {
                if (stack.getCount() > slot1.getSlotStackLimit()) {
                    slot.putStack(stack.splitStack(slot.getSlotStackLimit()));
                } else {
                    slot.putStack(stack.splitStack(stack.getCount()));
                }
                break;
            }
        }
        return stack;
    }

    // invtweaks compat

    public boolean hasReservedSortingSlots() {
        return reservedSortingSlots;
    }

    @ChestContainer.IsLargeCallback
    public boolean isVerticalInvTweaks() {
        return verticalInvTweaks;
    }

    @ChestContainer.RowSizeCallback
    public int getSlotsPerRow() {
        return slotsPerRow;
    }

    @ContainerSectionCallback
    public Map<ContainerSection, List<Slot>> getSectionSlots() {
        return this.reservedSortingSlots ? invTweaksSectionSlots : Collections.emptyMap();
    }

    public static boolean isPlayerInventory(ContainerSection section) {
        return section == ContainerSection.INVENTORY || section == ContainerSection.INVENTORY_HOTBAR || section == ContainerSection.INVENTORY_NOT_HOTBAR;
    }
}
