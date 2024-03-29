package com.cleanroommc.multiblocked.api.crafttweaker.expanders;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.recipe.Content;
import com.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import com.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import lach_01298.qmd.crafttweaker.particle.CTParticleStack;
import lach_01298.qmd.crafttweaker.particle.IParticleStack;
import lach_01298.qmd.particle.ParticleStack;
import mekanism.api.gas.GasStack;
import mekanism.common.integration.crafttweaker.gas.CraftTweakerGasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenCaster;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenExpansion("mods.multiblocked.recipe.Content")
public class ExpandRecipeContent {

    @ZenCaster
    @ZenMethod
    public static String asString(Content content) {
        if (content.content instanceof String) {
            return (String) content.content;
        }
        return null;
    }

    @ZenCaster
    @ZenMethod
    public static Integer asInteger(Content content) {
        if (content.content instanceof Integer) {
            return (Integer) content.content;
        }
        return null;
    }

    @ZenCaster
    @ZenMethod
    public static Float asFloat(Content content) {
        if (content.content instanceof Float) {
            return (Float) content.content;
        }
        return null;
    }

    @ZenCaster
    @ZenMethod
    public static Double asDouble(Content content) {
        if (content.content instanceof Double) {
            return (Double) content.content;
        }
        return null;
    }

    @ZenCaster
    @ZenMethod
    public static Boolean asBoolean(Content content) {
        if (content.content instanceof Boolean) {
            return (Boolean) content.content;
        }
        return null;
    }

    @ZenCaster
    @ZenMethod
    public static IIngredient asIIngredient(Content content) {
        if (content.content instanceof ItemsIngredient) {
            ItemsIngredient ingredient = (ItemsIngredient) content.content;
            if (((ItemsIngredient) content.content).isOre()) {
                return CraftTweakerMC.getOreDict(ingredient.getOreDict()).amount(ingredient.getAmount());
            } else {
                return CraftTweakerMC.getIIngredient(content.content).amount(ingredient.getAmount());
            }
        }
        return null;
    }

    @ZenCaster
    @ZenMethod
    public static IItemStack asIItemStack(Content content) {
        if (content.content instanceof ItemsIngredient) {
            return CraftTweakerMC.getIItemStack(((ItemsIngredient) content.content).getOutputStack());
        }
        return null;
    }

    @ZenCaster
    @ZenMethod
    public static ILiquidStack asILiquidStack(Content content) {
        if (content.content instanceof FluidStack) {
            return CraftTweakerMC.getILiquidStack((FluidStack) content.content);
        }
        return null;
    }

    @ZenCaster
    @ZenMethod
    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public static AspectStack asAspectStack(Content content) {
        if (content.content instanceof AspectStack) {
            return (AspectStack) content.content;
        }
        return null;
    }

    @ModOnly(Multiblocked.MODID_MEK)
    @ZenRegister
    @ZenExpansion("mods.multiblocked.recipe.Content")
    public static class Gas {
        @ZenCaster
        @Optional.Method(modid = Multiblocked.MODID_MEK)
        @ZenMethod
        public static IGasStack asIGasStack(Content content) {
            if (content.content instanceof GasStack) {
                return new CraftTweakerGasStack((GasStack) content.content);
            }
            return null;
        }
    }

    @ModOnly(Multiblocked.MODID_QMD)
    @ZenRegister
    @ZenExpansion("mods.multiblocked.recipe.Content")
    public static class Particle {

        @ZenCaster
        @ZenMethod
        @Optional.Method(modid = Multiblocked.MODID_QMD)
        public static IParticleStack asIParticleStack(Content content) {
            if (content.content instanceof ParticleStack) {
                return new CTParticleStack((ParticleStack) content.content);
            }
            return null;
        }
    }

}
