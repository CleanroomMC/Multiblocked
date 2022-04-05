package com.cleanroommc.multiblocked.network.s2c;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.client.particle.LaserBeamParticle;
import com.cleanroommc.multiblocked.client.particle.ParticleManager;
import com.cleanroommc.multiblocked.client.shader.Shaders;
import com.cleanroommc.multiblocked.network.IPacket;
import com.cleanroommc.multiblocked.util.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
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
        if (Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Reloaded Shaders"));
        }
        if (cmd.equals("mbd_test")) {
            ParticleManager.INSTANCE.clearAllEffects(true);
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
//        ResourceLocation texture = new ResourceLocation(Multiblocked.MODID, "textures/fx/fx.png");
//        CommonParticle particle = new CommonParticle(mc.world, player.posX, player.posY + 2, player.posZ + 1);
//        particle.isBackLayer = false;
//        particle.setScale(1);
//        particle.setGravity(1);
//        particle.setLife(20000);
//        particle.setTexture(texture);
//        ParticleManager.INSTANCE.addEffect(particle);
            LaserBeamParticle particle = new LaserBeamParticle(mc.world, new Vector3(player.getPosition()).add(0, 2, 0), new Vector3(player.getPosition()).add(2, 0, 2))
                    .setEmit(0.1f)
                    .setHeadWidth(0.3f)
                    .setBody(new ResourceLocation(Multiblocked.MODID,"textures/fx/laser.png")) // create a beam particle and set its texture.
                    .setHead(new ResourceLocation(Multiblocked.MODID,"textures/fx/laser_start.png")); // create a beam particle and set its texture.
            ParticleManager.INSTANCE.addEffect(particle);
        } else if (cmd.equals("mbd_reload_shaders")) {
            Shaders.reload();
        }
        return null;
    }
}
