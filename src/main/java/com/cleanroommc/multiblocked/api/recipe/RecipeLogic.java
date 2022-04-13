package com.cleanroommc.multiblocked.api.recipe;

import com.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import crafttweaker.annotations.ZenRegister;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.List;

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
    private long lastPeriod;

    public RecipeLogic(ControllerTileEntity controller) {
        this.controller = controller;
        this.definition = controller.getDefinition();
        this.timer = Multiblocked.RNG.nextInt();
        this.lastPeriod = Long.MIN_VALUE;
    }

    @ZenMethod
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
            long latestPeriod = MultiblockWorldSavedData.getOrCreate(controller.getWorld()).getPeriodID();
            if (latestPeriod < lastPeriod) {
                lastPeriod = latestPeriod;
                findAndHandleRecipe();
            } else {
                boolean needSearch = false;
                if (controller.hasProxies()) {
                    for (Long2ObjectOpenHashMap<CapabilityProxy<?>> map : controller.getCapabilities().values()) {
                        if (map != null) {
                            for (CapabilityProxy<?> proxy : map.values()) {
                                if (proxy != null) {
                                    if (proxy.getLatestPeriodID() > lastPeriod) {
                                        lastPeriod = proxy.getLatestPeriodID();
                                        needSearch = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (needSearch) break;
                    }
                    if (needSearch) findAndHandleRecipe();
                }
            }
        }
    }

    @ZenMethod
    public void findAndHandleRecipe() {
        Recipe recipe;
        if (lastRecipe != null && lastRecipe.match(this.controller)) {
            recipe = lastRecipe;
            lastRecipe = null;
            setupRecipe(recipe);
        } else {
            List<Recipe> matches = this.definition.recipeMap.searchRecipe(this.controller);
            lastRecipe = null;
            for (Recipe match : matches) {
                setupRecipe(match);
                if (lastRecipe != null && isWorking) {
                    break;
                }
            }
        }
    }

    @ZenMethod
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

    @ZenMethod
    public void onRecipeFinish() {
        if (definition.recipeFinish != null) {
            if (definition.recipeFinish.apply(this, lastRecipe)) {
                return;
            }
        }
        lastRecipe.handleOutput(this.controller);
        if (lastRecipe.match(this.controller)) {
            setupRecipe(lastRecipe);
        } else {
            isWorking = false;
            controller.setStatus("idle");
            progress = 0;
            duration = 0;
            markDirty();
        }
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
