package io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.BlockSelectorWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import io.github.cleanroommc.multiblocked.client.MultiblockedResourceLoader;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.B3DRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.GeoComponentRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.OBJRenderer;
import io.github.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class IRendererWidget extends WidgetGroup {
    public static BlockComponent rendererBlock;
    public Consumer<IRenderer> onSave;
    private final IRendererTileEntity tileEntity;
    private final DraggableScrollableWidgetGroup group;
    private Runnable onUpdate;


    public IRendererWidget(IRenderer renderer, Consumer<IRenderer> onSave) {
        super(0, 0, 384, 256);
        setClientSideWidget();
        this.onSave = onSave;
        this.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/irenderer.png")));
        TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlock(BlockPos.ORIGIN, new BlockInfo(rendererBlock));
        tileEntity = (IRendererTileEntity)world.getTileEntity(BlockPos.ORIGIN);
        tileEntity.renderer = renderer;
        this.addWidget(new SceneWidget(35, 59,  138, 138, world)
                .setRenderedCore(Collections.singleton(BlockPos.ORIGIN), null)
                .setRenderFacing(false));
        this.addWidget(group = new DraggableScrollableWidgetGroup(181, 80, 180, 120));
        this.addWidget(new ButtonWidget(300, 55, 20, 20, this::onUpdate)
                .setButtonTexture(new ResourceTexture("multiblocked:textures/gui/add.png"))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("update"));
        this.addWidget(new SelectorWidget(181, 55, 100, 20, Multiblocked.isModLoaded(Multiblocked.MODID_GEO) ?
                        Arrays.asList("BlockState", "B3D", "OBJ", "IModel", "Geo") :
                        Arrays.asList("BlockState", "B3D", "OBJ", "IModel"), 0xff333333)
                .setValue(getType(renderer))
                .setOnChanged(this::onChangeRenderer)
                .setButtonBackground(new ResourceTexture("multiblocked:textures/gui/button_common.png"))
                .setBackground(new ColorRectTexture(0xffaaaaaa))
                .setHoverTooltip("renderer"));
    }

    private void setNewRenderer(IRenderer newRenderer) {
        tileEntity.renderer = newRenderer;
        if (newRenderer != null && newRenderer.isRaw()) {
            Minecraft.getMinecraft().scheduleResourcesRefresh();
        }
    }

    private void onUpdate(ClickData clickData) {
        if (onUpdate != null) onUpdate.run();
    }

    private void onChangeRenderer(String s) {
        group.clearAllWidgets();
        onUpdate = null;
        IRenderer current = tileEntity.renderer;
        switch (s) {
            case "BlockState":
                BlockSelectorWidget blockSelectorWidget = new BlockSelectorWidget(0, 1, true);
                if (current instanceof BlockStateRenderer) {
                    blockSelectorWidget.setBlock(((BlockStateRenderer) current).state);
                }
                group.addWidget(blockSelectorWidget);
                onUpdate = () -> {
                    if (blockSelectorWidget.getBlock() == null) {
                        setNewRenderer(null);
                    } else {
                        setNewRenderer(new BlockStateRenderer(blockSelectorWidget.getBlock()));
                    }
                };
                break;
            case "B3D": {
                Set<BlockRenderLayer> layers = new HashSet<>();
                TextFieldWidget tfw = addModelSettings(layers);
                File path = new File(MultiblockedResourceLoader.location, "assets/multiblocked/b3d");
                group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(this, "title", path, true, r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString("multiblocked:b3d/" + r.getPath().replace(path.getPath(), "").substring(1).replace('\\', '/'));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/button_common.png")));
                if (current instanceof B3DRenderer) {
                    tfw.setCurrentString(((B3DRenderer) current).modelLocation.toString());
                }
                onUpdate = () -> {
                    if (tfw.getCurrentString().isEmpty()) {
                        setNewRenderer(null);
                    } else {
                        setNewRenderer(new B3DRenderer(new ResourceLocation(tfw.getCurrentString()), layers.toArray(new BlockRenderLayer[0])));
                    }
                };
                break;
            }
            case "OBJ": {
                Set<BlockRenderLayer> layers = new HashSet<>();
                TextFieldWidget tfw = addModelSettings(layers);
                File path = new File(MultiblockedResourceLoader.location, "assets/multiblocked/obj");
                group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(this, "title", path, true, r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString("multiblocked:obj/" + r.getPath().replace(path.getPath(), "").substring(1).replace('\\', '/'));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/button_common.png")));
                if (current instanceof OBJRenderer) {
                    tfw.setCurrentString(((OBJRenderer) current).modelLocation.toString());
                }
                onUpdate = () -> {
                    if (tfw.getCurrentString().isEmpty()) {
                        setNewRenderer(null);
                    } else {
                        setNewRenderer(new OBJRenderer(new ResourceLocation(tfw.getCurrentString()), layers.toArray(new BlockRenderLayer[0])));
                    }
                };
                break;
            }
            case "IModel": {
                Set<BlockRenderLayer> layers = new HashSet<>();
                TextFieldWidget tfw = addModelSettings(layers);
                File path = new File(MultiblockedResourceLoader.location, "assets/multiblocked/models");
                group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(this, "title", path, true, r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString("multiblocked:" + r.getPath().replace(path.getPath(), "").substring(1).replace(".json", "").replace('\\', '/'));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/button_common.png")));
                if (current instanceof IModelRenderer) {
                    tfw.setCurrentString(((IModelRenderer) current).modelLocation.toString());
                }
                onUpdate = () -> {
                    if (tfw.getCurrentString().isEmpty()) {
                        setNewRenderer(null);
                    } else {
                        setNewRenderer(new IModelRenderer(new ResourceLocation(tfw.getCurrentString()), layers.toArray(new BlockRenderLayer[0])));
                    }
                };
                break;
            }
            case "Geo": {
                TextFieldWidget tfw = new TextFieldWidget(1, 1, 150, 20, true, null, null);
                File path = new File(MultiblockedResourceLoader.location, "assets/multiblocked/geo");
                group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(this, "title", path, true, r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString(r.getName().replace(".geo.json", ""));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/button_common.png")));
                group.addWidget(tfw);
                onUpdate = () -> {
                    if (tfw.getCurrentString().isEmpty()) {
                        setNewRenderer(null);
                    } else {
                        setNewRenderer(new GeoComponentRenderer(tfw.getCurrentString()));
                    }
                };
                break;
            }
        }
    }

    private TextFieldWidget addModelSettings (Set<BlockRenderLayer> layers) {
        TextFieldWidget tfw = new TextFieldWidget(1,1,150,20,true, null, null);
        group.addWidget(tfw);
        int y = 25;
        IModelRenderer current = tileEntity.renderer instanceof IModelRenderer ? (IModelRenderer) tileEntity.renderer : null;
        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            group.addWidget(new SwitchWidget(1, y, 150, 15, (cd, r)->{
                if (r) layers.add(layer);
                else layers.remove(layer);
            })
                    .setPressed(current != null && current.renderLayer.contains(layer))
                    .setHoverBorderTexture(1, -1)
                    .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/button_common.png"), new TextTexture(layer.toString() + " (N)", -1).setDropShadow(true))
                    .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/button_common.png"), new TextTexture(layer.toString() + " (Y)", -1).setDropShadow(true))
                    .setHoverTooltip("should be rendered"));
            y += 20;
        }
        return tfw;
    }

    public static String getType(IRenderer renderer) {
        if (renderer instanceof BlockStateRenderer) {
            return "BlockState";
        } else if (renderer instanceof B3DRenderer) {
            return "B3D";
        } else if (renderer instanceof OBJRenderer) {
            return "OBJ";
        } else if (renderer instanceof IModelRenderer) {
            return "IModel";
        } else if (Multiblocked.isModLoaded(Multiblocked.MODID_GEO) && renderer instanceof GeoComponentRenderer) {
            return "Geo";
        }
        return "";
    }

    public static void registerBlock() {
        PartDefinition definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "irenderer"), IRendererTileEntity.class);
        definition.isOpaqueCube = false;
        definition.showInJei = false;
        MultiblockComponents.registerComponent(definition);
        rendererBlock = MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(definition.location);
        rendererBlock.setCreativeTab(null);
    }

    public static class IRendererTileEntity extends PartTileEntity<PartDefinition> {
        public IRenderer renderer;

        public IRendererTileEntity() {
            super();
        }

        @Override
        public boolean isFormed() {
            return false;
        }

        @Override
        public IRenderer getRenderer() {
            return renderer;
        }
    }
}
