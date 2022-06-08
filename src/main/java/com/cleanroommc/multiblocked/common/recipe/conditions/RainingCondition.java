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
import net.minecraft.world.World;


import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote WhetherCondition, specific whether
 */
public class RainingCondition extends RecipeCondition {

    public final static RainingCondition INSTANCE = new RainingCondition();
    private float level;

    private RainingCondition() {}

    public RainingCondition(float level) {
        this.level = level;
    }

    @Override
    public String getType() {
        return "rain";
    }

    @Override
    public ITextComponent getTooltips() {
        return new TextComponentTranslation("multiblocked.recipe.condition.rain.tooltip", level);
    }

    @Override
    public boolean test(@Nonnull Recipe recipe, @Nonnull RecipeLogic recipeLogic) {
        World level = recipeLogic.controller.getWorld();
        return level.getRainStrength(1) >= this.level;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new RainingCondition();
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("level", level);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
        super.deserialize(config);
        level = JsonUtils.getFloat(config, "level", 0);
        return this;
    }

    @Override
    public void openConfigurator(WidgetGroup group) {
        super.openConfigurator(group);
        group.addWidget(new TextFieldWidget(0, 20, 60, 15, true, null, s->level = Float.parseFloat(s))
                .setCurrentString(level + "")
                .setNumbersOnly(0f, 1f)
                .setHoverTooltip("multiblocked.gui.condition.rain.level"));
    }
}
