package com.cleanroommc.multiblocked.jei.recipeppage;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.jei.ModularWrapper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;

public class RecipeMapFuelCategory implements IRecipeCategory<ModularWrapper> {
    private final RecipeMap recipeMap;
    private final IDrawable background;
    private IDrawable icon;

    public RecipeMapFuelCategory(IJeiHelpers helpers, RecipeMap recipeMap) {
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(176, 44);
        this.recipeMap = recipeMap;
    }

    @Nonnull
    @Override
    public String getUid() {
        return Multiblocked.MODID + ":" + recipeMap.name + ".fuel";
    }

    @Nonnull
    @Override
    public String getTitle() {
        return I18n.format(recipeMap.getUnlocalizedName()) + " " + I18n.format("multiblocked.gui.dialogs.recipe_map.fuel_recipe");
    }

    @Nonnull
    @Override
    public String getModName() {
        return Multiblocked.MODID;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon == null ? (icon = recipeMap.categoryTexture == null ? null : recipeMap.fuelTexture.getSubTexture(0.0, 0.5, 1.0, 0.5).toDrawable(18, 18)) : icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, ModularWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        recipeWrapper.setRecipeLayout((RecipeLayout) recipeLayout);
    }
}
