package com.cleanroommc.multiblocked.api.definition;

import com.cleanroommc.multiblocked.api.crafttweaker.functions.*;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.item.MCItemStack;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.pattern.BlockPattern;
import com.cleanroommc.multiblocked.api.pattern.MultiblockShapeInfo;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenProperty;
import stanhebben.zenscript.annotations.ZenSetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Definition of a controller, which define its structure, logic, recipe chain and so on.
 */
@ZenClass("mods.multiblocked.definition.ControllerDefinition")
@ZenRegister
public class ControllerDefinition extends ComponentDefinition {
    @ZenProperty
    public transient BlockPattern basePattern;
    @ZenProperty
    public transient RecipeMap recipeMap;
    @ZenProperty
    public transient IDynamicPattern dynamicPattern;
    @ZenProperty
    public transient IStructureFormed structureFormed;
    @ZenProperty
    public transient IStructureInvalid structureInvalid;
    @ZenProperty
    public transient IUpdateFormed updateFormed;
    @ZenProperty
    public transient ISetupRecipe setupRecipe;
    @ZenProperty
    public transient IRecipeFinish recipeFinish;
    public ItemStack catalyst = ItemStack.EMPTY; // if null, checking pattern per second
    @ZenProperty
    public boolean consumeCatalyst;
    @ZenProperty
    public transient List<MultiblockShapeInfo> designs;
    @ZenProperty
    public boolean disableOthersRendering; // if true, only render controller, all other blocks of the multi do not render.

    // used for Gson
    public ControllerDefinition() {
        this(null, ControllerTileEntity.class);
    }

    public ControllerDefinition(ResourceLocation location) {
        this(location, ControllerTileEntity.class);
    }

    public ControllerDefinition(ResourceLocation location, Class<? extends ControllerTileEntity> clazz) {
        super(location, clazz);
        this.recipeMap = RecipeMap.EMPTY;
    }

    public List<MultiblockShapeInfo> getDesigns(World world) {
        if (designs != null) return designs;
        // auto gen
        if (basePattern != null) {
            return autoGenDFS(basePattern, new ArrayList<>(), new Stack<>());
        } else if (dynamicPattern != null) {
            return autoGenDFS(dynamicPattern.apply((ControllerTileEntity) createNewTileEntity(world)), new ArrayList<>(), new Stack<>());
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
    public IItemStack getCatalyst() {
        return catalyst == null ? null : new MCItemStack(catalyst);
    }

    @Optional.Method(modid = Multiblocked.MODID_CT)
    @ZenSetter("catalyst")
    public void setCatalyst(IItemStack catalyst) {
        this.catalyst = catalyst == null ? null : CraftTweakerMC.getItemStack(catalyst);
    }

    public String getDescription() {
        return location.getPath() + ".description";
    }

    @Override
    public boolean needUpdateTick() {
        return super.needUpdateTick() || catalyst == null;
    }
}
