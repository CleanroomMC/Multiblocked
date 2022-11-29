package com.cleanroommc.multiblocked.jei;

import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.FuelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ProgressWidget;
import com.cleanroommc.multiblocked.jei.ingredient.AspectListIngredient;
import com.cleanroommc.multiblocked.jei.multipage.MultiblockInfoCategory;
import com.cleanroommc.multiblocked.jei.recipeppage.FuelWrapper;
import com.cleanroommc.multiblocked.jei.recipeppage.RecipeMapCategory;
import com.cleanroommc.multiblocked.jei.recipeppage.RecipeMapFuelCategory;
import com.cleanroommc.multiblocked.jei.recipeppage.RecipeWrapper;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUIGuiHandler;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.RecipeWidget;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
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
    private static IJeiRuntime jeiRuntime;

    public static IRecipeWrapper getWrapper(RecipeLayout layout) {
        try {
            return (IRecipeWrapper) fieldRecipeWrapper.get(layout);
        } catch (IllegalAccessException e) {
            Multiblocked.LOGGER.error(e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
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
        for (ComponentDefinition definition : MbdComponents.DEFINITION_REGISTRY.values()) {
            if (!definition.properties.showInJei) {
                removed.add(definition.getStackForm());
            }
        }
        JeiPlugin.itemRegistry.removeIngredientsAtRuntime(VanillaTypes.ITEM, removed);
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        Multiblocked.LOGGER.info("JEI register categories");
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(new MultiblockInfoCategory(jeiHelpers));
        for (RecipeMap recipeMap : RecipeMap.RECIPE_MAP_REGISTRY.values()) {
            if (recipeMap == RecipeMap.EMPTY) continue;
            registry.addRecipeCategories(new RecipeMapCategory(jeiHelpers, recipeMap));
            registry.addRecipeCategories(new RecipeMapFuelCategory(jeiHelpers, recipeMap));
        }
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        Multiblocked.LOGGER.info("JEI register");
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        ModularUIGuiHandler modularUIGuiHandler = new ModularUIGuiHandler(jeiHelpers.recipeTransferHandlerHelper());
        registry.addAdvancedGuiHandlers(modularUIGuiHandler);
        registry.addGhostIngredientHandler(modularUIGuiHandler.getGuiContainerClass(), modularUIGuiHandler);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(modularUIGuiHandler, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        for (RecipeMap recipeMap : RecipeMap.RECIPE_MAP_REGISTRY.values()) {
            registry.addRecipes(recipeMap.recipes.values().stream()
                            .map(recipe -> new RecipeWidget(
                                    recipeMap,
                                    recipe,
                                    ProgressWidget.JEIProgress,
                                    ProgressWidget.JEIProgress))
                            .map(RecipeWrapper::new)
                            .collect(Collectors.toList()),
                    Multiblocked.MODID + ":" + recipeMap.name);
            if (recipeMap.isFuelRecipeMap()) {
                registry.addRecipes(recipeMap.recipes.values()
                                .stream()
                                .map(recipe -> new FuelWidget(recipeMap, recipe))
                                .map(FuelWrapper::new)
                                .collect(Collectors.toList()),
                        Multiblocked.MODID + ":" + recipeMap.name + ".fuel");
            }
        }
        MultiblockInfoCategory.registerRecipes(registry);
        itemRegistry = registry.getIngredientRegistry();
    }

    @Override
    public void registerIngredients(@Nonnull IModIngredientRegistration registry) {
        Multiblocked.LOGGER.info("JEI register ingredients");
        if (Loader.isModLoaded(Multiblocked.MODID_TC6) && !Loader.isModLoaded(Multiblocked.MODID_THAUMJEI)) {
            ((AspectListIngredient) AspectListIngredient.INSTANCE).registerIngredients(registry);
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

    public static IJeiRuntime getJeiRuntime() {
        return jeiRuntime != null ? jeiRuntime : (jeiRuntime = Internal.getRuntime());
    }
}
