package io.github.cleanroommc.multiblocked.api.gui.widget.imp.controller;

import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ProgressWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import io.github.cleanroommc.multiblocked.util.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RecipePage extends PageWidget{
    public static ResourceTexture resourceTexture = new ResourceTexture("multiblocked:textures/gui/recipe_page.png");
    public final ControllerTileEntity controller;
    private Recipe recipe;
    @SideOnly(Side.CLIENT)
    private RecipeWidget recipeWidget;
    private boolean isWorking;
    private int progress;
    
    
    public RecipePage(ControllerTileEntity controller, TabContainer tabContainer) {
        super(resourceTexture, tabContainer);
        this.controller = controller;
        this.addWidget(new WidgetGroup(8, 34, 160, 112));
        this.addWidget(new ImageWidget(7, 7, 162, 16,
                new TextTexture(controller.getUnlocalizedName(), -1)
                        .setType(TextTexture.TextType.ROLL)
                        .setWidth(162)
                        .setDropShadow(true)));
        this.addWidget(new ProgressWidget(this::getProgress, 17, 154, 143, 9).setProgressBar(
                (mouseX, mouseY, x, y, width, height) -> { // back
                    double imageU = 185. / 256;
                    double imageV = 0;
                    double imageWidth = 9. / 256;
                    double imageHeight = 143. / 256;
                    Minecraft.getMinecraft().renderEngine.bindTexture(resourceTexture.imageLocation);
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                    bufferbuilder.pos(x, y + height, 0.0D).tex(imageU + imageWidth, imageV + imageHeight).endVertex();
                    bufferbuilder.pos(x + width, y + height, 0.0D).tex(imageU + imageWidth, imageV).endVertex();
                    bufferbuilder.pos(x + width, y, 0.0D).tex(imageU, imageV).endVertex();
                    bufferbuilder.pos(x, y, 0.0D).tex(imageU, imageV + imageHeight).endVertex();
                    tessellator.draw();
                }, new IGuiTexture() {
                    @Override
                    public void draw(int mouseX, int mouseY, double x, double y, int width, int height) { }

                    @Override
                    public void drawSubArea(double x, double y, int width, int height, double drawnU, double drawnV, double drawnWidth, double drawnHeight) { // fill
                        double imageU = 176. / 256;
                        double imageV = 0;
                        double imageWidth = 9. / 256;
                        double imageHeight = 143. / 256;
                        Minecraft.getMinecraft().renderEngine.bindTexture(resourceTexture.imageLocation);
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferbuilder = tessellator.getBuffer();
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                        bufferbuilder.pos(x, y + height, 0.0D).tex(imageU + imageWidth, imageV + imageHeight).endVertex();
                        bufferbuilder.pos(x + width, y + height, 0.0D).tex(imageU + imageWidth,  imageV + imageHeight - drawnWidth * imageHeight).endVertex();
                        bufferbuilder.pos(x + width, y, 0.0D).tex(imageU, imageV + imageHeight - drawnWidth * imageHeight).endVertex();
                        bufferbuilder.pos(x, y, 0.0D).tex(imageU, imageV + imageHeight).endVertex();
                        tessellator.draw();
                    }
                }));
    }

    private double getProgress() {
        if (recipe == null) return 0;
        return progress * 1. / recipe.duration;
    }

    @Override
    public void writeInitialData(PacketBuffer buffer) {
        detectAndSendChanges();
        writeRecipe(buffer);
        writeStatus(buffer);
    }

    @Override
    public void readInitialData(PacketBuffer buffer) {
        readRecipe(buffer);
        readStatus(buffer);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (controller.recipeLogic != null) {
            if (recipe != controller.recipeLogic.lastRecipe) {
                recipe = controller.recipeLogic.lastRecipe;
                writeUpdateInfo(-1, this::writeRecipe);
            }
            if (isWorking != controller.recipeLogic.isWorking || progress != controller.recipeLogic.progress) {
                isWorking = controller.recipeLogic.isWorking;
                progress = controller.recipeLogic.progress;
                writeUpdateInfo(-2, this::writeStatus);
            }
        } else if (recipe != null) {
            recipe = null;
            writeUpdateInfo(-1, this::writeRecipe);
        }
    }

    private void writeStatus(PacketBuffer buffer) {
        buffer.writeBoolean(isWorking);
        buffer.writeVarInt(progress);
    }

    private void readStatus(PacketBuffer buffer) {
        isWorking = buffer.readBoolean();
        progress = buffer.readVarInt();
    }

    private void writeRecipe(PacketBuffer buffer) {
        if (recipe == null) {
            buffer.writeBoolean(false);
        }
        else {
            buffer.writeBoolean(true);
            buffer.writeString(recipe.uid);
        }
    }

    private void readRecipe(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            recipe = controller.getDefinition().recipeMap.recipes.get(buffer.readString(Short.MAX_VALUE));
            if (recipeWidget != null) {
                removeWidget(recipeWidget);
            }
            this.addWidget(recipeWidget = new RecipeWidget(recipe, controller.getDefinition().recipeMap.progressTexture));
            recipeWidget.setSelfPosition(new Position(0, 167));
        } else {
            if (recipeWidget != null) {
                removeWidget(recipeWidget);
            }
            isWorking = false;
            progress = 0;
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == -1) {
            readRecipe(buffer);
        } else if (id == -2) {
            readStatus(buffer);
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }
}
