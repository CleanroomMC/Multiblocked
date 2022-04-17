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
                if (controller.asyncRecipeSearching) {
                    if (mbwsd.getPeriodID() < lastPeriod) {
                        lastPeriod = mbwsd.getPeriodID();
                        progress++;
                        handleTickRecipe(lastRecipe);
                    } else {
                        if (controller.hasProxies() && asyncChanged()) {
                            progress++;
                            handleTickRecipe(lastRecipe);
                        }
                    }
                } else {
                    progress++;
                    handleTickRecipe(lastRecipe);
                }
            } else {
                progress++;
                handleTickRecipe(lastRecipe);
                if (progress == duration) {
                    onRecipeFinish();
                }
            }
        } else if (lastRecipe != null) {
            findAndHandleRecipe();
        } else if (timer % 5 == 0) {
            if (controller.asyncRecipeSearching) {
                if (mbwsd.getPeriodID() < lastPeriod) {
                    lastPeriod = mbwsd.getPeriodID();
                    findAndHandleRecipe();
                } else {
                    if (controller.hasProxies() && asyncChanged()) {
                        findAndHandleRecipe();
                    }
                }
            } else {
                findAndHandleRecipe();
            }
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
        if (lastRecipe != null && lastRecipe.matchRecipe(this.controller) && lastRecipe.matchTickRecipe(this.controller)) {
            recipe = lastRecipe;
            lastRecipe = null;
            setupRecipe(recipe);
        } else {
            List<Recipe> matches = this.definition.recipeMap.searchRecipe(this.controller);
            lastRecipe = null;
            for (Recipe match : matches) {
                setupRecipe(match);
                if (lastRecipe != null && getStatus() == Status.WORKING) {
                    break;
                }
            }
        }
    }

    @ZenMethod
    public void handleTickRecipe(Recipe recipe) {
        if (recipe.hasTick()) {
            if (recipe.matchTickRecipe(this.controller)) {
                recipe.handleTickRecipeIO(IO.IN, this.controller);
                recipe.handleTickRecipeIO(IO.OUT, this.controller);
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
            if (definition.setupRecipe.apply(this, recipe)) {
                return;
            }
        }
        if (recipe.handleRecipeIO(IO.IN, this.controller)) {
            lastRecipe = recipe;
            setStatus(Status.WORKING);
            progress = 0;
            duration = recipe.duration;
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
            if (definition.recipeFinish.apply(this, lastRecipe)) {
                return;
            }
        }
        lastRecipe.handleRecipeIO(IO.OUT, this.controller);
        if (lastRecipe.matchRecipe(this.controller) && lastRecipe.matchTickRecipe(this.controller)) {
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
            setStatus(compound.hasKey("status") ? Status.values()[compound.getInteger("status")] : Status.WORKING);
            duration = lastRecipe.duration;
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (lastRecipe != null && status != Status.IDLE) {
            compound.setString("recipe", lastRecipe.uid);
            compound.setInteger("status", status.ordinal());
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

        public String name;
        Status(String name) {
            this.name = name;
        }
    }
}
