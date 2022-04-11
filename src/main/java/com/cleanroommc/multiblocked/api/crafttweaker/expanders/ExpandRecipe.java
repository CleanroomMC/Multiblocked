package com.cleanroommc.multiblocked.api.crafttweaker.expanders;

import com.cleanroommc.multiblocked.api.recipe.Recipe;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.DataBool;
import crafttweaker.api.data.DataByte;
import crafttweaker.api.data.DataDouble;
import crafttweaker.api.data.DataFloat;
import crafttweaker.api.data.DataInt;
import crafttweaker.api.data.DataLong;
import crafttweaker.api.data.DataShort;
import crafttweaker.api.data.DataString;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.text.ITextComponent;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenExpansion("mods.multiblocked.recipe.Recipe")
public class ExpandRecipe {

    @ZenMethod
    public static IData getData(Recipe recipe, String key) {
        Object info = recipe.getData(key);
        if (info instanceof IData) {
            return (IData) info;
        } else if (info instanceof Boolean) {
            return new DataBool((Boolean) info);
        } else if (info instanceof Integer) {
            return new DataInt((Integer) info);
        } else if (info instanceof Byte) {
            return new DataByte((Byte) info);
        } else if (info instanceof Short) {
            return new DataShort((Short) info);
        } else if (info instanceof Long) {
            return new DataLong((Long) info);
        } else if (info instanceof Float) {
            return new DataFloat((Float) info);
        } else if (info instanceof Double) {
            return new DataDouble((Double) info);
        } else if (info instanceof String) {
            return new DataString((String) info);
        }
        return null;
    }

    @ZenMethod
    @ZenGetter("text")
    public static ITextComponent getText(Recipe recipe) {
        return CraftTweakerMC.getITextComponent(recipe.text);
    }
}
