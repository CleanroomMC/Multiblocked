package com.cleanroommc.multiblocked.proxies.thaumicjei;

import com.buuz135.thaumicjei.ThaumcraftJEIPlugin;
import mezz.jei.api.recipe.IIngredientType;
import thaumcraft.api.aspects.AspectList;

public class ActiveThaumicJEIProxy implements IThaumicJEIProxy {
    @Override
    public IIngredientType<AspectList> getIngredientInstance() {
        return ThaumcraftJEIPlugin.ASPECT_LIST;
    }
}
