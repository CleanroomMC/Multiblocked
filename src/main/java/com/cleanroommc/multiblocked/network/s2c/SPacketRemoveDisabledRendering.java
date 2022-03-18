package com.cleanroommc.multiblocked.network.s2c;

import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import com.cleanroommc.multiblocked.network.IPacket;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SPacketRemoveDisabledRendering implements IPacket {
    private Set<Long> poses;

    public SPacketRemoveDisabledRendering() {
    }

    public SPacketRemoveDisabledRendering(Set<Long> poses) {
        this.poses = poses;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(poses.size());
        poses.forEach(buf::writeLong);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.poses = new HashSet<>();
        for (int i = buf.readVarInt(); i > 0; i--) {
            poses.add(buf.readLong());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        MultiblockWorldSavedData.removeDisableModel(poses.stream().map(BlockPos::fromLong).collect(Collectors.toList()));
        return null;
    }
}
