package io.github.cleanroommc.multiblocked.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPacket extends IMessage {

    void encode(PacketBuffer buf);

    void decode(PacketBuffer buf);

    @Override
    default void fromBytes(ByteBuf buf){
        decode(new PacketBuffer(buf));
    }

    @Override
    default void toBytes(ByteBuf buf) {
        encode(new PacketBuffer(buf));
    }

    @SideOnly(Side.CLIENT)
    default IPacket executeClient(NetHandlerPlayClient handler) {
        return null;
    }

    default IPacket executeServer(NetHandlerPlayServer handler) {
        return null;
    }

}