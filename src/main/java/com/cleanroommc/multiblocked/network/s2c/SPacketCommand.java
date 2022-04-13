package com.cleanroommc.multiblocked.network.s2c;

import com.cleanroommc.multiblocked.api.crafttweaker.CTHelper;
import com.cleanroommc.multiblocked.client.particle.ShaderTextureParticle;
import com.cleanroommc.multiblocked.client.shader.Shaders;
import com.cleanroommc.multiblocked.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
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
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString(cmd));
        }
        if (cmd.equals("mbd_test")) {
            String script = "import mods.multiblocked.MBDRegistry;\n" +
                    "import mods.multiblocked.definition.ControllerDefinition;\n" +
                    "import mods.multiblocked.definition.ComponentDefinition;\n" +
                    "\n" +
                    "var definition as ComponentDefinition = MBDRegistry.getDefinition(\"multiblocked:test_controller\");\n" +
                    "\n" +
                    "if (definition instanceof ControllerDefinition) {\n" +
                    "    var test_controller = definition as ControllerDefinition;\n" +
                    "}\n" +
                    "\n";
            CTHelper.executeDynamicScript(script);
//            ParticleManager.INSTANCE.clearAllEffects(true);
//            Minecraft mc = Minecraft.getMinecraft();
//            EntityPlayer player = mc.player;
//
//            ResourceLocation texture = new ResourceLocation(Multiblocked.MODID, "textures/fx/fx.png");
//            CommonParticle particle = new CommonParticle(mc.world, player.posX, player.posY + 5, player.posZ + 1);
//            particle.isBackLayer = false;
//            particle.setScale(16);
//            particle.setLife(20000);
//            particle.setTexture(texture);

//            IParticle particle = new LaserBeamParticle(mc.world, new Vector3(player.getPosition()).add(0, 2, 0), new Vector3(player.getPosition()).add(2, 0, 2))
//                    .setEmit(0.1f)
//                    .setHeadWidth(0.3f)
//                    .setBody(new ResourceLocation(Multiblocked.MODID,"textures/fx/laser.png")) // create a beam particle and set its texture.
//                    .setHead(new ResourceLocation(Multiblocked.MODID,"textures/fx/laser_start.png")) // create a beam particle and set its texture.
//                    .setAddBlend(false);

//            ResourceLocation texture2 = new ResourceLocation(Multiblocked.MODID, "start");
//            ShaderTextureParticle particle2 = new ShaderTextureParticle(mc.world, player.posX, player.posY + 5, player.posZ + 1);
//            particle2.setBackLayer(true);
//            particle2.setScale(16);
//            particle2.setLightingMap(15, 15);
//            particle2.setImmortal();
//
//            particle2.setTexture(texture2);
//
//            ParticleManager.INSTANCE.addEffect(particle2);

        } else if (cmd.equals("mbd_reload_shaders")) {

            Shaders.reload();
            ShaderTextureParticle.clearShaders();
            ShaderTextureParticle.FBOShaderHandler.FBO.deleteFramebuffer();
            ShaderTextureParticle.FBOShaderHandler.FBO.createFramebuffer(1024, 1024);
        }
        return null;
    }
}
