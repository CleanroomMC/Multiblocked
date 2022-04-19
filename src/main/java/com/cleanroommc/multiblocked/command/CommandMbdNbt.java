package com.cleanroommc.multiblocked.command;

import com.cleanroommc.multiblocked.network.MultiblockedNetworking;
import com.cleanroommc.multiblocked.network.s2c.SPacketCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;

public class CommandMbdNbt extends CommandBase {


    public CommandMbdNbt() {
    }

    @Override
    @Nonnull
    public String getName() {
        return "nbt";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "get the nbt tag of the block looked";
    }


    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        if (sender instanceof EntityPlayerMP) {
            if (((EntityPlayerMP) sender).isCreative()) {
                EntityPlayerMP playerMP = (EntityPlayerMP) sender;
                double reachDist = playerMP.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
                RayTraceResult result = ForgeHooks.rayTraceEyes(playerMP, reachDist);
                if (result != null) {
                    TileEntity te = playerMP.world.getTileEntity(result.getBlockPos());
                    if (te != null) {
                        MultiblockedNetworking.sendToPlayer(new SPacketCommand("nbt: " + te.serializeNBT().toString()), playerMP);
                        return;
                    }
                }
                sender.sendMessage(new TextComponentString("no nbt tag"));
            } else {
                sender.sendMessage(new TextComponentString("only be executed in creative mode"));
            }
        }
    }

}
