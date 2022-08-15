package com.cleanroommc.multiblocked.api.recipe;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import crafttweaker.annotations.ZenRegister;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.ArrayList;
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
    public List<Recipe> lastFailedMatches;
    @ZenProperty
    public int progress;
    @ZenProperty
    public int duration;
    @ZenProperty
    public int timer;
    private Status status = Status.IDLE;
    private long lastPeriod;
    private final MultiblockWorldSavedData mbwsd;

    public RecipeLogic(ControllerTileEntity controller) {
        this.controller = controller;
        this.definition = controller.getDefinition();
        this.timer = Multiblocked.RNG.nextInt();
        this.lastPeriod = Long.MIN_VALUE;
        this.mbwsd = MultiblockWorldSavedData.getOrCreate(controller.getWorld());
    }

    @ZenMethod
    public void update() {
        timer++;
        if (getStatus() != Status.IDLE && lastRecipe != null) {
            if (getStatus() == Status.SUSPEND && timer % 5 == 0) {
                checkAsyncRecipeSearching(this::handleRecipeWorking);
            } else {
                handleRecipeWorking();
                if (progress >= duration) {
                    onRecipeFinish();
                }
            }
        } else if (lastRecipe != null) {
            findAndHandleRecipe();
        } else if (timer % 5 == 0) {
            checkAsyncRecipeSearching(this::findAndHandleRecipe);
            if (lastFailedMatches != null) {
                for (Recipe recipe : lastFailedMatches) {
                    if (recipe.checkConditions(this)) {
                        setupRecipe(recipe);
                    }
                    if (lastRecipe != null && getStatus() == Status.WORKING) {
                        lastFailedMatches = null;
                        return;
                    }
                }
            }
        }
    }

    @ZenMethod
    public void handleRecipeWorking() {
        if (lastRecipe.checkConditions(this)) {
            setStatus(Status.WORKING);
            progress++;
            handleTickRecipe(lastRecipe);
        } else {
            setStatus(Status.SUSPEND);
        }
        markDirty();
    }

    private void checkAsyncRecipeSearching(Runnable changed) {
        if (controller.asyncRecipeSearching) {
            if (mbwsd.getPeriodID() < lastPeriod) {
                lastPeriod = mbwsd.getPeriodID();
                changed.run();
            } else {
                if (controller.hasProxies() && asyncChanged()) {
                    changed.run();
                }
            }
        } else {
            changed.run();
        }
    }

    @ZenMethod
    public boolean asyncChanged() {
        boolean needSearch = false;
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
        return needSearch;
    }

    @ZenMethod
    public void findAndHandleRecipe() {
        Recipe recipe;
        lastFailedMatches = null;
        if (lastRecipe != null && lastRecipe.matchRecipe(this.controller, this) && lastRecipe.matchTickRecipe(this.controller, this) && lastRecipe.checkConditions(this)) {
            recipe = lastRecipe;
            lastRecipe = null;
            setupRecipe(recipe);
        } else {
            List<Recipe> matches = this.definition.recipeMap.searchRecipe(this.controller, this);
            lastRecipe = null;
            for (Recipe match : matches) {
                if (match.checkConditions(this)) {
                    setupRecipe(match);
                }
                if (lastRecipe != null && getStatus() == Status.WORKING) {
                    lastFailedMatches = null;
                    break;
                }
                if (lastFailedMatches == null) {
                    lastFailedMatches = new ArrayList<>();
                }
                lastFailedMatches.add(match);
            }
        }
    }

    @ZenMethod
    public void handleTickRecipe(Recipe recipe) {
        if (recipe.hasTick()) {
            if (recipe.matchTickRecipe(this.controller, this)) {
                recipe.handleTickRecipeIO(IO.IN, this.controller, this);
                recipe.handleTickRecipeIO(IO.OUT, this.controller, this);
                setStatus(Status.WORKING);
            } else {
                progress--;
                setStatus(Status.SUSPEND);
            }
        }
    }

    @ZenMethod
    public void setupRecipe(Recipe recipe) {
        if (definition.setupRecipe != null) {
            try {
                if (definition.setupRecipe.apply(this, recipe)) {
                    return;
                }
            } catch (Exception exception) {
                definition.setupRecipe = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "setupRecipe", exception);
            }
        }
        if (recipe.handleRecipeIO(IO.IN, this.controller, this)) {
            lastRecipe = recipe;
            setStatus(Status.WORKING);
            progress = 0;
            duration = definition.calcRecipeDuration == null ? recipe.duration : definition.calcRecipeDuration.apply(this, recipe, recipe.duration);
            markDirty();
        }
    }

    @ZenMethod
    public void setStatus(Status status) {
        if (this.status != status) {
            this.status = status;
            controller.setStatus(status.name);
        }
    }

    @ZenMethod
    public Status getStatus() {
        return status;
    }

    @ZenMethod
    @ZenGetter
    public boolean isWorking(){
        return status == Status.WORKING;
    }

    @ZenMethod
    @ZenGetter
    public boolean isIdle(){
        return status == Status.IDLE;
    }

    @ZenMethod
    @ZenGetter
    public boolean isSuspend(){
        return status == Status.SUSPEND;
    }

    @ZenMethod
    public void onRecipeFinish() {
        if (definition.recipeFinish != null) {
            try {
                if (definition.recipeFinish.apply(this, lastRecipe)) {
                    return;
                }
            } catch (Exception exception) {
                definition.recipeFinish = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "recipeFinish", exception);
            }
        }
        lastRecipe.handleRecipeIO(IO.OUT, this.controller, this);
        if (lastRecipe.matchRecipe(this.controller, this) && lastRecipe.matchTickRecipe(this.controller, this) && lastRecipe.checkConditions(this)) {
            setupRecipe(lastRecipe);
        } else {
            setStatus(Status.IDLE);
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
            status = compound.hasKey("status") ? Status.values()[compound.getInteger("status")] : Status.WORKING;
            duration = compound.hasKey("duration") ? compound.getInteger("duration") : lastRecipe.duration;
            progress = compound.hasKey("progress") ? compound.getInteger("progress") : 0;
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (lastRecipe != null && status != Status.IDLE) {
            compound.setString("recipe", lastRecipe.uid);
            compound.setInteger("status", status.ordinal());
            compound.setInteger("progress", progress);
            compound.setInteger("duration", duration);
        }
        return compound;
    }

    @ZenClass("mods.multiblocked.recipe.Status")
    @ZenRegister
    public enum Status {
        @ZenProperty
        IDLE("idle"),
        @ZenProperty
        WORKING("working"),
        @ZenProperty
        SUSPEND("suspend");

        @ZenProperty
        public final String name;
        Status(String name) {
            this.name = name;
        }
    }
}
