package com.cleanroommc.multiblocked.api.gui.widget.imp.recipe;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.google.common.collect.Lists;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class ContentWidget<T> extends SelectableWidgetGroup {
    protected T content;
    protected float chance;
    protected IO io;
    protected IGuiTexture background;
    protected Consumer<ContentWidget<T>> onPhantomUpdate;
    protected Consumer<ContentWidget<T>> onMouseClicked;

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

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        List<Target<?>> pattern = super.getPhantomTargets(ingredient);
        if (pattern != null && pattern.size() > 0) return pattern;
        if (!ingredient.getClass().equals(getJEIIngredient(getContent()).getClass())) {
            return Collections.emptyList();
        }
        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new IGhostIngredientHandler.Target<Object>() {
            @Nonnull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                if (ingredient.getClass().equals(getJEIIngredient(getContent()).getClass())) {
                    T content = getJEIContent(ingredient);
                    if (content != null) {
                        setContent(io, content, chance);
                        if (onPhantomUpdate != null) {
                            onPhantomUpdate.accept(ContentWidget.this);
                        }
                    }
                }
            }
        });
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        Object result = super.getIngredientOverMouse(mouseX, mouseY);
        if (result != null) return  result;
        if (isMouseOverElement(mouseX, mouseY)) {
            return getJEIIngredient(getContent());
        }
        return null;
    }

    /**
     * get the content from a JEI ingredient
     * @return content
     */
    @SuppressWarnings("unchecked")
    public T getJEIContent(Object content) {
        return (T)content;
    }

    /**
     * get the content's ingredient form in JEI
     * @return ingredient
     */
    public Object getJEIIngredient(T content) {
        return content;
    }

    public IO getIo() {
        return io;
    }

    public T getContent() {
        return content;
    }

    public float getChance() {
        return chance;
    }

    public ContentWidget<T> setOnMouseClicked(Consumer<ContentWidget<T>> onMouseClicked) {
        this.onMouseClicked = onMouseClicked;
        return this;
    }

    public ContentWidget<T> setOnPhantomUpdate(Consumer<ContentWidget<T>> onPhantomUpdate) {
        this.onPhantomUpdate = onPhantomUpdate;
        return this;
    }

    @Override
    public boolean allowSelected(int mouseX, int mouseY, int button) {
        return onSelected != null && isMouseOverElement(mouseX, mouseY);
    }

    @Override
    public void onUnSelected() {
        isSelected = false;
    }

    protected abstract void onContentUpdate();

    /**
     * Configurator.
     */
    public void openConfigurator(WidgetGroup dialog){
        dialog.addWidget(new LabelWidget(5, 8, "Chance:"));
        dialog.addWidget(new TextFieldWidget(125 - 60, 5, 60, 15, true, null, number -> setContent(io, content, Float.parseFloat(number))).setNumbersOnly(0f, 1f).setCurrentString(chance + ""));
    }

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
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && onMouseClicked != null) onMouseClicked.accept(this);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public final void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (background != null) {
            background.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
        }
        drawHookBackground(mouseX, mouseY, partialTicks);
        super.drawInBackground(mouseX, mouseY, partialTicks);
        drawChance();
        drawHoverOverlay(mouseX, mouseY);
        if (isSelected) {
            DrawerHelper.drawBorder(getPosition().x, getPosition().y, getSize().width, getSize().height, 0xff00aa00, 1);
        }
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
