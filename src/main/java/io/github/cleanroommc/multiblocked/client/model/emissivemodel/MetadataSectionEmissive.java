package io.github.cleanroommc.multiblocked.client.model.emissivemodel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.cleanroommc.multiblocked.Multiblocked;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class MetadataSectionEmissive implements IMetadataSection {
    public static final String SECTION_NAME = Multiblocked.MODID;
    private static final Map<ResourceLocation, MetadataSectionEmissive> METADATA_CACHE = new HashMap<>();

    public final boolean emissive;

    public MetadataSectionEmissive() {
        this.emissive = false;
    }

    public MetadataSectionEmissive(boolean emissive) {
        this.emissive = emissive;
    }

    @Nullable
    public static MetadataSectionEmissive getMetadata(ResourceLocation res) {
        if (METADATA_CACHE.containsKey(res)) {
            return METADATA_CACHE.get(res);
        }
        MetadataSectionEmissive ret;
        try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(res)) {
            ret = resource.getMetadata(MetadataSectionEmissive.SECTION_NAME);
        } catch (Exception e) {
            ret = null;
        }
        METADATA_CACHE.put(res, ret);
        return ret;
    }

    public static boolean isEmissive(TextureAtlasSprite sprite) {
        MetadataSectionEmissive ret = getMetadata(spriteToAbsolute(new ResourceLocation(sprite.getIconName())));
        return ret != null && ret.emissive;
    }

    public static ResourceLocation spriteToAbsolute(ResourceLocation sprite) {
        if (!sprite.getPath().startsWith("textures/")) {
            sprite = new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath());
        }
        if (!sprite.getPath().endsWith(".png")) {
            sprite = new ResourceLocation(sprite.getNamespace(), sprite.getPath() + ".png");
        }
        return sprite;
    }

    public static class Serializer implements IMetadataSectionSerializer<MetadataSectionEmissive> {

        @Override
        public @Nullable
        MetadataSectionEmissive deserialize(@Nullable JsonElement json, @Nullable Type typeOfT, @Nullable JsonDeserializationContext context) throws JsonParseException {
            if (json != null && json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                if (obj.has("emissive")) {
                    JsonElement element = obj.get("emissive");
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
                        return new MetadataSectionEmissive(element.getAsBoolean());
                    }
                }
            }
            return null;
        }

        @Override
        public @Nonnull
        String getSectionName() {
            return SECTION_NAME;
        }
    }
}
