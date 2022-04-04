package com.cleanroommc.multiblocked.network.s2c;

import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.particle.ParticleManager;
import com.cleanroommc.multiblocked.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
//        ParticleManager.INSTANCE.addEffect(new CommonParticle());
        return null;
    }
}
