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
public class FuelProgressTrait extends ProgressCapabilityTrait {

    public FuelProgressTrait() {
        super(null);
    }

    @Override
    protected String dynamicHoverTips(double progress) {
        return LocalizationUtils.format("multiblocked.top.fuel_progress", ((int)(progress * 100)) + "%");
    }

    @Override
    protected double getProgress() {
        if (component instanceof ControllerTileEntity) {
            ControllerTileEntity controller = (ControllerTileEntity) component;
            RecipeLogic recipeLogic = controller.getRecipeLogic();
            return recipeLogic == null ? 0 : Math.min(recipeLogic.fuelTime, recipeLogic.fuelMaxTime) * 1d / Math.max(1, recipeLogic.fuelMaxTime);
        }
        return 0;
    }

}
