package io.github.cleanroommc.multiblocked.api.gui.modular;

import io.github.cleanroommc.multiblocked.api.gui.ingredient.IGhostIngredientTarget;
import io.github.cleanroommc.multiblocked.api.gui.ingredient.IIngredientSlot;
import io.github.cleanroommc.multiblocked.api.gui.ingredient.IRecipeTransferHandlerWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ModularUIGuiHandler implements IAdvancedGuiHandler<ModularUIGuiContainer>, IGhostIngredientHandler<ModularUIGuiContainer>, IRecipeTransferHandler<ModularUIContainer> {

    private final IRecipeTransferHandlerHelper transferHelper;

    public ModularUIGuiHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Nonnull
    @Override
    public Class<ModularUIGuiContainer> getGuiContainerClass() {
        return ModularUIGuiContainer.class;
    }

    @Nonnull
    @Override
    public Class<ModularUIContainer> getContainerClass() {
        return ModularUIContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ModularUIContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Optional<IRecipeTransferHandlerWidget> transferHandler = container.getModularUI()
                .getFlatVisibleWidgetCollection().stream()
                .filter(it -> it instanceof IRecipeTransferHandlerWidget)
                .map(it -> (IRecipeTransferHandlerWidget) it)
                .findFirst();
        if (!transferHandler.isPresent()) {
            return transferHelper.createInternalError();
        }
        String errorTooltip = transferHandler.get().transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
        if (errorTooltip == null) {
            return null;
        }
        return transferHelper.createUserErrorWithTooltip(errorTooltip);
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(ModularUIGuiContainer gui, int mouseX, int mouseY) {
        for (Widget widget : gui.modularUI.guiWidgets.values()) {
            if (widget instanceof IIngredientSlot && widget.isVisible()) {
                Object result = ((IIngredientSlot) widget).getIngredientOverMouse(mouseX, mouseY);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public <I> List<Target<I>> getTargets(ModularUIGuiContainer gui, @Nonnull I ingredient, boolean doStart) {
        Collection<Widget> widgets = gui.modularUI.guiWidgets.values();
        List<Target<I>> targets = new ArrayList<>();
        for (Widget widget : widgets) {
            if (widget instanceof IGhostIngredientTarget) {
                IGhostIngredientTarget ghostTarget = (IGhostIngredientTarget) widget;
                List<Target<?>> widgetTargets = ghostTarget.getPhantomTargets(ingredient);
                //noinspection unchecked
                targets.addAll((List<Target<I>>) (Object) widgetTargets);
            }
        }
        return targets;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(@Nonnull ModularUIGuiContainer guiContainer) {
        return Collections.emptyList();
    }

    @Override
    public void onComplete() {
    }
}
