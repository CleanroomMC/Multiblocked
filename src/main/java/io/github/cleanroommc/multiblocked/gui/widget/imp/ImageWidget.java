package io.github.cleanroommc.multiblocked.gui.widget.imp;

import io.github.cleanroommc.multiblocked.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.gui.util.DrawerHelper;
import io.github.cleanroommc.multiblocked.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ImageWidget extends Widget {

    protected IGuiTexture area;

    private BooleanSupplier predicate;
    private boolean isVisible = true;
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

    public ImageWidget setPredicate(BooleanSupplier predicate) {
        this.predicate = predicate;
        this.isVisible = false;
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
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (predicate != null && predicate.getAsBoolean() != isVisible) {
            this.isVisible = predicate.getAsBoolean();
            writeUpdateInfo(1, buf -> buf.writeBoolean(isVisible));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.isVisible = buffer.readBoolean();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        if (!this.isVisible || area == null) return;
        Position position = getPosition();
        Size size = getSize();
        area.draw(mouseX, mouseY, position.x, position.y, size.width, size.height);
        if (border > 0) {
            DrawerHelper.drawBorder(position.x, position.y, size.width, size.height, borderColor, border);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY, float partialTicks) {
        if (this.isVisible && tooltipText != null && area != null && isMouseOverElement(mouseX, mouseY)) {
            List<String> hoverList = Arrays.asList(I18n.format(tooltipText).split("/n"));
            DrawerHelper.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY, gui.getScreenWidth(), gui.getScreenHeight());
        }
    }
}

