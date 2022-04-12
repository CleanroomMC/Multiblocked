package com.cleanroommc.multiblocked.api.capability;

import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.ResourceTextureWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class CapabilityTrait {
    public final MultiblockCapability<?> capability;
    protected ComponentTileEntity<?> component;

    protected CapabilityTrait(MultiblockCapability<?> capability) {
        this.capability = capability;
    }

    public void init(JsonObject jsonObject){

    }

    public void setComponent(ComponentTileEntity<?> component) {
        this.component = component;
    }

    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return null;
    }

    public void markAsDirty() {
        if (component != null) {
            component.markAsDirty();
        }
    }

    public void readFromNBT(NBTTagCompound compound) {
    }

    public void writeToNBT(NBTTagCompound compound) {
    }

    public void receiveCustomData(int id, PacketBuffer buffer) {
    }

    public final void writeCustomData(int id, Consumer<PacketBuffer> writer) {
        this.component.writeTraitData(this, id, writer);
    }

    public void createUI(WidgetGroup group, EntityPlayer player) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                group.addWidget(new SlotWidget(player.inventory, col + (row + 1) * 9, 5 + col * 18, 100 + row * 18)
                        .setLocationInfo(true, false));
            }
        }
        for (int slot = 0; slot < 9; slot++) {
            group.addWidget(new SlotWidget(player.inventory, slot, 5 + slot * 18, 158)
                    .setLocationInfo(true, true));
        }
    }

    public Function<JsonObject, JsonObject> getConfigurator(@Nullable JsonObject original, WidgetGroup dialog) {
        int x = (384 - 176) / 2;
        dialog.addWidget(new ImageWidget(0, 0, 384, 256, new ColorRectTexture(0xaf000000)));
        ImageWidget imageWidget;
        dialog.addWidget(imageWidget = new ImageWidget(x, 0, 176, 256, new ResourceTexture("multiblocked:textures/gui/custom_gui.png")));
        if (original != null) {
            imageWidget.setImage(new ResourceTexture(original.get("background").getAsString()));
        }
        dialog.addWidget(new ButtonWidget(x - 20,10, 20, 20, new ResourceTexture("multiblocked:textures/gui/option.png"), cd2 -> {
            new ResourceTextureWidget(dialog, texture -> {
                if (texture != null) {
                    imageWidget.setImage(texture);
                    new ResourceLocation(texture.imageLocation.toString().replace("textures/", "").replace(".png", ""));
                }
            });
        }).setHoverTooltip("set background texture"));
        return jsonObject -> {
            jsonObject.addProperty("background", ((ResourceTexture)imageWidget.getImage()).imageLocation.toString());
            return jsonObject;
        };
    }
}
