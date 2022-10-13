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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2022/05/27
 * @implNote DimensionCondition, specific dimension
 */
public class BiomeCondition extends RecipeCondition {

    public final static BiomeCondition INSTANCE = new BiomeCondition();
    private ResourceLocation biome = new ResourceLocation("dummy");

    private BiomeCondition() {}

    public BiomeCondition(ResourceLocation biome) {
        this.biome = biome;
    }

    @Override
    public String getType() {
        return "biome";
    }

    @Override
    public ITextComponent getTooltips() {
        return new TextComponentTranslation(String.format("biome.%s.%s", biome.getNamespace(), biome.getPath()));
    }

    @Override
    public boolean test(@Nonnull Recipe recipe, @Nonnull RecipeLogic recipeLogic) {
        World level = recipeLogic.controller.getWorld();
        Biome biome = level.getBiome(recipeLogic.controller.getPos());
        return biome.delegate.name().equals(this.biome);
    }

    @Override
    public RecipeCondition createTemplate() {
        return new BiomeCondition();
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("biome", biome.toString());
        return config;
    }

    @Override
    public RecipeCondition deserialize(@Nonnull JsonObject config) {
        super.deserialize(config);
        biome = new ResourceLocation(
                JsonUtils.getString(config, "biome", "dummy"));
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void openConfigurator(WidgetGroup group) {
        super.openConfigurator(group);
        Set<ResourceLocation> types = ForgeRegistries.BIOMES.getKeys();
        SelectorWidget selectorWidget = new SelectorWidget(0, 20, 80, 15, types.stream().map(ResourceLocation::toString).collect(Collectors.toList()), -1);
        SearchComponentWidget<ResourceLocation> searchComponentWidget = new SearchComponentWidget<>(0, 40, 80, 15, new SearchComponentWidget.IWidgetSearch<ResourceLocation>() {
            @Override
            public void search(String word, Consumer<ResourceLocation> find) {
                for (ResourceLocation type : types) {
                    if (type.toString().toLowerCase().contains(word.toLowerCase())) {
                        find.accept(type);
                    }
                }
            }

            @Override
            public String resultDisplay(ResourceLocation value) {
                return value.toString();
            }

            @Override
            public void selectResult(ResourceLocation value) {
                if (value != null) {
                    biome = value;
                    selectorWidget.setValue(value.toString());
                }
            }
        });
        group.addWidget(selectorWidget
                .setButtonBackground(new ColorRectTexture(0x7f2e2e2e))
                .setOnChanged(dim -> {
                    if (dim != null && !dim.isEmpty()) {
                        biome = new ResourceLocation(dim);
                        searchComponentWidget.setCurrentString(dim);
                    }
                }).setIsUp(true).setValue(types.contains(biome) ? biome.toString() : ""));
        group.addWidget(searchComponentWidget.setCapacity(2).setCurrentString(types.contains(biome) ? biome.toString() : ""));
    }
}
