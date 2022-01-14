package io.github.cleanroommc.multiblocked.api.definition;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.mc1120.item.MCItemStack;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.IDrops;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.IGetOutputRedstoneSignal;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.ILeftClick;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.INeighborChanged;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.IRightClick;
import io.github.cleanroommc.multiblocked.api.pattern.BlockInfo;
import io.github.cleanroommc.multiblocked.api.pattern.TraceabilityPredicate;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

/**
 * Definition of a component.
 */
@ZenClass("mods.multiblocked.definition.ComponentDefinition")
@ZenRegister
public class ComponentDefinition {
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
    @ZenProperty
    public IGetOutputRedstoneSignal getOutputRedstoneSignal;

    protected ComponentDefinition(ResourceLocation location, Class<? extends ComponentTileEntity<?>> clazz) {
        this.location = location;
        this.clazz = clazz;
        this.baseRenderer = null;
        this.isOpaqueCube = true;
    }

    public ComponentTileEntity<?> createNewTileEntity(World world){
        try {
            ComponentTileEntity<?> component = clazz.newInstance();
            component.setDefinition(this);
            component.setWorld(world);
            return component;
        } catch (InstantiationException | IllegalAccessException e) {
            Multiblocked.LOGGER.error(e);
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

    public ItemStack getStackForm() {
        return new ItemStack(MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(location), 1);
    }

    @Optional.Method(modid = Multiblocked.MODID_CT)
    @ZenMethod("getStackForm")
    public IItemStack stackForm(){
        return new MCItemStack(getStackForm());
    }

    @ZenMethod
    @ZenGetter
    public TraceabilityPredicate selfPredicate() {
        return new TraceabilityPredicate(state -> {
            Block block = state.getBlockState().getBlock();
            return block instanceof BlockComponent && ((BlockComponent) block).definition == this;
        }, ()-> new BlockInfo[]{new BlockInfo(MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(location))}).setCenter();
    }
}
