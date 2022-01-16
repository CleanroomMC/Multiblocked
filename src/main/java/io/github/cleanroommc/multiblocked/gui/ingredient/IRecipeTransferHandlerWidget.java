package io.github.cleanroommc.multiblocked.gui.ingredient;

import io.github.cleanroommc.multiblocked.gui.modular.ModularUIContainer;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.entity.player.EntityPlayer;

public interface IRecipeTransferHandlerWidget {

    String transferRecipe(ModularUIContainer container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer);
}
