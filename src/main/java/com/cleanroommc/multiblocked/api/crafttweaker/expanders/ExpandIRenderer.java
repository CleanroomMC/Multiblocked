package com.cleanroommc.multiblocked.api.crafttweaker.expanders;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.B3DRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.GTRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.GeoComponentRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.OBJRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.TextureParticleRenderer;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethodStatic;

import java.util.Arrays;

@ZenRegister
@ZenExpansion("mods.multiblocked.client.IRenderer")
public class ExpandIRenderer {

    @ZenMethodStatic
    public static IRenderer createB3DRenderer(String model) {
        return new B3DRenderer(new ResourceLocation(model));
    }

    @ZenMethodStatic
    public static IRenderer createOBJRenderer(String model, boolean flip) {
        return new OBJRenderer(new ResourceLocation(model), flip);
    }

    @ZenMethodStatic
    @Optional.Method(modid = Multiblocked.MODID_GEO)
    public static IRenderer createGeoRenderer(String modelName, boolean isGlobal) {
        return new GeoComponentRenderer(modelName, isGlobal);
    }

    @ZenMethodStatic
    public static IRenderer createGTRenderer(String base, String... overlay) {
        return new GTRenderer(new ResourceLocation(base), Arrays.stream(overlay).map(ResourceLocation::new).toArray(ResourceLocation[]::new));
    }

    @ZenMethodStatic
    public static IRenderer createIModelRenderer(String model) {
        return new IModelRenderer(new ResourceLocation(model));
    }

    @ZenMethodStatic
    public static IRenderer createBlockStateRenderer(IBlockState blockState) {
        return new BlockStateRenderer(CraftTweakerMC.getBlockState(blockState));
    }

    @ZenMethodStatic
    public static IRenderer createTextureParticleRenderer(String texture, int renderRange, int light, float scale, boolean isAddBlend, boolean isBackLayer, boolean isShader) {
        TextureParticleRenderer renderer =  new TextureParticleRenderer(new ResourceLocation(texture));
        renderer.renderRange = renderRange;
        renderer.isAddBlend = isAddBlend;
        renderer.isBackLayer = isBackLayer;
        renderer.scale = scale;
        renderer.isShader = isShader;
        renderer.light = light;
        return renderer;
    }

}
