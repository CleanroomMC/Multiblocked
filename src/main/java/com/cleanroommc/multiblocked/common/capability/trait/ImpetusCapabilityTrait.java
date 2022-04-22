package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.trait.MultiCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.common.capability.ImpetusThaumicAugmentationCapability;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.BufferedImpetusProsumer;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author youyihj
 */
public class ImpetusCapabilityTrait extends MultiCapabilityTrait {
    private ImpetusStorage storage;
    private BufferedImpetusProsumer node;
    private int capacity;
    private int maxReceive;
    private int maxExtract;
    private int maxInputs;
    private int maxOutputs;
    private double beamEndpointXOffset;
    private double beamEndpointYOffset;
    private double beamEndpointZOffset;

    public ImpetusCapabilityTrait() {
        super(ImpetusThaumicAugmentationCapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement == null) {
            jsonElement = new JsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        capacity = JsonUtils.getInt(jsonObject, "capacity", 300);
        maxReceive = JsonUtils.getInt(jsonObject, "maxReceive", 50);
        maxExtract = JsonUtils.getInt(jsonObject, "maxExtract", 50);
        maxInputs = JsonUtils.getInt(jsonObject, "maxInputs", 1);
        maxOutputs = JsonUtils.getInt(jsonObject, "maxOutputs", 1);
        beamEndpointXOffset = JsonUtils.getFloat(jsonObject, "beamEndpointXOffset", 0.5f);
        beamEndpointYOffset = JsonUtils.getFloat(jsonObject, "beamEndpointYOffset", 0.5f);
        beamEndpointZOffset = JsonUtils.getFloat(jsonObject, "beamEndpointZOffset", 0.5f);
        storage = new ImpetusStorage(capacity, maxReceive, maxExtract);
        node = new BufferedImpetusProsumer(maxInputs, maxOutputs, storage) {
            @Override
            public Vec3d getBeamEndpoint() {
                Vec3d offset = new Vec3d(beamEndpointXOffset, beamEndpointYOffset, beamEndpointZOffset);
                EnumFacing frontFacing = component.getFrontFacing();
                EnumFacing currentFacing = EnumFacing.NORTH;
                while (currentFacing != frontFacing) {
                    currentFacing = currentFacing.rotateYCCW();
                    offset = rotateYCCW(offset);
                }
                return super.getBeamEndpoint().add(offset);
            }
        };
    }

    private static Vec3d rotateYCCW(Vec3d toRotate) {
        double a = toRotate.x - 0.5;
        double b = toRotate.z - 0.5;
        return new Vec3d(b + 0.5, toRotate.y, -a + 0.5);
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = super.deserialize().getAsJsonObject();
        jsonObject.addProperty("capacity", capacity);
        jsonObject.addProperty("maxReceive", maxReceive);
        jsonObject.addProperty("maxExtract", maxExtract);
        jsonObject.addProperty("maxInputs", maxInputs);
        jsonObject.addProperty("maxOutputs", maxOutputs);
        jsonObject.addProperty("beamEndpointXOffset", beamEndpointXOffset);
        jsonObject.addProperty("beamEndpointYOffset", beamEndpointYOffset);
        jsonObject.addProperty("beamEndpointZOffset", beamEndpointZOffset);
        return jsonObject;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("impetus")) {
            NBTTagCompound impetus = compound.getCompoundTag("impetus");
            node.deserializeNBT(impetus);
            storage.deserializeNBT(impetus);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagCompound nbt = node.serializeNBT();
        nbt.merge(storage.serializeNBT());
        compound.setTag("impetus", nbt);
    }

    @Override
    public void onLoad() {
        node.setLocation(new DimensionalBlockPos(component.getPos(), component.getWorld().provider.getDimension()));
        node.init(component.getWorld());
    }

    @Override
    public void onChunkUnload() {
        node.unload();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(node);
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        World world = component.getWorld();
        BlockPos pos = component.getPos();
        ThaumicAugmentation.proxy.getRenderHelper().renderSpark(world, pos.getX() + world.rand.nextFloat(),
                pos.getY() + world.rand.nextFloat(), pos.getZ() + world.rand.nextFloat(), 1.5F, Aspect.ELDRITCH.getColor(), false);

        return true;
    }

    @Override
    public void createUI(ComponentTileEntity<?> component, WidgetGroup group, EntityPlayer player) {
        super.createUI(component, group, player);
        group.addWidget(new LabelWidget(100, 80, () ->
            String.format("Impetus Stored: %d / %d", storage.getEnergyStored(), storage.getMaxEnergyStored())
        ));
    }

    @Override
    public void invalidate() {
        if (component.getWorld().isRemote) {
            NodeHelper.syncDestroyedImpetusNode(node);
        }
        node.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(node);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE) {
            return CapabilityImpetusNode.IMPETUS_NODE.cast(node);
        }
        if (capability == CapabilityImpetusStorage.IMPETUS_STORAGE) {
            return CapabilityImpetusStorage.IMPETUS_STORAGE.cast(storage);
        }
        return super.getCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getInnerCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing);
    }
}
