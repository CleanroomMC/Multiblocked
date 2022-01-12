package io.github.cleanroommc.multiblocked.api.definition;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.IDrops;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.ILeftClick;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.INeighborChanged;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.IRightClick;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

/**
 * Definition of a component.
 */
@ZenClass("mods.multiblocked.definition.ComponentDefinition")
@ZenRegister
public abstract class ComponentDefinition {
    public final ResourceLocation location;
    public final Class<? extends ComponentTileEntity<?>> clazz;
    @ZenProperty
    public IRenderer baseRenderer;
    @ZenProperty
    public IRenderer formedRenderer;
    @ZenProperty
    public boolean isOpaqueCube;
    @ZenProperty
    public IDrops onDrops;
    @ZenProperty
    public ILeftClick onLeftClick;
    @ZenProperty
    public IRightClick onRightClick;
    @ZenProperty
    public INeighborChanged onNeighborChanged;

    protected ComponentDefinition(ResourceLocation location, Class<? extends ComponentTileEntity<?>> clazz) {
        this.location = location;
        this.clazz = clazz;
        this.baseRenderer = null;
        this.isOpaqueCube = true;
    }

    public ComponentTileEntity<?> createNewTileEntity(){
        try {
            ComponentTileEntity<?> component = clazz.newInstance();
            component.setDefinition(this);
            return component;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public IRenderer getRenderer() {
        return baseRenderer;
    }

    @Override
    @ZenMethod("getLocation")
    @ZenGetter("location")
    public String toString() {
        return location.toString();
    }
}
