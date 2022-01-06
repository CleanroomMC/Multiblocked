package io.github.cleanroommc.multiblocked.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import io.github.cleanroommc.multiblocked.api.framework.structure.Multiblock;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandReloadDefinitions implements ICommand {

    @Override
    public String getName() {
        return "multiblocked";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "multiblocked <reload>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("reload");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (checkPermission(server, sender)) {
            if (args[0].equals("reload")) {
                Multiblock.loadMultiblocks();
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(4, "multiblocked");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

}
