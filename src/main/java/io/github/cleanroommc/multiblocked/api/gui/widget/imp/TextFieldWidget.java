package io.github.cleanroommc.multiblocked.api.gui.widget.imp;

import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * @author brachy84
 */
public class TextFieldWidget extends Widget {

    // all positive whole numbers
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*");
    // all positive and negative numbers
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*");
    public static final Pattern DECIMALS = Pattern.compile("[0-9]*(\\.[0-9]*)?");
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");

    private String text;
    private String localisedPostFix;
    private final Supplier<String> supplier;
    private final Consumer<String> setter;
    private Consumer<TextFieldWidget> onFocus;
    private IGuiTexture background;
    private Pattern regex;
    private Function<String, String> validator = s -> s;
    private boolean initialised = false;
    private boolean centered;
    private int textColor = 0xFFFFFF;
    private int markedColor = 0x2F72A8;
    private boolean postFixRight = false;
    private int maxLength = 32;
    private float scale = 1;

    private int cursorPos;
    private int cursorPos2;

    private int clickTime = 20;
    private int cursorTime = 0;
    private boolean drawCursor = true;

    public TextFieldWidget(int x, int y, int width, int height, Supplier<String> supplier, Consumer<String> setter) {
        super(x, y, width, height);
        this.supplier = supplier;
        this.setter = setter;
        this.text = supplier.get();
    }

    public TextFieldWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    @Override
    public void initWidget() {
        if (isRemote()) {
            this.localisedPostFix = I18n.hasKey(localisedPostFix) ? I18n.format(localisedPostFix) : localisedPostFix;
        }
    }

    @Override
    public void updateScreen() {
        if (background != null) {
            background.updateTick();
        }
        if (isClientSideWidget) {
            text = supplier.get();
            if (cursorPos > text.length()) {
                cursorPos = text.length();
            }
            if (cursorPos2 > text.length()) {
                cursorPos2 = text.length();
            }
        }
        clickTime++;
        if (++cursorTime == 10) {
            cursorTime = 0;
            drawCursor = !drawCursor;
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (supplier != null) {
            String t = supplier.get();
            if (!initialised || (!isFocus() && !text.equals(t))) {
                text = t;
                writeUpdateInfo(-2, buf -> buf.writeString(text));
                initialised = true;
            }
        }
    }

    private int getTextX() {
        if (centered) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            int w = getSize().width;
            float textW = fontRenderer.getStringWidth(text) * scale;
            if (localisedPostFix != null && !localisedPostFix.isEmpty())
                textW += 3 + fontRenderer.getStringWidth(localisedPostFix) * scale;
            return (int) (w / 2f - textW / 2f + getPosition().x);
        }
        return getPosition().x + 1;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(mouseX, mouseY, partialTicks);
        if (background != null) {
            background.draw(mouseX, mouseY, getPosition().x, getPosition().y, getSize().width, getSize().height);
        }
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int y = getPosition().y;
        int textX = getTextX();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0);
        float scaleFactor = 1 / scale;
        y *= scaleFactor;
        if (cursorPos != cursorPos2) {
            // render marked text background
            float startX = fontRenderer.getStringWidth(text.substring(0, Math.min(cursorPos, cursorPos2))) * scale + textX;
            String marked = getSelectedText();
            float width = fontRenderer.getStringWidth(marked);
            drawSelectionBox(startX * scaleFactor, y, width);
        }
        fontRenderer.drawString(text, (int) (textX * scaleFactor), y, textColor);
        if (localisedPostFix != null && !localisedPostFix.isEmpty()) {
            // render postfix
            int x = postFixRight && !centered ?
                    getPosition().x + getSize().width - (fontRenderer.getStringWidth(localisedPostFix) + 1) :
                    textX + fontRenderer.getStringWidth(text) + 3;
            x *= scaleFactor;
            fontRenderer.drawString(localisedPostFix, x, y, textColor);
        }
        if (isFocus() && drawCursor) {
            // render cursor
            String sub = text.substring(0, cursorPos);
            float x = fontRenderer.getStringWidth(sub) * scale + textX;
            x *= scaleFactor;
            drawCursor(x, y);
        }
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    private void drawCursor(float x, float y) {
        x -= 0.9f;
        y -= 1;
        float endX = x + 0.5f * (1 / scale);
        float endY = y + 9;
        float red = (float) (textColor >> 16 & 255) / 255.0F;
        float green = (float) (textColor >> 8 & 255) / 255.0F;
        float blue = (float) (textColor & 255) / 255.0F;
        float alpha = (float) (textColor >> 24 & 255) / 255.0F;
        if (alpha == 0)
            alpha = 1f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, y, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    @SideOnly(Side.CLIENT)
    private void drawSelectionBox(float x, float y, float width) {
        float endX = x + width;
        y -= 1;
        float endY = y + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        float red = (float) (markedColor >> 16 & 255) / 255.0F;
        float green = (float) (markedColor >> 8 & 255) / 255.0F;
        float blue = (float) (markedColor & 255) / 255.0F;
        float alpha = (float) (markedColor >> 24 & 255) / 255.0F;
        if (alpha == 0)
            alpha = 1f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, y, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && isActive()) {
            if (onFocus != null) {
                onFocus.accept(this);
            }
            if (clickTime < 5) {
                cursorPos = text.length();
                cursorPos2 = 0;
            } else {
                cursorPos = getCursorPosFromMouse(mouseX);
                cursorPos2 = cursorPos;
            }
            clickTime = 0;
            return this;
        }
        return null;
    }

