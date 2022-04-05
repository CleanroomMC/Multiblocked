package com.cleanroommc.multiblocked.client.shader.management;

import com.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SideOnly(Side.CLIENT)
public class Shader {
    public final ShaderType shaderType;
    public final String source;
    private int shaderId;
    private boolean isCompiled;

    public Shader(ShaderType type, String source) {
        this.shaderType = type;
        this.source = source;
        this.shaderId = GL20.glCreateShader(shaderType.shaderMode);
        if (this.shaderId == 0) {
            Multiblocked.LOGGER.error("GL Shader Allocation Fail!");
            throw new RuntimeException("GL Shader Allocation Fail!");
        }
    }

    public void attachShader(ShaderProgram program) {
        if (!isCompiled) compileShader();
        OpenGlHelper.glAttachShader(program.programId, this.shaderId);
    }

    public void deleteShader() {
        if (shaderId == 0) return;
        OpenGlHelper.glDeleteShader(this.shaderId);
        shaderId = 0;
    }

    public Shader compileShader() {
        if (!this.isCompiled && shaderId != 0) {
            GL20.glShaderSource(this.shaderId, source);
            GL20.glCompileShader(this.shaderId);
            if (GL20.glGetShaderi(this.shaderId, OpenGlHelper.GL_COMPILE_STATUS) == 0) {
                int maxLength = GL20.glGetShaderi(this.shaderId, 35716);
                String error = String.format("Unable to compile %s shader object:\n%s", this.shaderType.name(), GL20.glGetShaderInfoLog(this.shaderId, maxLength));
                Multiblocked.LOGGER.error(error);
                throw new IllegalStateException(error);
            }
            this.isCompiled = true;
        }
        return this;
    }

    public static Shader loadShader(ShaderType type, String rawShader) {
        return new Shader(type, rawShader).compileShader();
    }

    public static Shader loadShader(ShaderType type, ResourceLocation resourceLocation) throws IOException {
        IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
        InputStream stream = iresource.getInputStream();
        StringBuilder sb = new StringBuilder();
        BufferedReader bin = new BufferedReader(new InputStreamReader(stream));
        String line;
        while((line = bin.readLine()) != null) {
            sb.append(line).append('\n');
        }
        stream.close();
        IOUtils.closeQuietly(iresource);
        return loadShader(type, sb.toString());
    }

    @SideOnly(Side.CLIENT)
    public static enum ShaderType {
        VERTEX("vertex", ".vert", OpenGlHelper.GL_VERTEX_SHADER),
        FRAGMENT("fragment", ".frag", OpenGlHelper.GL_FRAGMENT_SHADER);

        public final String shaderName;
        public final String shaderExtension;
        public final int shaderMode;

        ShaderType(String shaderNameIn, String shaderExtensionIn, int shaderModeIn) {
            this.shaderName = shaderNameIn;
            this.shaderExtension = shaderExtensionIn;
            this.shaderMode = shaderModeIn;
        }
    }
}
