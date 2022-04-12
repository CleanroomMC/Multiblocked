package com.cleanroommc.multiblocked.api.tile;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.CapabilityTrait;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.gui.factory.TileEntityUIFactory;
import com.cleanroommc.multiblocked.api.gui.modular.IUIHolder;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.util.ModularUIBuilder;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import com.google.gson.JsonElement;
import crafttweaker.api.data.IData;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A TileEntity that defies everything a TileEntity represents.
 *
 * This isn't going to be in-world.
 */
@SuppressWarnings("unchecked")
@Optional.Interface(modid = Multiblocked.MODID_CT, iface = "com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent")
public abstract class ComponentTileEntity<T extends ComponentDefinition> extends TileEntity implements IUIHolder, ICTComponent {
    // is good to write down all CT code here? or move them to @ZenExpansion.
    protected T definition;

    protected IRenderer currentRenderer;

    public Object persistentData; // used for CT

    public Object rendererObject; // used for renderer

    private EnumFacing frontFacing = EnumFacing.NORTH; // 0

    private int timer = Multiblocked.RNG.nextInt();

    protected String status = "unformed";

    protected Map<MultiblockCapability<?>, CapabilityTrait> traits = new HashMap<>();

    public final void setDefinition(ComponentDefinition definition) {
        this.definition = (T) definition;
        for (Map.Entry<String, JsonElement> entry : this.definition.traits.entrySet()) {
            MultiblockCapability<?> capability = MbdCapabilities.get(entry.getKey());
            if (capability != null) {
                CapabilityTrait trait = capability.createTrait();
                trait.init(entry.getValue().getAsJsonObject());
                traits.put(capability, trait);
            }
        }
    }

    public final void checkUpdate() {
        if (world != null && !isRemote() && (definition.needUpdateTick() || traits.values().stream().anyMatch(trait -> trait instanceof ITickable))) {
            MultiblockWorldSavedData.getOrCreate(world).addLoading(this);
        }
    }

    @Override
    public ComponentTileEntity<?> getInner() {
        return this;
    }

    public T getDefinition() {
        return definition;
    }

    public ResourceLocation getLocation() {
        return definition.location;
    }
    
    public String getUnlocalizedName() {
        return getLocation().getPath() + ".name";
    }
    
    public String getLocalizedName() {
        return I18n.format(getUnlocalizedName());
    }

    public abstract boolean isFormed();

    public int getTimer() {
        return timer;
    }

    public void update(){
        timer++;
        if (definition.updateTick != null) {
            definition.updateTick.apply(this);
        }
        if (!traits.isEmpty()) {
            for (CapabilityTrait trait : traits.values()) {
                if (trait instanceof ITickable) {
                    ((ITickable) trait).update();
                }
            }
        }
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (!isRemote()) {
            if (definition.statusChanged != null) {
                status = definition.statusChanged.apply(this, status);
            }
            if (!this.status.equals(status)) {
                this.status = status;
                writeCustomData(1, buffer->buffer.writeString(this.status));
            }
        }
    }

    public List<AxisAlignedBB> getCollisionBoundingBox() {
        return definition.getAABB(isFormed(), frontFacing);
    }

    public EnumFacing getFrontFacing() {
        return frontFacing;
    }

    public boolean setFrontFacing(EnumFacing facing) {
        if (!isValidFrontFacing(facing)) return false;
        frontFacing = facing;
        if (world != null && !world.isRemote) {
            markAsDirty();
            writeCustomData(0, buffer -> buffer.writeByte(frontFacing.getIndex()));
        }
        return true;
    }

    @Override
    public void rotate(@Nonnull Rotation rotationIn) {
        setFrontFacing(rotationIn.rotate(getFrontFacing()));
    }

    @Override
    public void mirror(@Nonnull Mirror mirrorIn) {
        rotate(mirrorIn.toRotation(getFrontFacing()));
    }

