package io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe;

import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public abstract class ContentWidget<T> extends WidgetGroup {
    protected T content;
    protected float chance;
    protected IO io;
    protected IGuiTexture background;

    public ContentWidget() {
        super(0, 0, 20, 20);
        setClientSideWidget();
    }

    public ContentWidget<T> setSelfPosition(int x, int y) {
        setSelfPosition(new Position(x, y));
        return this;
    }

    @SuppressWarnings("unchecked")
    public final ContentWidget<T> setContent(@Nonnull IO io, @Nonnull Object content, float chance) {
        this.io = io;
        this.content = (T) content;
        this.chance = chance;
        onContentUpdate();
        return this;
    }

    protected abstract void onContentUpdate();

    public ContentWidget<T> setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    @Override
    public ContentWidget<T> addWidget(Widget widget) {
        super.addWidget(widget);
        return this;
    }

    @Override
    public ContentWidget<T> setHoverTooltip(String tooltipText) {
        if (chance < 1) {
            tooltipText += chance == 0 ? (TextFormatting.RED + "\nno cost") : ("\nchance: " + TextFormatting.YELLOW + String.format("%.1f", chance * 100) + "%")  + TextFormatting.RESET;
        }
        super.setHoverTooltip(tooltipText);
        return this;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (background != null) {
            background.updateTick();
        }
    }

    @Override
    public final void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (background != null) {
            background.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
        }
        super.drawInBackground(mouseX, mouseY, partialTicks);
        drawHookBackground(mouseX, mouseY, partialTicks);
        drawChance();
        drawHoverOverlay(mouseX, mouseY);
    }

    protected void drawHookBackground(int mouseX, int mouseY, float partialTicks) {

    }


    @SideOnly(Side.CLIENT)
    public void drawChance() {
        if (chance == 1) return;
        Position pos = getPosition();
        Size size = getSize();
        GlStateManager.scale(0.5, 0.5, 1);
        GlStateManager.disableDepth();
        String s = chance == 0 ? "no cost" : String.format("%.1f", chance * 100) + "%";
        int color = chance == 0 ? 0xff0000 : 0xFFFF00;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(s, (pos.x + (size.width / 3f)) * 2 - fontRenderer.getStringWidth(s) + 23, (pos.y + (size.height / 3f) + 6) * 2 - size.height, color);
        GlStateManager.scale(2, 2, 1);
        GlStateManager.enableDepth();
    }

    @SideOnly(Side.CLIENT)
    public void drawHoverOverlay(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            DrawerHelper.drawSolidRect(getPosition().x + 1, getPosition().y + 1, 18, 18, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
        }
    }
}
