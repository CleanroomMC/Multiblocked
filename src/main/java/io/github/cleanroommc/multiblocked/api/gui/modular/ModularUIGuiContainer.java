package io.github.cleanroommc.multiblocked.api.gui.modular;

import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import io.github.cleanroommc.multiblocked.network.s2c.SPacketUIWidgetUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class ModularUIGuiContainer extends GuiContainer {

    public final ModularUI modularUI;
    public Widget focus;
    public int dragSplittingLimit;
    public int dragSplittingButton;

    public ModularUIGuiContainer(ModularUI modularUI) {
        super(new ModularUIContainer(modularUI));
        this.modularUI = modularUI;
        modularUI.setModularUIGui(this);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.xSize = modularUI.getWidth();
        this.ySize = modularUI.getHeight();
        super.initGui();
        this.modularUI.updateScreenSize(width, height);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        modularUI.guiWidgets.values().forEach(Widget::updateScreen);
    }

    public void handleWidgetUpdate(SPacketUIWidgetUpdate packet) {
        if (packet.windowId == inventorySlots.windowId) {
            Widget widget = modularUI.guiWidgets.get(packet.widgetId);
            int updateId = packet.updateData.readVarInt();
            if (widget != null) {
                widget.readUpdateInfo(updateId, packet.updateData);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.hoveredSlot = null;
        drawDefaultBackground();

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
            Slot slot = this.inventorySlots.inventorySlots.get(i);
            if (slot instanceof SlotWidget.ISlotWidget) {
                if (((SlotWidget.ISlotWidget) slot).isHover()) {
                    setHoveredSlot(slot);
                }
            } else if (isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY) && slot.isEnabled()) {
                renderSlotOverlay(slot);
                setHoveredSlot(slot);
            }
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();

        drawGuiContainerForegroundLayer(partialTicks, mouseX, mouseY);

        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0.0F);
        RenderHelper.enableGUIStandardItemLighting();

        MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawForeground(this, mouseX, mouseY));

        GlStateManager.enableDepth();
        renderItemStackOnMouse(mouseX, mouseY);
        renderReturningItemStack();

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();

        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (this.mc.player.inventory.getItemStack().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.getHasStack()) {
            ItemStack stack = this.hoveredSlot.getStack();
            if (this.hoveredSlot instanceof SlotWidget.ISlotWidget) {
                FontRenderer font = stack.getItem().getFontRenderer(stack);
                net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stack);
                this.drawHoveringText(((SlotWidget.ISlotWidget) this.hoveredSlot).getToolTips(this.getItemToolTip(stack)), mouseX, mouseY, (font == null ? fontRenderer : font));
                net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();
            } else {
                this.renderToolTip(stack, mouseX, mouseY);
            }
        }
    }

    public void setHoveredSlot(Slot hoveredSlot) {
        this.hoveredSlot = hoveredSlot;
    }

    @Deprecated
    public void renderSlotOverlay(Slot slot) {
        GlStateManager.disableDepth();
        int slotX = slot.xPos;
        int slotY = slot.yPos;
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
    }

    public ItemStack getDraggedStack() {
        return this.draggedStack;
    }

    private void renderItemStackOnMouse(int mouseX, int mouseY) {
        InventoryPlayer inventory = this.mc.player.inventory;
        ItemStack itemStack = this.draggedStack.isEmpty() ? inventory.getItemStack() : this.draggedStack;

        if (!itemStack.isEmpty()) {
            int dragOffset = this.draggedStack.isEmpty() ? 8 : 16;
            if (!this.draggedStack.isEmpty() && this.isRightMouseClick) {
                itemStack = itemStack.copy();
                itemStack.setCount(MathHelper.ceil((float) itemStack.getCount() / 2.0F));

            } else if (this.dragSplitting && this.dragSplittingSlots.size() > 1) {
                itemStack = itemStack.copy();
                itemStack.setCount(this.dragSplittingRemnant);
            }
            this.drawItemStack(itemStack, mouseX - guiLeft - 8, mouseY - guiTop - dragOffset, null);
        }
    }

    private void renderReturningItemStack() {
        if (!this.returningStack.isEmpty()) {
            float partialTicks = (float) (Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;
            if (partialTicks >= 1.0F) {
                partialTicks = 1.0F;
                this.returningStack = ItemStack.EMPTY;
            }
            int deltaX = this.returningStackDestSlot.xPos - this.touchUpX;
            int deltaY = this.returningStackDestSlot.yPos - this.touchUpY;
            int currentX = this.touchUpX + (int) ((float) deltaX * partialTicks);
            int currentY = this.touchUpY + (int) ((float) deltaY * partialTicks);
            //noinspection ConstantConditions
            this.drawItemStack(this.returningStack, currentX, currentY, null);
        }
    }

    protected void drawGuiContainerForegroundLayer(float partialTicks, int mouseX, int mouseY) {
        modularUI.guiWidgets.values().forEach(widget -> {
            if (!widget.isVisible()) return;
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            widget.drawInForeground(mouseX, mouseY, partialTicks);
            GlStateManager.popMatrix();
        });
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
        modularUI.backgroundPath.draw(mouseX, mouseY, guiLeft, guiTop, xSize, ySize);
        modularUI.guiWidgets.values().forEach(widget -> {
            if (!widget.isVisible()) return;
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            widget.drawInBackground(mouseX, mouseY, partialTicks);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
        });
    }

    public void switchFocus(Widget widget) {
        if (widget == null) return;
        if (focus == widget) return;
        if (focus != null) focus.setFocus(false);
        focus = widget;
        focus.setFocus(true);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheelMovement = Mouse.getEventDWheel();
        if (wheelMovement != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            mouseWheelMove(mouseX, mouseY, wheelMovement);
        }
    }

    protected void mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive() && widget.mouseWheelMove(mouseX, mouseY, wheelDelta) != null) {
                return;
            }
        }
    }

    public Set<Slot> getDragSplittingSlots() {
        return dragSplittingSlots;
    }

    public boolean getDragSplitting() {
        return dragSplitting;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive() && (widget = widget.mouseClicked(mouseX, mouseY, mouseButton)) != null) {
                switchFocus(widget);
                return;
            }
        }
        switchFocus(null);
    }

    public void superMouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (Exception ignored) { }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive() && widget.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick) != null) {
                return;
            }
        }
    }

    public void superMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive() && widget.mouseReleased(mouseX, mouseY, state) != null) {
                return;
            }
        }
    }

    public void superMouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive() && widget.keyTyped(typedChar, keyCode) != null) {
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

}
