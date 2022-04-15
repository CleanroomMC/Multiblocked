package com.cleanroommc.multiblocked.common.capability.widget;

import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.TextFormattingUtil;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class NumberContentWidget extends ContentWidget<Number> {
    protected boolean isDecimal;
    protected IGuiTexture contentTexture;
    protected String unit;

    public NumberContentWidget setContentTexture(IGuiTexture contentTexture) {
        this.contentTexture = contentTexture;
        return this;
    }

    public NumberContentWidget setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    @Override
    protected void onContentUpdate() {
        isDecimal = content instanceof Float || content instanceof Double;
        this.setHoverTooltip(content + " " + unit);
    }

    @Override
    public void openConfigurator(WidgetGroup dialog) {
        super.openConfigurator(dialog);
        TextFieldWidget textFieldWidget;
        int x = 5;
        int y = 25;
        dialog.addWidget(new LabelWidget(5, y + 3, "Number:"));
        dialog.addWidget(textFieldWidget = new TextFieldWidget(125 - 60, y, 60, 15, true, null, number -> {
            if (content instanceof Float) {
                content = Float.parseFloat(number);
            } else if (content instanceof Double) {
                content = Double.parseDouble(number);
            } else if (content instanceof Integer) {
                content = Integer.parseInt(number);
            } else if (content instanceof Long) {
                content = Long.parseLong(number);
            }
            onContentUpdate();
        }).setCurrentString(content.toString()));
        if (isDecimal) {
            textFieldWidget.setNumbersOnly(0f, Integer.MAX_VALUE);
        } else {
            if (content instanceof Long) {
                textFieldWidget.setNumbersOnly(0, Long.MAX_VALUE);
            } else {
                textFieldWidget.setNumbersOnly(0, Integer.MAX_VALUE);
            }
        }
        dialog.addWidget(createButton(textFieldWidget, -10000, x, y + 66));
        dialog.addWidget(createButton(textFieldWidget, -100, x, y + 44));
        dialog.addWidget(createButton(textFieldWidget, -1, x, y + 22));
        dialog.addWidget(createButton(textFieldWidget, 1, x + 75, y + 22));
        dialog.addWidget(createButton(textFieldWidget, 100, x + 75, y + 44));
        dialog.addWidget(createButton(textFieldWidget, 10000, x + 75, y + 66));
    }

    private ButtonWidget createButton(TextFieldWidget textFieldWidget, int num, int x, int y) {
        return (ButtonWidget) new ButtonWidget(x, y, 45, 18,
                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture((num >= 0 ? "+" : "") + num)),
                cd -> {
                    String number = textFieldWidget.getCurrentString();
                    Number newValue = null;
                    int scale = num * (cd.isShiftClick ? 10 : 1);
                    if (content instanceof Float) {
                        if (Float.parseFloat(number) + scale >= 0) {
                            newValue = Float.parseFloat(number) + scale;
                        }
                    } else if (content instanceof Double) {
                        if (Double.parseDouble(number) + scale >= 0) {
                            newValue = Double.parseDouble(number) + scale;
                        }
                    } else if (content instanceof Integer) {
                        if (Integer.parseInt(number) + scale >= 0) {
                            newValue = Integer.parseInt(number) + scale;
                        }
                    } else if (content instanceof Long) {
                        if (Long.parseLong(number) + scale >= 0) {
                            newValue = Long.parseLong(number) + scale;
                        }
                    }
                    if (newValue != null) {
                        content = newValue;
                        onContentUpdate();
                        textFieldWidget.setCurrentString(newValue.toString());
                    }
                }).setHoverBorderTexture(1, -1).setHoverTooltip("shift-click: " + (num >= 0 ? "+" : "") + num * 10);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (contentTexture != null) {
            contentTexture.updateTick();
        }
    }

    @Override
    public void drawHookBackground(int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (contentTexture != null) {
            contentTexture.draw(mouseX, mouseY, position.x + 1, position.y + 1, size.width - 2, size.height - 2);
        }
        GlStateManager.scale(0.5, 0.5, 1);
        GlStateManager.disableDepth();
        String s = TextFormattingUtil.formatLongToCompactString(content.intValue(), 4);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(s, (position.x + (size.width / 3f)) * 2 - fontRenderer.getStringWidth(s) + 21, (position.y + (size.height / 3f) + 6) * 2, 0xFFFFFF);
        GlStateManager.scale(2, 2, 1);
    }
}
