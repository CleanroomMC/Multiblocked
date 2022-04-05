package com.cleanroommc.multiblocked.client.shader.management;

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
	private final Set<Shader> loaders;
	private final UniformCache uniformCache;

	public ShaderProgram() {
		this.programId = GL20.glCreateProgram();
		this.loaders = new ReferenceOpenHashSet<>();
		if (this.programId == 0) {
			throw new IllegalStateException("Unable to create ShaderProgram.");
		}
		this.uniformCache = new UniformCache(this.programId);
	}

	public ShaderProgram attach(Shader loader) {
		if (this.loaders.contains(loader)) {
			throw new IllegalStateException(String.format("Unable to attach Shader as it is already attached:\n%s", loader.source));
		}
		this.loaders.add(loader);
		loader.attachShader(this);
		return this;
	}

	public void use(IUniformCallback callback) {
		this.uniformCache.invalidate();
		GL20.glLinkProgram(programId);
		GL20.glUseProgram(programId);
		callback.apply(uniformCache);
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
