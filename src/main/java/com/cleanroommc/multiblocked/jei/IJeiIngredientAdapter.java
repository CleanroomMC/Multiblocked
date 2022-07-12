package com.cleanroommc.multiblocked.jei;

import mezz.jei.api.recipe.IIngredientType;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author youyihj
 */
public interface IJeiIngredientAdapter<T, R> extends Function<T, Stream<R>> {
    Class<T> getInternalIngredientType();

    IIngredientType<R> getJeiIngredientType();
}
