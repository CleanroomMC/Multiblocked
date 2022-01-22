package io.github.cleanroommc.multiblocked.api.gui.ingredient;

import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUIContainer;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.entity.player.EntityPlayer;

public interface IRecipeTransferHandlerWidget {

    String transferRecipe(ModularUIContainer container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer);
}
