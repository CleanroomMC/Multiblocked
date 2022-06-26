package com.cleanroommc.multiblocked.api.registry;

import com.cleanroommc.multiblocked.api.recipe.RecipeCondition;
import com.cleanroommc.multiblocked.common.recipe.conditions.*;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote RecipeConditions
 */
public class MbdRecipeConditions {
    public static final Map<String, RecipeCondition> RECIPE_CONDITIONS_REGISTRY = Maps.newHashMap();

    public static void registerCondition(RecipeCondition condition) {
        RECIPE_CONDITIONS_REGISTRY.put(condition.getType().toLowerCase(), condition);
    }

    public static RecipeCondition getCondition(String type) {
        return RECIPE_CONDITIONS_REGISTRY.get(type.toLowerCase());
    }

    public static void registerConditions() {
        registerCondition(DimensionCondition.INSTANCE);
        registerCondition(ThunderCondition.INSTANCE);
        registerCondition(RainingCondition.INSTANCE);
        registerCondition(PositionYCondition.INSTANCE);
        registerCondition(BiomeCondition.INSTANCE);
        registerCondition(BlockCondition.INSTANCE);
    }
}
