package com.cleanroommc.multiblocked.jei.recipeppage;

import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import com.cleanroommc.multiblocked.api.recipe.Content;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.jei.IJeiIngredientAdapter;
import com.cleanroommc.multiblocked.jei.ModularWrapper;
import mezz.jei.api.ingredients.IIngredients;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeWrapper extends ModularWrapper {

    public final Recipe recipe;

    public RecipeWrapper(RecipeWidget widget) {
        super(widget, widget.getSize().width, widget.getSize().height);
        recipe = widget.recipe;
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
                        .flatMap(it -> adapter.apply(it).stream())
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
                        .flatMap(it -> adapter.apply(it).stream())
                        .collect(Collectors.toList());
                ingredients.setOutput(adapter.getJeiIngredientType(), jeiIngredients);
            }
        });
    }
}
