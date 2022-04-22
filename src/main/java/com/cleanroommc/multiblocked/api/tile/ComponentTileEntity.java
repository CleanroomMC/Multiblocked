package com.cleanroommc.multiblocked.api.tile;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.capability.IInnerCapabilityProvider;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.gui.factory.TileEntityUIFactory;
import com.cleanroommc.multiblocked.api.gui.modular.IUIHolder;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUI;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * A TileEntity that defies everything a TileEntity represents.
 *
 * This isn't going to be in-world.
 */
@SuppressWarnings("unchecked")
@Optional.Interface(modid = Multiblocked.MODID_CT, iface = "com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent")
public abstract class ComponentTileEntity<T extends ComponentDefinition> extends TileEntity implements IUIHolder, ICTComponent, IInnerCapabilityProvider {
    // is good to write down all CT code here? or move them to @ZenExpansion.
    protected T definition;

    protected IRenderer currentRenderer;

    public Object persistentData; // used for CT

    public Object rendererObject; // used for renderer

    private EnumFacing frontFacing = EnumFacing.NORTH; // 0

    private UUID owner;

    private final int offset = Multiblocked.RNG.nextInt();

    private int timer = offset;

    protected String status = "unformed";

    protected Map<MultiblockCapability<?>, CapabilityTrait> traits = new HashMap<>();

    public void setDefinition(ComponentDefinition definition) {
        this.definition = (T) definition;
        for (Map.Entry<String, JsonElement> entry : this.definition.traits.entrySet()) {
            MultiblockCapability<?> capability = MbdCapabilities.get(entry.getKey());
            if (capability != null && capability.hasTrait()) {
                CapabilityTrait trait = capability.createTrait();
                trait.serialize(entry.getValue());
                trait.setComponent(this);
                traits.put(capability, trait);
            }
        }
    }

