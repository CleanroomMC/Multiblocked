package com.cleanroommc.multiblocked.network.s2c;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.client.particle.CommonParticle;
import com.cleanroommc.multiblocked.client.particle.ParticleManager;
import com.cleanroommc.multiblocked.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
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
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        ResourceLocation texture = new ResourceLocation(Multiblocked.MODID, "textures/fx/particles.png");
        CommonParticle particle = new CommonParticle(mc.world, player.posX, player.posY + 2, player.posZ + 1);
        particle.isBackLayer = false;
        particle.setScale(16);
        particle.setGravity(1);
        particle.setLife(20000);
        particle.setTexture(texture);
        ParticleManager.INSTANCE.addEffect(particle);
//        ParticleManager.INSTANCE.clearAllEffects(true);
        return null;
    }
}
