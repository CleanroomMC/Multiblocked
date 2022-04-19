package com.cleanroommc.multiblocked.command;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class CommandMbdTree extends CommandTreeBase {
    public CommandMbdTree() {
        this.addSubcommand(new CommandClient("reload_shaders"));
        this.addSubcommand(new CommandMbdTps());
        this.addSubcommand(new CommandMbdNbt());
    }

    @Nonnull
    public String getName() {
        return "multiblocked";
    }

    @Nonnull
    public List<String> getAliases() {
        return Collections.singletonList("mbd");
    }

    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "multiblocked.command.usage";
    }
}
