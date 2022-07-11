package com.cleanroommc.multiblocked.jei;

import mezz.jei.api.recipe.IIngredientType;

import java.util.List;
import java.util.function.Function;

/**
 * @author youyihj
 */
public interface IJeiIngredientAdapter<T, R> extends Function<T, List<R>> {
    Class<T> getInternalIngredientType();

    IIngredientType<R> getJeiIngredientType();
}
