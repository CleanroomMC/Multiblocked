package com.cleanroommc.multiblocked.common.recipe.conditions;

import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeCondition;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote WhetherCondition, specific whether
 */
public class PositionYCondition extends RecipeCondition {

    public final static PositionYCondition INSTANCE = new PositionYCondition();
    private int min;
    private int max;

    private PositionYCondition() {}

    public PositionYCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String getType() {
        return "pos_y";
    }

    @Override
    public ITextComponent getTooltips() {
        return new TextComponentTranslation("multiblocked.recipe.condition.pos_y.tooltip", this.min, this.max);
    }

    @Override
    public boolean test(@Nonnull Recipe recipe, @Nonnull RecipeLogic recipeLogic) {
        int y = recipeLogic.controller.getPos().getY();
        return y >= this.min && y <= this.max;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new PositionYCondition();
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("min", this.min);
        config.addProperty("max", this.max);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
        super.deserialize(config);
        min = JsonUtils.getInt(config, "min", Integer.MIN_VALUE);
        max = JsonUtils.getInt(config, "max", Integer.MAX_VALUE);
        return this;
    }

    @Override
    public void openConfigurator(WidgetGroup group) {
        super.openConfigurator(group);
        group.addWidget(new TextFieldWidget(0, 20, 60, 15, true, null, s->min = Integer.parseInt(s))
                .setCurrentString(min + "")
                .setNumbersOnly(Integer.MIN_VALUE, Integer.MAX_VALUE)
                .setHoverTooltip("multiblocked.gui.condition.pos_y.min"));

        group.addWidget(new TextFieldWidget(0, 40, 60, 15, true, null, s->max = Integer.parseInt(s))
                .setCurrentString(max + "")
                .setNumbersOnly(Integer.MIN_VALUE, Integer.MAX_VALUE)
                .setHoverTooltip("multiblocked.gui.condition.pos_y.max"));
    }
}
