package com.cleanroommc.multiblocked.util;

import com.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ResourceUtils {
    private static final Map<String, ResourceLocation> cachedResources = new HashMap<>();
    public static final String RESOURCE_PREFIX = Multiblocked.MODID + ":";

    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    }

    public static ResourceLocation getResource(String rs) {
        if (!cachedResources.containsKey(rs)) {
            cachedResources.put(rs, new ResourceLocation(RESOURCE_PREFIX + rs));
        }
        return cachedResources.get(rs);
    }

    public static ResourceLocation getResourceRAW(String rs) {
        if (!cachedResources.containsKey(rs)) {
            cachedResources.put(rs, new ResourceLocation(rs));
        }
        return cachedResources.get(rs);
    }

    public static void bindTexture(String rs) {
        bindTexture(getResource(rs));
    }

    public static boolean isResourceExist(String rs) {
        if (!cachedResources.containsKey(rs)) {
            InputStream inputstream = ResourceUtils.class.getResourceAsStream(String.format("/assets/%s/%s", Multiblocked.MODID, rs));
            if(inputstream == null) {
                return false;
            }
            IOUtils.closeQuietly(inputstream);
            cachedResources.put(rs, new ResourceLocation(Multiblocked.MODID, rs));
        }
        return true;
    }

    public static boolean isTextureExist(ResourceLocation textureResource) {
        InputStream inputstream = ResourceUtils.class.getResourceAsStream(String.format("/assets/%s/textures/%s.png", textureResource.getNamespace(), textureResource.getPath()));
        if(inputstream == null) {
            return false;
        }
        IOUtils.closeQuietly(inputstream);
        return true;
    }
}
