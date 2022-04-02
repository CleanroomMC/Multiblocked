package com.cleanroommc.multiblocked.api.recipe;

import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import crafttweaker.annotations.ZenRegister;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenClass("mods.multiblocked.recipe.RecipeLogic")
@ZenRegister
public class RecipeLogic {
    @ZenProperty
    public final ControllerTileEntity controller;
    public final ControllerDefinition definition;
    @ZenProperty
    public Recipe lastRecipe;
    @ZenProperty
    public boolean isWorking;
    @ZenProperty
    public int progress;
    @ZenProperty
    public int duration;
    @ZenProperty
    public int timer;

    public RecipeLogic(ControllerTileEntity controller) {
        this.controller = controller;
        this.definition = controller.getDefinition();
        this.timer = Multiblocked.RNG.nextInt();
    }

    public void update() {
        timer++;
        if (isWorking) {
            progress++;
            if (progress == duration) {
                onRecipeFinish();
            }
        } else if (lastRecipe != null) {
            findAndHandleRecipe();
        } else if (timer % 5 == 0) {
            findAndHandleRecipe();
        }
    }

    public void findAndHandleRecipe() {
        Recipe recipe;
        if (lastRecipe != null && lastRecipe.match(this.controller)) {
            recipe = lastRecipe;
        } else {
            recipe = this.definition.recipeMap.searchRecipe(this.controller);
        }
        lastRecipe = null;
        if (recipe != null) setupRecipe(recipe);
    }

    public void setupRecipe(Recipe recipe) {
        if (definition.setupRecipe != null) {
            if (definition.setupRecipe.apply(this, recipe)) {
                return;
            }
        }
        lastRecipe = recipe;
        isWorking = true;
        controller.setStatus("working");
        progress = 0;
        duration = recipe.duration;
        recipe.handleInput(this.controller);
        markDirty();
    }

    public void onRecipeFinish() {
        if (definition.recipeFinish != null) {
            if (definition.recipeFinish.apply(this, lastRecipe)) {
                return;
            }
        }
        isWorking = false;
        controller.setStatus("idle");
        progress = 0;
        duration = 0;
        lastRecipe.handleOutput(this.controller);
        markDirty();
    }

    @ZenMethod
    public void markDirty() {
        this.controller.markAsDirty();
    }

    public void readFromNBT(NBTTagCompound compound) {
        lastRecipe = compound.hasKey("recipe") ? definition.recipeMap.recipes.get(compound.getString("recipe")) : null;
        if (lastRecipe != null) {
            isWorking = true;
            duration = lastRecipe.duration;
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (lastRecipe != null && isWorking) {
            compound.setString("recipe", lastRecipe.uid);
        }
        return compound;
    }
}
