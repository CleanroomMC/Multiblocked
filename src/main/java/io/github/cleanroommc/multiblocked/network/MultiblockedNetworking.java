package io.github.cleanroommc.multiblocked.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import io.github.cleanroommc.multiblocked.Multiblocked;

public class MultiblockedNetworking {

    private static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Multiblocked.MODID);

    public static void initializeC2S() { }

    public static void initializeS2C() {
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
