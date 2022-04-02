package com.cleanroommc.multiblocked.network;

import com.cleanroommc.multiblocked.network.s2c.SPacketUIOpen;
import com.cleanroommc.multiblocked.network.s2c.SPacketUIWidgetUpdate;
import com.cleanroommc.multiblocked.network.c2s.CPacketUIClientAction;
import com.cleanroommc.multiblocked.network.s2c.SPacketCommand;
import com.cleanroommc.multiblocked.network.s2c.SPacketRemoveDisabledRendering;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import com.cleanroommc.multiblocked.Multiblocked;
import net.minecraftforge.fml.relauncher.Side;

public class MultiblockedNetworking {

    private static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(Multiblocked.MODID);
    private static int AUTO_ID = 0;

    public static void init() {
        registerS2C(SPacketUIOpen.class);
        registerS2C(SPacketUIWidgetUpdate.class);
        registerS2C(SPacketRemoveDisabledRendering.class);
        registerS2C(SPacketCommand.class);
        registerC2S(CPacketUIClientAction.class);
    }

    private static void registerC2S(Class<? extends IPacket> clazz) {
        network.registerMessage(C2SHandler, clazz, AUTO_ID++, Side.SERVER);
    }

    private static void registerS2C(Class<? extends IPacket> clazz) {
        network.registerMessage(S2CHandler, clazz, AUTO_ID++, Side.CLIENT);
    }

    public static void sendToServer(IPacket packet) {
        network.sendToServer(packet);
    }

    public static void sendToWorld(IPacket packet, World world) {
        network.sendToDimension(packet, world.provider.getDimension());
    }

    public static void sendToPlayer(IPacket packet, EntityPlayerMP player) {
        network.sendTo(packet, player);
    }

    final static IMessageHandler<IPacket, IPacket> S2CHandler = (message, ctx) -> {
        NetHandlerPlayClient handler = ctx.getClientHandler();
        IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
        if (threadListener.isCallingFromMinecraftThread()) {
            return message.executeClient(handler);
        } else {
            threadListener.addScheduledTask(() -> message.executeClient(handler));
        }
        return null;
    };
    final static IMessageHandler<IPacket, IPacket> C2SHandler = (message, ctx) -> {
        NetHandlerPlayServer handler = ctx.getServerHandler();
        IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
        if (threadListener.isCallingFromMinecraftThread()) {
            return message.executeServer(handler);
        } else {
            threadListener.addScheduledTask(() -> message.executeServer(handler));
        }
        return null;
    };
}
