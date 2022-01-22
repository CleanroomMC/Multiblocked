package io.github.cleanroommc.multiblocked.api.gui.widget.imp;

import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class ImageWidget extends Widget {

    protected IGuiTexture area;

    private int border;
    private int borderColor;
    private String tooltipText;

    public ImageWidget(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height);
    }

    public ImageWidget(int xPosition, int yPosition, int width, int height, IGuiTexture area) {
        this(xPosition, yPosition, width, height);
        this.area = area;
    }

    public ImageWidget setImage(IGuiTexture area) {
        this.area = area;
        return this;
    }

    public ImageWidget setBorder(int border, int color) {
        this.border = border;
        this.borderColor = color;
        return this;
    }


    public ImageWidget setTooltip(String tooltipText) {
        this.tooltipText = tooltipText;
        return this;
    }

    @Override
    public void updateScreen() {
        if (area != null) {
            area.updateTick();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        if (area == null) return;
        Position position = getPosition();
        Size size = getSize();
        area.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
        if (border > 0) {
            DrawerHelper.drawBorder(position.x, position.y, size.width, size.height, borderColor, border);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY, float partialTicks) {
        if (area != null && isMouseOverElement(mouseX, mouseY) && tooltipText != null) {
            List<String> hoverList = Arrays.asList(I18n.format(tooltipText).split("/n"));
            DrawerHelper.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY, gui.getScreenWidth(), gui.getScreenHeight());
        }
    }
}

