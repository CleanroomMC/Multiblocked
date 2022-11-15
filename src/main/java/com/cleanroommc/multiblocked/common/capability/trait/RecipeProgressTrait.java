package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.trait.ProgressCapabilityTrait;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import com.cleanroommc.multiblocked.util.LocalizationUtils;

/**
 * @author KilaBash
 * @date 2022/11/15
 * @implNote RecipeProgressTrait
 */
public class RecipeProgressTrait extends ProgressCapabilityTrait {

    public RecipeProgressTrait() {
        super(null);
    }

    @Override
    protected String dynamicHoverTips(double progress) {
        return LocalizationUtils.format("multiblocked.top.recipe_progress", ((int)(progress * 100)) + "%");
    }

    @Override
    protected double getProgress() {
        if (component instanceof ControllerTileEntity) {
            RecipeLogic recipeLogic = ((ControllerTileEntity)component).getRecipeLogic();
            return recipeLogic == null ? 0 : (recipeLogic.progress * 1. / recipeLogic.duration);
        }
        return 0;
    }

}
