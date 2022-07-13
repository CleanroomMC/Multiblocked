package com.cleanroommc.multiblocked.jei.recipeppage;

import com.cleanroommc.multiblocked.api.gui.ingredient.IIngredientSlot;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import com.cleanroommc.multiblocked.api.recipe.Content;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.jei.IJeiIngredientAdapter;
import com.cleanroommc.multiblocked.jei.JeiPlugin;
import com.cleanroommc.multiblocked.jei.ModularWrapper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.input.MouseHelper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeWrapper extends ModularWrapper {

    public final Recipe recipe;
    public final RecipeWidget widget;

    public RecipeWrapper(RecipeWidget widget) {
        super(widget, widget.getSize().width, widget.getSize().height);
        this.widget = widget;
        this.recipe = widget.recipe;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getIngredients(@Nonnull IIngredients ingredients) {
        recipe.inputs.forEach((capability, contents) -> {
            IJeiIngredientAdapter<Object, Object> adapter = (IJeiIngredientAdapter<Object, Object>) capability.jeiIngredientAdapter;
            if (adapter != null) {
                List<Object> jeiIngredients = contents.stream()
                        .map(Content::getContent)
                        .map(adapter.getInternalIngredientType()::cast)
                        .flatMap(adapter)
                        .collect(Collectors.toList());
                ingredients.setInputs(adapter.getJeiIngredientType(), jeiIngredients);
            }
        });
        recipe.outputs.forEach((capability, contents) -> {
            IJeiIngredientAdapter<Object, Object> adapter = (IJeiIngredientAdapter<Object, Object>) capability.jeiIngredientAdapter;
            if (adapter != null) {
                List<Object> jeiIngredients = contents.stream()
                        .map(Content::getContent)
                        .map(adapter.getInternalIngredientType()::cast)
                        .flatMap(adapter)
                        .collect(Collectors.toList());
                ingredients.setOutputs(adapter.getJeiIngredientType(), jeiIngredients);
            }
        });
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        // needs gui screen xy position
        mouseX = MouseHelper.getX();
        mouseY = MouseHelper.getY();
        IFocus.Mode mode;
        if (mouseButton == 0) {
            mode = IFocus.Mode.OUTPUT;
        } else if (mouseButton == 1) {
            mode = IFocus.Mode.INPUT;
        } else return false;
        IIngredientSlot clicked = findClickedSlot(widget.inputs, mouseX, mouseY);
        if (clicked == null) clicked = findClickedSlot(widget.outputs, mouseX, mouseY);
        if (clicked != null) {
            Object ingredient = clicked.getIngredientOverMouse(mouseX, mouseY);
            if (ingredient != null) {
                JeiPlugin.jeiRuntime.getRecipesGui().show(JeiPlugin.jeiRuntime.getRecipeRegistry().createFocus(mode, ingredient));
                return true;
            }
        }
        return false;
    }

    private IIngredientSlot findClickedSlot(DraggableScrollableWidgetGroup group, int mouseX, int mouseY) {
        for (Widget w : group.widgets) {
            if (w instanceof IIngredientSlot && w.isMouseOverElement(mouseX, mouseY)) {
                return ((IIngredientSlot) w);
            }
        }
        return null;
    }
}