    public IRenderer updateCurrentRenderer() {
        if (definition.dynamicRenderer != null) {
            return definition.dynamicRenderer.apply(this);
        }
        if (isFormed()) {
            return definition.formedRenderer == null ? definition.baseRenderer : definition.formedRenderer;
        }
        return definition.baseRenderer;
    }

    public IRenderer getRenderer() {
        IRenderer lastRenderer = currentRenderer;
        currentRenderer = updateCurrentRenderer();
        if (lastRenderer != currentRenderer && Multiblocked.isClient()) {
            if (lastRenderer != null) {
                lastRenderer.onPostAccess(this);
            }
            if (currentRenderer != null) {
                currentRenderer.onPreAccess(this);
            }
        }
        return currentRenderer;
    }

    public boolean isValidFrontFacing(EnumFacing facing) {
        return definition.allowRotate;
    }

    public boolean canConnectRedstone(EnumFacing facing) {
        return definition.getOutputRedstoneSignal != null;
    }

    public int getOutputRedstoneSignal(EnumFacing facing) {
        if (definition.getOutputRedstoneSignal != null) {
            return definition.getOutputRedstoneSignal.apply(this, CraftTweakerMC.getIFacing(facing));
        }
        return 0;
    }

    public void scheduleChunkForRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    @Override
    public void markAsDirty() {
        super.markDirty();
    }

    @Override
    public void setExtraData(IData data) {

    }

    @Override
    public IData getExtraData() {
        return null;
    }

    //************* TESR *************//

    @Override
    public boolean shouldRenderInPass(int pass) {
        IRenderer renderer = getRenderer();
        return renderer != null && renderer.shouldRenderInPass(world, pos, pass);
    }

    public boolean hasTESRRenderer() {
        IRenderer renderer = getRenderer();
        return renderer != null && renderer.hasTESR(getWorld(), getPos());
    }

    //************* events *************//

