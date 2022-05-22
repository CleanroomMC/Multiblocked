package com.cleanroommc.multiblocked.api.gui.widget.imp.recipe;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.util.LocalizationUtils;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.google.common.collect.Lists;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class ContentWidget<T> extends SelectableWidgetGroup {
    protected T content;
    protected float chance;
    protected IO io;
    protected boolean perTick;
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
    public final ContentWidget<T> setContent(@Nonnull IO io, @Nonnull Object content, float chance, boolean perTick) {
        this.io = io;
        this.content = (T) content;
        this.chance = chance;
        this.perTick = perTick;
        onContentUpdate();
        return this;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        List<Target<?>> pattern = super.getPhantomTargets(ingredient);
        if (pattern != null && pattern.size() > 0) return pattern;
        Object ingredientContent = getJEIIngredient(getContent());
        if (ingredientContent == null || !ingredient.getClass().equals(ingredientContent.getClass())) {
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
                Object ingredientContent = getJEIIngredient(getContent());
                if (ingredientContent != null && ingredient.getClass().equals(ingredientContent.getClass())) {
                    T content = getJEIContent(ingredient);
                    if (content != null) {
                        setContent(io, content, chance, perTick);
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
    @Nullable
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

    public boolean getPerTick() {
        return perTick;
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
        dialog.addWidget(new LabelWidget(5, 8, "multiblocked.gui.label.chance"));
        dialog.addWidget(new TextFieldWidget(125 - 60, 5, 30, 15, true, null, number -> setContent(io, content, Float.parseFloat(number), perTick)).setNumbersOnly(0f, 1f).setCurrentString(chance + ""));
        dialog.addWidget(new SwitchWidget(125 - 25 , 5, 15, 15, (cd, r) -> setContent(io, content, chance, r))
                .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0,1,0.5))
                .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0.5,1,0.5))
                .setHoverBorderTexture(1, -1)
                .setPressed(perTick)
                .setHoverTooltip("multiblocked.gui.content.per_tick"));
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
            tooltipText += "\n" + (chance == 0 ?
                    LocalizationUtils.format("multiblocked.gui.content.chance_0") :
                    LocalizationUtils.format("multiblocked.gui.content.chance_1", String.format("%.1f", chance * 100) + "%%"));
        }
        if (perTick) {
            tooltipText += "\n" + LocalizationUtils.format("multiblocked.gui.content.per_tick");
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
        drawTick();
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
        String s = chance == 0 ? LocalizationUtils.format("multiblocked.gui.content.chance_0_short") : String.format("%.1f", chance * 100) + "%";
        int color = chance == 0 ? 0xff0000 : 0xFFFF00;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(s, (pos.x + (size.width / 3f)) * 2 - fontRenderer.getStringWidth(s) + 23, (pos.y + (size.height / 3f) + 6) * 2 - size.height, color);
        GlStateManager.scale(2, 2, 1);
    }

    @SideOnly(Side.CLIENT)
    public void drawTick() {
        if (perTick) {
            Position pos = getPosition();
            Size size = getSize();
            GlStateManager.scale(0.5, 0.5, 1);
            GlStateManager.disableDepth();
            String s = LocalizationUtils.format("multiblocked.gui.content.tips.per_tick_short");
            int color = 0xFFFF00;
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawStringWithShadow(s, (pos.x + (size.width / 3f)) * 2 - fontRenderer.getStringWidth(s) + 23, (pos.y + (size.height / 3f) + 6) * 2 - size.height + (chance == 1 ? 0 : 10), color);
            GlStateManager.scale(2, 2, 1);
        }
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
