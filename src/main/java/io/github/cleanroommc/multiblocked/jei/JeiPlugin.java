package io.github.cleanroommc.multiblocked.jei;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUIGuiHandler;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import io.github.cleanroommc.multiblocked.api.recipe.RecipeMap;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.jei.ingredient.AspectListIngredient;
import io.github.cleanroommc.multiblocked.jei.multipage.MultiblockInfoCategory;
import io.github.cleanroommc.multiblocked.jei.recipeppage.RecipeMapCategory;
import io.github.cleanroommc.multiblocked.jei.recipeppage.RecipeWrapper;
import mezz.jei.Internal;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.Constants;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.InputHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JEIPlugin
public class JeiPlugin implements IModPlugin {
    public static Field fieldRecipeWrapper;
    public static Field fieldRecipeLayouts;
    private static IIngredientRegistry itemRegistry;

    static {
        try {
            fieldRecipeWrapper = RecipeLayout.class.getDeclaredField("recipeWrapper");
            fieldRecipeWrapper.setAccessible(true);
            fieldRecipeLayouts = RecipesGui.class.getDeclaredField("recipeLayouts");
            fieldRecipeLayouts.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Multiblocked.LOGGER.error(e);
        }
    }
    public static IJeiRuntime jeiRuntime;

    public static IRecipeWrapper getWrapper(RecipeLayout layout) {
        try {
            return (IRecipeWrapper) fieldRecipeWrapper.get(layout);
        } catch (IllegalAccessException e) {
            Multiblocked.LOGGER.error(e);
        }
        return null;
    }

    public static List<RecipeLayout> getRecipeLayouts(RecipesGui recipesGui) {
        try {
            return (List<RecipeLayout>) fieldRecipeLayouts.get(recipesGui);
        } catch (IllegalAccessException e) {
            Multiblocked.LOGGER.error(e);
        }
        return null;
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        JeiPlugin.jeiRuntime = jeiRuntime;
        List<ItemStack> removed = new ArrayList<>();
        for (ComponentDefinition definition : MultiblockComponents.DEFINITION_REGISTRY.values()) {
            if (!definition.showInJei) {
                removed.add(definition.getStackForm());
            }
        }
        JeiPlugin.itemRegistry.removeIngredientsAtRuntime(VanillaTypes.ITEM, removed);
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(new MultiblockInfoCategory(jeiHelpers));
        for (RecipeMap recipeMap : RecipeMap.RECIPE_MAP_REGISTRY.values()) {
            registry.addRecipeCategories(new RecipeMapCategory(jeiHelpers, recipeMap));
        }
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        ModularUIGuiHandler modularUIGuiHandler = new ModularUIGuiHandler(jeiHelpers.recipeTransferHandlerHelper());
        registry.addAdvancedGuiHandlers(modularUIGuiHandler);
        registry.addGhostIngredientHandler(modularUIGuiHandler.getGuiContainerClass(), modularUIGuiHandler);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(modularUIGuiHandler, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        for (RecipeMap recipeMap : RecipeMap.RECIPE_MAP_REGISTRY.values()) {
            registry.addRecipes(recipeMap.recipes.values().stream()
                            .map(recipe -> new RecipeWidget(recipe, recipeMap.progressTexture))
                            .map(RecipeWrapper::new)
                            .collect(Collectors.toList()),
                    Multiblocked.MODID + ":" + recipeMap.name);
        }
        MultiblockInfoCategory.registerRecipes(registry);
        itemRegistry = registry.getIngredientRegistry();
    }

    @Override
    public void registerIngredients(@Nonnull IModIngredientRegistration registry) {
        if (Multiblocked.isModLoaded("thaumcraft") && !Multiblocked.isModLoaded("thaumicjei")) {
            AspectListIngredient.INSTANCE.registerIngredients(registry);
        }
    }

    public static void setupInputHandler() {
        try {
            Field inputHandlerField = Internal.class.getDeclaredField("inputHandler");
            inputHandlerField.setAccessible(true);
            InputHandler inputHandler = (InputHandler) inputHandlerField.get(null);
            List<IShowsRecipeFocuses> showsRecipeFocuses = ObfuscationReflectionHelper.getPrivateValue(InputHandler.class, inputHandler, "showsRecipeFocuses");
            showsRecipeFocuses.add(0, new MultiblockInfoRecipeFocusShower());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
