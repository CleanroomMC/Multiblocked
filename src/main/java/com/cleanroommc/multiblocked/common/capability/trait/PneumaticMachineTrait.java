package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.trait.InterfaceUser;
import com.cleanroommc.multiblocked.api.capability.trait.SingleCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.common.capability.PneumaticPressureCapability;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.block.tubes.IPneumaticPosProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * @author youyihj
 */
@InterfaceUser(IPneumaticPosProvider.class)
public class PneumaticMachineTrait extends SingleCapabilityTrait implements IPneumaticPosProvider {
    private IAirHandler airHandler;
    public float dangerPressure;
    public float criticalPressure;
    public int volume;

    public PneumaticMachineTrait() {
        super(PneumaticPressureCapability.CAP);
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = super.deserialize().getAsJsonObject();
        jsonObject.addProperty("dangerPressure", dangerPressure);
        jsonObject.addProperty("criticalPressure", criticalPressure);
        jsonObject.addProperty("volume", volume);
        return jsonObject;
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement == null) {
            jsonElement = new JsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        dangerPressure = JsonUtils.getFloat(jsonObject, "dangerPressure", 5.0f);
        criticalPressure = JsonUtils.getFloat(jsonObject, "criticalPressure", 7.0f);
        volume = JsonUtils.getInt(jsonObject, "volume", 10000);
        airHandler = PneumaticCraftAPIHandler.getInstance().getAirHandlerSupplier().createAirHandler(dangerPressure, criticalPressure, volume);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        airHandler.readFromNBT(compound);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        airHandler.writeToNBT(compound);
    }

    @Override
    public IAirHandler getAirHandler(EnumFacing enumFacing) {
        return airHandler;
    }

    @Override
    public World world() {
        return component.getWorld();
    }

    @Override
    public BlockPos pos() {
        return component.getPos();
    }

    @Override
    public void validate() {
        airHandler.validate(component);
    }

    @Override
    public void update() {
        airHandler.update();
    }

    @Override
    public boolean hasUpdate() {
        return true;
    }

    @Override
    public void createUI(ComponentTileEntity<?> component, WidgetGroup group, EntityPlayer player) {
        super.createUI(component, group, player);
        group.addWidget(new PressureWidget(x, y, dangerPressure, criticalPressure, airHandler.getPressure()));
    }

    @Override
    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot) {
        super.initSettingDialog(dialog, slot);

        dialog.addWidget(new TextFieldWidget(60, 25, 100, 15, true, null, s -> {
            dangerPressure = Float.parseFloat(s);
            updateSettings();
        })
                .setNumbersOnly(0.0f, Float.MAX_VALUE)
                .setCurrentString(dangerPressure + "")
                .setHoverTooltip("multiblocked.gui.trait.pressure.tips.0"));
        dialog.addWidget(new TextFieldWidget(60, 45, 100, 15, true, null, s -> {
            criticalPressure = Float.parseFloat(s);
            updateSettings();
        })
                .setNumbersOnly(0.0f, Float.MAX_VALUE)
                .setCurrentString(criticalPressure + "")
                .setHoverTooltip("multiblocked.gui.trait.pressure.tips.1"));
        dialog.addWidget(new TextFieldWidget(60, 65, 100, 15, true, null, s -> {
            volume = Integer.parseInt(s);
            updateSettings();
        })
                .setNumbersOnly(0, Integer.MAX_VALUE)
                .setCurrentString(volume + "")
                .setHoverTooltip("multiblocked.gui.trait.pressure.tips.2"));
    }

    @Override
    public void onNeighborChanged() {
        airHandler.onNeighborChange();
    }

    public void updateSettings() {
        int air = airHandler.getAir();
        airHandler = PneumaticCraftAPIHandler.getInstance().getAirHandlerSupplier().createAirHandler(dangerPressure, criticalPressure, volume);
        airHandler.addAir(air);
        airHandler.validate(component);
    }

    public class PressureWidget extends Widget {

        private float dangerPressure;
        private float criticalPressure;
        private float pressure;

        public PressureWidget(int xPosition, int yPosition, float dangerPressure, float criticalPressure, float pressure) {
            super(new Position(xPosition, yPosition), new Size(44, 44));
            this.dangerPressure = dangerPressure;
            this.criticalPressure = criticalPressure;
            this.pressure = pressure;
        }

        @Override
        public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
            GuiUtils.drawPressureGauge(Minecraft.getMinecraft().fontRenderer, -1.0f, criticalPressure, dangerPressure,  -3.4028235E38F, pressure, getPosition().x, getPosition().y, 0);
        }

        @Override
        public void detectAndSendChanges() {
            if (PneumaticMachineTrait.this.dangerPressure - dangerPressure != 0) {
                dangerPressure = PneumaticMachineTrait.this.dangerPressure;
                writeUpdateInfo(0, buffer -> buffer.writeFloat(dangerPressure));
            }
            if (PneumaticMachineTrait.this.criticalPressure - criticalPressure != 0) {
                criticalPressure = PneumaticMachineTrait.this.criticalPressure;
                writeUpdateInfo(1, buffer -> buffer.writeFloat(criticalPressure));
            }
            if (airHandler.getPressure() - pressure != 0) {
                pressure = airHandler.getPressure();
                writeUpdateInfo(2, buffer -> buffer.writeFloat(pressure));
            }
        }

        @Override
        public void readUpdateInfo(int id, PacketBuffer buffer) {
            float value = buffer.readFloat();
            switch (id) {
                case 0:
                    dangerPressure = value;
                    break;
                case 1:
                    criticalPressure = value;
                    break;
                case 2:
                    pressure = value;
                    break;
            }
        }
    }
}
