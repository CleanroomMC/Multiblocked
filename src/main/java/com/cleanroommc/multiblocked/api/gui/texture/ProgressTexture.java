package com.cleanroommc.multiblocked.api.gui.texture;

import net.minecraft.util.math.MathHelper;

/**
 * @author youyihj
 */
public class ProgressTexture implements IGuiTexture {
    protected final IGuiTexture emptyBarArea;
    protected final IGuiTexture filledBarArea;

    protected double progress;
    protected FillDirection fillDirection = FillDirection.LEFT_TO_RIGHT;

    public ProgressTexture(IGuiTexture emptyBarArea, IGuiTexture filledBarArea) {
        this.emptyBarArea = emptyBarArea;
        this.filledBarArea = filledBarArea;
    }

    public void setProgress(double progress) {
        this.progress = MathHelper.clamp(0.0, progress, 1.0);
    }

    public void setFillDirection(FillDirection fillDirection) {
        this.fillDirection = fillDirection;
    }

    @Override
    public void draw(int mouseX, int mouseY, double x, double y, int width, int height) {
        if (emptyBarArea != null) {
            emptyBarArea.draw(mouseX, mouseY, x, y, width, height);
        }
        if (filledBarArea != null) {
            double drawnU = fillDirection.getDrawnU(progress);
            double drawnV = fillDirection.getDrawnV(progress);
            double drawnWidth = fillDirection.getDrawnWidth(progress);
            double drawnHeight = fillDirection.getDrawnHeight(progress);
            filledBarArea.drawSubArea(x + drawnU * width, y + drawnV * height, ((int) (width * drawnWidth)), ((int) (height * drawnHeight)),
                    drawnU, drawnV, ((int) (drawnWidth * width)) / (width * 1.0), ((int) (drawnHeight * height)) / (height * 1.0));
        }
    }

    public static class Auto extends ProgressTexture {

        public Auto(IGuiTexture emptyBarArea, IGuiTexture filledBarArea) {
            super(emptyBarArea, filledBarArea);
        }

        @Override
        public void updateTick() {
            progress = Math.abs(System.currentTimeMillis() % 2000) / 2000.0;
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
        },

        ALWAYS_FULL {
            @Override
            public double getDrawnHeight(double progress) {
                return 1.0;
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
