package com.cleanroommc.multiblocked.client.particle;

import com.cleanroommc.multiblocked.client.shader.Shaders;
import com.cleanroommc.multiblocked.client.shader.management.Shader;
import com.cleanroommc.multiblocked.client.shader.management.ShaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.glGetInteger;

/**
 * using shader program render the texture in FBO and then bind this texture for particle.
 */
@SideOnly(Side.CLIENT)
public class ShaderTextureParticle extends CommonParticle {
    private static final Map<ResourceLocation, FBOShaderHandler> textureMap = new HashMap<>();

    public ShaderTextureParticle(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
    }

    public static void clearShaders(){
        textureMap.values().forEach(handler -> handler.shader.deleteShader());
        textureMap.clear();
    }

    @Override
    public IParticleHandler getGLHandler() {
        return textureMap.computeIfAbsent(customTexture, shader -> new FBOShaderHandler(Shaders.load(Shader.ShaderType.FRAGMENT, customTexture)));
    }

    public static class FBOShaderHandler implements IParticleHandler {
        protected final Shader shader;
        public final static Framebuffer FBO = new Framebuffer(1024, 1024, false);
        private static final IntBuffer VIEWPORT_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();

        static {
            FBO.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        }

        public FBOShaderHandler(Shader shader) {
            this.shader = shader;
        }

        @Override
        public final void preDraw(BufferBuilder buffer) {
            if (shader != null) {
                int lastID = glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);

                GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER); // read original port
                VIEWPORT_BUFFER.rewind();
                int x = VIEWPORT_BUFFER.get();
                int y = VIEWPORT_BUFFER.get();
                int width = VIEWPORT_BUFFER.get();
                int height = VIEWPORT_BUFFER.get();
                VIEWPORT_BUFFER.rewind();

                FBO.framebufferClear();
                ShaderManager.getInstance().renderFullImageInFramebuffer(FBO, shader, uniformCache -> {
                    Minecraft mc = Minecraft.getMinecraft();
                    float time;
                    if (mc.player != null) {
                        time = (mc.player.ticksExisted + mc.getRenderPartialTicks()) / 20;
                    } else {
                        time = System.currentTimeMillis() / 1000f;
                    }
                    uniformCache.glUniform1F("iTime", time);
                });

                GlStateManager.viewport(x, y, width, height);
                OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
                GlStateManager.bindTexture(FBO.framebufferTexture);

            }
            GlStateManager.color(1,1,1,1);
            buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        }

        @Override
        public final void postDraw(BufferBuilder buffer) {
            Tessellator.getInstance().draw();
        }

    }
}
