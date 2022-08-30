package com.cleanroommc.multiblocked.api.capability.trait;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
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
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.ResourceTextureWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ProgressWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ProgressWidget.FillDirection;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.util.JsonUtil;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.JsonUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class ProgressCapabilityTrait extends SingleCapabilityTrait {

    protected int width;
    protected int height;
    protected String texture;
    protected FillDirection fillDirection;

    public ProgressCapabilityTrait(MultiblockCapability<?> capability) {
        super(capability);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement == null) {
            jsonElement = new JsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        width = JsonUtils.getInt(jsonObject, "width", 60);
        height = JsonUtils.getInt(jsonObject, "height", 18);
        texture = JsonUtils.getString(jsonObject, "texture", "multiblocked:textures/gui/energy_bar.png");
        fillDirection = JsonUtil.getEnumOr(jsonObject, "fillDirection", FillDirection.class, FillDirection.LEFT_TO_RIGHT);
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = super.deserialize().getAsJsonObject();
        jsonObject.addProperty("width", width);
        jsonObject.addProperty("height", height);
        jsonObject.addProperty("texture", texture);
        jsonObject.addProperty("fillDirection", fillDirection.ordinal());
        return jsonObject;
    }

    protected abstract String dynamicHoverTips(double progress);

    protected abstract double getProgress();
    
    @Override
    public void createUI(ComponentTileEntity<?> component, WidgetGroup group, EntityPlayer player) {
        super.createUI(component, group, player);
        group.addWidget(new ProgressWidget(
                this::getProgress,
                x, y, width, height,
                new ResourceTexture(texture))
                .setDynamicHoverTips(this::dynamicHoverTips)
                .setFillDirection(fillDirection));
    }

    protected void refreshSlots(DraggableScrollableWidgetGroup dragGroup) {
        dragGroup.widgets.forEach(dragGroup::waitToRemoved);
        ButtonWidget setting = (ButtonWidget) new ButtonWidget(width - 8, 0, 8, 8, new ResourceTexture("multiblocked:textures/gui/option.png"), null).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.settings");
        ImageWidget imageWidget = new ImageWidget(0, 0, width, height, new GuiTextureGroup(new ResourceTexture(texture).getSubTexture(0, 0, 1, 0.5), new ColorBorderTexture(1, getColorByIO(capabilityIO))));
        setting.setVisible(false);
        DraggableWidgetGroup slot = new DraggableWidgetGroup(x, y, width, height);
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

    @Override
    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot) {
        ImageWidget imageWidget = (ImageWidget) slot.widgets.get(0);
        ButtonWidget setting = (ButtonWidget) slot.widgets.get(1);
        ButtonWidget imageSelector = (ButtonWidget) new ButtonWidget(5, 85, width, height, new GuiTextureGroup(new ColorBorderTexture(1, -1), new ResourceTexture(texture).getSubTexture(0, 0, 1, 0.5)), null)
                .setHoverTooltip("multiblocked.gui.tips.select_image");
        dialog.addWidget(new TextFieldWidget(5, 25, 50, 15, true, null, s -> {
            width = Integer.parseInt(s);
            Size size = new Size(width, height);
            slot.setSize(size);
            imageWidget.setSize(size);
            imageSelector.setSize(size);
            setting.setSelfPosition(new Position(width - 8, 0));
        }).setCurrentString(width + "").setNumbersOnly(1, 180).setHoverTooltip("multiblocked.gui.trait.set_width"));
        dialog.addWidget(new TextFieldWidget(5, 45, 50, 15, true, null, s -> {
            height = Integer.parseInt(s);
            Size size = new Size(width, height);
            slot.setSize(size);
            imageWidget.setSize(size);
            imageSelector.setSize(size);
            setting.setSelfPosition(new Position(width - 8, 0));
        }).setCurrentString(height + "").setNumbersOnly(1, 180).setHoverTooltip("multiblocked.gui.trait.set_height"));
        dialog.addWidget(new SelectorWidget(5, 5, 50, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(
                Collectors.toList()), -1)
                .setValue(capabilityIO.name())
                .setOnChanged(io-> {
                    capabilityIO = IO.valueOf(io);
                    imageWidget.setImage(new GuiTextureGroup(new ResourceTexture(texture).getSubTexture(0, 0, 1, 0.5), new ColorBorderTexture(1, getColorByIO(capabilityIO))));
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("multiblocked.gui.trait.capability_io"));

        dialog.addWidget(imageSelector);
        dialog.addWidget(new SelectorWidget(5, 65, 60, 15, Arrays.stream(FillDirection.VALUES).map(Enum::name).collect(Collectors.toList()), -1)
                .setValue(fillDirection.name())
                .setOnChanged(io -> fillDirection = FillDirection.valueOf(io))
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("multiblocked.gui.trait.fill_direction"));
        imageSelector.setOnPressCallback(cd -> new ResourceTextureWidget((WidgetGroup) dialog.parent.getGui().guiWidgets.get(0), texture1 -> {
            if (texture1 != null) {
                texture = texture1.imageLocation.toString();
                ResourceTexture resourceTexture = new ResourceTexture(texture).getSubTexture(0, 0, 1, 0.5);
                imageSelector.setButtonTexture(new GuiTextureGroup(new ColorBorderTexture(1, -1), resourceTexture));
                imageWidget.setImage(new GuiTextureGroup(resourceTexture, new ColorBorderTexture(1, getColorByIO(capabilityIO))));
            }
        }));
    }

}
