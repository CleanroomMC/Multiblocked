package io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe;

import com.google.common.collect.ImmutableList;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;

import java.util.Map;

public class RecipeWidget extends WidgetGroup {
    public final Recipe recipe;

    public RecipeWidget(Recipe recipe) {
        super(0, 0, 176, 166);
        this.recipe = recipe;
        DraggableScrollableWidgetGroup inputs = new DraggableScrollableWidgetGroup(5, 30, 70, 70).setBackground(new ColorRectTexture(-1));
        DraggableScrollableWidgetGroup outputs = new DraggableScrollableWidgetGroup(85, 30, 70, 70).setBackground(new ColorRectTexture(-1));
        this.addWidget(inputs);
        this.addWidget(outputs);
        int index = 0;
        for (Map.Entry<MultiblockCapability, ImmutableList<Object>> entry : recipe.inputs.entrySet()) {
            MultiblockCapability capability = entry.getKey();
            for (Object o : entry.getValue()) {
                inputs.addWidget(capability.createContentWidget().setContent(IO.IN, o).setSelfPosition(2 + 22 * (index % 3), 2 + 22 * (index / 3)));
                index++;
            }
        }

        index = 0;
        for (Map.Entry<MultiblockCapability, ImmutableList<Object>> entry : recipe.outputs.entrySet()) {
            MultiblockCapability capability = entry.getKey();
            for (Object o : entry.getValue()) {
                outputs.addWidget(capability.createContentWidget().setContent(IO.OUT, o).setSelfPosition(2 + 22 * (index % 3), 2 + 22 * (index / 3)));
                index++;
            }
        }
    }
}
