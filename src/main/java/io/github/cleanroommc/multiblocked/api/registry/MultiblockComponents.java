package io.github.cleanroommc.multiblocked.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

public class MultiblockComponents {
    public static final BiMap<ResourceLocation, ComponentTileEntity> REGISTRY = HashBiMap.create();
    public static final Map<ComponentTileEntity, BlockComponent> COMPONENT_BLOCKS = Maps.newHashMap();

    public static void registerComponent(ComponentTileEntity component) {
        REGISTRY.put(component.getLocation(), component);
        COMPONENT_BLOCKS.computeIfAbsent(component, BlockComponent::new).setRegistryName(component.getUnlocalizedName());
    }

    public static void registerTileEntity() {
        for (ComponentTileEntity component : REGISTRY.values()) {
            GameRegistry.registerTileEntity(component.getClass(), component.getLocation());
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        COMPONENT_BLOCKS.values().forEach(BlockComponent::onModelRegister);
    }
}
