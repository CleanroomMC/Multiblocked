package com.cleanroommc.multiblocked.api.gui.texture;

public class ResourceBorderTexture extends ResourceTexture {
    public static final ResourceBorderTexture BORDERED_BACKGROUND = new ResourceBorderTexture("multiblocked:textures/gui/bordered_background.png", 195, 136, 4, 4);
    public static final ResourceBorderTexture BORDERED_BACKGROUND_BLUE = new ResourceBorderTexture("multiblocked:textures/gui/bordered_background_blue.png", 195, 136, 4, 4);
    public static final ResourceBorderTexture BUTTON_COMMON = new ResourceBorderTexture("multiblocked:textures/gui/button_common.png", 198, 18, 1, 1);
    public static final ResourceBorderTexture BAR = new ResourceBorderTexture("multiblocked:textures/gui/button_common.png", 180, 20, 1, 1);

    public final int pixelCornerWidth;
    public final int pixelCornerHeight;
    public final int pixelImageWidth;
    public final int pixelImageHeight;

    public ResourceBorderTexture(String imageLocation, int imageWidth, int imageHeight, int cornerWidth, int cornerHeight) {
        super(imageLocation);
        this.pixelImageWidth = imageWidth;
        this.pixelImageHeight = imageHeight;
        this.pixelCornerWidth = cornerWidth;
        this.pixelCornerHeight = cornerHeight;
    }

    @Override
    public void drawSubArea(double x, double y, int width, int height, double drawnU, double drawnV, double drawnWidth, double drawnHeight) {
        //compute relative sizes
        double cornerWidth = pixelCornerWidth * 1. / pixelImageWidth;
        double cornerHeight = pixelCornerHeight * 1. / pixelImageHeight;
        //draw up corners
        super.drawSubArea(x, y, pixelCornerWidth, pixelCornerHeight, 0.0, 0.0, cornerWidth, cornerHeight);
        super.drawSubArea(x + width - pixelCornerWidth, y, pixelCornerWidth, pixelCornerHeight, 1.0 - cornerWidth, 0.0, cornerWidth, cornerHeight);
        //draw down corners
        super.drawSubArea(x, y + height - pixelCornerHeight, pixelCornerWidth, pixelCornerHeight, 0.0, 1.0 - cornerHeight, cornerWidth, cornerHeight);
        super.drawSubArea(x + width - pixelCornerWidth, y + height - pixelCornerHeight, pixelCornerWidth, pixelCornerHeight, 1.0 - cornerWidth, 1.0 - cornerHeight, cornerWidth, cornerHeight);
        //draw horizontal connections
        super.drawSubArea(x + pixelCornerWidth, y, width - 2 * pixelCornerWidth, pixelCornerHeight,
                cornerWidth, 0.0, 1.0 - 2 * cornerWidth, cornerHeight);
        super.drawSubArea(x + pixelCornerWidth, y + height - pixelCornerHeight, width - 2 * pixelCornerWidth, pixelCornerHeight,
                cornerWidth, 1.0 - cornerHeight, 1.0 - 2 * cornerWidth, cornerHeight);
        //draw vertical connections
        super.drawSubArea(x, y + pixelCornerHeight, pixelCornerWidth, height - 2 * pixelCornerHeight,
                0.0, cornerHeight, cornerWidth, 1.0 - 2 * cornerHeight);
        super.drawSubArea(x + width - pixelCornerWidth, y + pixelCornerHeight, pixelCornerWidth, height - 2 * pixelCornerHeight,
                1.0 - cornerWidth, cornerHeight, cornerWidth, 1.0 - 2 * cornerHeight);
        //draw central body
        super.drawSubArea(x + pixelCornerWidth, y + pixelCornerHeight,
                width - 2 * pixelCornerWidth, height - 2 * pixelCornerHeight,
                cornerWidth, cornerHeight, 1.0 - 2 * cornerWidth, 1.0 - 2 * cornerHeight);
    }
}
