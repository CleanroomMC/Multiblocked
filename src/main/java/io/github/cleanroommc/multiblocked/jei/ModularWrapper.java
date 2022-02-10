package io.github.cleanroommc.multiblocked.jei;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.gui.modular.IUIHolder;
import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import io.github.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.ModularUIBuilder;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.io.IOException;

public abstract class ModularWrapper implements IRecipeWrapper {
    protected final Widget widget;
    protected final JEIModularUIGuiContainer guiContainer;
    private static long lastRender;
    private static Object focus;

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
        lastRender = System.currentTimeMillis();
        guiContainer.drawInfo(minecraft, mouseX, mouseY);
    }

    public void handleMouseInput() {
        try {
            guiContainer.handleMouseInput();
        } catch (IOException e) {
            Multiblocked.LOGGER.error(e);
        }
    }

    public static void setFocus(Object focus) {
        ModularWrapper.focus = focus;
    }

    public static Object getFocus() {
        if(lastRender > System.currentTimeMillis() - 100) {
            return focus;
        }
        return null;
    }
}
