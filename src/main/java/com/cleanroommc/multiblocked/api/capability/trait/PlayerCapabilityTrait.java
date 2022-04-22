package com.cleanroommc.multiblocked.api.capability.trait;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
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
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.util.JsonUtil;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JsonUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class PlayerCapabilityTrait extends CapabilityTrait {
    protected String playerName = "";
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected TextTexture.TextType textType;

    public PlayerCapabilityTrait(MultiblockCapability<?> capability) {
        super(capability);
    }

    public String getPlayerName() {
        return playerName;
    }

    @Nullable
    public EntityPlayer getPlayer() {
        return component.getOwner();
    }

    @Override
    public void setComponent(ComponentTileEntity<?> component) {
        super.setComponent(component);
        if (component != null && component.getOwner() != null) {
            playerName = component.getOwner().getName();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        playerName = compound.getString("player");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("player", playerName);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        if (jsonElement == null) {
            jsonElement = new JsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        x = JsonUtils.getInt(jsonObject, "x", 5);
        y = JsonUtils.getInt(jsonObject, "y", 5);
        width = JsonUtils.getInt(jsonObject, "width", 60);
        height = JsonUtils.getInt(jsonObject, "height", 18);
        textType = JsonUtil.getEnumOr(jsonObject, "textType", TextTexture.TextType.class, TextTexture.TextType.LEFT);
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", x);
        jsonObject.addProperty("y", y);
        jsonObject.addProperty("width", width);
        jsonObject.addProperty("height", height);
        jsonObject.addProperty("textType", textType.ordinal());
        return jsonObject;
    }

    @Override
    public void createUI(ComponentTileEntity<?> component, WidgetGroup group, EntityPlayer player) {
        super.createUI(component, group, player);
        group.addWidget(new ImageWidget(x, y, width, height, new TextTexture("").setSupplier(()->playerName).setWidth(width).setType(textType)) {
            @Override
            public void writeInitialData(PacketBuffer buffer) {
                super.writeInitialData(buffer);
                buffer.writeString(playerName);
            }

            @Override
            public void readInitialData(PacketBuffer buffer) {
                super.readInitialData(buffer);
                playerName = buffer.readString(Short.MAX_VALUE);
            }
        });
    }

    protected void refreshSlots(DraggableScrollableWidgetGroup dragGroup) {
        dragGroup.widgets.forEach(dragGroup::waitToRemoved);
        ButtonWidget setting = (ButtonWidget) new ButtonWidget(width - 8, 0, 8, 8, new ResourceTexture("multiblocked:textures/gui/option.png"), null).setHoverBorderTexture(1, -1).setHoverTooltip("settings");
        ImageWidget imageWidget = new ImageWidget(0, 0, width, height, new TextTexture("Player Name").setBackgroundColor(0xff000000));
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

    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot) {
        ImageWidget imageWidget = (ImageWidget) slot.widgets.get(0);
        ButtonWidget setting = (ButtonWidget) slot.widgets.get(1);
        dialog.addWidget(new TextFieldWidget(5, 25, 50, 15, true, null, s -> {
            width = Integer.parseInt(s);
            Size size = new Size(width, height);
            slot.setSize(size);
            imageWidget.setSize(size);
            ((TextTexture)imageWidget.getImage()).setWidth(width);
            setting.setSelfPosition(new Position(width - 8, 0));
        }).setCurrentString(width + "").setNumbersOnly(10, 180).setHoverTooltip("set width"));
        dialog.addWidget(new TextFieldWidget(5, 45, 50, 15, true, null, s -> {
            height = Integer.parseInt(s);
            Size size = new Size(width, height);
            slot.setSize(size);
            imageWidget.setSize(size);
            setting.setSelfPosition(new Position(width - 8, 0));
        }).setCurrentString(height + "").setNumbersOnly(10, 180).setHoverTooltip("set height"));
        dialog.addWidget(new SelectorWidget(5, 5, 50, 15, Arrays.stream(IO.VALUES).map(Enum::name).collect(
                Collectors.toList()), -1)
                .setValue(textType.name())
                .setOnChanged(io-> {
                    textType = TextTexture.TextType.valueOf(io);
                    ((TextTexture)imageWidget.getImage()).setType(textType);
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("TextType"));
    }

    @Override
    public void openConfigurator(WidgetGroup parentDialog) {
        DraggableScrollableWidgetGroup dragGroup = new DraggableScrollableWidgetGroup((384 - 176) / 2, 0, 176, 256);
        parentDialog.addWidget(dragGroup);
        refreshSlots(dragGroup);
    }

}
