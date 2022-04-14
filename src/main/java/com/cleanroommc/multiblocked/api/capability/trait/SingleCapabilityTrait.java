package com.cleanroommc.multiblocked.api.capability.trait;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import com.cleanroommc.multiblocked.util.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class SingleCapabilityTrait extends CapabilityTrait {
    protected IO capabilityIO;
    protected IO guiIO;
    protected int x;
    protected int y;

    public SingleCapabilityTrait(MultiblockCapability<?> capability) {
        super(capability);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        if (jsonElement == null) {
            jsonElement = new JsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        capabilityIO = JsonUtil.getEnumOr(jsonObject, "cIO", IO.class, IO.BOTH);
        guiIO = JsonUtil.getEnumOr(jsonObject, "gIO", IO.class, IO.BOTH);
        x = JsonUtils.getInt(jsonObject, "x", 5);
        y = JsonUtils.getInt(jsonObject, "y", 5);
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cIO", capabilityIO.ordinal());
        jsonObject.addProperty("gIO", guiIO.ordinal());
        jsonObject.addProperty("x", x);
        jsonObject.addProperty("y", y);
        return jsonObject;
    }

    protected int getColorByIO(IO io) {
        return io == IO.IN ? 0xaf00ff00 : io == IO.OUT ? 0xafff0000 : 0xaf0000ff;
    }

    protected void refreshSlots(DraggableScrollableWidgetGroup dragGroup) {
        dragGroup.widgets.forEach(dragGroup::waitToRemoved);
        ButtonWidget setting = (ButtonWidget) new ButtonWidget(10, 0, 8, 8, new ResourceTexture("multiblocked:textures/gui/option.png"), null).setHoverBorderTexture(1, -1).setHoverTooltip("settings");
        ImageWidget imageWidget = new ImageWidget(1, 1, 16, 16, new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO)), new ColorBorderTexture(1, getColorByIO(capabilityIO))));
        setting.setVisible(false);
        DraggableWidgetGroup slot = new DraggableWidgetGroup(x, y, 18, 18);
        slot.setOnSelected(w -> setting.setVisible(true));
        slot.setOnUnSelected(w -> setting.setVisible(false));
        slot.addWidget(imageWidget);
        slot.addWidget(setting);
        slot.setOnEndDrag(b -> {
            x = b.getSelfPosition().x;
            y = b.getSelfPosition().y;
        });
        dragGroup.addWidget(slot);

        setting.setOnPressCallback(cd2 -> {
            DialogWidget dialog = new DialogWidget(dragGroup, true);
            dialog.addWidget(new ImageWidget(0, 0, 176, 256, new ColorRectTexture(0xaf000000)));
            initSettingDialog(dialog, slot);
        });
    }

    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot) {
        ImageWidget imageWidget = (ImageWidget) slot.widgets.get(0);
        dialog.addWidget(new SelectorWidget(5, 5, 40, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(Collectors.toList()), -1)
                .setValue(capabilityIO.name())
                .setOnChanged(io-> {
                    capabilityIO = IO.valueOf(io);
                    imageWidget.setImage(new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO)), new ColorBorderTexture(1, getColorByIO(capabilityIO))));
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("Capability IO (e.g., pipe interaction)"));
        dialog.addWidget(new SelectorWidget(50, 5, 40, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(Collectors.toList()), -1)
                .setValue(guiIO.name())
                .setOnChanged(io-> {
                    guiIO = IO.valueOf(io);
                    imageWidget.setImage(new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO)), new ColorBorderTexture(1, getColorByIO(capabilityIO))));
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("Gui IO (e.g. gui interaction)"));
    }

    @Override
    public void openConfigurator(WidgetGroup parentDialog) {
        DraggableScrollableWidgetGroup dragGroup = new DraggableScrollableWidgetGroup((384 - 176) / 2, 0, 176, 256);
        parentDialog.addWidget(dragGroup);
        refreshSlots(dragGroup);
    }

}
