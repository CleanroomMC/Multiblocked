package com.cleanroommc.multiblocked.api.gui.widget.imp.recipe;

import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;

import java.util.function.DoubleSupplier;
import java.util.function.Function;

public class ProgressWidget extends Widget {
    public final static DoubleSupplier JEIProgress = () -> Math.abs(System.currentTimeMillis() % 2000) / 2000.;

    public final DoubleSupplier progressSupplier;
    private IGuiTexture emptyBarArea;
    private IGuiTexture filledBarArea;
    private Function<Double, String> dynamicHoverTips;
    private FillDirection fillDirection = FillDirection.LEFT_TO_RIGHT;

    private double lastProgressValue;

    public ProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, ResourceTexture fullImage) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
        this.emptyBarArea = fullImage.getSubTexture(0.0, 0.0, 1.0, 0.5);
        this.filledBarArea = fullImage.getSubTexture(0.0, 0.5, 1.0, 0.5);
        this.lastProgressValue = -1;
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

    public ProgressWidget setDynamicHoverTips(Function<Double, String> hoverTips) {
        this.dynamicHoverTips = hoverTips;
        return this;
    }

    public ProgressWidget setFillDirection(FillDirection fillDirection) {
        this.fillDirection = fillDirection;
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
                if (dynamicHoverTips != null) {
                    setHoverTooltip(dynamicHoverTips.apply(lastProgressValue));
                }
            }
            double progress = MathHelper.clamp(0.0, lastProgressValue, 1.0);
            double drawnU = fillDirection.getDrawnU(progress);
            double drawnV = fillDirection.getDrawnV(progress);
            double drawnWidth = fillDirection.getDrawnWidth(progress);
            double drawnHeight = fillDirection.getDrawnHeight(progress);
            filledBarArea.drawSubArea(pos.x + drawnU * size.width, pos.y + drawnV * size.height, ((int) (size.width * drawnWidth)), ((int) (size.height * drawnHeight)),
                    drawnU, drawnV, drawnWidth, drawnHeight);
        }
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

    public enum FillDirection {
        LEFT_TO_RIGHT {
            @Override
            public double getDrawnHeight(double progress) {
                return 1.0;
            }
        },
        RIGHT_TO_LEFT {
            @Override
            public double getDrawnU(double progress) {
                return 1.0 - progress;
            }

            @Override
            public double getDrawnHeight(double progress) {
                return 1.0;
            }
        },
        UP_TO_DOWN {
            @Override
            public double getDrawnWidth(double progress) {
                return 1.0;
            }
        },
        DOWN_TO_UP {
            @Override
            public double getDrawnV(double progress) {
                return 1.0 - progress;
            }

            @Override
            public double getDrawnWidth(double progress) {
                return 1.0;
            }
        };

        public static final FillDirection[] VALUES = FillDirection.values();

        public double getDrawnU(double progress) {
            return 0.0;
        }

        public double getDrawnV(double progress) {
            return 0.0;
        }

        public double getDrawnWidth(double progress) {
            return progress;
        }

        public double getDrawnHeight(double progress) {
            return progress;
        }
    }

}
