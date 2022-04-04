package com.cleanroommc.multiblocked.command;

import com.cleanroommc.multiblocked.network.MultiblockedNetworking;
import com.cleanroommc.multiblocked.network.s2c.SPacketCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class CommandReload extends CommandBase {

    @Override
    @Nonnull
    public String getName() {
        return "mbd_reload";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "multiblocked <reload>";
    }


    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        if (sender instanceof EntityPlayerMP) {
            MultiblockedNetworking.sendToPlayer(new SPacketCommand("dddd"), (EntityPlayerMP) sender);
            sender.sendMessage(new TextComponentString("Reloaded Shaders"));
        } 
    }


}
