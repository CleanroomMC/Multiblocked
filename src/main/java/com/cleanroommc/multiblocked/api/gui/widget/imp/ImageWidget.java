package com.cleanroommc.multiblocked.api.gui.widget.imp;

import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ImageWidget extends Widget {

    protected IGuiTexture area;

    private int border;
    private int borderColor;

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
}

