package com.cleanroommc.multiblocked.network.s2c;

import com.cleanroommc.multiblocked.api.gui.factory.UIFactory;
import com.cleanroommc.multiblocked.network.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketUIOpen implements IPacket {
    private int uiFactoryId;
    private PacketBuffer serializedHolder;
    private int windowId;

    public SPacketUIOpen() {
    }

    public SPacketUIOpen(int uiFactoryId, PacketBuffer serializedHolder, int windowId) {
        this.uiFactoryId = uiFactoryId;
        this.serializedHolder = serializedHolder;
        this.windowId = windowId;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(serializedHolder.readableBytes());
        buf.writeBytes(serializedHolder);

        buf.writeVarInt(uiFactoryId);
        buf.writeVarInt(windowId);
    }

    @Override
    public void decode(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        this.serializedHolder = new PacketBuffer(copiedDataBuffer);

        this.uiFactoryId = buf.readVarInt();
        this.windowId = buf.readVarInt();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        UIFactory<?> uiFactory = UIFactory.FACTORIES.get(uiFactoryId);
        if (uiFactory != null) {
            uiFactory.initClientUI(serializedHolder, windowId);
        }
        return null;
    }
}
