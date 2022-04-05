package com.cleanroommc.multiblocked.client.shader;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.client.shader.management.Shader;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class Shaders {

	public static Shader IMAGE_F;
	public static Shader IMAGE_V;
	public static Shader FBM;

	public static void init() {
		IMAGE_F = load(Shader.ShaderType.FRAGMENT, new ResourceLocation(Multiblocked.MODID, "image"));
		IMAGE_V = load(Shader.ShaderType.VERTEX, new ResourceLocation(Multiblocked.MODID, "image"));
		FBM = load(Shader.ShaderType.FRAGMENT, new ResourceLocation(Multiblocked.MODID, "fbm"));
	}

	public static Map<ResourceLocation, Shader> CACHE = new HashMap<>();

	public static void reload() {
		for (Shader shader : CACHE.values()) {
			if (shader != null) {
				shader.deleteShader();
			}
		}
		CACHE.clear();
		init();
	}

	public static Shader load(Shader.ShaderType shaderType, ResourceLocation resourceLocation) {
		return CACHE.computeIfAbsent(new ResourceLocation(resourceLocation.getNamespace(), "shaders/" + resourceLocation.getPath() + shaderType.shaderExtension), key -> {
			try {
				return Shader.loadShader(shaderType, key);
			} catch (IOException e) {
				Multiblocked.LOGGER.error("load shader {} resource {} failed", shaderType, resourceLocation);
				return null;
			}
		});
	}

}