    public void onDrops(NonNullList<ItemStack> drops, EntityPlayer player) {
        if (definition.onDrops != null) {
            for (IItemStack drop : definition.onDrops.apply(this, CraftTweakerMC.getIPlayer(player))) {
                drops.add(CraftTweakerMC.getItemStack(drop));
            }
        } else {
            drops.add(definition.getStackForm());
        }
    }

    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (definition.onRightClick != null) {
            if (definition.onRightClick.apply(this, CraftTweakerMC.getIPlayer(player), CraftTweakerMC.getIFacing(facing), hitX, hitY, hitZ)) return true;
        }
        if (!player.isSneaking()) {
            if (!world.isRemote && player instanceof EntityPlayerMP) {
                return TileEntityUIFactory.INSTANCE.openUI(this, (EntityPlayerMP) player);
            }
        }
        return false;
    }

    public void onLeftClick(EntityPlayer player) {
        if (definition.onLeftClick != null) {
            definition.onLeftClick.apply(this, CraftTweakerMC.getIPlayer(player));
        }
    }

    public void onNeighborChanged() {
        if (definition.onNeighborChanged != null) {
            definition.onNeighborChanged.apply(this);
        }
    }
    //************* capability *************//

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return traits.values().stream().anyMatch(trait -> trait.hasCapability(capability, facing));
    }

    @Override
    @Nullable
    public <K> K getCapability(@Nonnull Capability<K> capability, @Nullable EnumFacing facing) {
        for (CapabilityTrait trait : traits.values()) {
            K result = trait.getCapability(capability, facing);
            if (result != null) {
                return result;
            }
        }
        return null;
    }


    //************* gui *************//

    @Override
    public final boolean isRemote() {
        return world == null ? Multiblocked.isClient() : world.isRemote;
    }

    @Override
    public ModularUI createUI(EntityPlayer entityPlayer) {
        if (traits.isEmpty()) return null;
        TabContainer tabContainer = new TabContainer(0, 0, 200, 232);
        initTraitUI(tabContainer, entityPlayer);
        return new ModularUIBuilder(IGuiTexture.EMPTY, 196, 256)
                .widget(tabContainer)
                .build(this, entityPlayer);
    }

    protected void initTraitUI(TabContainer tabContainer, EntityPlayer entityPlayer) {
        WidgetGroup group = new WidgetGroup(20, 0, 176, 256);
        tabContainer.addTab(new TabButton(0, tabContainer.containerGroup.widgets.size() * 20, 20, 20)
                        .setTexture(new ColorRectTexture(-1), new ColorRectTexture(0xffff0000)), group);
        group.addWidget(new ImageWidget(0, 0, 176, 256, new ResourceTexture(JsonUtils.getString(definition.traits, "background", "multiblocked:textures/gui/custom_gui.png"))));
        for (CapabilityTrait trait : traits.values()) {
            trait.createUI(group, entityPlayer);
        }
    }

    public final void writeTraitData(CapabilityTrait trait, int internalId, Consumer<PacketBuffer> dataWriter) {
        this.writeCustomData(3, (buffer) -> {
            buffer.writeString(trait.capability.name);
            buffer.writeVarInt(internalId);
            dataWriter.accept(buffer);
        });
    }


    //************* data sync *************//

    private static class UpdateEntry {
        private final int discriminator;
        private final byte[] updateData;

        public UpdateEntry(int discriminator, byte[] updateData) {
            this.discriminator = discriminator;
            this.updateData = updateData;
        }
    }

    protected final List<UpdateEntry> updateEntries = new ArrayList<>();

    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeString(getLocation().toString());
        buf.writeByte(this.frontFacing.getIndex());
        buf.writeString(status);
        if (definition.writeInitialData != null) { // ct
            IData data = definition.writeInitialData.apply(this);
            if (data != null) {
                buf.writeBoolean(true);
                NBTTagCompound tag = new NBTTagCompound();
                tag.setTag("data", CraftTweakerMC.getNBT(data));
                buf.writeCompoundTag(tag);
            } else {
                buf.writeBoolean(false);
            }
        } else {
            buf.writeBoolean(false);
        }
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        setDefinition(MbdComponents.DEFINITION_REGISTRY.get(new ResourceLocation(buf.readString(Short.MAX_VALUE))));
        this.frontFacing = EnumFacing.VALUES[buf.readByte()];
        status = buf.readString(Short.MAX_VALUE);
        if (buf.readBoolean()) { // ct
            try {
                NBTTagCompound nbt = buf.readCompoundTag();
                if (nbt != null && definition.readInitialData != null) {
                    definition.readInitialData.apply(this, CraftTweakerMC.getIData(nbt.getTag("data")));
                }
            } catch (IOException e) {
                Multiblocked.LOGGER.error("handling ct initial data error");
            }
        }
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == 0) {
            this.frontFacing = EnumFacing.VALUES[buf.readByte()];
            scheduleChunkForRenderUpdate();
        } else if (dataId == 1) {
            status = buf.readString(Short.MAX_VALUE);
            scheduleChunkForRenderUpdate();
        } else if (dataId == 2) {
            int id = buf.readVarInt();
            try {
                NBTTagCompound nbt = buf.readCompoundTag();
                if (nbt != null && definition.receiveCustomData != null) {
                    definition.receiveCustomData.apply(this, id, CraftTweakerMC.getIData(nbt.getTag("data")));
                }
            } catch (IOException e) {
                Multiblocked.LOGGER.error("handling ct custom data error id:{}", id);
            }
        } else if (dataId == 3) {
            MultiblockCapability<?> capability = MbdCapabilities.get(buf.readString(Short.MAX_VALUE));
            if (traits.containsKey(capability)) {
                traits.get(capability).receiveCustomData(buf.readVarInt(), buf);
            }
        }
    }

    @Override
    public final void deserializeNBT(@Nonnull NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
    }

    @Override
    public final NBTTagCompound serializeNBT() {
        return super.serializeNBT();
    }

    @Override
    protected final void setWorldCreate(@Nonnull World worldIn) {
        setWorld(worldIn);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        setDefinition(MbdComponents.DEFINITION_REGISTRY.get(new ResourceLocation(compound.getString("loc"))));
        checkUpdate();
        this.frontFacing = compound.hasKey("frontFacing") ? EnumFacing.byIndex(compound.getByte("frontFacing")) : this.frontFacing;
        if (Multiblocked.isModLoaded(Multiblocked.MODID_CT)) {
            persistentData = CraftTweakerMC.getIData(compound.getTag("ct_persistent"));
        }
        NBTTagCompound traitTag = compound.getCompoundTag("trait");
        for (Map.Entry<MultiblockCapability<?>, CapabilityTrait> entry : traits.entrySet()) {
            entry.getValue().readFromNBT(traitTag.getCompoundTag(entry.getKey().name));
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("loc", definition.location.toString());
        compound.setByte("frontFacing", (byte) frontFacing.getIndex());
        compound.setString("mbd_def", definition.location.toString());
        if (Multiblocked.isModLoaded(Multiblocked.MODID_CT) && persistentData instanceof IData) {
            compound.setTag("ct_persistent", CraftTweakerMC.getNBT((IData) persistentData));
        }
        NBTTagCompound traitTag = new NBTTagCompound();
        for (Map.Entry<MultiblockCapability<?>, CapabilityTrait> entry : traits.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            entry.getValue().writeToNBT(tag);
            traitTag.setTag(entry.getKey().name, tag);
        }
        compound.setTag("trait", traitTag);
        return compound;
    }

    public void writeCustomData(int discriminator, Consumer<PacketBuffer> dataWriter) {
        ByteBuf backedBuffer = Unpooled.buffer();
        dataWriter.accept(new PacketBuffer(backedBuffer));
        byte[] updateData = Arrays.copyOfRange(backedBuffer.array(), 0, backedBuffer.writerIndex());
        updateEntries.add(new UpdateEntry(discriminator, updateData));
        @SuppressWarnings("deprecation")
        IBlockState blockState = getBlockType().getStateFromMeta(getBlockMetadata());
        world.notifyBlockUpdate(getPos(), blockState, blockState, 0);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound updateTag = new NBTTagCompound();
        NBTTagList tagList = new NBTTagList();
        for (UpdateEntry updateEntry : updateEntries) {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setInteger("i", updateEntry.discriminator);
            entryTag.setByteArray("d", updateEntry.updateData);
            tagList.appendTag(entryTag);
        }
        this.updateEntries.clear();
        updateTag.setTag("d", tagList);
        return new SPacketUpdateTileEntity(getPos(), 0, updateTag);
    }

    @Override
    public void onDataPacket(@Nonnull NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound updateTag = pkt.getNbtCompound();
        NBTTagList tagList = updateTag.getTagList("d", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound entryTag = tagList.getCompoundTagAt(i);
            int discriminator = entryTag.getInteger("i");
            byte[] updateData = entryTag.getByteArray("d");
            ByteBuf backedBuffer = Unpooled.copiedBuffer(updateData);
            receiveCustomData(discriminator, new PacketBuffer(backedBuffer));
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = super.getUpdateTag();
        ByteBuf backedBuffer = Unpooled.buffer();
        writeInitialSyncData(new PacketBuffer(backedBuffer));
        byte[] updateData = Arrays.copyOfRange(backedBuffer.array(), 0, backedBuffer.writerIndex());
        updateTag.setByteArray("d", updateData);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
        super.readFromNBT(tag);
        byte[] updateData = tag.getByteArray("d");
        ByteBuf backedBuffer = Unpooled.copiedBuffer(updateData);
        receiveInitialSyncData(new PacketBuffer(backedBuffer));
    }

    @Override
    @Nonnull
    public AxisAlignedBB getRenderBoundingBox() {
        return getRenderer().isGlobalRenderer(this) ? INFINITE_EXTENT_AABB : super.getRenderBoundingBox();
    }
}