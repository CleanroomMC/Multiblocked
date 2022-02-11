package io.github.cleanroommc.multiblocked.api.capability;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.ContentWidget;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.stream.Collectors;

/**
 * Used to detect whether a machine has a certain capability. And provide its capability in proxy {@link CapabilityProxy}.
 *
 */
@ZenClass("mods.multiblocked.capability.capability")
@ZenRegister
public abstract class MultiblockCapability<T> {
    @SideOnly(Side.CLIENT)
    private EnumMap<IO, IBlockState[]> scannedStates;
    @ZenProperty
    public final String name;
    @ZenProperty
    public final int color;

    public MultiblockCapability(String name, int color) {
        this.name = name;
        this.color = color;
        if (Multiblocked.isClient()) {
            scannedStates = new EnumMap<>(IO.class);
        }
    }

    public String getUnlocalizedName() {
        return "multiblocked.capability." + name;
    }

    /**
     * detect whether this block has capability
     */
    public abstract boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity);

    /**
     * deep copy of this content. recipe need it for searching and such things
     */
    public abstract T copyInner(T content);
    
    /**
     * create a proxy of this block.
     */
    public abstract CapabilityProxy<? extends T> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity);

    /**
     * Create a Widget of given contents
     */
    public ContentWidget<? super T> createContentWidget() {
        return new ContentWidget<Object>() {
            @Override
            protected void onContentUpdate() {
                if (Multiblocked.isClient()) {
                    setHoverTooltip(I18n.format("multiblocked.content.miss", io, I18n.format(MultiblockCapability.this.getUnlocalizedName()), content.toString()));
                }
            }
        }.setBackground(new ColorRectTexture(color));
    }

    /**
     * get candidates for rendering in jei.
     */
    public IBlockState[] getCandidates(IO io) {
        if (Multiblocked.isClient()) {
            return scanForCandidates(io);
        }
        return new IBlockState[0];
    }

    @SideOnly(Side.CLIENT)
    protected IBlockState[] scanForCandidates(IO io){
        if (!scannedStates.containsKey(io)) {
            scannedStates.put(io, ForgeRegistries.BLOCKS.getValuesCollection()
                    .stream()
                    .map(Block::getDefaultState)
                    .filter(s -> {
                        try {
                            TileEntity tile = s.getBlock().createTileEntity(Minecraft.getMinecraft().world, s);
                            if (tile == null) return false;
                            return isBlockHasCapability(io, tile);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .toArray(IBlockState[]::new));
            Multiblocked.LOGGER.info("Available blocks for {} capability: {}", name,
                    Arrays.stream(scannedStates.get(io))
                            .map(IBlockState::getBlock)
                            .distinct()
                            .map(Block::getLocalizedName)
                            .collect(Collectors.joining(", ")));
        }
        return scannedStates.get(io);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
