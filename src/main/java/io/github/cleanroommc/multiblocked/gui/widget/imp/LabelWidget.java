package io.github.cleanroommc.multiblocked.gui.widget.imp;

import io.github.cleanroommc.multiblocked.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

public class LabelWidget extends Widget {

    protected final Supplier<String> textSupplier;
    private String lastTextValue = "";
    private int color;

    public LabelWidget(int xPosition, int yPosition, Supplier<String> text) {
        super(new Position(xPosition, yPosition), Size.ZERO);
        this.textSupplier = text;
    }

    public LabelWidget setTextColor(int color) {
        this.color = color;
        return this;
    }

    @SideOnly(Side.CLIENT)
    private void updateSize() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String resultText = lastTextValue;
        setSize(new Size(fontRenderer.getStringWidth(resultText), fontRenderer.FONT_HEIGHT));
        if (uiAccess != null) {
            uiAccess.notifySizeChange();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY, float particleTicks) {
        String suppliedText = textSupplier.get();
        if (!suppliedText.equals(lastTextValue)) {
            this.lastTextValue = suppliedText;
            updateSize();
        }
        String[] split = textSupplier.get().split("\n");
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position position = getPosition();
        for (int i = 0; i < split.length; i++) {
            fontRenderer.drawString(split[i], position.x, position.y + (i * (fontRenderer.FONT_HEIGHT + 2)), color);
        }
    }

}
