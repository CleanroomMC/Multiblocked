package io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe;

import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.network.PacketBuffer;

import java.util.function.DoubleSupplier;

public class ProgressWidget extends Widget {
    public final static DoubleSupplier JEIProgress = () -> Math.abs(System.currentTimeMillis() % 2000) / 2000.;

    public final DoubleSupplier progressSupplier;
    private IGuiTexture emptyBarArea;
    private IGuiTexture filledBarArea;

    private double lastProgressValue;

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, ResourceTexture fullImage) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
        this.emptyBarArea = fullImage.getSubTexture(0.0, 0.0, 1.0, 0.5);
        this.filledBarArea = fullImage.getSubTexture(0.0, 0.5, 1.0, 0.5);
    }

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
    }

    public ProgressWidget setProgressBar(IGuiTexture emptyBarArea, IGuiTexture filledBarArea) {
        this.emptyBarArea = emptyBarArea;
        this.filledBarArea = filledBarArea;
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        Position pos = getPosition();
        Size size = getSize();
        if (emptyBarArea != null) {
            emptyBarArea.draw(mouseX, mouseY, pos.x, pos.y, size.width, size.height);
        }
        if (filledBarArea != null) {
            if (progressSupplier == JEIProgress) {
                lastProgressValue = progressSupplier.getAsDouble();
            }
            filledBarArea.drawSubArea(pos.x, pos.y, (int) (size.width * lastProgressValue), size.height,
                    0.0, 0.0, ((int) (size.width * lastProgressValue)) / (size.width * 1.0), 1.0);
        }
    }

    @Override
    public void detectAndSendChanges() {
        double actualValue = progressSupplier.getAsDouble();
        if (Math.abs(actualValue - lastProgressValue) > 0.005) {
            this.lastProgressValue = actualValue;
            writeUpdateInfo(0, buffer -> buffer.writeDouble(actualValue));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 0) {
            this.lastProgressValue = buffer.readDouble();
        }
    }

}
