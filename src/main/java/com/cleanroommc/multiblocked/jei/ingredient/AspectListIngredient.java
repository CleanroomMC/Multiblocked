package com.cleanroommc.multiblocked.jei.ingredient;

import com.buuz135.thaumicjei.ThaumcraftJEIPlugin;
import com.cleanroommc.multiblocked.Multiblocked;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class AspectListIngredient extends AbstractIngredient<AspectList> {
    public static IIngredientType<AspectList> INSTANCE = Loader.isModLoaded(Multiblocked.MODID_THAUMJEI) ? ThaumcraftJEIPlugin.ASPECT_LIST : new AspectListIngredient();

    private AspectListIngredient() {}

    public void registerIngredients(IModIngredientRegistration registry) {
        registry.register(INSTANCE, getAllIngredients(), this, this);
    }

    @Override
    public Collection<AspectList> getAllIngredients() {
        List<AspectList> aspects = new ArrayList<>();
        aspects.addAll(Aspect.getPrimalAspects().stream().map(aspect -> new AspectList().add(aspect, 1)).collect(Collectors.toList()));
        aspects.addAll(Aspect.getCompoundAspects().stream().map(aspect -> new AspectList().add(aspect, 1)).collect(Collectors.toList()));
        return aspects;
    }

    @Override
    @Nonnull
    public Class<? extends AspectList> getIngredientClass() {
        return AspectList.class;
    }

    @Override
    @Nonnull
    public List<AspectList> expandSubtypes(@Nonnull List<AspectList> ingredients) {
        return ingredients;
    }

    @Nullable
    @Override
    public AspectList getMatch(@Nonnull Iterable<AspectList> ingredients, @Nonnull AspectList ingredientToMatch) {
        for (AspectList list : ingredients) {
            if (list.getAspects()[0].getName().equalsIgnoreCase(ingredientToMatch.getAspects()[0].getName()))
                return list;
        }
        return null;
    }

    @Override
    @Nonnull
    public String getDisplayName(@Nonnull AspectList ingredient) {
        return ingredient.getAspects()[0].getName();
    }

    @Override
    @Nonnull
    public String getUniqueId(@Nonnull AspectList ingredient) {
        return ingredient.getAspects()[0].getName();
    }

    @Override
    @Nonnull
    public String getWildcardId(@Nonnull AspectList ingredient) {
        return getUniqueId(ingredient);
    }

    @Override
    @Nonnull
    public String getModId(@Nonnull AspectList ingredient) {
        return "thaumcraft";
    }

    @Override
    @Nonnull
    public Iterable<Color> getColors(@Nonnull AspectList ingredient) {
        return Collections.singletonList(new Color(ingredient.getAspects()[0].getColor()));
    }

    @Override
    @Nonnull
    public String getResourceId(@Nonnull AspectList ingredient) {
        return ingredient.getAspects()[0].getName();
    }

    @Override
    @Nonnull
    public AspectList copyIngredient(@Nonnull AspectList ingredient) {
        return ingredient.copy();
    }

    @Override
    @Nonnull
    public String getErrorInfo(AspectList ingredient) {
        return "";
    }

    @Override
    public void render(@Nonnull Minecraft minecraft, int xPosition, int yPosition, @Nullable AspectList ingredient) {
        if (ingredient != null && ingredient.size() > 0) {
            GlStateManager.pushMatrix();
            minecraft.renderEngine.bindTexture(ingredient.getAspects()[0].getImage());
            GlStateManager.enableBlend();
            Color c = new Color(ingredient.getAspects()[0].getColor());
            GL11.glColor4f((float) c.getRed() / 255.0F, (float) c.getGreen() / 255.0F, (float) c.getBlue() / 255.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, 16, 16, 16, 16);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            GL11.glScaled(0.5, 0.5, 0.5);
            int amount = ingredient.getAmount(ingredient.getAspects()[0]);
            if (amount > 1) {
                minecraft.fontRenderer.drawStringWithShadow(TextFormatting.WHITE + "" + amount, (xPosition + 16) * 2, (yPosition + 12) * 2, 0);
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    @Nonnull
    public List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull AspectList ingredient, @Nonnull ITooltipFlag tooltipFlag) {
        return ingredient.size() > 0 ? Arrays.asList(TextFormatting.AQUA + ingredient.getAspects()[0].getName(), TextFormatting.GRAY + ingredient.getAspects()[0].getLocalizedDescription()) : Collections.emptyList();
    }

}
