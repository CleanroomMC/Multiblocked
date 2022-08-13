package com.cleanroommc.multiblocked.api.gui.widget.imp.controller;

import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ProgressWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RecipePage extends PageWidget{
    public static ResourceTexture resourceTexture = new ResourceTexture("multiblocked:textures/gui/recipe_page.png");
    public final ControllerTileEntity controller;
    public final DraggableScrollableWidgetGroup tips;
    private Recipe recipe;
    @SideOnly(Side.CLIENT)
    private RecipeWidget recipeWidget;
    private RecipeLogic.Status status;
    private int progress;
    private int duration;
    
    public RecipePage(ControllerTileEntity controller, TabContainer tabContainer) {
        super(resourceTexture, tabContainer);
        this.controller = controller;
        this.status = RecipeLogic.Status.IDLE;
        this.addWidget(tips = new DraggableScrollableWidgetGroup(8, 34, 160, 112));
        tips.addWidget(new LabelWidget(5, 5, () -> I18n.format("multiblocked.recipe.status." + status.name)).setTextColor(-1));
        tips.addWidget(new LabelWidget(5, 20, () -> I18n.format("multiblocked.recipe.remaining", recipe == null ? 0 : (duration - progress) / 20)).setTextColor(-1));
        this.addWidget(new SwitchWidget(153, 131, 12, 12, (cd, r) -> {
            controller.asyncRecipeSearching = r;
            if (!cd.isRemote) {
                controller.markAsDirty();
            }
        })
                .setPressed(controller.asyncRecipeSearching)
                .setSupplier(() -> controller.asyncRecipeSearching)
                .setTexture(resourceTexture.getSubTexture(176 / 256.0, 143 / 256.0, 12 / 256.0, 12 / 256.0),
                        resourceTexture.getSubTexture(176 / 256.0, 155 / 256.0, 12 / 256.0, 12 / 256.0))
                .setHoverTooltip("Async/Sync recipes searching:\n" +
                        "Async has better performance and only tries to match recipes when the internal contents changed\n" +
                        "Sync always tries to match recipes, never miss matching recipes"));
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
        return progress * 1. / duration;
    }

    @Override
    public void writeInitialData(PacketBuffer buffer) {
        super.writeInitialData(buffer);
        detectAndSendChanges();
        writeRecipe(buffer);
        writeStatus(buffer);
    }

    @Override
    public void readInitialData(PacketBuffer buffer) {
        super.readInitialData(buffer);
        readRecipe(buffer);
        readStatus(buffer);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (controller.getRecipeLogic() != null) {
            RecipeLogic recipeLogic = controller.getRecipeLogic();
            if (recipe != recipeLogic.lastRecipe) {
                recipe = recipeLogic.lastRecipe;
                duration = recipeLogic.duration;
                writeUpdateInfo(-1, this::writeRecipe);
            }
            if (status != recipeLogic.getStatus() || progress != recipeLogic.progress) {
                status = recipeLogic.getStatus();
                progress = recipeLogic.progress;
                writeUpdateInfo(-2, this::writeStatus);
            }
        } else if (recipe != null) {
            recipe = null;
            writeUpdateInfo(-1, this::writeRecipe);
        }
    }

    private void writeStatus(PacketBuffer buffer) {
        buffer.writeEnumValue(status);
        buffer.writeVarInt(progress);
    }

    private void readStatus(PacketBuffer buffer) {
        status = buffer.readEnumValue(RecipeLogic.Status.class);
        progress = buffer.readVarInt();
    }

    private void writeRecipe(PacketBuffer buffer) {
        if (recipe == null) {
            buffer.writeBoolean(false);
        }
        else {
            buffer.writeBoolean(true);
            buffer.writeVarInt(duration);
            buffer.writeString(recipe.uid);
        }
    }

    private void readRecipe(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            duration = buffer.readVarInt();
            recipe = controller.getDefinition().recipeMap.recipes.get(buffer.readString(Short.MAX_VALUE));
            if (recipeWidget != null) {
                removeWidget(recipeWidget);
            }
            this.addWidget(recipeWidget = new RecipeWidget(recipe, controller.getDefinition().recipeMap.progressTexture, null));
            recipeWidget.inputs.addSelfPosition(5, 0);
            recipeWidget.outputs.addSelfPosition(-5, 0);
            recipeWidget.setSelfPosition(new Position(0, 167));
        } else {
            if (recipeWidget != null) {
                removeWidget(recipeWidget);
            }
            status = RecipeLogic.Status.IDLE;
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
