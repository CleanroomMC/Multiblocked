package com.cleanroommc.multiblocked.network.s2c;

import com.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import com.cleanroommc.multiblocked.network.IPacket;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketRemoveDisabledRendering implements IPacket {
    private BlockPos controllerPos;

    public SPacketRemoveDisabledRendering() {
    }

    public SPacketRemoveDisabledRendering(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarLong(controllerPos.toLong());
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.controllerPos = BlockPos.fromLong(buf.readVarLong());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        MultiblockWorldSavedData.removeDisableModel(controllerPos);
        return null;
    }
}
