package io.github.cleanroommc.multiblocked.api.gui.widget.imp.controller;

import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import io.github.cleanroommc.multiblocked.util.Position;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RecipePage extends PageWidget{
    public final ControllerTileEntity controller;
    private Recipe recipe;
    @SideOnly(Side.CLIENT)
    private RecipeWidget recipeWidget;
    
    
    public RecipePage(ControllerTileEntity controller, TabContainer tabContainer) {
        super(new ResourceTexture("multiblocked:textures/gui/io_page.png"), tabContainer);
        this.controller = controller;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (controller.recipeLogic != null) {
            if (recipe == controller.recipeLogic.lastRecipe) {
                return;
            }
            recipe = controller.recipeLogic.lastRecipe;
            writeUpdateInfo(-1, this::writeRecipe);
        } else if (recipe != null) {
            recipe = null;
            writeUpdateInfo(-1, this::writeRecipe);
        }
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
            this.addWidget(recipeWidget = new RecipeWidget(recipe));
            recipeWidget.setSelfPosition(new Position(0, 30));
        } else {
            if (recipeWidget != null) {
                removeWidget(recipeWidget);
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == -1) {
            readRecipe(buffer);
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }
}
