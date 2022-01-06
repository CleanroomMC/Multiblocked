package io.github.cleanroommc.multiblocked.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.network.packet.PacketSyncMultiblockWorldSavedData;

public class MultiblockedNetworking {

    private static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Multiblocked.MODID);
    private static final byte SYNC_MULTIBLOCKS = 1;

    public static void initializeC2S() { }

    public static void initializeS2C() {
        network.registerMessage(new PacketSyncMultiblockWorldSavedData.Handler(), PacketSyncMultiblockWorldSavedData.class, SYNC_MULTIBLOCKS, Side.CLIENT);
    }

    public static void requestFromServer(IMessage packet) {
        network.sendToServer(packet);
    }

    public static void sendToWorld(IMessage packet, World world) {
        network.sendToDimension(packet, world.provider.getDimension());
    }

    public static void sendToPlayer(IMessage packet, EntityPlayerMP player) {
        network.sendTo(packet, player);
    }

}
