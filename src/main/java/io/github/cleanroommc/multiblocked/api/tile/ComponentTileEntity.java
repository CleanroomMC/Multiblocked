package io.github.cleanroommc.multiblocked.api.tile;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A TileEntity that defies everything a TileEntity represents.
 *
 * This isn't going to be in-world. But the parent MultiblockInstance would.
 */
public abstract class ComponentTileEntity extends TileEntity {

    protected ResourceLocation location;
    protected EnumFacing frontFacing = EnumFacing.NORTH; // 0

    public ComponentTileEntity() {
        location = new ResourceLocation(Multiblocked.MODID, "error");
    }

    public ComponentTileEntity(ResourceLocation location) {
        this.location = location;
    }

    public abstract ComponentTileEntity createNewTileEntity();

    public ResourceLocation getLocation() {
        return location;
    }

    public String getUnlocalizedName() {
        return location.getPath();
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        return I18n.format(getUnlocalizedName());
    }

    public ItemStack getStackForm() {
        return new ItemStack(MultiblockComponents.COMPONENT_BLOCKS.get(MultiblockComponents.REGISTRY.get(getLocation())), 1);
    }

    public List<AxisAlignedBB> getCollisionBoundingBox() {
        return Collections.singletonList(Block.FULL_BLOCK_AABB);
    }

    public EnumFacing getFrontFacing() {
        return frontFacing;
    }

    public boolean setFrontFacing(EnumFacing facing) {
        if (isValidFrontFacing(facing)) {
            frontFacing = facing;
            markDirty();
            writeCustomData(0, buffer -> buffer.writeByte(frontFacing.getIndex()));
            return true;
        }
        return false;
    }

    public IRenderer getRenderer() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void registerRenderers(TextureMap textureMap) {
    }

    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    public void getDrops(NonNullList<ItemStack> drops, EntityPlayer player) {
    }

    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return false;
    }

    public void onLeftClick(EntityPlayer player) {
    }

    public boolean canConnectRedstone(EnumFacing facing) {
        return false;
    }

    public int getOutputRedstoneSignal(EnumFacing facing) {
        return 0;
    }

    public void onNeighborChanged() {
    }

    public boolean isOpaqueCube() {
        return true;
    }

    public void scheduleChunkForRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeString(location.toString());
        buf.writeByte(this.frontFacing.getIndex());
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        this.location = new ResourceLocation(buf.readString(Short.MAX_VALUE));
        this.frontFacing = EnumFacing.VALUES[buf.readByte()];
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == 0) {
            this.frontFacing = EnumFacing.VALUES[buf.readByte()];
            scheduleChunkForRenderUpdate();
        }
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.location = compound.hasKey("loc") ? new ResourceLocation(compound.getString("loc")) : this.location;
        this.frontFacing = compound.hasKey("frontFacing") ? EnumFacing.byIndex(compound.getByte("frontFacing")) : this.frontFacing;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("loc", location.toString());
        compound.setByte("frontFacing", (byte) frontFacing.getIndex());
        return compound;
    }

    @Override
    public boolean hasCapability(@Nullable Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nullable Capability<T> capability, @Nullable EnumFacing facing) {
        return super.getCapability(capability, facing);
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
