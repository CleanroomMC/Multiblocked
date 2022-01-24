package io.github.cleanroommc.multiblocked.api.gui.widget.imp;

import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUIGuiContainer;
import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiConsumer;

public class SlotWidget extends Widget {

    protected final Slot slotReference;
    protected final boolean canTakeItems;
    protected final boolean canPutItems;
    public boolean isPlayerInventory;
    public boolean isPlayerHotBar;

    protected IGuiTexture backgroundTexture;
    protected Runnable changeListener;
    protected BiConsumer<SlotWidget, List<String>> onAddedTooltips;

    public SlotWidget(IInventory inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems) {
        super(new Position(xPosition, yPosition), new Size(18, 18));
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.slotReference = createSlot(inventory, slotIndex);
    }

    public SlotWidget(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems) {
        super(new Position(xPosition, yPosition), new Size(18, 18));
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.slotReference = createSlot(itemHandler, slotIndex);
    }

    protected Slot createSlot(IInventory inventory, int index) {
        return new WidgetSlot(inventory, index, 0, 0);
    }

    protected Slot createSlot(IItemHandler itemHandler, int index) {
        return new WidgetSlotItemHandler(itemHandler, index, 0, 0);
    }

    public SlotWidget setOnAddedTooltips(BiConsumer<SlotWidget, List<String>> onAddedTooltips) {
        this.onAddedTooltips = onAddedTooltips;
        return this;
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(mouseX, mouseY, partialTicks);
        ((ISlotWidget) slotReference).setHover(isMouseOverElement(mouseX, mouseY) && isActive());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            backgroundTexture.draw(mouseX, mouseY, pos.x, pos.y, size.width, size.height);
        }
        ItemStack itemStack = slotReference.getStack();
        ModularUIGuiContainer modularUIGui = gui == null ? null : gui.getModularUIGui();
        if (itemStack.isEmpty() && modularUIGui!= null && modularUIGui.getDragSplitting() && modularUIGui.getDragSplittingSlots().contains(slotReference)) { // draw split
            int splitSize = modularUIGui.getDragSplittingSlots().size();
            itemStack = gui.entityPlayer.inventory.getItemStack();
            if (!itemStack.isEmpty() && splitSize > 1 && Container.canAddItemToSlot(slotReference, itemStack, true)) {
                itemStack = itemStack.copy();
                Container.computeStackSize(modularUIGui.getDragSplittingSlots(), modularUIGui.dragSplittingLimit, itemStack, slotReference.getStack().isEmpty() ? 0 : slotReference.getStack().getCount());
                int k = Math.min(itemStack.getMaxStackSize(), slotReference.getItemStackLimit(itemStack));
                if (itemStack.getCount() > k) {
                    itemStack.setCount(k);
                }
            }
        }
        if (!itemStack.isEmpty()) {
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
            RenderHelper.disableStandardItemLighting();
            RenderHelper.enableStandardItemLighting();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
            itemRender.renderItemAndEffectIntoGUI(itemStack, pos.x + 1, pos.y + 1);
            itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, itemStack, pos.x + 1, pos.y + 1, null);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
        }
        if (isActive()) {
            if (slotReference instanceof ISlotWidget) {
                if (isMouseOverElement(mouseX, mouseY)) {
                    GlStateManager.disableDepth();
                    GlStateManager.colorMask(true, true, true, false);
                    DrawerHelper.drawSolidRect(getPosition().x + 1, getPosition().y + 1, 16, 16, -2130706433);
                    GlStateManager.colorMask(true, true, true, true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableBlend();
                }
            }
        } else {
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            DrawerHelper.drawSolidRect(getPosition().x + 1, getPosition().y + 1, 16, 16, 0xbf000000);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
        }
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            ModularUIGuiContainer modularUIGui = gui.getModularUIGui();
            boolean last = modularUIGui.getDragSplitting();
            gui.getModularUIGui().superMouseClicked(mouseX, mouseY, button);
            if (last != modularUIGui.getDragSplitting()) {
                modularUIGui.dragSplittingButton = button;
                if (button == 0) {
                    modularUIGui.dragSplittingLimit = 0;
                }
                else if (button == 1) {
                    modularUIGui.dragSplittingLimit = 1;
                }
                else if (Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isActiveAndMatches(button - 100)) {
                    modularUIGui.dragSplittingLimit = 2;
                }
            }
            return this;
        }
        return null;
    }

    @Override
    public Widget mouseReleased(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            gui.getModularUIGui().superMouseReleased(mouseX, mouseY, button);
            return this;
        }
        return null;
    }

    @Override
    public Widget mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            gui.getModularUIGui().superMouseClickMove(mouseX, mouseY, button, timeDragged);
            return this;
        }
        return null;
    }

    @Override
    protected void onPositionUpdate() {
        if (slotReference != null && gui != null) {
            Position position = getPosition();
            this.slotReference.xPos = position.x + 1 - gui.getGuiLeft();
            this.slotReference.yPos = position.y + 1 - gui.getGuiTop();
        }
    }

    public SlotWidget setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        this(itemHandler, slotIndex, xPosition, yPosition, true, true);
    }

    public SlotWidget(IInventory inventory, int slotIndex, int xPosition, int yPosition) {
        this(inventory, slotIndex, xPosition, yPosition, true, true);
    }

    public SlotWidget setBackgroundTexture(IGuiTexture backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public boolean canPutStack(ItemStack stack) {
        return isEnabled() && canPutItems;
    }

    public boolean canTakeStack(EntityPlayer player) {
        return isEnabled() && canTakeItems;
    }

    public boolean isEnabled() {
        return this.isActive() && isVisible();
    }

    public boolean canMergeSlot(ItemStack stack) {
        return isEnabled();
    }

    public void onSlotChanged() {
        if (gui == null) return;
        gui.holder.markDirty();
    }

    public ItemStack slotClick(int dragType, ClickType clickTypeIn, EntityPlayer player) {
        return null;
    }

    public final Slot getHandle() {
        return slotReference;
    }

    public Widget setLocationInfo(boolean isPlayerInventory, boolean isPlayerHotBar) {
        this.isPlayerHotBar = isPlayerHotBar;
        this.isPlayerInventory = isPlayerInventory;
        return this;
    }

    public ItemStack onItemTake(EntityPlayer player, ItemStack stack, boolean simulate) {
        return stack;
    }

    private List<String> getToolTips(List<String> list) {
        if (this.onAddedTooltips != null) {
            this.onAddedTooltips.accept(this, list);
        }
        return list;
    }

    public interface ISlotWidget {
        void setHover(boolean isHover);
        boolean isHover();
        List<String> getToolTips(List<String> list);
    }

    protected class WidgetSlot extends Slot implements ISlotWidget {
        boolean isHover;

        public WidgetSlot(IInventory inventory, int index, int xPosition, int yPosition) {
            super(inventory, index, xPosition, yPosition);
        }

        @Override
        public void setHover(boolean isHover) {
            this.isHover = isHover;
        }

        @Override
        public boolean isHover() {
            return isHover;
        }

        @Override
        public List<String> getToolTips(List<String> list) {
            return SlotWidget.this.getToolTips(list);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && super.isItemValid(stack);
        }

        @Override
        public boolean canTakeStack(@Nonnull EntityPlayer playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && super.canTakeStack(playerIn);
        }

        @Override
        public void putStack(@Nonnull ItemStack stack) {
            if(!SlotWidget.this.canPutStack(stack)) return;
            super.putStack(stack);
            if (changeListener != null) {
                changeListener.run();
            }
        }

        @Nonnull
        @Override
        public final ItemStack onTake(@Nonnull EntityPlayer thePlayer, @Nonnull ItemStack stack) {
            return onItemTake(thePlayer, super.onTake(thePlayer, stack), false);
        }

        @Override
        public void onSlotChanged() {
            SlotWidget.this.onSlotChanged();
        }

        @Override
        public boolean isEnabled() {
            return SlotWidget.this.isEnabled();
        }

    }

    protected class WidgetSlotItemHandler extends SlotItemHandler implements ISlotWidget {
        boolean isHover;

        public WidgetSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public void setHover(boolean isHover) {
            this.isHover = isHover;
        }

        @Override
        public boolean isHover() {
            return isHover;
        }

        @Override
        public List<String> getToolTips(List<String> list) {
            return SlotWidget.this.getToolTips(list);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && super.isItemValid(stack);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && super.canTakeStack(playerIn);
        }

        @Override
        public void putStack(@Nonnull ItemStack stack) {
            if(!SlotWidget.this.canPutStack(stack)) return;
            super.putStack(stack);
            if (changeListener != null) {
                changeListener.run();
            }
        }

        @Nonnull
        @Override
        public final ItemStack onTake(@Nonnull EntityPlayer thePlayer, @Nonnull ItemStack stack) {
            return onItemTake(thePlayer, super.onTake(thePlayer, stack), false);
        }

        @Override
        public void onSlotChanged() {
            SlotWidget.this.onSlotChanged();
        }

        @Override
        public boolean isEnabled() {
            return SlotWidget.this.isEnabled();
        }

    }
}
