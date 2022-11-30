package com.cleanroommc.multiblocked.api.gui.widget.imp.recipe;

import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ProgressTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import net.minecraft.network.PacketBuffer;

import java.util.function.DoubleSupplier;
import java.util.function.Function;

public class ProgressWidget extends Widget {
    public final static DoubleSupplier JEIProgress = () -> Math.abs(System.currentTimeMillis() % 2000) / 2000.;

    public final DoubleSupplier progressSupplier;
    private ProgressTexture progressBar;
    private Function<Double, String> dynamicHoverTips;

    private double lastProgressValue;

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, ResourceTexture fullImage) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
        this.progressBar = new ProgressTexture(fullImage.getSubTexture(0.0, 0.0, 1.0, 0.5), fullImage.getSubTexture(0.0, 0.5, 1.0, 0.5));
        this.lastProgressValue = -1;
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
    }

    public ProgressWidget setProgressBar(IGuiTexture emptyBarArea, IGuiTexture filledBarArea) {
        this.progressBar = new ProgressTexture(emptyBarArea, filledBarArea);
        return this;
    }

    public ProgressWidget setDynamicHoverTips(Function<Double, String> hoverTips) {
        this.dynamicHoverTips = hoverTips;
        return this;
    }

    public ProgressWidget setFillDirection(ProgressTexture.FillDirection fillDirection) {
        this.progressBar.setFillDirection(fillDirection);
        return this;
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.lastProgressValue = progressSupplier.getAsDouble();
    }

    @Override
    public void writeInitialData(PacketBuffer buffer) {
        super.writeInitialData(buffer);
        buffer.writeDouble(lastProgressValue);
    }

    @Override
    public void readInitialData(PacketBuffer buffer) {
        super.readInitialData(buffer);
        lastProgressValue = buffer.readDouble();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position pos = getPosition();
        Size size = getSize();
        if (progressSupplier == JEIProgress || isClientSideWidget) {
            lastProgressValue = progressSupplier.getAsDouble();
            if (dynamicHoverTips != null) {
                setHoverTooltip(dynamicHoverTips.apply(lastProgressValue));
            }
        }
        progressBar.setProgress(lastProgressValue);
        progressBar.draw(mouseX, mouseY, pos.x, pos.y, size.width, size.height);
    }

    @Override
    public void detectAndSendChanges() {
        double actualValue = progressSupplier.getAsDouble();
        if (actualValue - lastProgressValue != 0) {
            this.lastProgressValue = actualValue;
            writeUpdateInfo(0, buffer -> buffer.writeDouble(actualValue));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 0) {
            this.lastProgressValue = buffer.readDouble();
            if (dynamicHoverTips != null) {
                setHoverTooltip(dynamicHoverTips.apply(lastProgressValue));
            }
        }
    }

}
