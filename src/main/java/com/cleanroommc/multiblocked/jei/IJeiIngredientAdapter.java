package com.cleanroommc.multiblocked.jei;

import mezz.jei.api.recipe.IIngredientType;
import net.minecraftforge.fml.common.Optional;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author youyihj
 */
public interface IJeiIngredientAdapter<T, R> extends Function<T, Stream<R>> {
    Class<T> getInternalIngredientType();

    @Optional.Method(modid = "jei")
    IIngredientType<R> getJeiIngredientType();
}