    @Override
    public Widget mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (isFocus() && button == 0 && isActive()) {
            if (mouseX < getPosition().x) {
                cursorPos = 0;
                return this;
            }
            cursorPos = getCursorPosFromMouse(mouseX);
        }
        return isFocus() ? this : null;
    }

    private int getCursorPosFromMouse(int mouseX) {
        int base = mouseX - getTextX();
        float x = 1;
        int i = 0;
        while (x < base) {
            if (i == text.length())
                break;
            x += (Minecraft.getMinecraft().fontRenderer.getCharWidth(text.charAt(i))) * scale;
            i++;
        }
        return i;
    }

    public String getSelectedText() {
        return text.substring(Math.min(cursorPos, cursorPos2), Math.max(cursorPos, cursorPos2));
    }

    @Override
    public Widget keyTyped(char charTyped, int keyCode) {
        if (isFocus() && isActive()) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                return null;
            }
            if (keyCode == Keyboard.KEY_RETURN) {
                return this;
            }
            if (GuiScreen.isKeyComboCtrlA(keyCode)) {
                cursorPos = text.length();
                cursorPos2 = 0;
                return this;
            }
            if (GuiScreen.isKeyComboCtrlC(keyCode)) {
                GuiScreen.setClipboardString(this.getSelectedText());
                return this;
            } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
                String clip = GuiScreen.getClipboardString();
                if (text.length() + clip.length() > maxLength || !isAllowed(clip))
                    return this;
                replaceMarkedText(clip);
                return this;
            } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
                GuiScreen.setClipboardString(this.getSelectedText());
                replaceMarkedText(null);
                return this;
            }
            if (keyCode == Keyboard.KEY_LEFT && cursorPos > 0) {
                int amount = 1;
                int pos = cursorPos;
                if (isCtrlDown()) {
                    for (int i = pos - 1; i >= 0; i--) {
                        if (i == 0 || text.charAt(i) == ' ') {
                            amount = pos - i;
                            break;
                        }
                    }
                }
                cursorPos -= amount;
                if (cursorPos < 0)
                    cursorPos = 0;
                if (!isShiftDown()) {
                    cursorPos2 = cursorPos;
                }
                return this;
            }
            if (keyCode == Keyboard.KEY_RIGHT && cursorPos < text.length()) {
                int amount = 1;
                int pos = cursorPos;
                if (isCtrlDown()) {
                    for (int i = pos + 1; i < text.length(); i++) {
                        if (i == text.length() - 1 || text.charAt(i) == ' ') {
                            amount = i - pos;
                            break;
                        }
                    }
                }
                cursorPos += amount;
                if (cursorPos > text.length())
                    cursorPos = text.length();
                if (!isShiftDown()) {
                    cursorPos2 = cursorPos;
                }
                return this;
            }
            if (keyCode == Keyboard.KEY_BACK && text.length() > 0) {
                if (cursorPos != cursorPos2) {
                    replaceMarkedText(null);
                } else if (cursorPos > 0) {
                    String t1 = text.substring(0, cursorPos - 1);
                    String t2 = text.substring(cursorPos);
                    text = t1 + t2;
                    cursorPos--;
                    cursorPos2 = cursorPos;
                }
            } else if (keyCode == Keyboard.KEY_DELETE && text.length() > 0) {
                if (cursorPos != cursorPos2) {
                    replaceMarkedText(null);
                } else if (cursorPos < text.length()) {
                    String t1 = text.substring(0, cursorPos);
                    String t2 = text.substring(cursorPos + 1);
                    text = t1 + t2;
                    cursorPos2 = cursorPos;
                }
            }
            if (charTyped != Character.MIN_VALUE && text.length() < maxLength) {
                int min = Math.min(cursorPos, cursorPos2);
                int max = Math.max(cursorPos, cursorPos2);
                String t1 = text.substring(0, min);
                String t2 = text.substring(max);
                t1 += charTyped;
                if (isAllowed(t1 + t2)) {
                    text = t1 + t2;
                    cursorPos = t1.length();
                    cursorPos2 = cursorPos;
                }
            }
            if (isClientSideWidget && setter != null) {
                setter.accept(text);
            }
            return this;
        }
        return isFocus() ? this : null;
    }

    private boolean isAllowed(String t) {
        return regex == null || regex.matcher(t).matches();
    }

    private void replaceMarkedText(String replacement) {
        int min = Math.min(cursorPos, cursorPos2);
        int max = Math.max(cursorPos, cursorPos2);
        String t1 = text.substring(0, min);
        String t2 = text.substring(max);
        if (replacement != null) {
            if (t1.length() + t2.length() + replacement.length() > maxLength)
                return;
        }
        if (replacement == null) {
            text = t1 + t2;
            cursorPos = min;
        } else {
            text = t1 + replacement + t2;
            cursorPos = t1.length() + replacement.length();
        }
        cursorPos2 = cursorPos;
    }

    public String getText() {
        return text;
    }

    @Override
    public void setFocus(boolean isFocus) {
        super.setFocus(isFocus);
        if (!isFocus) {
            cursorPos2 = cursorPos;
            text = validator.apply(text);
            setter.accept(text);
            writeClientAction(-1, buf -> buf.writeString(text));
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) {
            text = buffer.readString(maxLength);
            setter.accept(text);
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == -2) {
            text = buffer.readString(maxLength);
            setter.accept(text);
            initialised = true;
            if (cursorPos > text.length()) {
                cursorPos = text.length();
            }
            if (cursorPos2 > text.length()) {
                cursorPos2 = text.length();
            }
        }
    }

    /**
     * @param textColor text color. Default is 0xFFFFFF (white)
     */
    public TextFieldWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    /**
     * If a key is pressed, the new string will be matched against this pattern.
     * If it doesn't match, the char will not be typed.
     *
     * @param regex pattern
     */
    public TextFieldWidget setAllowedChars(Pattern regex) {
        this.regex = regex;
        return this;
    }

    /**
     * Called after unfocusing (press enter or click anywhere, but the field) the field
     *
     * @param validator determines whether the entered string is valid. Returns true by default
     */
    public TextFieldWidget setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    /**
     * A predefined validator to only accept integer numbers
     *
     * @param min minimum accepted value
     * @param max maximum accepted value
     */
    public TextFieldWidget setNumbersOnly(int min, int max) {
        if (this.regex == null) {
            if (min < 0)
                regex = WHOLE_NUMS;
            else
                regex = NATURAL_NUMS;
        }
        setValidator(val -> {
            if (val.isEmpty()) {
                return String.valueOf(min);
            }
            for (int i = 0; i < val.length(); i++) {
                char c = val.charAt(i);
                if (c == '-' && (min >= 0 || i != 0)) {
                    return String.valueOf(min);
                }

            }
            int num;
            try {
                num = Integer.parseInt(val);
            } catch (NumberFormatException ignored) {
                return String.valueOf(max);
            }
            if (num < min) {
                return String.valueOf(min);
            }
            if (num > max) {
                return String.valueOf(max);
            }
            return val;
        });
        return this;
    }

    /**
     * @param centered whether to center the text and post fix in the x axis. Default is false
     */
    public TextFieldWidget setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    /**
     * @param postFix a string that will be rendered after the editable text
     */
    public TextFieldWidget setPostFix(String postFix) {
        this.localisedPostFix = postFix;
        if (gui != null && gui.holder != null && isRemote()) {
            this.localisedPostFix = I18n.hasKey(localisedPostFix) ? I18n.format(localisedPostFix) : localisedPostFix;
        }
        return this;
    }

    /**
     * @param markedColor background color of marked text. Default is 0x2F72A8 (lapis lazuli blue)
     */
    public TextFieldWidget setMarkedColor(int markedColor) {
        this.markedColor = markedColor;
        return this;
    }

    /**
     * @param postFixRight whether to bind the post fix to the right side. Default is false
     */
    public TextFieldWidget bindPostFixToRight(boolean postFixRight) {
        this.postFixRight = postFixRight;
        return this;
    }

    /**
     * Will scale the text, the marked background and the cursor. f.e. 0.5 is half the size
     *
     * @param scale scale factor
     */
    public TextFieldWidget setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public TextFieldWidget setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * Called when the text field gets focused. Only called on client.
     * Use it to un focus other text fields.
     * Optimally this should be done automatically, but that is not really possible with the way Modular UI is made
     */
    public TextFieldWidget setOnFocus(Consumer<TextFieldWidget> onFocus) {
        this.onFocus = onFocus;
        return this;
    }
}
