package com.cleanroommc.multiblocked.api.capability.trait;

import com.cleanroommc.multiblocked.api.block.CustomProperties;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import com.cleanroommc.multiblocked.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class MultiCapabilityTrait extends CapabilityTrait {
    protected IO[] capabilityIO;
    protected IO[] guiIO;
    protected int[] x;
    protected int[] y;
    protected boolean[] autoIO;

    public MultiCapabilityTrait(MultiblockCapability<?> capability) {
        super(capability);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        if (jsonElement == null) {
            jsonElement = new JsonArray();
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        int size = jsonArray.size();
        capabilityIO = new IO[size];
        guiIO = new IO[size];
        x = new int[size];
        y = new int[size];
        autoIO = new boolean[size];
        int i = 0;
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            capabilityIO[i] = JsonUtil.getEnumOr(jsonObject, "cIO", IO.class, IO.BOTH);
            guiIO[i] = JsonUtil.getEnumOr(jsonObject, "gIO", IO.class, IO.BOTH);
            x[i] = JsonUtils.getInt(jsonObject, "x", 5);
            y[i] = JsonUtils.getInt(jsonObject, "y", 5);
            autoIO[i] = JsonUtils.getBoolean(jsonObject, "autoIO", false);
            i++;
        }
    }

    @Override
    public JsonElement deserialize() {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < capabilityIO.length; i++) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("cIO", capabilityIO[i].ordinal());
            jsonObject.addProperty("gIO", guiIO[i].ordinal());
            jsonObject.addProperty("x", x[i]);
            jsonObject.addProperty("y", y[i]);
            jsonObject.addProperty("autoIO", autoIO[i]);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public EnumFacing[] getIOFacing() {
        if (component.getDefinition().properties.rotationState != CustomProperties.RotationState.NONE) {
            return new EnumFacing[]{component.getFrontFacing()};
        }
        return EnumFacing.VALUES;
    }

    protected int getColorByIO(IO io) {
        return io == IO.IN ? 0xaf00ff00 : io == IO.OUT ? 0xafff0000 : 0xaf0000ff;
    }

    protected void refreshSlots(DraggableScrollableWidgetGroup dragGroup) {
        dragGroup.widgets.forEach(dragGroup::waitToRemoved);
        for (int i = 0; i < guiIO.length; i++) {
            int finalI = i;
            ButtonWidget setting =
                    (ButtonWidget) new ButtonWidget(10, 0, 8, 8, new ResourceTexture("multiblocked:textures/gui/option.png"), null).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.settings");
            ImageWidget imageWidget = new ImageWidget(1, 1, 16, 16, new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO[finalI])), new ColorBorderTexture(1, getColorByIO(capabilityIO[finalI]))));
            setting.setVisible(false);
            DraggableWidgetGroup slot = new DraggableWidgetGroup(x[finalI], y[finalI], 18, 18);
            slot.setOnSelected(w -> setting.setVisible(true));
            slot.setOnUnSelected(w -> setting.setVisible(false));
            slot.addWidget(imageWidget);
            slot.addWidget(setting);
            slot.setOnEndDrag(b -> {
                x[finalI] = b.getSelfPosition().x;
                y[finalI] = b.getSelfPosition().y;
            });
            dragGroup.addWidget(slot);

            setting.setOnPressCallback(cd2 -> {
                DialogWidget dialog = new DialogWidget(dragGroup, true);
                dialog.addWidget(new ImageWidget(0, 0, 176, 256, new ColorRectTexture(0xaf000000)));
                dialog.addWidget(new ButtonWidget(5, 5, 85, 20, new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("multiblocked.gui.trait.remove_slot")), cd3 -> {
                    removeSlot(finalI);
                    refreshSlots(dragGroup);
                    dialog.close();
                }).setHoverBorderTexture(1, -1));
                initSettingDialog(dialog, slot, finalI);
            });
        }
    }

    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot, final int index) {
        ImageWidget imageWidget = (ImageWidget) slot.widgets.get(0);
        dialog.addWidget(new SelectorWidget(5, 30, 40, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(Collectors.toList()), -1)
                .setValue(capabilityIO[index].name())
                .setOnChanged(io-> {
                    capabilityIO[index] = IO.valueOf(io);
                    imageWidget.setImage(new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO[index])), new ColorBorderTexture(1, getColorByIO(capabilityIO[index]))));
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("multiblocked.gui.trait.capability_io"));
        dialog.addWidget(new SelectorWidget(50, 30, 40, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(Collectors.toList()), -1)
                .setValue(guiIO[index].name())
                .setOnChanged(io-> {
                    guiIO[index] = IO.valueOf(io);
                    imageWidget.setImage(new GuiTextureGroup(new ColorRectTexture(getColorByIO(guiIO[index])), new ColorBorderTexture(1, getColorByIO(capabilityIO[index]))));
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("multiblocked.gui.trait.gui_io"));
        dialog.addWidget(new SelectorWidget(100, 30, 65, 15, Arrays.asList("multiblocked.gui.trait.auto_io", "multiblocked.gui.trait.passive_io"), -1)
                .setValue(autoIO[index] ? "multiblocked.gui.trait.auto_io" : "multiblocked.gui.trait.passive_io")
                .setOnChanged(auto -> autoIO[index] = auto.equals("multiblocked.gui.trait.auto_io"))
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("multiblocked.gui.trait.auto"));
    }
    
    protected void addSlot() {
        capabilityIO = ArrayUtils.add(capabilityIO, IO.BOTH);
        guiIO = ArrayUtils.add(guiIO, IO.BOTH);
        autoIO = ArrayUtils.add(autoIO, false);
        x = ArrayUtils.add(x, 5);
        y = ArrayUtils.add(y, 5);
    }

    protected void removeSlot(int index) {
        capabilityIO = ArrayUtils.remove(capabilityIO, index);
        guiIO = ArrayUtils.remove(guiIO, index);
        autoIO = ArrayUtils.remove(autoIO, index);
        x = ArrayUtils.remove(x, index);
        y = ArrayUtils.remove(y, index);
    }

    @Override
    public void openConfigurator(WidgetGroup parentDialog) {
        DraggableScrollableWidgetGroup dragGroup = new DraggableScrollableWidgetGroup((384 - 176) / 2, 0, 176, 256);
        parentDialog.addWidget(dragGroup);
        refreshSlots(dragGroup);
        // add new slot
        parentDialog.addWidget(new ButtonWidget((384 - 176) / 2 -20,35, 20, 20, new ResourceTexture("multiblocked:textures/gui/add.png"), cd -> {
            addSlot();
            refreshSlots(dragGroup);
        }).setHoverTooltip("multiblocked.gui.trait.add_slot"));
    }

}
