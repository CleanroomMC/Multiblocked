package com.cleanroommc.multiblocked.command;

import com.cleanroommc.multiblocked.api.recipe.RecipeLogic;
import com.cleanroommc.multiblocked.network.MultiblockedNetworking;
import com.cleanroommc.multiblocked.network.s2c.SPacketCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

public class CommandClient extends CommandBase {

    public final String cmd;

    public CommandClient (String cmd) {
        this.cmd = cmd;
    }

    @Override
    @Nonnull
    public String getName() {
        return cmd;
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "multiblocked <reload>";
    }


    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        if (sender instanceof EntityPlayerMP) {
            MultiblockedNetworking.sendToPlayer(new SPacketCommand(cmd), (EntityPlayerMP) sender);
        }
    }


}
