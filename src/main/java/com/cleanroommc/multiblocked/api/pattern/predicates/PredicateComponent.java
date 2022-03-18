package com.cleanroommc.multiblocked.api.pattern.predicates;

import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.google.gson.JsonObject;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.block.BlockComponent;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class PredicateComponent extends SimplePredicate {
    public ResourceLocation location = new ResourceLocation("mod_id", "component_id");
    public transient ComponentDefinition definition;

    public PredicateComponent() {
        super("component");
    }

    public PredicateComponent(ComponentDefinition definition) {
        this(definition.location);
        this.definition = definition;
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
        candidates = () -> {
            if (MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.containsKey(location)) {
                return new BlockInfo[]{new BlockInfo(MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(location).getDefaultState(), MultiblockComponents.DEFINITION_REGISTRY.get(location).createNewTileEntity(null))};
            } else {
                if (definition == null) return new BlockInfo[0];
                DummyComponentTileEntity te = new DummyComponentTileEntity();
                te.setDefinition(definition);
                return new BlockInfo[]{new BlockInfo(MultiblockComponents.DummyComponentBlock.getDefaultState(), te)};
            }
        };
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
