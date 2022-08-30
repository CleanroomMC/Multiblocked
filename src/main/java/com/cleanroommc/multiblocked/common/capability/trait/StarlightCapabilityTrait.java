package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.trait.InterfaceUser;
import com.cleanroommc.multiblocked.api.capability.trait.ProgressCapabilityTrait;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ProgressWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.common.capability.StarlightAstralCapability;
import com.cleanroommc.multiblocked.common.capability.StarlightAstralCapability.ILinkableStarlightReceiver;
import com.cleanroommc.multiblocked.util.LocalizationUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.distribution.ConstellationSkyHandler;
import hellfirepvp.astralsorcery.common.constellation.distribution.WorldSkyHandler;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightUpdateHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.SkyCollectionHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author youyihj
 */
@InterfaceUser(ILinkableStarlightReceiver.class)
public class StarlightCapabilityTrait extends ProgressCapabilityTrait implements ILinkableStarlightReceiver {
    private int starlight;
    private int capacity;
    private IConstellation constellation;
    private boolean passiveStarlight = true;
    private boolean isNetworkInformed;
    private boolean canSeeSky;
    private float posDistribution = -1;

    public StarlightCapabilityTrait() {
        super(StarlightAstralCapability.CAP);
    }

    @Override
    public void serialize(@Nullable JsonElement jsonElement) {
        super.serialize(jsonElement);
        if (jsonElement == null) {
            jsonElement = new JsonObject();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        capacity = JsonUtils.getInt(jsonObject, "capacity", 2000);
        passiveStarlight = JsonUtils.getBoolean(jsonObject, "passiveStarlight", true);
    }

    @Override
    public JsonElement deserialize() {
        JsonObject jsonObject = super.deserialize().getAsJsonObject();
        jsonObject.addProperty("capacity", capacity);
        jsonObject.addProperty("passiveStarlight", passiveStarlight);
        return jsonObject;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        starlight = compound.getInteger("starlight");
        constellation = IConstellation.readFromNBT(compound, "constellation");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("starlight", starlight);
        constellation.writeToNBT(compound, "constellation");
    }

    @Override
    public void createUI(ComponentTileEntity<?> component, WidgetGroup group, EntityPlayer player) {
        group.addWidget(new ProgressWidget(this::getProgress, x, y, width, height, new ResourceTexture(texture)) {
            private IConstellation lastConstellation;

            @Override
            public void detectAndSendChanges() {
                if (lastConstellation != constellation) {
                    lastConstellation = constellation;
                    writeUpdateInfo(1, buffer -> buffer.writeString(constellation.getUnlocalizedName()));
                }
                super.detectAndSendChanges();
            }

            @Override
            public void readUpdateInfo(int id, PacketBuffer buffer) {
                if (id == 1) {
                    lastConstellation = constellation = ConstellationRegistry.getConstellationByName(buffer.readString(32767));
                }
                super.readUpdateInfo(id, buffer);
            }
        }.setDynamicHoverTips(this::dynamicHoverTips)
                .setFillDirection(fillDirection));
    }

    @Override
    protected void initSettingDialog(DialogWidget dialog, DraggableWidgetGroup slot) {
        super.initSettingDialog(dialog, slot);
        dialog.addWidget(new TextFieldWidget(60, 5, 100, 15, true, null, s -> capacity = Integer.parseInt(s))
                .setNumbersOnly(1, Integer.MAX_VALUE)
                .setCurrentString(capacity + "")
                .setHoverTooltip("multiblocked.gui.trait.starlight.tips.0"));
        dialog.addWidget(new SwitchWidget(60, 25, 15, 15, ((clickData, bool) -> passiveStarlight = bool))
                .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0, 0, 1, 0.5))
                .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0, 0.5, 1, 0.5))
                .setHoverTexture(new ColorBorderTexture(1, 0xff545757))
                .setPressed(passiveStarlight)
                .setHoverTooltip("multiblocked.gui.trait.starlight.tips.1"));
        dialog.addWidget(new LabelWidget(80, 25, "passiveStarlight"));
    }

    @Override
    public boolean hasUpdate() {
        return true;
    }

    @Override
    public void update() {
        if (component.getWorld().isRemote) {
            return;
        }
        if (!isNetworkInformed && !isInAstralNetwork()) {
            informNetworkPlacement();
            isNetworkInformed = true;
        }
        if (passiveStarlight) {
            passiveStarlight();
        }
    }

    @Override
    public void onDrops(NonNullList<ItemStack> drops, EntityPlayer player) {
        if (component.getWorld().isRemote) return;
        informNetworkRemoval();
        isNetworkInformed = false;
    }

    @Override
    protected String dynamicHoverTips(double progress) {
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add(LocalizationUtils.format("multiblocked.gui.trait.starlight.current", ((int) (progress * getStarlightCapacity())), getStarlightCapacity()));
        if (getFocusedConstellation() != null) {
            stringJoiner.add(LocalizationUtils.format("multiblocked.gui.trait.starlight.constellation", LocalizationUtils.format(getFocusedConstellation().getUnlocalizedName())));
        } else {
            stringJoiner.add(LocalizationUtils.format("multiblocked.gui.trait.starlight.no_constellation"));
        }
        return stringJoiner.toString();
    }

    @Override
    protected double getProgress() {
        return ((double) getStarlightStored()) / getStarlightCapacity();
    }

    @Override
    public World getLinkWorld() {
        return getTrWorld();
    }

    @Override
    public BlockPos getLinkPos() {
        return getTrPos();
    }

    @Nullable
    @Override
    public String getUnLocalizedDisplayName() {
        return component.getBlockType().getTranslationKey() + ".name";
    }

    @Override
    public void onLinkCreate(EntityPlayer entityPlayer, BlockPos blockPos) {

    }

    @Override
    public boolean tryLink(EntityPlayer entityPlayer, BlockPos blockPos) {
        return false;
    }

    @Override
    public boolean tryUnlink(EntityPlayer entityPlayer, BlockPos blockPos) {
        return false;
    }

    @Override
    public List<BlockPos> getLinkedPositions() {
        return new LinkedList<>();
    }

    @Nonnull
    @Override
    public ITransmissionReceiver provideEndpoint(BlockPos blockPos) {
        return new MultiblockedTransmissionReceiver(blockPos);
    }

    @Nonnull
    @Override
    public BlockPos getTrPos() {
        return component.getPos();
    }

    @Nonnull
    @Override
    public World getTrWorld() {
        return component.getWorld();
    }

    @Override
    public int getStarlightStored() {
        return starlight;
    }

    @Override
    public void setStarlightStored(int starlight) {
        this.starlight = starlight;
        markAsDirty();
    }

    @Override
    public int getStarlightCapacity() {
        return capacity;
    }

    @Override
    public IConstellation getFocusedConstellation() {
        return constellation;
    }

    @Override
    public void setFocusedConstellation(IConstellation constellation) {
        if (this.constellation == null || !this.constellation.equals(constellation)) {
            this.constellation = constellation;
        }
    }

    private boolean isInAstralNetwork() {
        WorldNetworkHandler networkHandler = WorldNetworkHandler.getNetworkHandler(getTrWorld());
        return networkHandler.getTransmissionNode(getTrPos()) != null;
    }

    private void informNetworkPlacement() {
        WorldNetworkHandler networkHandler = WorldNetworkHandler.getNetworkHandler(getTrWorld());
        networkHandler.addTransmissionTile(this);
        IPrismTransmissionNode node = networkHandler.getTransmissionNode(getTrPos());
        if (node != null && node.needsUpdate()) {
            StarlightUpdateHandler.getInstance().addNode(getTrWorld(), node);
        }
    }

    private void informNetworkRemoval() {
        WorldNetworkHandler networkHandler = WorldNetworkHandler.getNetworkHandler(getTrWorld());
        IPrismTransmissionNode node = networkHandler.getTransmissionNode(getTrPos());
        if (node != null) {
            StarlightUpdateHandler.getInstance().removeNode(getTrWorld(), node);
        }
        networkHandler.removeTransmission(this);
    }

    private void passiveStarlight() {
        if ((component.getTimer() & 15) == 0) {
            canSeeSky = MiscUtils.canSeeSky(component.getWorld(), component.getPos().up(), true, canSeeSky);
        }
        starlight *= 0.95;
        WorldSkyHandler handle = ConstellationSkyHandler.getInstance().getWorldHandler(component.getWorld());
        if (canSeeSky && handle != null) {
            int yLevel = component.getPos().getY();
            if (yLevel > 40) {
                float collect = 160;

                float dstr;
                if (yLevel > 120) {
                    dstr = 1F + ((yLevel - 120) / 272F);
                } else {
                    dstr = (yLevel - 20) / 100F;
                }

                if (posDistribution == -1) {
                    posDistribution = SkyCollectionHelper.getSkyNoiseDistribution(component.getWorld(), component.getPos());
                }

                collect *= dstr;
                collect *= (0.6 + (0.4 * posDistribution));
                collect *= 0.2 + (0.8 * ConstellationSkyHandler.getInstance().getCurrentDaytimeDistribution(component.getWorld()));

                setStarlightStored(Math.min(getStarlightCapacity(), ((int) (getStarlightStored() + collect))));
                markAsDirty();
            }
        }
    }

    public static class MultiblockedTransmissionReceiver extends SimpleTransmissionReceiver {

        public MultiblockedTransmissionReceiver(BlockPos thisPos) {
            super(thisPos);
        }

        @Override
        public void onStarlightReceive(World world, boolean isChunkLoaded, IWeakConstellation type, double amount) {
            if (isChunkLoaded) {
                if (amount <= 0.001) return;
                ILinkableStarlightReceiver tile = MiscUtils.getTileAt(world, getLocationPos(), ILinkableStarlightReceiver.class, false);
                if (tile != null) {
                    tile.setStarlightStored(Math.min(tile.getStarlightCapacity(), (int) (tile.getStarlightStored() + (amount * 200D))));
                    tile.setFocusedConstellation(type);
                    ((TileEntity) tile).markDirty();
                }
            }
        }

        @Override
        public TransmissionClassRegistry.TransmissionProvider getProvider() {
            return new MultiblockedTransmissionProvider();
        }
    }

    public static class MultiblockedTransmissionProvider implements TransmissionClassRegistry.TransmissionProvider {

        @Override
        public IPrismTransmissionNode provideEmptyNode() {
            return new MultiblockedTransmissionReceiver(null);
        }

        @Override
        public String getIdentifier() {
            return Multiblocked.MODID + ":StarlightTrait";
        }
    }
}
