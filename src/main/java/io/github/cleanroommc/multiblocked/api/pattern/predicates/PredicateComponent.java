package io.github.cleanroommc.multiblocked.api.pattern.predicates;

import com.google.gson.JsonObject;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class PredicateComponent extends SimplePredicate {
    public ResourceLocation location = new ResourceLocation("mod_id", "component_id");

    public PredicateComponent() {
        super("component");
    }

    public PredicateComponent(ComponentDefinition definition) {
        this(definition.location);
        buildPredicate();
    }

    public PredicateComponent(ResourceLocation location) {
        this();
        this.location = location;
        buildPredicate();
    }

    @Override
    public SimplePredicate buildPredicate() {
        predicate = state -> state.getBlockState().getBlock() instanceof BlockComponent && ((BlockComponent) state.getBlockState().getBlock()).definition.location.equals(location);
        candidates = () -> !MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.containsKey(location) ? new BlockInfo[0] : new BlockInfo[]{new BlockInfo(MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(location))};
        return this;
    }

    @Override
    public List<WidgetGroup> getConfigWidget(List<WidgetGroup> groups) {
        super.getConfigWidget(groups);
        WidgetGroup group = new WidgetGroup(0, 0, 100, 20);
        groups.add(group);
        group.addWidget(new LabelWidget(0, 0, ()->"Component registry name:").setDrop(true).setTextColor(-1));
        group.addWidget(new TextFieldWidget(0, 10, 120, 20, true, null, s -> {
            if (s != null && !s.isEmpty()) {
                location = new ResourceLocation(s);
                buildPredicate();
            }
        }).setCurrentString(location.toString()));
        return groups;
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject) {
        jsonObject.add("location", Multiblocked.GSON.toJsonTree(location));
        return super.toJson(jsonObject);
    }
}
