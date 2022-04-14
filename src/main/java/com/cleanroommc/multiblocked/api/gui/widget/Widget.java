package com.cleanroommc.multiblocked.api.gui.widget;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import com.cleanroommc.multiblocked.api.gui.modular.WidgetUIAccess;
import com.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Widget is functional element of ModularUI
 * It can draw, perform actions, react to key press and mouse
 * It's information is also synced to client
 */
public class Widget {

    protected ModularUI gui;
    protected WidgetUIAccess uiAccess;
    private Position parentPosition = Position.ORIGIN;
    private Position selfPosition;
    private Position position;
    private Size size;
    private boolean isVisible;
    private boolean isActive;
    private boolean isFocus;
    protected boolean isClientSideWidget;
    private String tooltipText;

    public Widget(Position selfPosition, Size size) {
        Preconditions.checkNotNull(selfPosition, "selfPosition");
        Preconditions.checkNotNull(size, "size");
        this.selfPosition = selfPosition;
        this.size = size;
        this.position = this.parentPosition.add(selfPosition);
        this.isVisible = true;
        this.isActive = true;
    }

    public Widget(int x, int y, int width, int height) {
        this(new Position(x, y), new Size(width, height));
    }

    public Widget setClientSideWidget() {
        isClientSideWidget = true;
        return this;
    }

    public Widget setHoverTooltip(String tooltipText) {
        this.tooltipText = tooltipText;
        return this;
    }

    public void setGui(ModularUI gui) {
        this.gui = gui;
    }
    
    public ModularUI getGui() {
        return gui;
    }

    public void setUiAccess(WidgetUIAccess uiAccess) {
        this.uiAccess = uiAccess;
    }

    public void setParentPosition(Position parentPosition) {
        Preconditions.checkNotNull(parentPosition, "parentPosition");
        this.parentPosition = parentPosition;
        recomputePosition();
    }

    public void setSelfPosition(Position selfPosition) {
        Preconditions.checkNotNull(selfPosition, "selfPosition");
        this.selfPosition = selfPosition;
        recomputePosition();
    }

    public Position addSelfPosition(int addX, int addY) {
        this.selfPosition = new Position(selfPosition.x + addX, selfPosition.y + addY);
        recomputePosition();
        return this.selfPosition;
    }

    public Position getSelfPosition() {
        return selfPosition;
    }

    public void setSize(Size size) {
        Preconditions.checkNotNull(size, "size");
        this.size = size;
        onSizeUpdate();
    }

    public final Position getPosition() {
        return position;
    }

    public final Size getSize() {
        return size;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Rectangle toRectangleBox() {
        Position pos = getPosition();
        Size size = getSize();
        return new Rectangle(pos.x, pos.y, size.width, size.height);
    }

    protected void recomputePosition() {
        this.position = this.parentPosition.add(selfPosition);
        onPositionUpdate();
    }

    protected void onPositionUpdate() {
    }

    protected void onSizeUpdate() {
    }

    public boolean isMouseOverElement(int mouseX, int mouseY) {
        Position position = getPosition();
        Size size = getSize();
        return isMouseOver(position.x, position.y, size.width, size.height, mouseX, mouseY);
    }

    public static boolean isMouseOver(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && x + width > mouseX && y + height > mouseY;
    }

    /**
     * Called on both sides to initialize widget data
     */
    public void initWidget() {
    }

    public void writeInitialData(PacketBuffer buffer) {
    }

    public void readInitialData(PacketBuffer buffer) {
        
    }
    
    /**
     * Called on serverside to detect changes and synchronize them with clients
     */
    public void detectAndSendChanges() {
    }

    /**
     * Called clientside every tick with this modular UI open
     */
    public void updateScreen() {
    }

    /**
     * Called each draw tick to draw this widget in GUI
     */
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY, float partialTicks) {
        if (tooltipText != null && gui != null && isMouseOverElement(mouseX, mouseY)) {
            GlStateManager.enableDepth();
            List<String> hoverList = Arrays.asList(I18n.format(tooltipText).split("\n"));
            DrawerHelper.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY, gui.getScreenWidth(), gui.getScreenHeight());
        }
    }

    /**
     * Called each draw tick to draw this widget in GUI
     */
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
    }

    /**
     * Called when mouse wheel is moved in GUI
     * For some -redacted- reason mouseX position is relative against GUI not game window as in other mouse events
     */
    @SideOnly(Side.CLIENT)
    public Widget mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        return null;
    }

    /**
     * Called when mouse is clicked in GUI
     */
    @SideOnly(Side.CLIENT)
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        return null;
    }

    /**
     * Called when mouse is pressed and hold down in GUI
     */
    @SideOnly(Side.CLIENT)
    public Widget mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return null;
    }

    /**
     * Called when mouse is released in GUI
     */
    @SideOnly(Side.CLIENT)
    public Widget mouseReleased(int mouseX, int mouseY, int button) {
        return null;
    }

    /**
     * Called when key is typed in GUI
     */
    @SideOnly(Side.CLIENT)
    public Widget keyTyped(char charTyped, int keyCode) {
        return null;
    }

    public void setFocus(boolean focus) {
        isFocus = focus;
    }

    public boolean isFocus() {
        return isFocus;
    }

    /**
     * Read data received from server's {@link #writeUpdateInfo}
     */
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
    }

    public void handleClientAction(int id, PacketBuffer buffer) {
    }

    public List<SlotWidget> getNativeWidgets() {
        if (this instanceof SlotWidget) {
            return Collections.singletonList((SlotWidget) this);
        }
        return Collections.emptyList();
    }

    /**
     * Writes data to be sent to client's {@link #readUpdateInfo}
     */
    protected final void writeUpdateInfo(int id, Consumer<PacketBuffer> packetBufferWriter) {
        if (uiAccess != null && gui != null) {
            uiAccess.writeUpdateInfo(this, id, packetBufferWriter);
        }
    }

    @SideOnly(Side.CLIENT)
    protected final void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {
        if (uiAccess != null && !isClientSideWidget) {
            uiAccess.writeClientAction(this, id, packetBufferWriter);
        }
    }

    @SideOnly(Side.CLIENT)
    protected static void playButtonClickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @SideOnly(Side.CLIENT)
    protected static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @SideOnly(Side.CLIENT)
    protected static boolean isCtrlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    public boolean isRemote() {
        return (gui != null && gui.holder != null) ? gui.holder.isRemote() : Multiblocked.isClient();
    }

}
