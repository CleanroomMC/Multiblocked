package com.cleanroommc.multiblocked.client.shader.management;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.client.shader.Shaders;
import com.cleanroommc.multiblocked.client.shader.uniform.UniformCache;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class ShaderManager {

	private static final BooleanSupplier optifine$shaderPackLoaded;

	static {
		Field shaderPackLoadedField = null;
		try {
			Class<?> shadersClass = Class.forName("net.optifine.shaders.Shaders");
			shaderPackLoadedField = shadersClass.getDeclaredField("shaderPackLoaded");
		} catch (Exception ignored) {
			Multiblocked.LOGGER.debug("Cannot detect Optifine, not going to do any specific compatibility patches.");
		}
		if (shaderPackLoadedField == null) {
			optifine$shaderPackLoaded = () -> false;
		} else {
			Field finalShaderPackLoadedField = shaderPackLoadedField;
			optifine$shaderPackLoaded = () -> {
				try {
					return finalShaderPackLoadedField.getBoolean(null);
				} catch (IllegalAccessException ignored) { }
				return false;
			};
		}
	}

	private static final ShaderManager INSTANCE = new ShaderManager();

	public static ShaderManager getInstance() {
		return INSTANCE;
	}

	public static boolean allowedShader() {
		return OpenGlHelper.shadersSupported;
	}

	public static boolean isShadersCompatible() {
		return OpenGlHelper.areShadersSupported() && !optifine$shaderPackLoaded.getAsBoolean();
	}

	private final Reference2ReferenceMap<Shader, ShaderProgram> programs;

	private ShaderManager() {
		this.programs = new Reference2ReferenceOpenHashMap<>();
	}

	public Framebuffer renderFullImageInFramebuffer(Framebuffer fbo, Shader frag, Consumer<UniformCache> consumeCache) {
		if (fbo == null || frag == null || !allowedShader()) {
			return fbo;
		}
		// int lastID = glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		fbo.bindFramebuffer(true);
		ShaderProgram program = programs.get(frag);
		if (program == null) {
			programs.put(frag, program = new ShaderProgram());
			program.attach(Shaders.IMAGE_V).attach(frag);
		}
		program.use(cache -> {
			cache.glUniform2F("u_resolution", fbo.framebufferWidth, fbo.framebufferHeight);
			if (consumeCache != null) {
				consumeCache.accept(cache);
			}
		});
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(-1, 1, 0).tex(0, 0).endVertex();
		buffer.pos(-1, -1, 0).tex(0, 1).endVertex();
		buffer.pos(1, -1, 0).tex(1, 1).endVertex();
		buffer.pos(1, 1, 0).tex(1, 0).endVertex();
		tessellator.draw();
		program.release();
		// GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
		// OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
		return fbo;
	}

}
