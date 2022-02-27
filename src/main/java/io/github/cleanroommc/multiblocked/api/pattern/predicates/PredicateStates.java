package io.github.cleanroommc.multiblocked.api.pattern.predicates;

import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;

public class PredicateStates extends SimplePredicate {
    public IBlockState[] states;

    public PredicateStates() {
        super("states");
    }
    
    public PredicateStates(IBlockState... states) {
        this();
        this.states = states;
        buildObjectFromJson();
    }

    @Override
    public SimplePredicate buildObjectFromJson() {
        predicate = state -> ArrayUtils.contains(states, state.getBlockState());
        candidates = () -> Arrays.stream(states).map(state -> new BlockInfo(state, null)).toArray(BlockInfo[]::new);
        return this;
    }

    @Override
    public void getConfigWidget(List<WidgetGroup> groups) {
        WidgetGroup group = new WidgetGroup(0, 0, 100, 100);
        groups.add(group);
        DraggableScrollableWidgetGroup container = new DraggableScrollableWidgetGroup(0, 20, 100, 80);
        group.addWidget(container);
        group.addWidget(new ButtonWidget(0, 0, 20, 20, cd -> {
        }).setButtonTexture(new ColorRectTexture(-1), new TextTexture("add", 0xff000000)));

        super.getConfigWidget(groups);
    }
}
