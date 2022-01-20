package io.github.cleanroommc.multiblocked.common.recipe.content;

import thaumcraft.api.aspects.Aspect;

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
}
