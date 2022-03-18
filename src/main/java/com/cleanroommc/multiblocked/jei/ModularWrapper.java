package com.cleanroommc.multiblocked.jei;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.modular.IUIHolder;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.util.ModularUIBuilder;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.io.IOException;

public abstract class ModularWrapper implements IRecipeWrapper {
    protected final Widget widget;
    protected final JEIModularUIGuiContainer guiContainer;

    public ModularWrapper(Widget widget, int width, int height) {
        ModularUI gui = new ModularUIBuilder(IGuiTexture.EMPTY, width, height)
                .widget(widget)
                .build(IUIHolder.EMPTY, Minecraft.getMinecraft().player);
        gui.initWidgets();
        this.widget = widget;
        guiContainer = new JEIModularUIGuiContainer(gui);
    }

    public void setRecipeLayout(RecipeLayout layout) {
        guiContainer.setRecipeLayout(layout);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        guiContainer.drawInfo(minecraft, mouseX, mouseY);
    }

    public void handleMouseInput() {
        try {
            guiContainer.handleMouseInput();
        } catch (IOException e) {
            Multiblocked.LOGGER.error(e);
        }
    }

}
