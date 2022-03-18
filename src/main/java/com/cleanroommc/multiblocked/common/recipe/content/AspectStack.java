package com.cleanroommc.multiblocked.common.recipe.content;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class AspectStack {
    public Aspect aspect;
    public int amount;

    public AspectStack(Aspect aspect, int amount) {
        this.aspect = aspect;
        this.amount = amount;
    }

    public AspectStack copy() {
        return new AspectStack(aspect, amount);
    }

    public AspectList toAspectList() {
        AspectList list = new AspectList();
        list.add(aspect, amount);
        return list;
    }
}
