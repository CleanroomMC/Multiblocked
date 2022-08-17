package com.cleanroommc.multiblocked.api.definition;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.json.IRendererTypeAdapterFactory;
import com.cleanroommc.multiblocked.api.sound.SoundState;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.util.ShapeUtils;
import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;


import java.util.*;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/8/8
 * @implNote StatusProperties
 */
public class StatusProperties {
    public final static String UNFORMED = "unformed";
    public final static String IDLE = "idle";
    public final static String WORKING = "working";
    public final static String SUSPEND = "suspend";

    public final static StatusProperties EMPTY = new StatusProperties(UNFORMED);

    public final boolean builtin;
    public String name;
    public StatusProperties parent;
    public Supplier<IRenderer> renderer;
    public Integer lightEmissive;
    public List<AxisAlignedBB> shape;
    public SoundState sound;
    private Map<EnumFacing, List<AxisAlignedBB>> cache;
    public StatusProperties(String name) {
        this(name, null, false);
    }

    public StatusProperties(String name, StatusProperties parent) {
        this(name, parent, false);
    }

    public StatusProperties(String name, StatusProperties parent, boolean builtin) {
        this.name = name;
        this.parent = parent;
        this.builtin = builtin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(StatusProperties parent) {
        this.parent = parent;
    }

    public StatusProperties getParent() {
        return parent;
    }

    public IRenderer getRenderer() {
        return renderer == null ? parent == null ? null : parent.getRenderer() : renderer.get();
    }

    public int getLightEmissive() {
        return lightEmissive == null ? parent == null ? 0 : parent.getLightEmissive() : lightEmissive;
    }

    public void setLightEmissive(Integer lightEmissive) {
        this.lightEmissive = lightEmissive;
    }

    public List<AxisAlignedBB> getShape() {
        return shape == null ? parent == null ? Collections.singletonList(Block.FULL_BLOCK_AABB) : parent.getShape() : shape;
    }

    public List<AxisAlignedBB> getShape(EnumFacing direction) {
        if (this.cache == null) {
            this.cache = new EnumMap<>(EnumFacing.class);
        }
        List<AxisAlignedBB> shape = getShape();
        if (shape.isEmpty() || shape.contains(Block.FULL_BLOCK_AABB)) return shape;
        return this.cache.computeIfAbsent(direction, dir -> ShapeUtils.rotate(shape, dir));
    }

    public SoundState getSound() {
        return sound == null ? parent == null ? SoundState.EMPTY : parent.getSound() : sound;
    }

    public void setRenderer(Supplier<IRenderer> renderer) {
        this.renderer = renderer;
    }

    public void setLightEmissive(int lightEmissive) {
        this.lightEmissive = lightEmissive;
    }

    public void setShape(List<AxisAlignedBB> shape) {
        this.shape = shape;
        this.cache = null;
    }

    public void setSound(SoundState sound) {
        this.sound = sound;
    }

    public void setRenderer(IRenderer renderer) {
        this.renderer = () -> renderer;
    }

    // ******* serialize ******* //

    public void fromJson(JsonObject json) {
        if (json.has("renderer")) {
            JsonElement jsonElement = json.get("renderer");
            if (IRendererTypeAdapterFactory.INSTANCE.isPostRenderer(jsonElement)) {
                setRenderer(Suppliers.memoize(() -> Multiblocked.GSON.fromJson(jsonElement, IRenderer.class)));
            } else {
                setRenderer(Multiblocked.GSON.fromJson(jsonElement, IRenderer.class));
            }
        }
        if (json.has("lightEmissive")) {
            lightEmissive = json.get("lightEmissive").getAsInt();
        }
        if (json.has("shape")) {
            shape = new ArrayList<>();
            JsonArray array = json.get("shape").getAsJsonArray();
            for (JsonElement element : array) {
                JsonArray a = element.getAsJsonArray();
                shape.add(new AxisAlignedBB(
                        a.get(0).getAsFloat(),
                        a.get(1).getAsFloat(),
                        a.get(2).getAsFloat(),
                        a.get(3).getAsFloat(),
                        a.get(4).getAsFloat(),
                        a.get(5).getAsFloat()
                ));
            }
        }
        if (json.has("sound")) {
            sound = Multiblocked.GSON.fromJson(json.get("sound"), SoundState.class);
            if (sound.sound.equals(SoundState.EMPTY.sound)) {
                sound = SoundState.EMPTY;
            } else {
                sound.status = name;
            }
        }
    }

    public JsonObject toJson(JsonObject json) {
        if (renderer != null) {
            json.add("renderer", Multiblocked.GSON.toJsonTree(renderer.get()));
        }
        if (lightEmissive != null) {
            json.addProperty("lightEmissive", lightEmissive);
        }
        if (shape != null) {
            JsonArray array = new JsonArray();
            for (AxisAlignedBB aabb : shape) {
                JsonArray a = new JsonArray();
                a.add(aabb.minX);
                a.add(aabb.minY);
                a.add(aabb.minZ);
                a.add(aabb.maxX);
                a.add(aabb.maxY);
                a.add(aabb.maxZ);
                array.add(a);
            }
            json.add("shape", array);
        }
        if (parent != null) {
            json.addProperty("parent", parent.name);
        }
        if (sound != null) {
            json.add("sound", Multiblocked.GSON.toJsonTree(sound));
        }
        return json;
    }
}
