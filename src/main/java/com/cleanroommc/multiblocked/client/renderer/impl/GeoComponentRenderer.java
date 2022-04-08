package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.ICustomRenderer;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class GeoComponentRenderer extends AnimatedGeoModel<GeoComponentRenderer.ComponentFactory> implements ICustomRenderer, IGeoRenderer<GeoComponentRenderer.ComponentFactory> {
    public final static GeoComponentRenderer INSTANCE = new GeoComponentRenderer(null, false);

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
    public final boolean isGlobal;
    @SideOnly(Side.CLIENT)
    private ComponentFactory itemFactory;

    public GeoComponentRenderer(String modelName, boolean isGlobal) {
        this.modelName = modelName;
        this.isGlobal = isGlobal;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderItem(ItemStack stack) {
        if (itemFactory == null) {
            itemFactory = new ComponentFactory(null, this);
        }
        GeoModel model = this.getModel(this.getModelLocation(itemFactory));
        this.setLivingAnimations(itemFactory, this.getUniqueID(itemFactory));
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.01f, 0);
        GlStateManager.translate(0.5, 0, 0.5);
        Minecraft.getMinecraft().renderEngine.bindTexture(getTextureLocation(itemFactory));
        render(model, itemFactory, Minecraft.getMinecraft().getRenderPartialTicks(), 1, 1, 1, 1);
        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldRenderInPass(World world, BlockPos pos, int pass) {
        return true;
    }

    @Override
    public boolean isGlobalRenderer(@Nonnull TileEntity te) {
        return isGlobal;
    }

    @Override
    public String getType() {
        return "Geo";
    }

    @Override
    public IRenderer fromJson(Gson gson, JsonObject jsonObject) {
        return new GeoComponentRenderer(jsonObject.get("modelName").getAsString(), JsonUtils.getBoolean(jsonObject, "isGlobal", false));
    }

    @Override
    public JsonObject toJson(Gson gson, JsonObject jsonObject) {
        jsonObject.addProperty("modelName", modelName);
        if (isGlobal) {
            jsonObject.addProperty("isGlobal", true);
        }
        return jsonObject;
    }

    @Override
    public Supplier<IRenderer> createConfigurator(WidgetGroup parent, DraggableScrollableWidgetGroup group, IRenderer current) {
        TextFieldWidget tfw = new TextFieldWidget(1, 1, 150, 20, true, null, null);
        File path = new File(Multiblocked.location, "assets/multiblocked/geo");
        AtomicBoolean isGlobal = new AtomicBoolean(false);
        if (current instanceof GeoComponentRenderer) {
            tfw.setCurrentString(((GeoComponentRenderer) current).modelName);
            isGlobal.set(((GeoComponentRenderer) current).isGlobal);
        }
        group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(parent, "select a geo file", path, true,
                DialogWidget.suffixFilter(".geo.json"), r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString(r.getName().replace(".geo.json", ""));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1)).setHoverTooltip("select file"));
        group.addWidget(tfw);
        group.addWidget(createBoolSwitch(1,25, "isGlobal", "Whether global rendering is required. Do it if your model is large enough", isGlobal.get(), isGlobal::set));
        return () -> {
            if (tfw.getCurrentString().isEmpty()) {
                return null;
            } else {
                return new GeoComponentRenderer(tfw.getCurrentString(), isGlobal.get());
            }
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isRaw() {
        return !GeckoLibCache.getInstance().getGeoModels().containsKey(this.getModelLocation(null));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasTESR(World world, BlockPos pos) {
        return true;
    }

    @Override
    public void onPreAccess(TileEntity te) {
        if (te instanceof ComponentTileEntity<?>) {
            ComponentTileEntity<?> component = (ComponentTileEntity<?>) te;
            component.rendererObject = new GeoComponentRenderer.ComponentFactory(component, this);
        }
    }

    @Override
    public void onPostAccess(TileEntity te) {
        if (te instanceof ComponentTileEntity<?>) {
            ComponentTileEntity<?> component = (ComponentTileEntity<?>) te;
            component.rendererObject = null;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderTESR(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te instanceof ComponentTileEntity<?> && ((ComponentTileEntity<?>) te).rendererObject instanceof  ComponentFactory) {
            ComponentTileEntity<?> controller = (ComponentTileEntity<?>) te;
            ComponentFactory factory = (ComponentFactory) controller.rendererObject;
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
            render(model, factory, partialTicks, 1, 1, 1, 1);
            GlStateManager.popMatrix();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderRecursively(BufferBuilder builder, GeoBone bone, float red, float green, float blue, float alpha) {
        int pass = MinecraftForgeClient.getRenderPass();
        if (pass < 0) {
            rawRenderRecursively(builder, bone, red, green, blue, alpha);
        }
        boolean isTranslucent = bone.name.equals("translucent");
        if (pass == 1 && isTranslucent) {
            rawRenderRecursively(builder, bone, red, green, blue, alpha);
        } else if (pass == 0 && !isTranslucent) {
            MATRIX_STACK.push();
            MATRIX_STACK.translate(bone);
            MATRIX_STACK.moveToPivot(bone);
            MATRIX_STACK.rotate(bone);
            MATRIX_STACK.scale(bone);
            MATRIX_STACK.moveBackFromPivot(bone);
            if (!bone.isHidden) {
                for (GeoCube cube : bone.childCubes) {
                    MATRIX_STACK.push();
                    GlStateManager.pushMatrix();
                    renderCube(builder, cube, red, green, blue, alpha);
                    GlStateManager.popMatrix();
                    MATRIX_STACK.pop();
                }
                for (GeoBone childBone : bone.childBones) {
                    renderRecursively(builder, childBone, red, green, blue, alpha);
                }
            }
            MATRIX_STACK.pop();
        } else if (pass == 1) {
            MATRIX_STACK.push();
            MATRIX_STACK.translate(bone);
            MATRIX_STACK.moveToPivot(bone);
            MATRIX_STACK.rotate(bone);
            MATRIX_STACK.scale(bone);
            MATRIX_STACK.moveBackFromPivot(bone);
            if (!bone.isHidden) {
                for (GeoBone childBone : bone.childBones) {
                    renderRecursively(builder, childBone, red, green, blue, alpha);
                }
            }
            MATRIX_STACK.pop();
        }
    }

    @SideOnly(Side.CLIENT)
    private void rawRenderRecursively(BufferBuilder builder, GeoBone bone, float red, float green, float blue, float alpha) {
        MATRIX_STACK.push();
        MATRIX_STACK.translate(bone);
        MATRIX_STACK.moveToPivot(bone);
        MATRIX_STACK.rotate(bone);
        MATRIX_STACK.scale(bone);
        MATRIX_STACK.moveBackFromPivot(bone);
        if (!bone.isHidden) {
            for (GeoCube cube : bone.childCubes) {
                MATRIX_STACK.push();
                GlStateManager.pushMatrix();
                renderCube(builder, cube, red, green, blue, alpha);
                GlStateManager.popMatrix();
                MATRIX_STACK.pop();
            }
            for (GeoBone childBone : bone.childBones) {
                rawRenderRecursively(builder, childBone, red, green, blue, alpha);
            }
        }
        MATRIX_STACK.pop();
    }

    @SideOnly(Side.CLIENT)
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
        public final AnimationFile animationFile;
        public String currentStatus;

        public ComponentFactory(ComponentTileEntity<?> component, GeoComponentRenderer renderer) {
            this.component = component;
            this.renderer = renderer;
            animationFile = GeckoLibCache.getInstance().getAnimations().get(renderer.getAnimationFileLocation(this));
        }

        private final AnimationFactory factory = new AnimationFactory(this);

        private PlayState predicate(AnimationEvent<ComponentFactory> event) {
            AnimationController<ComponentFactory> controller = event.getController();
            String lastStatus = currentStatus;
            currentStatus = component.getStatus();
            if (!Objects.equals(lastStatus, currentStatus)) {
                if (currentStatus == null) return PlayState.STOP;
                AnimationBuilder animationBuilder = new AnimationBuilder();
                if (lastStatus != null) {
                    Animation trans = animationFile.getAnimation(lastStatus + "-" + currentStatus);
                    if (trans != null) animationBuilder.addAnimation(trans.animationName);
                }
                if (animationFile.getAnimation(currentStatus) != null) {
                    animationBuilder.addAnimation(currentStatus);
                }
                controller.setAnimation(animationBuilder);
            }
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
