package com.cleanroommc.multiblocked;

import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.pattern.BlockPattern;
import com.cleanroommc.multiblocked.api.pattern.MultiblockState;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

public class Listeners {

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld());
        }
    }

    @SubscribeEvent
    public static void onWorldUnLoad(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld()).releaseSearchingThread();
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld()).getControllerInChunk(event.getChunk().getPos()).forEach(MultiblockState::onChunkLoad);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            MultiblockWorldSavedData.getOrCreate(event.getWorld()).getControllerInChunk(event.getChunk().getPos()).forEach(MultiblockState::onChunkUnload);
        }
    }


    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World level = event.getWorld();
        if (level.isRemote) return;
        ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
        ControllerDefinition[] definitions = MbdComponents.checkNoNeedController(held);
        if (definitions.length > 0) {
            BlockPos pos = event.getPos();
            EnumFacing face = event.getFace();
            EntityPlayer player= event.getEntityPlayer();
            EnumFacing[] facings;
            if (face == null || face.getAxis() == EnumFacing.Axis.Y) {
                facings = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
            } else {
                facings = new EnumFacing[]{face};
            }

            for (ControllerDefinition definition : definitions) {
                BlockPattern pattern = definition.getBasePattern();
                if (pattern != null && definition.noNeedController) {
                    Set<MultiblockCapability<?>> inputCapabilities = definition.getRecipeMap().inputCapabilities;
                    Set<MultiblockCapability<?>> outputCapabilities = definition.getRecipeMap().outputCapabilities;
                    MultiblockState worldState = new MultiblockState(level, pos);
                    IBlockState oldState = level.getBlockState(pos);
                    TileEntity oldBlockEntity = level.getTileEntity(pos);
                    if (oldBlockEntity instanceof ControllerTileEntity) {
                        return;
                    }
                    for (EnumFacing facing : facings) {
                        if (pattern.checkPatternAt(worldState, pos, facing, false, inputCapabilities, outputCapabilities)) {
                            NBTTagCompound oldNbt = null;
                            if (oldBlockEntity != null) {
                                oldNbt = oldBlockEntity.serializeNBT();
                            }
                            level.setBlockState(pos, MbdComponents.COMPONENT_BLOCKS_REGISTRY.get(definition.location).getDefaultState());
                            TileEntity newBlockEntity = level.getTileEntity(pos);
                            if (newBlockEntity instanceof ControllerTileEntity) {
                                ControllerTileEntity controller = (ControllerTileEntity) newBlockEntity;
                                controller.state = worldState;
                                controller.setFrontFacing(facing);
                                if (controller.checkCatalystPattern(player, event.getHand(), held)) { // formed
                                    controller.saveOldBlock(oldState, oldNbt);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
