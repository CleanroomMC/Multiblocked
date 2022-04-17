package com.cleanroommc.multiblocked.command;

import com.cleanroommc.multiblocked.api.pattern.MultiblockState;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import com.cleanroommc.multiblocked.network.MultiblockedNetworking;
import com.cleanroommc.multiblocked.network.s2c.SPacketCommand;
import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
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
            } else if (cmd.equals("mbd_test")) {
                World world = ((EntityPlayerMP) sender).world;
                IBlockState state= MbdComponents.COMPONENT_BLOCKS_REGISTRY.get(new ResourceLocation("multiblocked:fusion_sun")).getDefaultState();
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        BlockPos pos = sender.getPosition().offset(EnumFacing.SOUTH, i * 6).offset(EnumFacing.EAST, j * 6).offset(EnumFacing.UP, 3);
                        world.setBlockState(pos, state);
//                        TileEntity tileEntity = world.getTileEntity(pos);
//                        if (tileEntity instanceof ControllerTileEntity) {
//                            ((ControllerTileEntity) tileEntity).getPattern().autoBuild((EntityPlayer) sender, new MultiblockState(world, pos));
//                        }
                    }
                }

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
