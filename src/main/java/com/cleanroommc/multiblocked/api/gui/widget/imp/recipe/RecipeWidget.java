package com.cleanroommc.multiblocked.api.gui.widget.imp.recipe;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.google.common.collect.ImmutableList;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Tuple;

import java.util.Map;
import java.util.function.DoubleSupplier;

public class RecipeWidget extends WidgetGroup {
    public final Recipe recipe;
    public final DraggableScrollableWidgetGroup inputs;
    public final DraggableScrollableWidgetGroup outputs;

    public RecipeWidget(Recipe recipe, ResourceTexture progress, IGuiTexture background) {
        this(recipe, ProgressWidget.JEIProgress, progress, background);
    }

    public RecipeWidget(Recipe recipe, ResourceTexture progress) {
        this(recipe, ProgressWidget.JEIProgress, progress, new ColorRectTexture(0x1f000000));
    }

    public RecipeWidget(Recipe recipe, DoubleSupplier doubleSupplier, ResourceTexture progress, IGuiTexture background) {
        super(0, 0, 176, 84);
        this.recipe = recipe;
        setClientSideWidget();
        inputs = new DraggableScrollableWidgetGroup(5, 5, 64, 64).setBackground(background);
        outputs = new DraggableScrollableWidgetGroup(176 - 64 - 5, 5, 64, 64).setBackground(background);
        this.addWidget(inputs);
        this.addWidget(outputs);
        this.addWidget(new ProgressWidget(doubleSupplier, 78, 27, 20, 20, progress));
        this.addWidget(new LabelWidget(5, 73, () -> I18n.format("multiblocked.recipe.duration", this.recipe.duration / 20.)));
        if (recipe.text != null) {
            this.addWidget(new LabelWidget(80, 73, recipe.text.getFormattedText()));
        }
        int index = 0;
        for (Map.Entry<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> entry : recipe.inputs.entrySet()) {
            MultiblockCapability<?> capability = entry.getKey();
            for (Tuple<Object, Float> in : entry.getValue()) {
                inputs.addWidget(capability.createContentWidget().setContent(IO.IN, in.getFirst(), in.getSecond()).setSelfPosition(2 + 20 * (index % 3), 2 + 20 * (index / 3)));
                index++;
            }
        }

        index = 0;
        for (Map.Entry<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> entry : recipe.outputs.entrySet()) {
            MultiblockCapability<?> capability = entry.getKey();
            for (Tuple<Object, Float> out : entry.getValue()) {
                outputs.addWidget(capability.createContentWidget().setContent(IO.OUT, out.getFirst(), out.getSecond()).setSelfPosition(2 + 20 * (index % 3), 2 + 20 * (index / 3)));
                index++;
            }
        }
    }
}
