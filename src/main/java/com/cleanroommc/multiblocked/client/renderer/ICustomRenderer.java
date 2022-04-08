package com.cleanroommc.multiblocked.client.renderer;

import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Customizable renderer that supports serialization and visual configuration
 */
public interface ICustomRenderer extends IRenderer {
    /**
     * unique type id.
     */
    String getType();

    default String getUnlocalizedName() {
        return "multiblocked.renderer." + getType();
    }

    /**
     * deserialize.
     */
    IRenderer fromJson(Gson gson, JsonObject jsonObject);

    /**
     * serialize.
     */
    JsonObject toJson(Gson gson, JsonObject jsonObject);

    /**
     * configurator.
     * @param group group widget.
     * @param current current renderer.
     * @return called when updated.
     */
    default Supplier<IRenderer> createConfigurator(WidgetGroup parent, DraggableScrollableWidgetGroup group, IRenderer current) {
        group.addWidget(new LabelWidget(5,5,"no configurator"));
        return null;
    }

    default WidgetGroup createBoolSwitch(int x, int y, String text, String tips, boolean init, Consumer<Boolean> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new SwitchWidget(0, 0, 15, 15, (cd, r)->onPressed.accept(r))
                .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0,1,0.5))
                .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0.5,1,0.5))
                .setHoverTexture(new ColorBorderTexture(1, 0xff545757))
                .setPressed(init)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(20, 3, ()->text).setTextColor(-1).setDrop(true));
        return widgetGroup;
    }
}
