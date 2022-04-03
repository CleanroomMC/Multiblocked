package com.cleanroommc.multiblocked.common.recipe.content;

import com.cleanroommc.multiblocked.Multiblocked;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenConstructor;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenOperator;
import stanhebben.zenscript.annotations.ZenProperty;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

@ZenClass("mods.thaumcraft.AspectStack")
@ZenRegister
@ModOnly(value = Multiblocked.MODID_TC6)
public class AspectStack {
    public final Aspect aspect;
    @ZenProperty
    public int amount;

    public AspectStack(Aspect aspect, int amount) {
        this.aspect = aspect;
        this.amount = amount;
    }

    @ZenConstructor
    public AspectStack(String aspect, int amount) {
        this.aspect = Aspect.getAspect(aspect);
        this.amount = amount;
    }

    @ZenMethod
    public String getAspectName() {
        return aspect.getName();
    }

    @ZenMethod
    public AspectStack copy() {
        return new AspectStack(aspect, amount);
    }

    public AspectList toAspectList() {
        AspectList list = new AspectList();
        list.add(aspect, amount);
        return list;
    }

    @ZenOperator(OperatorType.MUL)
    public AspectStack multi(int other) {
        return new AspectStack(aspect, amount * other);
    }
}
