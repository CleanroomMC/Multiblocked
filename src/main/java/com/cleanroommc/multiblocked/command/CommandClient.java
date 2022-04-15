package com.cleanroommc.multiblocked.command;

import com.cleanroommc.multiblocked.network.MultiblockedNetworking;
import com.cleanroommc.multiblocked.network.s2c.SPacketCommand;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

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
            if (cmd.equals("mbd_tps")) {
                StringBuilder tps = new StringBuilder();
                float sumTps = 0;
                for (WorldServer world : server.worlds) {
                    MultiblockWorldSavedData mwsd = MultiblockWorldSavedData.getOrCreate(world);
                    long periodID = mwsd.getPeriodID();
                    float worldTPS = mwsd.getTPS();
                    sumTps += worldTPS;
                    tps.append(String.format(getDimensionPrefix(world.provider.getDimension()) + ": PeriodID: %d. Mean TPS: %.2f\n", periodID, worldTPS));
                }
                tps.append(String.format("Overall: Mean TPS: %.2f", sumTps / server.worlds.length));
                sender.sendMessage(new TextComponentString(tps.toString()));
            } else {
                MultiblockedNetworking.sendToPlayer(new SPacketCommand(cmd), (EntityPlayerMP) sender);
            }
        }
    }

    private static String getDimensionPrefix(int dimId) {
        DimensionType providerType = DimensionManager.getProviderType(dimId);
        if (providerType == null) {
            return String.format("Dim %2d", dimId);
        } else {
            return String.format("Dim %2d (%s)", dimId, providerType.getName());
        }
    }


}
