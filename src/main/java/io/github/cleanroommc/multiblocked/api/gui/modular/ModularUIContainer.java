package io.github.cleanroommc.multiblocked.api.gui.modular;

import io.github.cleanroommc.multiblocked.api.gui.util.PerTickIntCounter;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import io.github.cleanroommc.multiblocked.network.MultiblockedNetworking;
import io.github.cleanroommc.multiblocked.network.c2s.CPacketUIClientAction;
import io.github.cleanroommc.multiblocked.network.s2c.SPacketUIWidgetUpdate;
import io.netty.buffer.Unpooled;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketSetSlot;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModularUIContainer extends Container implements WidgetUIAccess {

    protected final HashMap<Slot, SlotWidget> slotMap = new HashMap<>();
    private final ModularUI modularUI;

    public ModularUIContainer(ModularUI modularUI) {
        this.modularUI = modularUI;
        modularUI.guiWidgets.values().forEach(widget -> widget.setUiAccess(this));
        modularUI.guiWidgets.values().stream()
                .flatMap(widget -> widget.getNativeWidgets().stream())
                .forEach(nativeWidget -> {
                    Slot slot = nativeWidget.getHandle();
                    slotMap.put(slot, nativeWidget);
                    addSlotToContainer(slot);
                });
    }

    @Override
    public void notifySizeChange() {
    }

    //WARNING! WIDGET CHANGES SHOULD BE *STRICTLY* SYNCHRONIZED BETWEEN SERVER AND CLIENT,
    //OTHERWISE ID MISMATCH CAN HAPPEN BETWEEN ASSIGNED SLOTS!
    @Override
    public void notifyWidgetChange() {
        List<SlotWidget> nativeWidgets = modularUI.guiWidgets.values().stream()
                .flatMap(widget -> widget.getNativeWidgets().stream())
                .collect(Collectors.toList());

        Set<SlotWidget> removedWidgets = new HashSet<>(slotMap.values());
        removedWidgets.removeAll(nativeWidgets);
        if (!removedWidgets.isEmpty()) {
            for (SlotWidget removedWidget : removedWidgets) {
                Slot slotHandle = removedWidget.getHandle();
                this.slotMap.remove(slotHandle);
                //replace removed slot with empty placeholder to avoid list index shift
                EmptySlotPlaceholder emptySlotPlaceholder = new EmptySlotPlaceholder();
                emptySlotPlaceholder.slotNumber = slotHandle.slotNumber;
                this.inventorySlots.set(slotHandle.slotNumber, emptySlotPlaceholder);
                this.inventoryItemStacks.set(slotHandle.slotNumber, ItemStack.EMPTY);
            }
        }

        Set<SlotWidget> addedWidgets = new HashSet<>(nativeWidgets);
        addedWidgets.removeAll(slotMap.values());
        if (!addedWidgets.isEmpty()) {
            int[] emptySlotIndexes = inventorySlots.stream()
                    .filter(it -> it instanceof EmptySlotPlaceholder)
                    .mapToInt(slot -> slot.slotNumber).toArray();
            int currentIndex = 0;
            for (SlotWidget addedWidget : addedWidgets) {
                Slot slotHandle = addedWidget.getHandle();
                //add or replace empty slot in inventory
                this.slotMap.put(slotHandle, addedWidget);
                if (currentIndex < emptySlotIndexes.length) {
                    int slotIndex = emptySlotIndexes[currentIndex++];
                    slotHandle.slotNumber = slotIndex;
                    this.inventorySlots.set(slotIndex, slotHandle);
                    this.inventoryItemStacks.set(slotIndex, ItemStack.EMPTY);
                } else {
                    slotHandle.slotNumber = this.inventorySlots.size();
                    this.inventorySlots.add(slotHandle);
                    this.inventoryItemStacks.add(ItemStack.EMPTY);
                }
            }
        }
    }

    public ModularUI getModularUI() {
        return modularUI;
    }

    @Override
    public void onContainerClosed(@Nonnull EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        modularUI.guiWidgets.values().forEach(Widget::detectAndSendChanges);
    }

    @Override
    public void sendSlotUpdate(SlotWidget slot) {
        Slot slotHandle = slot.getHandle();
        for (IContainerListener listener : listeners) {
            listener.sendSlotContents(this, slotHandle.slotNumber, slotHandle.getStack());
        }
    }

    @Override
    public void sendHeldItemUpdate() {
        for (IContainerListener listener : listeners) {
            if (listener instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) listener;
                player.connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (listeners.size() > 0) {
            modularUI.guiWidgets.values().forEach(Widget::detectAndSendChanges);
        }
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull EntityPlayer player) {
        if (slotId >= 0 && slotId < inventorySlots.size()) {
            Slot slot = getSlot(slotId);
            ItemStack result = slotMap.get(slot).slotClick(dragType, clickTypeIn, player);
            if (result == null) {
                return super.slotClick(slotId, dragType, clickTypeIn, player);
            }
            return result;
        }
        if (slotId == -999) {
            super.slotClick(slotId, dragType, clickTypeIn, player);
        }
        return ItemStack.EMPTY;
    }

    private final PerTickIntCounter transferredPerTick = new PerTickIntCounter(0);

    private List<SlotWidget> getShiftClickSlots(ItemStack itemStack, boolean fromContainer) {
        return slotMap.values().stream()
                .filter(it -> it.canMergeSlot(itemStack))
                .filter(it -> it.isPlayerInventory == fromContainer)
                .sorted(Comparator.comparing(s -> (fromContainer ? -1 : 1) * s.getHandle().slotNumber))
                .collect(Collectors.toList());
    }

    @Override
    public boolean attemptMergeStack(ItemStack itemStack, boolean fromContainer, boolean simulate) {
        List<Slot> inventorySlots = getShiftClickSlots(itemStack, fromContainer).stream()
                .map(SlotWidget::getHandle)
                .collect(Collectors.toList());
        return mergeItemStack(itemStack, inventorySlots, simulate);
    }

    public static boolean mergeItemStack(ItemStack itemStack, List<Slot> slots, boolean simulate) {
        if (itemStack.isEmpty())
            return false; //if we are merging empty stack, return

        boolean merged = false;
        //iterate non-empty slots first
        //to try to insert stack into them
        for (Slot slot : slots) {
            if (!slot.isItemValid(itemStack))
                continue; //if itemstack cannot be placed into that slot, continue
            ItemStack stackInSlot = slot.getStack();
            if (!ItemStack.areItemsEqual(itemStack, stackInSlot) ||
                    !ItemStack.areItemStackTagsEqual(itemStack, stackInSlot))
                continue; //if itemstacks don't match, continue
            int slotMaxStackSize = Math.min(stackInSlot.getMaxStackSize(), slot.getItemStackLimit(stackInSlot));
            int amountToInsert = Math.min(itemStack.getCount(), slotMaxStackSize - stackInSlot.getCount());
            if (amountToInsert == 0)
                continue; //if we can't insert anything, continue
            //shrink our stack, grow slot's stack and mark slot as changed
            if (!simulate) {
                stackInSlot.grow(amountToInsert);
            }
            itemStack.shrink(amountToInsert);
            slot.onSlotChanged();
            merged = true;
            if (itemStack.isEmpty())
                return true; //if we inserted all items, return
        }

        //then try to insert itemstack into empty slots
        //breaking it into pieces if needed
        for (Slot slot : slots) {
            if (!slot.isItemValid(itemStack))
                continue; //if itemstack cannot be placed into that slot, continue
            if (slot.getHasStack())
                continue; //if slot contains something, continue
            int amountToInsert = Math.min(itemStack.getCount(), slot.getItemStackLimit(itemStack));
            if (amountToInsert == 0)
                continue; //if we can't insert anything, continue
            //split our stack and put result in slot
            ItemStack stackInSlot = itemStack.splitStack(amountToInsert);
            if (!simulate) {
                slot.putStack(stackInSlot);
            }
            merged = true;
            if (itemStack.isEmpty())
                return true; //if we inserted all items, return
        }
        return merged;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (!slot.canTakeStack(player)) {
            return ItemStack.EMPTY;
        }
        if (!slot.getHasStack()) {
            //return empty if we can't transfer it
            return ItemStack.EMPTY;
        }
        ItemStack stackInSlot = slot.getStack();
        ItemStack stackToMerge = slotMap.get(slot).onItemTake(player, stackInSlot.copy(), true);
        boolean fromContainer = !slotMap.get(slot).isPlayerInventory;
        if (!attemptMergeStack(stackToMerge, fromContainer, true)) {
            return ItemStack.EMPTY;
        }
        int itemsMerged;
        if (stackToMerge.isEmpty() || slotMap.get(slot).canMergeSlot(stackToMerge)) {
            itemsMerged = stackInSlot.getCount() - stackToMerge.getCount();
        } else {
            //if we can't have partial stack merge, we have to use all the stack
            itemsMerged = stackInSlot.getCount();
        }
        int itemsToExtract = itemsMerged;
        itemsMerged += transferredPerTick.get(player.world);
        if (itemsMerged > stackInSlot.getMaxStackSize()) {
            //we can merge at most one stack at a time
            return ItemStack.EMPTY;
        }
        transferredPerTick.increment(player.world, itemsToExtract);
        //otherwise, perform extraction and merge
        ItemStack extractedStack = stackInSlot.splitStack(itemsToExtract);
        if (stackInSlot.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        extractedStack = slotMap.get(slot).onItemTake(player, extractedStack, false);
        ItemStack resultStack = extractedStack.copy();
        if (!attemptMergeStack(extractedStack, fromContainer, false)) {
            resultStack = ItemStack.EMPTY;
        }
        if (!extractedStack.isEmpty()) {
            player.dropItem(extractedStack, false, false);
            resultStack = ItemStack.EMPTY;
        }
        return resultStack;
    }

    @Override
    public boolean canMergeSlot(@Nonnull ItemStack stack, @Nonnull Slot slotIn) {
        return slotMap.get(slotIn).canMergeSlot(stack);
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }

    @Override
    public void writeClientAction(Widget widget, int updateId, Consumer<PacketBuffer> payloadWriter) {
        int widgetId = modularUI.guiWidgets.inverse().get(widget);
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeVarInt(updateId);
        payloadWriter.accept(packetBuffer);
        if (modularUI.entityPlayer instanceof EntityPlayerSP) {
            MultiblockedNetworking.sendToServer(new CPacketUIClientAction(windowId, widgetId, packetBuffer));
        }
    }

    @Override
    public void writeUpdateInfo(Widget widget, int updateId, Consumer<PacketBuffer> payloadWriter) {
        int widgetId = modularUI.guiWidgets.inverse().get(widget);
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeVarInt(updateId);
        payloadWriter.accept(packetBuffer);
        if (modularUI.entityPlayer instanceof EntityPlayerMP) {
            SPacketUIWidgetUpdate widgetUpdate = new SPacketUIWidgetUpdate(windowId, widgetId, packetBuffer);
            MultiblockedNetworking.sendToPlayer(widgetUpdate, (EntityPlayerMP) modularUI.entityPlayer);
        }
    }

    private static class EmptySlotPlaceholder extends Slot {

        private static final IInventory EMPTY_INVENTORY = new InventoryBasic("Empty Inventory", false, 0);

        public EmptySlotPlaceholder() {
            super(EMPTY_INVENTORY, 0, -100000, -100000);
        }

        @Nonnull
        @Override
        public ItemStack getStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public void putStack(@Nonnull ItemStack stack) {
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(@Nonnull EntityPlayer playerIn) {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }
}
