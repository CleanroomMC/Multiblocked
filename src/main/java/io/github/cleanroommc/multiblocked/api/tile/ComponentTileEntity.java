package io.github.cleanroommc.multiblocked.api.tile;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IFacing;
import crafttweaker.api.world.IWorld;
import crafttweaker.mc1120.player.MCPlayer;
import crafttweaker.mc1120.world.MCBlockPos;
import crafttweaker.mc1120.world.MCFacing;
import crafttweaker.mc1120.world.MCWorld;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import io.github.cleanroommc.multiblocked.api.gui.factory.TileEntityUIFactory;
import io.github.cleanroommc.multiblocked.api.gui.modular.IUIHolder;
import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUI;
import io.github.cleanroommc.multiblocked.util.RayTraceUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
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
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A TileEntity that defies everything a TileEntity represents.
 *
 * This isn't going to be in-world.
 */
@SuppressWarnings("unchecked")
@ZenClass("mods.multiblocked.tile.Component")
@ZenRegister
public abstract class ComponentTileEntity<T extends ComponentDefinition> extends TileEntity implements IUIHolder {
    // is good to write down all CT code here? or move them to @ZenExpansion.
    protected T definition;

    private EnumFacing frontFacing = EnumFacing.NORTH; // 0

    public final void setDefinition(ComponentDefinition definition) {
        this.definition = (T) definition;
    }

    @ZenMethod
    @ZenGetter("definition")
    public T getDefinition() {
        return definition;
    }

    public ResourceLocation getLocation() {
        return definition.location;
    }

    @Method(modid = Multiblocked.MODID_CT)
    @ZenMethod("getWorld")
    @ZenGetter
    public IWorld world(){
        return world == null ? null : new MCWorld(world);
    }

    @Method(modid = Multiblocked.MODID_CT)
    @ZenMethod("getPos")
    @ZenGetter
    public IBlockPos pos(){
        return pos == null ? null : new MCBlockPos(pos);
    }

    @ZenMethod
    public String getUnlocalizedName() {
        return getLocation().getPath();
    }

    @SideOnly(Side.CLIENT)
    @ZenMethod
    public String getLocalizedName() {
        return I18n.format(getUnlocalizedName());
    }

    @ZenMethod
    @ZenGetter
    public abstract boolean isFormed();

    public List<AxisAlignedBB> getCollisionBoundingBox() {
        return definition.getAABB(isFormed(), frontFacing);
    }

    public EnumFacing getFrontFacing() {
        return frontFacing;
    }

    @Method(modid = Multiblocked.MODID_CT)
    @ZenMethod("getFrontFacing")
    @ZenGetter
    public IFacing frontFacing() {
        return new MCFacing(getFrontFacing());
    }

    public boolean setFrontFacing(EnumFacing facing) {
        if (!isValidFrontFacing(facing)) return false;
        frontFacing = facing;
        if (world != null && !world.isRemote) {
            markDirty();
            writeCustomData(0, buffer -> buffer.writeByte(frontFacing.getIndex()));
        }
        return true;
    }

    @Method(modid = Multiblocked.MODID_CT)
    @ZenMethod()
    @ZenSetter("frontFacing")
    public void setFrontFacing(IFacing facing) {
        setFrontFacing(CraftTweakerMC.getFacing(facing));
    }

    @Override
    public void rotate(@Nonnull Rotation rotationIn) {
        setFrontFacing(rotationIn.rotate(getFrontFacing()));
    }

    @Override
    public void mirror(@Nonnull Mirror mirrorIn) {
        rotate(mirrorIn.toRotation(getFrontFacing()));
    }

    @ZenMethod
    public IRenderer getRenderer() {
        if (isFormed()) {
            return definition.formedRenderer == null ? definition.baseRenderer : definition.formedRenderer;
        }
        return definition.baseRenderer;
    }

    public boolean isValidFrontFacing(EnumFacing facing) {
        return definition.allowRotate;
    }

    @Method(modid = Multiblocked.MODID_CT)
    @ZenMethod()
    public boolean isValidFrontFacing(IFacing facing) {
        return isValidFrontFacing(CraftTweakerMC.getFacing(facing));
    }

    public boolean canConnectRedstone(EnumFacing facing) {
        return definition.getOutputRedstoneSignal != null;
    }

    public int getOutputRedstoneSignal(EnumFacing facing) {
        if (definition.getOutputRedstoneSignal != null) {
            return definition.getOutputRedstoneSignal.apply(this, new MCFacing(facing));
        }
        return 0;
    }

    @ZenMethod
    public void scheduleChunkForRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    @ZenMethod
    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    @Override
    @ZenMethod
    public void markDirty() {
        super.markDirty();
    }

    //************* events *************//

    public void onDrops(NonNullList<ItemStack> drops, EntityPlayer player) {
        if (definition.onDrops != null) {
            for (IItemStack drop : definition.onDrops.apply(this, new MCPlayer(player))) {
                drops.add(CraftTweakerMC.getItemStack(drop));
            }
        } else {
            drops.add(definition.getStackForm());
        }
    }

    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (definition.onRightClick != null) {
            if (definition.onRightClick.apply(this, new MCPlayer(player), new MCFacing(facing), hitX, hitY, hitZ)) return true;
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
            definition.onLeftClick.apply(this, new MCPlayer(player));
        }
    }

    public void onNeighborChanged() {
        if (definition.onNeighborChanged != null) {
            definition.onNeighborChanged.apply(this);
        }
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
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("loc", definition.location.toString());
        compound.setByte("frontFacing", (byte) frontFacing.getIndex());
        compound.setString("mbd_def", definition.location.toString());
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
