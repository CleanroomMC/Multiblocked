package io.github.cleanroommc.multiblocked.network.s2c;

import io.github.cleanroommc.multiblocked.api.gui.modular.ModularUIGuiContainer;
import io.github.cleanroommc.multiblocked.network.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketUIWidgetUpdate implements IPacket {

    public int windowId;
    public int widgetId;
    public PacketBuffer updateData;

    public SPacketUIWidgetUpdate() {
    }

    public SPacketUIWidgetUpdate(int windowId, int widgetId, PacketBuffer updateData) {
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

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen instanceof ModularUIGuiContainer) {
            ((ModularUIGuiContainer) currentScreen).handleWidgetUpdate(this);
        }
        return null;
    }
}
