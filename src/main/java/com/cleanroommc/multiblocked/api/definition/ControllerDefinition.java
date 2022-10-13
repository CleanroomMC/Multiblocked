package com.cleanroommc.multiblocked.api.definition;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.crafttweaker.functions.*;
import com.cleanroommc.multiblocked.api.pattern.BlockPattern;
import com.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import com.cleanroommc.multiblocked.api.pattern.MultiblockShapeInfo;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.item.MCItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenProperty;
import stanhebben.zenscript.annotations.ZenSetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * Definition of a controller, which define its structure, logic, recipe chain and so on.
 */
@ZenClass("mods.multiblocked.definition.ControllerDefinition")
@ZenRegister
public class ControllerDefinition extends ComponentDefinition {
    protected Supplier<BlockPattern> basePattern;
    protected Supplier<RecipeMap> recipeMap;
    protected Supplier<ItemStack> catalyst; // if null, checking pattern per second

    @ZenProperty
    public IDynamicPattern dynamicPattern;
    @ZenProperty
    public IStructureFormed structureFormed;
    @ZenProperty
    public IStructureInvalid structureInvalid;
    @ZenProperty
    public IUpdateFormed updateFormed;
    @ZenProperty
    public ISetupRecipe setupRecipe;
    @ZenProperty
    public IRecipeFinish recipeFinish;
    @ZenProperty
    public IApplyContentModifier applyContentModifier;
    @ZenProperty
    public ICalcRecipeDuration calcRecipeDuration;
    @ZenProperty
    public boolean consumeCatalyst;
    public boolean noNeedController;
    @ZenProperty
    public List<MultiblockShapeInfo> designs;

    // used for Gson
    public ControllerDefinition() {
        this(null, ControllerTileEntity.class);
    }

    public ControllerDefinition(ResourceLocation location) {
        this(location, ControllerTileEntity.class);
    }

    public ControllerDefinition(ResourceLocation location, Class<? extends ControllerTileEntity> clazz) {
        super(location, clazz);
        this.recipeMap = ()->RecipeMap.EMPTY;
    }

    public List<MultiblockShapeInfo> getDesigns() {
        if (designs != null) return designs;
        // auto gen
        if (getBasePattern() != null) {
            return autoGenDFS(getBasePattern(), new ArrayList<>(), new Stack<>());
        } else if (dynamicPattern != null) {
            try {
                return autoGenDFS(dynamicPattern.apply((ControllerTileEntity) createNewTileEntity(null)), new ArrayList<>(), new Stack<>());
            } catch (Exception exception) {
                dynamicPattern = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", location, "dynamicPattern", exception);
            }
        }
        return Collections.emptyList();
    }

    private List<MultiblockShapeInfo> autoGenDFS(BlockPattern structurePattern, List<MultiblockShapeInfo> pages, Stack<Integer> repetitionStack) {
        int[][] aisleRepetitions = structurePattern.aisleRepetitions;
        if (repetitionStack.size() == aisleRepetitions.length) {
            int[] repetition = new int[repetitionStack.size()];
            for (int i = 0; i < repetitionStack.size(); i++) {
                repetition[i] = repetitionStack.get(i);
            }
            pages.add(new MultiblockShapeInfo(structurePattern.getPreview(repetition)));
        } else {
            for (int i = aisleRepetitions[repetitionStack.size()][0]; i <= aisleRepetitions[repetitionStack.size()][1]; i++) {
                repetitionStack.push(i);
                autoGenDFS(structurePattern, pages, repetitionStack);
                repetitionStack.pop();
            }
        }
        return pages;
    }

    @Optional.Method(modid = Multiblocked.MODID_CT)
    @ZenGetter("catalyst")
    public IItemStack getCatalystCT() {
        return catalyst == null ? null : new MCItemStack(catalyst.get());
    }

    @Optional.Method(modid = Multiblocked.MODID_CT)
    @ZenSetter("catalyst")
    public void setCatalystCT(IItemStack catalyst) {
        setCatalyst(catalyst == null ? null : CraftTweakerMC.getItemStack(catalyst));
    }

    public String getDescription() {
        return location.getNamespace() + "." + location.getPath() + ".description";
    }

    @Override
    public boolean needUpdateTick() {
        return super.needUpdateTick() || catalyst == null;
    }

    @ZenGetter("basePattern")
    public BlockPattern getBasePattern() {
        return basePattern == null ? null : basePattern.get();
    }

    @ZenGetter("recipeMap")
    public RecipeMap getRecipeMap() {
        return recipeMap == null ? null : recipeMap.get();
    }

    public ItemStack getCatalyst() {
        return catalyst == null ? null : catalyst.get();
    }

    @ZenSetter("basePattern")
    public void setBasePattern(BlockPattern basePattern) {
        this.basePattern = () -> basePattern;
    }

    public void setBasePattern(Supplier<BlockPattern> basePattern) {
        this.basePattern = basePattern;
    }

    @ZenSetter("recipeMap")
    public void setRecipeMap(RecipeMap recipeMap) {
        this.recipeMap = () -> recipeMap;
    }

    public void setRecipeMap(Supplier<RecipeMap> recipeMap) {
        this.recipeMap = recipeMap;
    }

    public void setDesigns(List<MultiblockShapeInfo> designs) {
        this.designs = designs;
    }

    public void setCatalyst(Supplier<ItemStack> catalyst) {
        this.catalyst = catalyst;
    }

    public void setCatalyst(ItemStack catalyst) {
        this.catalyst = () -> catalyst;
    }

    @Override
    public void fromJson(JsonObject json) {
        super.fromJson(json);
        if (json.has("basePattern")) {
            basePattern = Suppliers.memoize(()-> Multiblocked.GSON.fromJson(json.get("basePattern"), JsonBlockPattern.class).build());
        }
        if (json.has("recipeMap")) {
            recipeMap = Suppliers.memoize(()-> RecipeMap.RECIPE_MAP_REGISTRY.getOrDefault(json.get("recipeMap").getAsString(), RecipeMap.EMPTY));
        } else {
            setRecipeMap(RecipeMap.EMPTY);
        }
        if (json.has("catalyst")) {
            catalyst = Suppliers.memoize(()-> Multiblocked.GSON.fromJson(json.get("catalyst"), ItemStack.class));
            consumeCatalyst = JsonUtils.getBoolean(json, "consumeCatalyst", consumeCatalyst);
            noNeedController = JsonUtils.getBoolean(json, "noNeedController", noNeedController);
        }
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        if (getRecipeMap() != null) {
            json.addProperty("recipeMap", getRecipeMap().name);
        }
        if (getCatalyst() != null) {
            json.add("catalyst", Multiblocked.GSON.toJsonTree(getCatalyst()));
            json.addProperty("consumeCatalyst", consumeCatalyst);
            json.addProperty("noNeedController", noNeedController);
        }
        return json;
    }
}
