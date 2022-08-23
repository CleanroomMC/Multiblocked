package com.cleanroommc.multiblocked.api.definition;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.block.CustomProperties;
import com.cleanroommc.multiblocked.api.crafttweaker.functions.*;
import com.cleanroommc.multiblocked.api.json.IRendererTypeAdapterFactory;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.mc1120.item.MCItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Definition of a component.
 */
@ZenClass("mods.multiblocked.definition.ComponentDefinition")
@ZenRegister
public class ComponentDefinition {
    public final ResourceLocation location;
    public Class<? extends ComponentTileEntity<?>> clazz;
    public JsonObject traits;

    // ******* status properties ******* //
    public final Map<String, StatusProperties> status;

    // ******* block item properties ******* //
    @ZenProperty
    public CustomProperties properties;

    @ZenProperty
    public IDynamicRenderer dynamicRenderer;
    @ZenProperty
    public IDrops onDrops;
    @ZenProperty
    public ILeftClick onLeftClick;
    @ZenProperty
    public IRightClick onRightClick;
    @ZenProperty
    public INeighborChanged onNeighborChanged;
    @ZenProperty
    public IGetOutputRedstoneSignal getOutputRedstoneSignal;
    @ZenProperty
    public IUpdateTick updateTick;
    @ZenProperty
    public IStatusChanged statusChanged;
    @ZenProperty
    public IShouldCheckPattern shouldCheckPattern;
    @ZenProperty
    public IReceiveCustomData receiveCustomData;
    @ZenProperty
    public IWriteInitialData writeInitialData;
    @ZenProperty
    public IReadInitialData readInitialData;

    public ComponentDefinition(ResourceLocation location, Class<? extends ComponentTileEntity<?>> clazz) {
        this.location = location;
        this.clazz = clazz;
        this.status = new LinkedHashMap<>();
        this.status.put(StatusProperties.UNFORMED, new StatusProperties(StatusProperties.UNFORMED, null, true));
        this.status.put(StatusProperties.IDLE, new StatusProperties(StatusProperties.IDLE, getBaseStatus(), true));
        this.status.put(StatusProperties.WORKING, new StatusProperties(StatusProperties.WORKING, getIdleStatus(), true));
        this.status.put(StatusProperties.SUSPEND, new StatusProperties(StatusProperties.SUSPEND, getWorkingStatus(), true));
        traits = new JsonObject();
        this.properties = new CustomProperties();
    }

    public ComponentTileEntity<?> createNewTileEntity(World world){
        try {
            ComponentTileEntity<?> component = clazz.newInstance();
            component.setWorld(world);
            component.setDefinition(this);
            return component;
        } catch (InstantiationException | IllegalAccessException e) {
            Multiblocked.LOGGER.error(e);
        }
        return null;
    }

    public StatusProperties getStatus(String status) {
        return this.status.containsKey(status) ? this.status.get(status) : this.status.getOrDefault(StatusProperties.UNFORMED, StatusProperties.EMPTY);
    }

    public StatusProperties getBaseStatus() {
        return getStatus(StatusProperties.UNFORMED);
    }

    public StatusProperties getIdleStatus() {
        return getStatus(StatusProperties.IDLE);
    }

    public StatusProperties getWorkingStatus() {
        return getStatus(StatusProperties.WORKING);
    }

    public StatusProperties getSuspendStatus() {
        return getStatus(StatusProperties.SUSPEND);
    }

    public IRenderer getRenderer() {
        return getBaseStatus().getRenderer();
    }

    @Override
    @ZenMethod("getLocation")
    @ZenGetter("location")
    public String toString() {
        return location.toString();
    }

    public ItemStack getStackForm() {
        return new ItemStack(MbdComponents.COMPONENT_ITEMS_REGISTRY.get(location), 1);
    }

    @Optional.Method(modid = Multiblocked.MODID_CT)
    @ZenMethod("getStackForm")
    public IItemStack stackForm(){
        return new MCItemStack(getStackForm());
    }

    public boolean needUpdateTick() {
        return updateTick != null;
    }

    @SuppressWarnings("unchecked")
    public void setTileEntityClass(Class<?> clazz) {
        if (ComponentTileEntity.class.isAssignableFrom(clazz)) {
            this.clazz = (Class<? extends ComponentTileEntity<?>>) clazz;
        }
    }

