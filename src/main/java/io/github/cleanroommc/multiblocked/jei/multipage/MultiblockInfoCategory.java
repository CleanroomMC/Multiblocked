package io.github.cleanroommc.multiblocked.jei.multipage;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultiblockInfoCategory implements IRecipeCategory<MultiblockInfoRecipeWrapper> {
    private final static String UID = Multiblocked.MODID + ":multiblock_info";
    private final IDrawable background;
    private final IDrawable icon;

    public MultiblockInfoCategory(IJeiHelpers helpers) {
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(176, 256);
        this.icon = guiHelper.drawableBuilder(new ResourceLocation(Multiblocked.MODID, "textures/test/d.png"), 0, 0, 18, 18).setTextureSize(18, 18).build();
    }

    public static final List<ControllerDefinition> REGISTER = new ArrayList<>();

    public static void registerMultiblock(ControllerDefinition controllerDefinition) {
        REGISTER.add(controllerDefinition);
    }

    public static void registerRecipes(IModRegistry registry) {
        registry.addRecipes(REGISTER.stream().map(MultiblockInfoRecipeWrapper::new).collect(Collectors.toList()), UID);
    }

    @Nonnull
    @Override
    public String getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return I18n.format(UID);
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
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, MultiblockInfoRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        recipeWrapper.setRecipeLayout((RecipeLayout) recipeLayout);
    }
}
