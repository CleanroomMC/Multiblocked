package com.cleanroommc.multiblocked.core.mods.mixins;

import com.cleanroommc.multiblocked.jei.MultiblockInfoRecipeFocusShower;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.InputHandler;
import mezz.jei.runtime.JeiRuntime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(InputHandler.class)
public class InputHandlerMixin {

    @Shadow @Final private List<IShowsRecipeFocuses> showsRecipeFocuses;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addNewShowRecipeFocus(JeiRuntime runtime, IngredientRegistry ingredientRegistry, IngredientListOverlay ingredientListOverlay, GuiScreenHelper guiScreenHelper, LeftAreaDispatcher leftAreaDispatcher, BookmarkList bookmarkList, CallbackInfo ci) {
        this.showsRecipeFocuses.add(3, new MultiblockInfoRecipeFocusShower()); // Before generic GuiContainerWrapper
    }

}
