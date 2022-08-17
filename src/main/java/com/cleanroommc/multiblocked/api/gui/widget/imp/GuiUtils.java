package com.cleanroommc.multiblocked.api.gui.widget.imp;

import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/8/16
 * @implNote GuiUtils
 */
public class GuiUtils {

    public static WidgetGroup createSelector(int x, int y, String text, String tips, String init, List<String> candidates, Consumer<String> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new SelectorWidget(0, 0, 65, 15, candidates, -1)
                .setValue(init)
                .setOnChanged(onPressed)
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xff333333))
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(70, 3, text));
        return widgetGroup;
    }

    public static WidgetGroup createBoolSwitch(int x, int y, String text, String tips, boolean init, Consumer<Boolean> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new SwitchWidget(0, 0, 15, 15, (cd, r)->onPressed.accept(r))
                .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0,1,0.5))
                .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0.5,1,0.5))
                .setHoverTexture(new ColorBorderTexture(1, 0xff545757))
                .setPressed(init)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(20, 3, text));
        return widgetGroup;
    }

    public static WidgetGroup createIntField(int x, int y, String text, String tips, int init, int min, int max, Consumer<Integer> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new TextFieldWidget(0, 2, 30, 10, true, null, s -> onPressed.accept(Integer.parseInt(s)))
                .setCurrentString(init + "")
                .setNumbersOnly(min, max)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(35, 3, text));
        return widgetGroup;
    }

    public static WidgetGroup createFloatField(int x, int y, String text, String tips, float init, float min, float max, Consumer<Float> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new TextFieldWidget(0, 2, 30, 10, true, null, s -> onPressed.accept(Float.parseFloat(s)))
                .setCurrentString(init + "")
                .setNumbersOnly(min, max)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(35, 3, text));
        return widgetGroup;
    }

    public static WidgetGroup createStringField(int x, int y, String text, String tips, String init, Consumer<String> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new TextFieldWidget(0, 2, 60, 10, true, null, onPressed)
                .setCurrentString(init)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(65, 3, text));
        return widgetGroup;
    }
}
