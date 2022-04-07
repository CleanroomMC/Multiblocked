package com.cleanroommc.multiblocked.api.crafttweaker.interfaces;

import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IFacing;
import crafttweaker.api.world.IWorld;
import crafttweaker.mc1120.world.MCBlockPos;
import crafttweaker.mc1120.world.MCFacing;
import crafttweaker.mc1120.world.MCWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

@ZenClass("mods.multiblocked.tile.Component")
@ZenRegister
public interface ICTComponent {
    ComponentTileEntity<?> getInner();

    @ZenGetter("definition")
    default ComponentDefinition getDefinition() {
        return getInner().getDefinition();
    }

    @ZenGetter
    default IWorld world(){
        World world = getInner().getWorld();
        return world == null ? null : new MCWorld(world);
    }

    @ZenGetter
    default IBlockPos pos(){
        BlockPos pos = getInner().getPos();
        return pos == null ? null : new MCBlockPos(pos);
    }

    @ZenGetter
    default boolean isRemote() {
        return getInner().isRemote();
    }

    @ZenMethod
    default String getUnlocalizedName() {
        return getInner().getUnlocalizedName();
    }

    @SideOnly(Side.CLIENT)
    @ZenMethod
    default String getLocalizedName() {
        return getInner().getLocalizedName();
    }

    @ZenMethod
    @ZenGetter
    default boolean isFormed() {
        return getInner().isFormed();
    }

    @ZenMethod
    @ZenGetter("timer")
    default int getTimer() {
        return getInner().getTimer();
    }

    @ZenMethod
    default void update(){
        getInner().update();
    }

    @ZenGetter("status")
    default String getStatus() {
        return getInner().getStatus();
    }

    @ZenSetter("status")
    default void setStatus(String status) {
        getInner().setStatus(status);
    }

    @ZenGetter
    default IFacing frontFacing() {
        return new MCFacing(getInner().getFrontFacing());
    }

    @ZenSetter
    default void frontFacing(IFacing facing) {
        getInner().setFrontFacing(CraftTweakerMC.getFacing(facing));
    }

    @ZenMethod
    @ZenGetter("renderer")
    default IRenderer getRenderer() {
        return getInner().getRenderer();
    }

    @ZenMethod
    default boolean isFrontFacingValid(IFacing facing) {
        return getInner().isValidFrontFacing(CraftTweakerMC.getFacing(facing));
    }

    @ZenMethod
    default void scheduleChunkForRenderUpdate() {
        getInner().scheduleChunkForRenderUpdate();
    }

    @ZenMethod
    default void notifyBlockUpdate() {
       getInner().notifyBlockUpdate();
    }

    @ZenMethod
    default void markAsDirty() {
        getInner().markAsDirty();
    }

    /**
     * Store extra data for retrieval with {@link #getExtraData()}
     * <p>
     * This will be stored in the tile's NBT when the world is saved,
     * so will persist even through restarts.
     *
     * @param data The extra data to store on the controller.
     * @zenSetter extraData
     */
    @ZenMethod
    @ZenSetter("extraData")
    default void setExtraData(IData data) {
        getInner().persistentData = data;
    }

    /**
     * Retrieve extra data stored with {@link #setExtraData(IData)}
     *
     * @return The extra data stored on this controller.
     */
    @ZenMethod
    @ZenGetter("extraData")
    default IData getExtraData() {
        return (IData) getInner().persistentData;
    }

    //************* data sync *************//

    @ZenMethod
    default void sendCustomData(int id, IData data) {
        getInner().writeCustomData(2, packetBuffer -> {
            packetBuffer.writeVarInt(id);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("data", CraftTweakerMC.getNBT(data));
            packetBuffer.writeCompoundTag(tag);
        });
    }
}