    // ******* serialize ******* //

    public final static int VERSION = 1;

    public void fromJson(JsonObject json) {
        int version = JsonUtils.getIntOr("version", json, 0);

        if (version > VERSION) {
            throw new IllegalArgumentException(String.format("using outdated version of mbd. script is {%d}, mbd supports {%d}", version, VERSION));
        }

        if (json.has("traits")) {
            traits = json.get("traits").getAsJsonObject();
        }
        if (json.has("properties")) {
            properties = Multiblocked.GSON.fromJson(json.get("properties"), CustomProperties.class);
        }

        if (version > 0) {
            JsonObject statusJson = json.get("status").getAsJsonObject();
            getBaseStatus().fromJson(statusJson.get(StatusProperties.UNFORMED).getAsJsonObject());
            getIdleStatus().fromJson(statusJson.get(StatusProperties.IDLE).getAsJsonObject());
            getWorkingStatus().fromJson(statusJson.get(StatusProperties.WORKING).getAsJsonObject());
            getSuspendStatus().fromJson(statusJson.get(StatusProperties.SUSPEND).getAsJsonObject());
            for (Map.Entry<String, JsonElement> entry : statusJson.entrySet()) {
                parseStatus(entry.getKey(), statusJson);
            }
        } else { // legacy
            properties.rotationState = JsonUtils.getBooleanOr("allowRotate", json, true) ? CustomProperties.RotationState.ALL : CustomProperties.RotationState.NONE;
            properties.showInJei = JsonUtils.getBooleanOr("showInJei", json, properties.showInJei);
            properties.isOpaque = JsonUtils.getBooleanOr("isOpaqueCube", json, properties.isOpaque);

            if (json.has("baseRenderer")) {
                JsonElement renderer = json.get("baseRenderer");
                if (IRendererTypeAdapterFactory.INSTANCE.isPostRenderer(renderer)) {
                    getBaseStatus().setRenderer(() -> Multiblocked.GSON.fromJson(renderer, IRenderer.class));
                } else {
                    getBaseStatus().setRenderer(Multiblocked.GSON.fromJson(renderer, IRenderer.class));
                }
            }

            if (json.has("formedRenderer")) {
                JsonElement renderer = json.get("formedRenderer");
                if (IRendererTypeAdapterFactory.INSTANCE.isPostRenderer(renderer)) {
                    getIdleStatus().setRenderer(() -> Multiblocked.GSON.fromJson(renderer, IRenderer.class));
                } else {
                    getIdleStatus().setRenderer(Multiblocked.GSON.fromJson(renderer, IRenderer.class));
                }
            }

            if (json.has("workingRenderer")) {
                JsonElement renderer = json.get("workingRenderer");
                if (IRendererTypeAdapterFactory.INSTANCE.isPostRenderer(renderer)) {
                    getWorkingStatus().setRenderer(() -> Multiblocked.GSON.fromJson(renderer, IRenderer.class));
                } else {
                    getWorkingStatus().setRenderer(Multiblocked.GSON.fromJson(renderer, IRenderer.class));
                }
            }
        }
    }

    private StatusProperties parseStatus(String name, JsonObject json) {
        if (status.containsKey(name)) {
            return status.get(name);
        } else {
            StatusProperties parent = null;
            JsonObject statusJson = json.get(name).getAsJsonObject();
            if (statusJson.has("parent")) {
                String parentName = statusJson.get("parent").getAsString();
                parent = json.has(parentName) ? parseStatus(parentName, json) : null;
            }
            StatusProperties result = new StatusProperties(name, parent);
            result.fromJson(statusJson);
            status.put(name, result);
            return result;
        }
    }

    public JsonObject toJson(JsonObject json) {
        json.addProperty("version", VERSION);
        json.addProperty("location", location.toString());
        json.add("traits", traits);
        json.add("properties", Multiblocked.GSON.toJsonTree(properties));
        JsonObject statusJson = new JsonObject();
        status.forEach((name, status) -> statusJson.add(name, status.toJson(new JsonObject())));
        json.add("status", statusJson);
        return json;
    }

}
