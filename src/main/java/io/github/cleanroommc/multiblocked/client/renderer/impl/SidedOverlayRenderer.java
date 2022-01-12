package io.github.cleanroommc.multiblocked.client.renderer.impl;


import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.client.model.ModelFactory;
import io.github.cleanroommc.multiblocked.client.model.emissivemodel.EmissiveBakedModel;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import io.github.cleanroommc.multiblocked.util.ResourceUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.EnumMap;
import java.util.Map;

import static io.github.cleanroommc.multiblocked.client.ClientProxy.registerNeeds;

public class SidedOverlayRenderer implements IRenderer {
    @SideOnly(Side.CLIENT)
    private static IModel model;

    public Map<RelativeDirection, String> paths;
    @SideOnly(Side.CLIENT)
    private Map<RelativeDirection, TextureAtlasSprite> sprites;
    private Map<RelativeDirection, TextureAtlasSprite> spritesLayer2;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite particles;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite void_texture;
    @SideOnly(Side.CLIENT)
    private IBakedModel itemModel;

    public SidedOverlayRenderer(Map<RelativeDirection, String> paths) {
        this.paths = paths;
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.sprites = new EnumMap<>(RelativeDirection.class);
            this.spritesLayer2 = new EnumMap<>(RelativeDirection.class);
            registerNeeds.add(this);
        }
    }

    @Override
    public void renderItem(ItemStack stack) {
        RenderItem ri = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        if (itemModel == null) {
            itemModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM,
                    location -> {
                        String[] strings = location.getPath().split("_");
                        if (strings.length < 2) return void_texture;
                        boolean isBot = strings[0].equals("overlay/bot");
                        EnumFacing facing = EnumFacing.byName(strings[1]);
                        RelativeDirection dir = RelativeDirection.getRealFacing(EnumFacing.NORTH, facing);
                        if (isBot) {
                            return sprites.getOrDefault(dir, particles);
                        } else {
                            return spritesLayer2.getOrDefault(dir, void_texture);
                        }
                    });
        }
        ri.renderItem(stack, itemModel);
    }

    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED) {
            ComponentTileEntity<?> component = (ComponentTileEntity<?>) blockAccess.getTileEntity(pos);
            BlockModelRenderer blockModelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
            assert component != null;
            EnumFacing frontFacing = component.getFrontFacing();
            IBakedModel bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK,
                    location -> {
                        String[] strings = location.getPath().split("_");
                        if (strings.length < 2) return void_texture;
                        boolean isBot = strings[0].equals("overlay/bot");
                        EnumFacing facing = EnumFacing.byName(strings[1]);
                        RelativeDirection dir = RelativeDirection.getRealFacing(frontFacing, facing);
                        if (isBot) {
                            return sprites.getOrDefault(dir, particles);
                        } else {
                            return spritesLayer2.getOrDefault(dir, void_texture);
                        }
                    });
            blockModelRenderer.renderModel(blockAccess, new EmissiveBakedModel(bakedModel), state, pos, buffer, true);
        }
        return false;
    }

    @Override
    public void register(TextureMap map) {
        if (model == null) {
            model = ModelFactory.ModelTemplate.CUBE_2_LAYER.getModel();
            void_texture = map.registerSprite(new ResourceLocation(Multiblocked.MODID, "void"));
        }
        paths.forEach((dir, path) -> {
            TextureAtlasSprite sprite = map.registerSprite(new ResourceLocation(path));
            if (particles == null) {
                particles = sprite;
            }
            sprites.put(dir, sprite);
            ResourceLocation emissiveLocation = new ResourceLocation(path + "_layer2");
            if (ResourceUtils.isTextureExist(emissiveLocation)) {
                spritesLayer2.put(dir, map.registerSprite(emissiveLocation));
            }
        });
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return particles == null ? IRenderer.super.getParticleTexture() : particles;
    }

    @ZenClass("mods.multiblocked.client.RelativeDirection")
    @ZenRegister
    public enum RelativeDirection{
        @ZenProperty UP,
        @ZenProperty DOWN,
        @ZenProperty LEFT,
        @ZenProperty RIGHT,
        @ZenProperty FRONT,
        @ZenProperty BACK;

        @SuppressWarnings("DuplicatedCode")
        static RelativeDirection getRealFacing(EnumFacing frontFacing, EnumFacing facing) {
            switch (frontFacing) {
                case NORTH:
                    switch (facing) {
                        case NORTH: return FRONT;
                        case SOUTH: return BACK;
                        case EAST: return LEFT;
                        case WEST: return RIGHT;
                        case DOWN: return DOWN;
                        case UP: return UP;
                    }
                case SOUTH:
                    switch (facing) {
                        case NORTH: return BACK;
                        case SOUTH: return FRONT;
                        case EAST: return RIGHT;
                        case WEST: return LEFT;
                        case DOWN: return DOWN;
                        case UP: return UP;
                    }
                case EAST:
                    switch (facing) {
                        case NORTH: return RIGHT;
                        case SOUTH: return LEFT;
                        case EAST: return FRONT;
                        case WEST: return BACK;
                        case DOWN: return DOWN;
                        case UP: return UP;
                    }
                case WEST:
                    switch (facing) {
                        case NORTH: return LEFT;
                        case SOUTH: return RIGHT;
                        case EAST: return BACK;
                        case WEST: return FRONT;
                        case DOWN: return DOWN;
                        case UP: return UP;
                    }
                case UP:
                    switch (facing) {
                        case NORTH: return DOWN;
                        case SOUTH: return UP;
                        case EAST: return LEFT;
                        case WEST: return RIGHT;
                        case DOWN: return BACK;
                        case UP: return FRONT;
                    }
                case DOWN:
                    switch (facing) {
                        case NORTH: return UP;
                        case SOUTH: return DOWN;
                        case EAST: return LEFT;
                        case WEST: return RIGHT;
                        case DOWN: return FRONT;
                        case UP: return BACK;
                    }
            }
            return FRONT;
        }
    }
}
