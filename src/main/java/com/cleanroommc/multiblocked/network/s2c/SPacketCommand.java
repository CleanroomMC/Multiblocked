package com.cleanroommc.multiblocked.network.s2c;

import com.cleanroommc.multiblocked.client.particle.ShaderTextureParticle;
import com.cleanroommc.multiblocked.client.shader.Shaders;
import com.cleanroommc.multiblocked.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
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
        if (cmd.startsWith("nbt: ")) {
            String tag = cmd.substring(5);
            if (Minecraft.getMinecraft().player != null) {
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString(
                        TextFormatting.RED + "tag has been copied to the paste board"));
                GuiScreen.setClipboardString(tag);
            }

        } else if (cmd.equals("reload_shaders")) {
            Shaders.reload();
            ShaderTextureParticle.clearShaders();
            ShaderTextureParticle.FBOShaderHandler.FBO.deleteFramebuffer();
            ShaderTextureParticle.FBOShaderHandler.FBO.createFramebuffer(1024, 1024);
        }
        return null;
    }
}