    public boolean needAlwaysUpdate() {
        return world != null && !isRemote() && (definition.needUpdateTick() || traits.values().stream().anyMatch(CapabilityTrait::hasUpdate));
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

    public int getOffset() {
        return offset;
    }

    public int getTimer() {
        return timer;
    }

    public UUID getOwnerUUID() {
        return owner;
    }

    @Nullable
    public EntityPlayer getOwner() {
        return owner == null ? null : this.world.getPlayerEntityByUUID(owner);
    }

    public void setOwner(UUID player) {
        this.owner = player;
    }

    public void setOwner(EntityPlayer player) {
        this.owner = player.getUniqueID();
    }

    public void update(){
        timer++;
        if (definition.updateTick != null) {
            try {
                definition.updateTick.apply(this);
            } catch (Exception exception) {
                definition.updateTick = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "updateTick", exception);
            }
        }
        if (!traits.isEmpty()) {
            for (CapabilityTrait trait : traits.values()) {
                trait.update();
            }
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (!isRemote()) {
            if (!this.status.equals(status)) {
                if (definition.statusChanged != null) {
                    try {
                        status = definition.statusChanged.apply(this, status);
                    } catch (Exception exception) {
                        definition.statusChanged = null;
                        Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "statusChanged", exception);
                    }
                }
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
            try {
                return definition.dynamicRenderer.apply(this);
            } catch (Exception exception) {
                definition.dynamicRenderer = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "dynamicRenderer", exception);
            }
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
            try {
                return definition.getOutputRedstoneSignal.apply(this, CraftTweakerMC.getIFacing(facing));
            } catch (Exception exception) {
                definition.getOutputRedstoneSignal = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "getOutputRedstoneSignal", exception);
            }
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
            try {
                for (IItemStack drop :  definition.onDrops.apply(this, CraftTweakerMC.getIPlayer(player))) {
                    drops.add(CraftTweakerMC.getItemStack(drop));
                }
                return;
            } catch (Exception exception) {
                definition.onDrops = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "onDrops", exception);
            }
        }
        drops.add(definition.getStackForm());
    }

    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (definition.onRightClick != null) {
            try {
                if (definition.onRightClick.apply(this, CraftTweakerMC.getIPlayer(player), CraftTweakerMC.getIFacing(facing), hitX, hitY, hitZ)) return true;
            } catch (Exception exception) {
                definition.onRightClick = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "onRightClick", exception);
            }
        }
        if (!player.isSneaking()) {
            if (!world.isRemote && player instanceof EntityPlayerMP) {
                return TileEntityUIFactory.INSTANCE.openUI(this, (EntityPlayerMP) player);
            } else {
                return !traits.isEmpty();
            }
        }
        return false;
    }

    public void onLeftClick(EntityPlayer player) {
        if (definition.onLeftClick != null) {
            try {
                definition.onLeftClick.apply(this, CraftTweakerMC.getIPlayer(player));
            } catch (Exception exception) {
                definition.onLeftClick = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "onLeftClick", exception);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        traits.values().forEach(CapabilityTrait::onLoad);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        traits.values().forEach(CapabilityTrait::onChunkUnload);
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        boolean result = false;
        for (CapabilityTrait trait : traits.values()) {
            result |= trait.receiveClientEvent(id, type);
        }
        return result;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        traits.values().forEach(CapabilityTrait::invalidate);
    }

    public void onNeighborChanged() {
        if (definition.onNeighborChanged != null) {
            try {
                definition.onNeighborChanged.apply(this);
            } catch (Exception exception) {
                definition.onNeighborChanged = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "onNeighborChanged", exception);
            }
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

    @Nullable
    @Override
    public <K> K getInnerCapability(@Nonnull Capability<K> capability, @Nullable EnumFacing facing) {
        for (CapabilityTrait trait : traits.values()) {
            K result = trait.getInnerCapability(capability, facing);
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
                        .setTexture(
                                new ResourceTexture("multiblocked:textures/gui/custom_gui_tab_button.png").getSubTexture(0, 0, 1, 0.5),
                                new ResourceTexture("multiblocked:textures/gui/custom_gui_tab_button.png").getSubTexture(0, 0.5, 1, 0.5)), group);
        group.addWidget(new ImageWidget(0, 0, 176, 256, new ResourceTexture(JsonUtils.getString(definition.traits, "background", "multiblocked:textures/gui/custom_gui.png"))));
        for (CapabilityTrait trait : traits.values()) {
            trait.createUI(this, group, entityPlayer);
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
            try {
                IData data = definition.writeInitialData.apply(this);
                if (data != null) {
                    buf.writeBoolean(true);
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setTag("data", CraftTweakerMC.getNBT(data));
                    buf.writeCompoundTag(tag);
                } else {
                    buf.writeBoolean(false);
                }
            } catch (Exception exception) {
                definition.writeInitialData = null;
                Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "writeInitialData", exception);
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
                    try {
                        definition.readInitialData.apply(this, CraftTweakerMC.getIData(nbt.getTag("data")));
                    } catch (Exception exception) {
                        definition.readInitialData = null;
                        Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "readInitialData", exception);
                    }
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
                    try {
                        definition.receiveCustomData.apply(this, id, CraftTweakerMC.getIData(nbt.getTag("data")));
                    } catch (Exception exception) {
                        definition.receiveCustomData = null;
                        Multiblocked.LOGGER.error("definition {} custom logic {} error", definition.location, "receiveCustomData", exception);
                    }
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
    public void setPos(@Nonnull BlockPos pos) {
        super.setPos(pos);
        if (needAlwaysUpdate()) {
            MultiblockWorldSavedData.getOrCreate(world).addLoading(this);
        }
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        try {
            setDefinition(MbdComponents.DEFINITION_REGISTRY.get(new ResourceLocation(compound.getString("loc"))));
        } catch (Exception e) {
            if (definition == null) {
                getWorld().setBlockToAir(getPos());
                getWorld().setTileEntity(getPos(), null);
                throw new IllegalStateException("null definition set: " + compound.getString("loc"));
            }
            throw e;
        }
        if (needAlwaysUpdate()) {
            MultiblockWorldSavedData.getOrCreate(world).addLoading(this);
        }
        this.frontFacing = compound.hasKey("frontFacing") ? EnumFacing.byIndex(compound.getByte("frontFacing")) : this.frontFacing;
        if (compound.hasKey("owner")) {
            this.owner = compound.getUniqueId("owner");
        }
        if (Loader.isModLoaded(Multiblocked.MODID_CT)) {
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
        if (this.owner != null) {
            compound.setUniqueId("owner", this.owner);
        }
        compound.setString("mbd_def", definition.location.toString());
        if (Loader.isModLoaded(Multiblocked.MODID_CT) && persistentData instanceof IData) {
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
