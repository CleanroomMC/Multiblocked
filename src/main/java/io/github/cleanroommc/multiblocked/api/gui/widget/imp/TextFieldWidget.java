package io.github.cleanroommc.multiblocked.api.gui.widget.imp;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TextFieldWidget extends Widget {

    @SideOnly(Side.CLIENT)
    protected GuiTextField textField;

    protected int maxStringLength = Integer.MAX_VALUE;
    protected Predicate<String> textValidator = (s)->true;
    protected Supplier<String> textSupplier;
    protected Consumer<String> textResponder;
    protected String currentString;
    private IGuiTexture background;
    private boolean enableBackground;

    public TextFieldWidget(int xPosition, int yPosition, int width, int height, boolean enableBackground, Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        if (Multiblocked.isClient()) {
            this.enableBackground = enableBackground;
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            if (enableBackground) {
                this.textField = new GuiTextField(0, fontRenderer, xPosition, yPosition, width, height);
            } else {
                this.textField = new GuiTextField(0, fontRenderer, xPosition + 1, yPosition + (height - fontRenderer.FONT_HEIGHT) / 2 + 1, width - 2, height);
            }
            this.textField.setCanLoseFocus(true);
            this.textField.setEnableBackgroundDrawing(enableBackground);
            this.textField.setMaxStringLength(this.maxStringLength);
            this.textField.setGuiResponder(createTextFieldResponder(this::onTextChanged));
        }
        this.textSupplier = textSupplier;
        this.textResponder = textResponder;
    }

    public TextFieldWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public TextFieldWidget setCurrentString(String currentString) {
        this.currentString = currentString;
        this.textField.setText(currentString);
        return this;
    }

    public String getCurrentString() {
        if (isRemote()) {
            return this.textField.getText();
        }
        return this.currentString;
    }

    @Override
    public void setFocus(boolean focus) {
        super.setFocus(focus);
        if (!focus) {
            this.textField.setFocused(false);
        }
    }

    @Override
    protected void onPositionUpdate() {
        if (Multiblocked.isClient() && textField != null) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            Position position = getPosition();
            Size size = getSize();
            GuiTextField textField = this.textField;
            textField.x = enableBackground ? position.x : position.x + 1;
            textField.y = enableBackground ? position.y : position.y + (size.height - fontRenderer.FONT_HEIGHT) / 2 + 1;
        }
    }

    @Override
    protected void onSizeUpdate() {
        if (Multiblocked.isClient() && textField != null) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            Position position = getPosition();
            Size size = getSize();
            GuiTextField textField = this.textField;
            textField.width = enableBackground ? size.width : size.width - 2;
            textField.height = size.height;
            textField.y = enableBackground ? position.y : position.y + (getSize().height - fontRenderer.FONT_HEIGHT) / 2 + 1;

        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(mouseX, mouseY, partialTicks);
        if (background != null) {
            Position position = getPosition();
            Size size = getSize();
            background.draw(position.x, position.y, size.width, size.height);
        }
        this.textField.drawTextBox();
        GlStateManager.enableBlend();
        GlStateManager.color(1,1,1,1);
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        return this.textField.mouseClicked(mouseX, mouseY, button) ? this : null;
    }

    @Override
    public Widget keyTyped(char charTyped, int keyCode) {
        return this.textField.textboxKeyTyped(charTyped, keyCode) || isFocus() ? this : null;
    }

    @Override
    public Widget mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        return isFocus() ? this : super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public Widget mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return isFocus() ? this : super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    public Widget mouseReleased(int mouseX, int mouseY, int button) {
        return isFocus() ? this : super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void updateScreen() {
        if (background != null) {
            background.updateTick();
        }
        if (textSupplier != null && isClientSideWidget&& !textSupplier.get().equals(currentString)) {
            this.currentString = textSupplier.get();
            this.textField.setText(currentString);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (textSupplier != null && !textSupplier.get().equals(currentString)) {
            this.currentString = textSupplier.get();
            writeUpdateInfo(1, buffer -> buffer.writeString(currentString));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.currentString = buffer.readString(Short.MAX_VALUE);
            this.textField.setText(currentString);
        }
    }

    protected void onTextChanged(String newTextString) {
        if (textValidator.test(newTextString)) {
            if (isClientSideWidget && textResponder != null) {
                textResponder.accept(newTextString);
            }
            writeClientAction(1, buffer -> buffer.writeString(newTextString));
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            String clientText = buffer.readString(Short.MAX_VALUE);
            clientText = clientText.substring(0, Math.min(clientText.length(), maxStringLength));
            if (textValidator.test(clientText)) {
                this.currentString = clientText;
                if (textResponder != null) {
                    this.textResponder.accept(clientText);
                }
            }
        }
    }

    public TextFieldWidget setTextColor(int textColor) {
        if (Multiblocked.isClient()) {
            this.textField.setTextColor(textColor);
        }
        return this;
    }

    public TextFieldWidget setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
        if (Multiblocked.isClient()) {
            this.textField.setMaxStringLength(maxStringLength);
        }
        return this;
    }

    public TextFieldWidget setValidator(Predicate<String> validator) {
        this.textValidator = validator;
        if (Multiblocked.isClient()) {
            this.textField.setValidator(validator::test);
        }
        return this;
    }

    public TextFieldWidget setNumbersOnly(int minValue, int maxValue) {
        setValidator(s -> {
            try {
                int value = Integer.parseInt(s);
                if (minValue <= value && value <= maxValue) return true;
            } catch (NumberFormatException ignored) { }
            return false;
        });
        return this;
    }

    private static GuiPageButtonList.GuiResponder createTextFieldResponder(Consumer<String> onChanged) {
        return new GuiPageButtonList.GuiResponder() {
            @Override
            public void setEntryValue(int id, boolean value) {
            }

            @Override
            public void setEntryValue(int id, float value) {
            }

            @Override
            public void setEntryValue(int id, @Nonnull String value) {
                onChanged.accept(value);
            }
        };
    }


}
