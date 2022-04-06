package com.cleanroommc.multiblocked.api.tile;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.crafttweaker.interfaces.ICTComponent;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.gui.factory.TileEntityUIFactory;
import com.cleanroommc.multiblocked.api.gui.modular.IUIHolder;
import com.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import com.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public final void setDefinition(ComponentDefinition definition) {
        this.definition = (T) definition;
        if (!isRemote() && definition.needUpdateTick()) {
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
        if (lastRenderer != currentRenderer) {
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

    //************* gui *************//

    @Override
    public final boolean isRemote() {
        return world == null ? Multiblocked.isClient() : world.isRemote;
    }

    @Override
    public ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
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
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        setDefinition(MultiblockComponents.DEFINITION_REGISTRY.get(new ResourceLocation(buf.readString(Short.MAX_VALUE))));
        this.frontFacing = EnumFacing.VALUES[buf.readByte()];
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == 0) {
            this.frontFacing = EnumFacing.VALUES[buf.readByte()];
            scheduleChunkForRenderUpdate();
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
        setDefinition(MultiblockComponents.DEFINITION_REGISTRY.get(new ResourceLocation(compound.getString("loc"))));
        this.frontFacing = compound.hasKey("frontFacing") ? EnumFacing.byIndex(compound.getByte("frontFacing")) : this.frontFacing;
        if (Multiblocked.isModLoaded(Multiblocked.MODID_CT)) {
            persistentData = CraftTweakerMC.getIData(compound.getTag("ct_persistent"));
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

}
