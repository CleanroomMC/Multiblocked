package io.github.cleanroommc.multiblocked.network.s2c;

import io.github.cleanroommc.multiblocked.network.IPacket;
import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SPacketCommand implements IPacket {
    private String cmd;

    public SPacketCommand() {
    }

    public SPacketCommand(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeString(cmd);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.cmd = buf.readString(Short.MAX_VALUE);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        Minecraft.getMinecraft().scheduleResourcesRefresh();
        return null;
    }
}
