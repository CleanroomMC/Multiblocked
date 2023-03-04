package com.cleanroommc.multiblocked.common.recipe.conditions;

import com.cleanroommc.multiblocked.api.crafttweaker.functions.IPredicateFunction;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeCondition;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import com.google.gson.JsonObject;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class PredicateCondition extends RecipeCondition {

    public static final PredicateCondition INSTANCE = new PredicateCondition();
    public static final String  DEFAULT_TOOLTIP ="multiblocked.recipe.condition.predicate.tooltip.default";
    private final IPredicateFunction predicate;
    private  String tooltip;

    public PredicateCondition() {
        this.tooltip = DEFAULT_TOOLTIP;
        this.predicate = (recipe, logic) -> true;
    }

    public PredicateCondition(String tooltip, IPredicateFunction predicate) {
        this.tooltip = tooltip;
        this.predicate = predicate;
    }
    @Override
    public String getType() {
        return "predicate";
    }

    @Override
    public ITextComponent getTooltips() {
        return new TextComponentTranslation(tooltip);
    }

    @Override
    public boolean test(@Nonnull Recipe recipe, @Nonnull RecipeLogic recipeLogic) {
        return predicate.test(recipeLogic, recipe);
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject config =  super.serialize();
        if (!tooltip.equals(DEFAULT_TOOLTIP))
            config.addProperty("tooltip", tooltip);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
         super.deserialize(config);
        if (config.has("tooltip"))
            this.tooltip = config.get("tooltip").getAsString();
         return this;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new PredicateCondition();
    }
}
