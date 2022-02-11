package io.github.cleanroommc.multiblocked.jei.ingredient;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IIngredientType;

import java.util.Collection;

public abstract class AbstractIngredient<T> implements IIngredientType<T>, IIngredientHelper<T>, IIngredientRenderer<T> {
    
    public void registerIngredients(IModIngredientRegistration registry) {
        registry.register(this, getAllIngredients(), this, this);
    }

    public abstract Collection<T> getAllIngredients();
}
