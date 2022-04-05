package com.cleanroommc.multiblocked.api.gui.texture;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import com.cleanroommc.multiblocked.client.shader.Shaders;
import com.cleanroommc.multiblocked.client.shader.management.Shader;
import com.cleanroommc.multiblocked.client.shader.management.ShaderManager;
import com.cleanroommc.multiblocked.client.shader.management.ShaderProgram;
import com.cleanroommc.multiblocked.client.shader.uniform.UniformCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

public class ShaderTexture implements IGuiTexture {
    @SideOnly(Side.CLIENT)
    private ShaderProgram program;
    @SideOnly(Side.CLIENT)
    private Shader shader;
    private float resolution = 2;
    private Consumer<UniformCache> uniformCache;

    private ShaderTexture() {

    }

    public void dispose() {
        if (shader != null) {
            shader.deleteShader();
        }
        if (program != null) {
            program.delete();
        }
        shader = null;
        program = null;
    }

    public void updateRawShader(String rawShader) {
        if (Multiblocked.isClient() && ShaderManager.allowedShader()) {
            dispose();
            shader = new Shader(Shader.ShaderType.FRAGMENT, rawShader).compileShader();
            program = new ShaderProgram();
            program.attach(shader);
        }
    }

    @SideOnly(Side.CLIENT)
    private ShaderTexture(Shader shader) {
        if (shader == null) return;
        this.program = new ShaderProgram();
        this.shader = shader;
        program.attach(shader);
    }

    public static ShaderTexture createShader(ResourceLocation location) {
        if (Multiblocked.isClient() && ShaderManager.allowedShader()) {
            Shader shader = Shaders.load(Shader.ShaderType.FRAGMENT, location);
            return new ShaderTexture(shader);
        } else {
            return new ShaderTexture();
        }
    }

    public static ShaderTexture createRawShader(String rawShader) {
        if (Multiblocked.isClient() && ShaderManager.allowedShader()) {
            Shader shader = new Shader(Shader.ShaderType.FRAGMENT, rawShader).compileShader();
            return new ShaderTexture(shader);
        } else {
            return new ShaderTexture();
        }
    }

    public ShaderTexture setUniformCache(Consumer<UniformCache> uniformCache) {
        this.uniformCache = uniformCache;
        return this;
    }

    public ShaderTexture setResolution(float resolution) {
        this.resolution = resolution;
        return this;
    }

    public float getResolution() {
        return resolution;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(int mouseX, int mouseY, double x, double y, int width, int height) {
        if (program != null) {
            Minecraft mc = Minecraft.getMinecraft();
            final float time;
            if (mc.player != null) {
                time = (mc.player.ticksExisted + mc.getRenderPartialTicks()) / 20;
            } else {
                time = System.currentTimeMillis() / 1000f;
            }
            program.use(cache->{
                float mX = (float) MathHelper.clamp((mouseX - x), 0, width);
                float mY = (float) MathHelper.clamp((mouseY - y), 0, height);
                cache.glUniform2F("u_resolution", width * resolution, height * resolution);
                cache.glUniform2F("u_mouse", mX * resolution, mY * resolution);
                cache.glUniform1F("u_time", time);
                if (uniformCache != null) {
                    uniformCache.accept(cache);
                }
            });
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(x, y + height, 0.0D).tex(0.0D, 0.0D).endVertex();
            buffer.pos(x + width, y + height, 0.0D).tex(1.0D, 0.0D).endVertex();
            buffer.pos(x + width, y, 0.0D).tex(1.0D, 1.0D).endVertex();
            buffer.pos(x, y, 0.0D).tex(0.0D, 1.0D).endVertex();
            tessellator.draw();
            program.release();
        } else {
            DrawerHelper.drawText("Error compiling shader", (float)x + 2, (float)y + 2, 1, 0xffff0000);
        }
    }
}
