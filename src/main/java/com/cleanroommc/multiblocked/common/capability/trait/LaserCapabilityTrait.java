package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.trait.InterfaceUser;
import com.cleanroommc.multiblocked.api.capability.trait.ProgressCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;
import com.cleanroommc.multiblocked.common.capability.LaserMekanismCapability;
import com.cleanroommc.multiblocked.common.capability.ManaBotaniaCapability;
import com.google.common.base.Predicates;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mekanism.api.lasers.ILaserReceptor;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.AxisAlignedBB;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.common.block.tile.mana.TilePool;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author WaitingIdly
 */
@InterfaceUser(ILaserReceptor.class)
public class LaserCapabilityTrait extends ProgressCapabilityTrait implements ILaserReceptor {
    private double energy;
    private double capacity;

    public LaserCapabilityTrait() {
        super(LaserMekanismCapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement == null) {
            jsonElement = new JsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        capacity = jsonObject.has("capacity") ? jsonObject.get("capacity").getAsDouble() : Double.MAX_VALUE;
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = super.deserialize().getAsJsonObject();
        jsonObject.addProperty("capacity", capacity);
        return jsonObject;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setDouble("energy", energy);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        energy = compound.getDouble("energy");
    }

    @Override
    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot) {
        super.initSettingDialog(dialog, slot);
        dialog.addWidget(new TextFieldWidget(60, 5, 100, 15, true, null, s -> capacity = Double.parseDouble(s))
                .setNumbersOnly(1L, (long) Double.MAX_VALUE)
                .setCurrentString(capacity + "")
                .setHoverTooltip("multiblocked.gui.trait.laser.tips.0"));
    }

    @Override
    protected String dynamicHoverTips(double progress) {
        return String.format("Mana: %f/%f", progress * capacity, capacity);
    }

    @Override
    protected double getProgress() {
        return energy / capacity;
    }

    public void setEnergy(double energy) {
        this.energy = Math.max(0.0D, Math.min(energy, capacity));
    }

    public double getEnergy() {
        return energy;
    }

    @Override
    public void receiveLaserEnergy(double v, EnumFacing enumFacing) {
        setEnergy(getEnergy() + v);
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }
}
