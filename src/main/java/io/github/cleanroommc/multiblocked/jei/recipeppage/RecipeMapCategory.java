package io.github.cleanroommc.multiblocked.jei.recipeppage;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.recipe.RecipeMap;
import io.github.cleanroommc.multiblocked.jei.ModularWrapper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class RecipeMapCategory implements IRecipeCategory<ModularWrapper> {
    private final RecipeMap recipeMap;
    private final IDrawable background;
    private final IDrawable icon;

    public RecipeMapCategory(IJeiHelpers helpers, RecipeMap recipeMap) {
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(176, 84);
        this.icon = recipeMap.categoryTexture == null ? guiHelper.createBlankDrawable(18, 18) : recipeMap.categoryTexture.toDrawable(18, 18);
        this.recipeMap = recipeMap;
    }

    @Nonnull
    @Override
    public String getUid() {
        return Multiblocked.MODID + ":" + recipeMap.name;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return I18n.format(recipeMap.getUnlocalizedName());
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
        return icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, ModularWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        recipeWrapper.setRecipeLayout((RecipeLayout) recipeLayout);
    }
}
