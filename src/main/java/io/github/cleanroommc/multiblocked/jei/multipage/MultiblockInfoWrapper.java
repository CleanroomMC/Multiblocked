package io.github.cleanroommc.multiblocked.jei.multipage;

import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.controller.structure.PatternWidget;
import io.github.cleanroommc.multiblocked.jei.ModularWrapper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

import javax.annotation.Nonnull;

public class MultiblockInfoWrapper extends ModularWrapper {
    public final ControllerDefinition definition;

    public MultiblockInfoWrapper(ControllerDefinition definition) {
        super(PatternWidget.getPatternWidget(definition), 176, 256);
        this.definition = definition;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, ((PatternWidget)widget).allItemStackInputs);
        ingredients.setOutput(VanillaTypes.ITEM, definition.getStackForm());
    }

}
