package com.cleanroommc.multiblocked.api.recipe;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.math.MathHelper;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenConstructor;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenClass("mods.multiblocked.recipe.ContentModifier")
@ZenRegister
public class ContentModifier {
    @ZenProperty
    public final double value;
    @ZenProperty
    public final boolean affectChance;
    @ZenProperty
    public final Operation operation;

    @ZenConstructor
    public ContentModifier(double value, boolean affectChance, Operation operation) {
        this.value = value;
        this.affectChance = affectChance;
        this.operation = operation;
    }

    public double apply(double target) {
        if (affectChance) return target;
        return operation == Operation.ADDITION ? target + value : target * value;
    }

    public float applyToChance(float chance) {
        if (!affectChance) return chance;
        return MathHelper.clamp((float) (operation == Operation.ADDITION ? chance + value : chance * value), 0.0f, 1.0f);
    }

    @ZenClass("mods.multiblocked.recipe.ContentModifierOperation")
    @ZenRegister
    public enum Operation {
        @ZenProperty
        ADDITION,
        @ZenProperty
        MULTIPLICATION
    }
}
