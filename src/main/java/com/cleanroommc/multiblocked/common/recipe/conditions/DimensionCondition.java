package com.cleanroommc.multiblocked.common.recipe.conditions;

import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SearchComponentWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeCondition;
import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote DimensionCondition, specific dimension
 */
public class DimensionCondition extends RecipeCondition {

    public final static DimensionCondition INSTANCE = new DimensionCondition();
    private String dimension = "dummy";

    private DimensionCondition() {}

    public DimensionCondition(String dimension) {
        this.dimension = dimension;
    }

    @Override
    public String getType() {
        return "dimension";
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public ITextComponent getTooltips() {
        return new TextComponentTranslation("multiblocked.recipe.condition.dimension.tooltip", dimension);
    }

    @Override
    public boolean test(@Nonnull Recipe recipe, @Nonnull RecipeLogic recipeLogic) {
        World level = recipeLogic.controller.getWorld();
        return dimension.equals(level.provider.getDimensionType().getName());
    }

    @Override
    public RecipeCondition createTemplate() {
        return new DimensionCondition();
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("dim", dimension);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
        super.deserialize(config);
        dimension = JsonUtils.getString(config, "dim", "dummy");
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void openConfigurator(WidgetGroup group) {
        super.openConfigurator(group);
        Set<String> types = Arrays.stream(DimensionType.values()).map(DimensionType::getName).collect(Collectors.toSet());
        SelectorWidget selectorWidget = new SelectorWidget(0, 20, 80, 15, new ArrayList<>(types), -1);
        SearchComponentWidget<String> searchComponentWidget = new SearchComponentWidget<>(0, 40, 80, 15, new SearchComponentWidget.IWidgetSearch<String>() {
            @Override
            public void search(String word, Consumer<String> find) {
                for (String type : types) {
                    if (type.toLowerCase().contains(word.toLowerCase())) {
                        find.accept(type);
                    }
                }
            }

            @Override
            public String resultDisplay(String value) {
                return value;
            }

            @Override
            public void selectResult(String value) {
                if (value != null) {
                    dimension = value;
                    selectorWidget.setValue(value);
                }
            }
        });
        group.addWidget(selectorWidget
                .setButtonBackground(new ColorRectTexture(0x7f2e2e2e))
                .setOnChanged(dim -> {
                    if (dim != null && !dim.isEmpty()) {
                        dimension = dim;
                        searchComponentWidget.setCurrentString(dim);
                    }
                }).setIsUp(true).setValue(types.contains(dimension) ? dimension : ""));
        group.addWidget(searchComponentWidget.setCapacity(2).setCurrentString(types.contains(dimension) ? dimension : ""));
    }
}
