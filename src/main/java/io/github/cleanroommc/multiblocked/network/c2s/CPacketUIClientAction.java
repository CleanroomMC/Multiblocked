package io.github.cleanroommc.multiblocked.network.c2s;

import io.github.cleanroommc.multiblocked.gui.modular.ModularUI;
import io.github.cleanroommc.multiblocked.gui.modular.ModularUIContainer;
import io.github.cleanroommc.multiblocked.network.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

public class CPacketUIClientAction implements IPacket {

    private int windowId;
    private int widgetId;
    private PacketBuffer updateData;

    public CPacketUIClientAction() {
    }

    public CPacketUIClientAction(int windowId, int widgetId, PacketBuffer updateData) {
        this.windowId = windowId;
        this.widgetId = widgetId;
        this.updateData = updateData;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(updateData.readableBytes());
        buf.writeBytes(updateData);

        buf.writeVarInt(windowId);
        buf.writeVarInt(widgetId);
    }

    @Override
    public void decode(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        this.updateData = new PacketBuffer(copiedDataBuffer);
        
        this.windowId = buf.readVarInt();
        this.widgetId = buf.readVarInt();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        Container openContainer = handler.player.openContainer;
        if (openContainer instanceof ModularUIContainer && openContainer.windowId == windowId) {
            ModularUI modularUI = ((ModularUIContainer) openContainer).getModularUI();
            modularUI.guiWidgets.get(widgetId).handleClientAction(updateData.readVarInt(), updateData);
        }
        return null;
    }
}
