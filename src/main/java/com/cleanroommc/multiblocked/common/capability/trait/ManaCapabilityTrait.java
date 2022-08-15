package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.trait.InterfaceUser;
import com.cleanroommc.multiblocked.api.capability.trait.ProgressCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;
import com.cleanroommc.multiblocked.common.capability.ManaBotaniaCapability;
import com.google.common.base.Predicates;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.AxisAlignedBB;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.common.block.tile.mana.TilePool;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author youyihj
 */
@InterfaceUser(ISparkAttachable.class)
public class ManaCapabilityTrait extends ProgressCapabilityTrait implements ISparkAttachable {
    private int mana;
    private int capacity;
    private boolean allowSpark;

    public ManaCapabilityTrait() {
        super(ManaBotaniaCapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement == null) {
            jsonElement = new JsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        capacity = JsonUtils.getInt(jsonObject, "capacity", TilePool.MAX_MANA);
        allowSpark = JsonUtils.getBoolean(jsonObject, "allowSpark", false);
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = super.deserialize().getAsJsonObject();
        jsonObject.addProperty("capacity", capacity);
        jsonObject.addProperty("allowSpark", allowSpark);
        return jsonObject;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("mana", mana);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        mana = compound.getInteger("mana");
    }

    @Override
    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot) {
        super.initSettingDialog(dialog, slot);
        dialog.addWidget(new TextFieldWidget(60, 5, 100, 15, true, null, s -> capacity = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(capacity + "")
                .setHoverTooltip("multiblocked.gui.trait.mana.tips.0"));
        dialog.addWidget(new SwitchWidget(60, 25, 15, 15, ((clickData, bool) -> allowSpark = bool))
                .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0,1,0.5))
                .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0.5,1,0.5))
                .setHoverTexture(new ColorBorderTexture(1, 0xff545757))
                .setPressed(allowSpark)
                .setHoverTooltip("multiblocked.gui.trait.mana.tips.1"));
        dialog.addWidget(new LabelWidget(80, 25, "allowSpark"));
    }

    @Override
    public boolean isFull() {
        return this.getCurrentMana() >= capacity;
    }

    @Override
    public void recieveMana(int i) {
        mana = Math.max(0, Math.min(this.getCurrentMana() + i, capacity));
    }

    @Override
    public boolean canRecieveManaFromBursts() {
        return true;
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    protected String dynamicHoverTips(double progress) {
        return String.format("Mana: %d/%d", ((int) (progress * capacity)), capacity);
    }

    @Override
    protected double getProgress() {
        return ((double) this.getCurrentMana()) / capacity;
    }

    @Override
    public boolean canAttachSpark(ItemStack itemStack) {
        return allowSpark;
    }

    @Override
    public void attachSpark(ISparkEntity iSparkEntity) {

    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, capacity - this.getCurrentMana());
    }

    @Override
    public ISparkEntity getAttachedSpark() {
        List<Entity> sparks = component.getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(component.getPos().up(), component.getPos().up().add(1, 1, 1)), Predicates.instanceOf(ISparkEntity.class));
        if (sparks.size() == 1) {
            return (ISparkEntity) sparks.get(0);
        } else {
            return null;
        }
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return false;
    }
}
