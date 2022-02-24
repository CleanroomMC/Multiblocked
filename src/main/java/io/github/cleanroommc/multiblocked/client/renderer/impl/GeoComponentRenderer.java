package io.github.cleanroommc.multiblocked.client.renderer.impl;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

import javax.annotation.Nonnull;

@SuppressWarnings("unchecked")
public class GeoComponentRenderer extends AnimatedGeoModel<GeoComponentRenderer.ComponentFactory> implements IRenderer, IGeoRenderer<GeoComponentRenderer.ComponentFactory> {
    static {
        if (Multiblocked.isClient()) {
            AnimationController.addModelFetcher((IAnimatable object) -> {
                if (object instanceof ComponentFactory) {
                    ComponentFactory factory = (ComponentFactory) object;
                    return (IAnimatableModel<Object>) factory.renderer.getGeoModelProvider();
                }
                return null;
            });
        }
    }

    public final String modelName;

    public GeoComponentRenderer(String modelName) {
        this.modelName = modelName;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderItem(ItemStack stack) {

    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasTESR() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te instanceof ComponentTileEntity<?>) {
            ComponentTileEntity<?> controller = (ComponentTileEntity<?>) te;
            ComponentFactory factory = controller.getFactory(this);
            GeoModel model = this.getModel(this.getModelLocation(factory));
            this.setLivingAnimations(factory, this.getUniqueID(factory));

            int light = te.getWorld().getCombinedLight(te.getPos(), 0);
            int lx = light % 65536;
            int ly = light / 65536;

            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            OpenGlHelper.setLightmapTextureCoords(GL11.GL_TEXTURE_2D, lx, ly);
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.translate(0, 0.01f, 0);
            GlStateManager.translate(0.5, 0, 0.5);

            rotateBlock(controller.getFrontFacing());

            Minecraft.getMinecraft().renderEngine.bindTexture(getTextureLocation(factory));
            Color renderColor = getRenderColor(factory, partialTicks);
            render(model, factory, partialTicks, (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f, (float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
            GlStateManager.popMatrix();
        }
    }

    protected void rotateBlock(EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case NORTH:
                /* There is no need to rotate by 0 */
                break;
            case EAST:
                GlStateManager.rotate(270, 0, 1, 0);
                break;
            case UP:
                GlStateManager.rotate(90, 1, 0, 0);
                break;
            case DOWN:
                GlStateManager.rotate(90, -1, 0, 0);
                break;
        }
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ComponentFactory entity) {
        return new ResourceLocation(Multiblocked.MODID, String.format("animations/%s.animation.json", modelName));
    }

    @Override
    public ResourceLocation getModelLocation(ComponentFactory animatable) {
        return new ResourceLocation(Multiblocked.MODID, String.format("geo/%s.geo.json", modelName));
    }

    @Override
    public ResourceLocation getTextureLocation(ComponentFactory entity) {
        return new ResourceLocation(Multiblocked.MODID, String.format("textures/%s.png", modelName));
    }

    @Override
    public GeoModelProvider<?> getGeoModelProvider() {
        return this;
    }

    public static class ComponentFactory implements IAnimatable {
        public final ComponentTileEntity<?> component;
        public final GeoComponentRenderer renderer;

        public ComponentFactory(ComponentTileEntity<?> component, GeoComponentRenderer renderer) {
            this.component = component;
            this.renderer = renderer;
        }

        private final AnimationFactory factory = new AnimationFactory(this);

        private PlayState predicate(AnimationEvent<ComponentFactory> event) {
            AnimationController<ComponentFactory> controller = event.getController();
            controller.transitionLengthTicks = 0;
            //TODO set animation name + custom animation calling
            controller.setAnimation(new AnimationBuilder().addAnimation("Botarium.anim.deploy", true));
            return PlayState.CONTINUE;
        }

        @Override
        public void registerControllers(AnimationData data) {
            data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
        }

        @Override
        public AnimationFactory getFactory() {
            return factory;
        }

    }

}
