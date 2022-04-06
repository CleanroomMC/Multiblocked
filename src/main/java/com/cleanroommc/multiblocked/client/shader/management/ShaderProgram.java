package com.cleanroommc.multiblocked.client.shader.management;

import codechicken.lib.render.OpenGLUtils;
import com.cleanroommc.multiblocked.client.shader.uniform.IUniformCallback;
import com.cleanroommc.multiblocked.client.shader.uniform.UniformCache;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL20;

import java.util.Set;

@SideOnly(Side.CLIENT)
public class ShaderProgram {

	public final int programId;
	public final Set<Shader> shaders;
	public final UniformCache uniformCache;
	private boolean unLinked;

	public ShaderProgram() {
		this.programId = GL20.glCreateProgram();
		this.shaders = new ReferenceOpenHashSet<>();
		if (this.programId == 0) {
			throw new IllegalStateException("Unable to create ShaderProgram.");
		}
		this.uniformCache = new UniformCache(this.programId);
	}

	public ShaderProgram attach(Shader loader) {
		if (loader == null) return this;
		if (this.shaders.contains(loader)) {
			throw new IllegalStateException(String.format("Unable to attach Shader as it is already attached:\n%s", loader.source));
		}
		this.shaders.add(loader);
		loader.attachShader(this);
		this.unLinked = true;
		return this;
	}

	public void use(IUniformCallback callback) {
		this.use();
		callback.apply(uniformCache);
	}

	public void use() {
		if (unLinked) {
			this.uniformCache.invalidate();
			GL20.glLinkProgram(programId);
			if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
				throw new RuntimeException(String.format("ShaderProgram validation has failed!\n%s", OpenGLUtils.glGetProgramInfoLog(programId)));
			}
			this.unLinked = false;
		}
		GL20.glUseProgram(programId);
	}

	public void release() {
		GL20.glUseProgram(0);
	}

	public void delete() {
		if (this.programId != 0) {
			GL20.glDeleteProgram(programId);
		}
	}

}
