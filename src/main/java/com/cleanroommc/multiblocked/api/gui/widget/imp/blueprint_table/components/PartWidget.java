package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components;

import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class PartWidget extends ComponentWidget<PartDefinition>{
    protected JsonBlockPattern pattern;

    public PartWidget(WidgetGroup group, PartDefinition definition, Consumer<JsonObject> onSave) {
        super(group, definition, onSave);
        int x = 47;
        S1.addWidget(createBoolSwitch(x + 100, 90, "canShared", "can be shared with different multiblocks", definition.canShared, r -> definition.canShared = r));

    }
}
