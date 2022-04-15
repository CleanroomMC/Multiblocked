package com.cleanroommc.multiblocked.client.renderer.impl;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.ResourceTextureWidget;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import com.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import com.cleanroommc.multiblocked.client.model.ModelFactory;
import com.cleanroommc.multiblocked.client.model.custommodel.CustomBakedModel;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class GTRenderer extends IModelRenderer {
    public final static GTRenderer INSTANCE = new GTRenderer();

    public ResourceLocation baseTexture = new ResourceLocation("multiblocked:blocks/gregtech_base");
    public ResourceLocation frontOverlay = new ResourceLocation("multiblocked:blocks/gregtech_front");

    public ResourceLocation backOverlay;
    public ResourceLocation leftOverlay;
    public ResourceLocation rightOverlay;
    public ResourceLocation upOverlay;
    public ResourceLocation downOverlay;

    public boolean formedAsController;

    private GTRenderer() {

    }

    public GTRenderer(ResourceLocation baseTexture, ResourceLocation frontOverlay) {
        super();
        this.baseTexture = baseTexture;
        this.frontOverlay = frontOverlay;
        if (Multiblocked.isClient()) {
            registerTextureSwitchEvent();
            blockModels = new EnumMap<>(EnumFacing.class);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getType() {
        return "gregtech";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isRaw() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder buffer) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (formedAsController && te instanceof PartTileEntity) {
            PartTileEntity<?> part = (PartTileEntity<?>) te;
            List<ControllerTileEntity> controllers = part.getControllers();
            for (ControllerTileEntity controller : controllers) {
                if (controller.isFormed() && controller.getRenderer() instanceof GTRenderer) {
                    CustomBakedModel model = new CustomBakedModel(getModel(((GTRenderer) controller.getRenderer()).baseTexture).bake(
                            TRSRTransformation.from(part.getFrontFacing()),
                            DefaultVertexFormats.BLOCK,
                            ModelLoader.defaultTextureGetter()));
                    if (!model.shouldRenderInLayer(state, MathHelper.getPositionRandom(pos))) return false;
                    BlockModelRenderer blockModelRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
                    blockModelRenderer.renderModel(blockAccess, model, state, pos, buffer, true);
                    return true;
                }
            }
        }
        return super.renderBlock(state, pos, blockAccess, buffer);
    }

    @SideOnly(Side.CLIENT)
    protected IModel getModel(ResourceLocation baseTexture) {
        ModelFactory factory = new ModelFactory(ModelFactory.ModelTemplate.CUBE_2_LAYER)
                .addSprite("bot_down", baseTexture)
                .addSprite("bot_up", baseTexture)
                .addSprite("bot_north", baseTexture)
                .addSprite("bot_south", baseTexture)
                .addSprite("bot_west", baseTexture)
                .addSprite("bot_east", baseTexture)
                .addSprite("top_north", frontOverlay);
        if (backOverlay != null) {
            factory .addSprite("top_south", backOverlay);
        }
        if (leftOverlay != null) {
            factory .addSprite("top_west", leftOverlay);
        }
        if (rightOverlay != null) {
            factory .addSprite("top_east", rightOverlay);
        }
        if (upOverlay != null) {
            factory .addSprite("top_up", upOverlay);
        }
        if (downOverlay != null) {
            factory .addSprite("top_down", downOverlay);
        }
        return factory.getMappedModel();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IModel getModel() {
        return getModel(baseTexture);
    }

    @Override
    public JsonObject toJson(Gson gson, JsonObject jsonObject) {
        jsonObject.add("baseTexture", gson.toJsonTree(baseTexture, ResourceLocation.class));
        jsonObject.add("frontTexture", gson.toJsonTree(frontOverlay, ResourceLocation.class));
        if (backOverlay != null) {
            jsonObject.add("backTexture", gson.toJsonTree(backOverlay, ResourceLocation.class) );
        }
        if (leftOverlay != null) {
            jsonObject.add("leftTexture", gson.toJsonTree(leftOverlay, ResourceLocation.class) );
        }
        if (rightOverlay != null) {
            jsonObject.add("rightTexture", gson.toJsonTree(rightOverlay, ResourceLocation.class) );
        }
        if (upOverlay != null) {
            jsonObject.add("upTexture", gson.toJsonTree(upOverlay, ResourceLocation.class) );
        }
        if (downOverlay != null) {
            jsonObject.add("downTexture", gson.toJsonTree(downOverlay, ResourceLocation.class) );
        }
        if (formedAsController) {
            jsonObject.addProperty("formedAsController", true);
        }
        return jsonObject;
    }

    @Override
    public IRenderer fromJson(Gson gson, JsonObject jsonObject) {
        GTRenderer renderer =  new GTRenderer(gson.fromJson(jsonObject.get("baseTexture"), ResourceLocation.class), gson.fromJson(jsonObject.get("frontTexture"), ResourceLocation.class));
        if (jsonObject.has("backTexture")) {
            renderer.backTexture = gson.fromJson(jsonObject.get("backTexture"), ResourceLocation.class);
        }
        if (jsonObject.has("leftTexture")) {
            renderer.leftOverlay = gson.fromJson(jsonObject.get("leftTexture"), ResourceLocation.class);
        }
        if (jsonObject.has("rightTexture")) {
            renderer.rightOverlay = gson.fromJson(jsonObject.get("rightTexture"), ResourceLocation.class);
        }
        if (jsonObject.has("upTexture")) {
            renderer.upOverlay = gson.fromJson(jsonObject.get("upTexture"), ResourceLocation.class);
        }
        if (jsonObject.has("downTexture")) {
            renderer.downOverlay = gson.fromJson(jsonObject.get("downTexture"), ResourceLocation.class);
        }
        if (jsonObject.has("formedAsController")) {
            renderer.formedAsController = jsonObject.get("formedAsController").getAsBoolean();
        }
        return renderer;
    }

    @Override
    public Supplier<IRenderer> createConfigurator(WidgetGroup parent, DraggableScrollableWidgetGroup group, IRenderer current) {
        GTRenderer renderer = new GTRenderer();
        if (current instanceof GTRenderer) {
            renderer.formedAsController = ((GTRenderer) current).formedAsController;
            renderer.baseTexture = ((GTRenderer) current).baseTexture;
            renderer.frontOverlay = ((GTRenderer) current).frontOverlay;
            renderer.backOverlay = ((GTRenderer) current).backOverlay;
            renderer.leftOverlay = ((GTRenderer) current).leftOverlay;
            renderer.rightOverlay = ((GTRenderer) current).rightOverlay;
            renderer.upOverlay = ((GTRenderer) current).upOverlay;
            renderer.downOverlay = ((GTRenderer) current).downOverlay;
        }
        addTextureSelector(1, 1, 60, 60, "base texture", parent, group, renderer.baseTexture, r -> renderer.baseTexture = r);
        addTextureSelector(1 + 64, 1, 30, 30, "front overlay", parent, group, renderer.frontOverlay, r -> renderer.frontOverlay = r);
        addTextureSelector(1 + 64 + 34, 1, 30, 30, "back overlay", parent, group, renderer.backOverlay, r -> renderer.backOverlay = r);
        addTextureSelector(1 + 64 + 34 * 2, 1, 30, 30, "left overlay", parent, group, renderer.leftOverlay, r -> renderer.leftOverlay = r);
        addTextureSelector(1 + 64, 34, 30, 30, "right overlay", parent, group, renderer.rightOverlay, r -> renderer.rightOverlay = r);
        addTextureSelector(1 + 64 + 34, 34, 30, 30, "up overlay", parent, group, renderer.upOverlay, r -> renderer.upOverlay = r);
        addTextureSelector(1 + 64 + 34 * 2, 34, 30, 30, "down overlay", parent, group, renderer.downOverlay, r -> renderer.downOverlay = r);

        group.addWidget(createBoolSwitch(1, 70, "formedAsController", "When the multi formed, if its true and the controller also uses the GregTech Model, it will change the base texture to the controller’s base texture.", renderer.formedAsController, r -> renderer.formedAsController = r));
        return () -> {
            GTRenderer result = new GTRenderer(renderer.baseTexture, renderer.frontOverlay);
            result.backOverlay = renderer.backOverlay;
            result.leftOverlay = renderer.leftOverlay;
            result.rightOverlay = renderer.rightOverlay;
            result.upOverlay = renderer.upOverlay;
            result.downOverlay = renderer.downOverlay;
            result.formedAsController = renderer.formedAsController;
            return result;
        };
    }

    protected void addTextureSelector(int x, int y, int width, int height, String text, WidgetGroup parent, WidgetGroup group, ResourceLocation init, Consumer<ResourceLocation> newTexture) {
        ImageWidget imageWidget;
        if (init != null) {
            imageWidget = new ImageWidget(x, y, width, height, new GuiTextureGroup(new ColorBorderTexture(1, -1), new ResourceTexture(init.getNamespace() + ":textures/" + init.getPath() + ".png")));
        } else {
            imageWidget = new ImageWidget(x, y, width, height, new ColorBorderTexture(1, -1));
        }
        group.addWidget(imageWidget);
        group.addWidget(new ButtonWidget(x, y, width, height, null, cd -> new ResourceTextureWidget(parent, texture -> {
            if (texture != null) {
                imageWidget.setImage(new GuiTextureGroup(new ColorBorderTexture(1, -1), texture));
                newTexture.accept(new ResourceLocation(texture.imageLocation.toString().replace("textures/", "").replace(".png", "")));
            }
        })).setHoverTexture(new ColorRectTexture(0x4faaaaaa)).setHoverTooltip(String.format("select the %s texture", text)));
    }
}
