package io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components;

import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.PhantomSlotWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.JsonBlockPatternWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import io.github.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import io.github.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class ControllerWidget extends ComponentWidget{
    protected final JsonBlockPattern pattern;
    protected final WidgetGroup S2;

    public ControllerWidget(JsonBlockPattern pattern, ControllerDefinition definition) {
        super(definition);
        this.pattern = pattern;
        int x = 47;
        SimplePredicate predicate = pattern.predicates.get("controller");
        if (predicate instanceof PredicateComponent) {
            ((PredicateComponent) predicate).definition = definition;
        }
        S1.addWidget(createBoolSwitch(x + 100, 90, "consumeCatalyst", "consume Catalyst", definition.consumeCatalyst, r -> definition.consumeCatalyst = r));
        S1.addWidget(createBoolSwitch(x + 100, 105, "disableOthersRendering", "disable Others Rendering", definition.disableOthersRendering, r -> definition.disableOthersRendering = r));

        IItemHandlerModifiable handler;
        PhantomSlotWidget phantomSlotWidget = new PhantomSlotWidget(handler = new ItemStackHandler(1), 0, x + 205, 73);
        LabelWidget labelWidget = new LabelWidget(x + 230, 78, () -> "Catalyst").setTextColor(-1).setDrop(true);
        S1.addWidget(phantomSlotWidget);
        S1.addWidget(labelWidget);
        phantomSlotWidget.setClearSlotOnRightClick(true)
                .setChangeListener(() -> definition.catalyst = handler.getStackInSlot(0))
                .setBackgroundTexture(new ColorBorderTexture(1, -1))
                .setHoverTooltip("Catalyst, if it is empty then right click can be formed")
                .setVisible(definition.catalyst != null);
        handler.setStackInSlot(0, definition.catalyst);
        labelWidget.setVisible(definition.catalyst != null);

        S1.addWidget(createBoolSwitch(x + 100, 75, "needCatalyst", "If no catalyst is needed, the structure will try to formed per second.", definition.catalyst != null, r -> {
            definition.catalyst = !r ? null : ItemStack.EMPTY;
            phantomSlotWidget.setVisible(r);
            labelWidget.setVisible(r);
        }));

        tabContainer.addTab(new TabButton(65, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S2"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S2"))
                        .setHoverTooltip("Step 2: structure pattern setup"),
                S2 = new WidgetGroup(0, 0, getSize().width, getSize().height));
        TrackedDummyWorld dummyWorld = new TrackedDummyWorld();
//        S2.addWidget(new SceneWidget(50, 50, 200, 200, dummyWorld));
        S2.addWidget(new ButtonWidget(50, 50, 20, 20, new ColorRectTexture(-1), cd -> new JsonBlockPatternWidget(this, pattern, null)));
    }

    @Override
    protected void updateRegistryName(String s) {
        super.updateRegistryName(s);
        SimplePredicate predicate = pattern.predicates.get("controller");
        if (predicate instanceof PredicateComponent) {
            ((PredicateComponent) predicate).location = location;
        }
    }
}
