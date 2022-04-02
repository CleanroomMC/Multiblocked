package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.MultiblockedResourceLoader;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.BlockSelectorWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.client.renderer.impl.B3DRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.GeoComponentRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.OBJRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

public class IRendererWidget extends DialogWidget {
    public Consumer<IRenderer> onSave;
    public final DummyComponentTileEntity tileEntity;
    private final DraggableScrollableWidgetGroup group;
    private Runnable onUpdate;

    public IRendererWidget(WidgetGroup parent, IRenderer renderer, Consumer<IRenderer> onSave) {
        super(parent, true);
        this.onSave = onSave;
        this.addWidget(new ImageWidget(0, 0, getSize().width, getSize().height, new ColorRectTexture(0xaf000000)));
        TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlock(BlockPos.ORIGIN, new BlockInfo(MultiblockComponents.DummyComponentBlock));
        tileEntity = (DummyComponentTileEntity) world.getTileEntity(BlockPos.ORIGIN);
        setNewRenderer(renderer);
        this.addWidget(new ImageWidget(35, 59, 138, 138, new GuiTextureGroup(new ColorBorderTexture(3, -1), new ColorRectTexture(0xaf444444))));
        this.addWidget(new SceneWidget(35, 59,  138, 138, world)
                .setRenderedCore(Collections.singleton(BlockPos.ORIGIN), null)
                .setRenderSelect(false)
                .setRenderFacing(false));
        this.addWidget(group = new DraggableScrollableWidgetGroup(181, 80, 180, 120));
        this.addWidget(new ButtonWidget(285, 55, 40, 20, this::onUpdate)
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("update", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("update"));
        this.addWidget(new ButtonWidget(330, 55, 45, 20, cd -> Minecraft.getMinecraft().scheduleResourcesRefresh())
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("refresh", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("§4NOTE: Resources refresh will cause the game to stall for a while.§r" +
                        "\n§2Must refresh to display models which have never been loaded before (obj, b3d, and geo).§r" +
                        "\n§3Also need to refresh if the texture is not loaded.§r"));
        this.addWidget(new SelectorWidget(181, 55, 100, 20, Multiblocked.isModLoaded(Multiblocked.MODID_GEO) ?
                        Arrays.asList("null", "BlockState", "B3D", "OBJ", "IModel", "Geo") :
                        Arrays.asList("null", "BlockState", "B3D", "OBJ", "IModel"), -1)
                .setValue(getType(renderer))
                .setOnChanged(this::onChangeRenderer)
                .setButtonBackground(new ColorBorderTexture(1, -1), new ColorRectTexture(0xff444444))
                .setBackground(new ColorRectTexture(0xff999999))
                .setHoverTooltip("renderer"));
        this.addWidget(new ButtonWidget(285, 30, 40, 20, cd -> {
            if (onSave != null && tileEntity != null) {
                onSave.accept(tileEntity.getRenderer());
            }
            super.close();
        })
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("save", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("update"));
    }

    @Override
    public void close() {
        super.close();
        if (onSave != null) {
            onSave.accept(null);
        }
    }

    private void setNewRenderer(IRenderer newRenderer) {
        PartDefinition definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "i_renderer"));
        definition.baseRenderer = newRenderer;
        tileEntity.setDefinition(definition);
    }

    private void onUpdate(ClickData clickData) {
        if (onUpdate != null) onUpdate.run();
    }

    private void onChangeRenderer(String s) {
        group.clearAllWidgets();
        onUpdate = null;
        IRenderer current = tileEntity.getRenderer();
        switch (s) {
            case "null":
                onUpdate = () -> setNewRenderer(null);
                break;
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
                TextFieldWidget tfw = addModelSettings();
                File path = new File(MultiblockedResourceLoader.location, "assets/multiblocked/b3d");
                group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(this, "title", path, true,
                        DialogWidget.suffixFilter(".b3d"), r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString("multiblocked:b3d/" + r.getPath().replace(path.getPath(), "").substring(1).replace('\\', '/'));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1)).setHoverTooltip("select file"));
                if (current instanceof B3DRenderer) {
                    tfw.setCurrentString(((B3DRenderer) current).modelLocation.toString());
                }
                onUpdate = () -> {
                    if (tfw.getCurrentString().isEmpty()) {
                        setNewRenderer(null);
                    } else {
                        setNewRenderer(new B3DRenderer(new ResourceLocation(tfw.getCurrentString())));
                    }
                };
                break;
            }
            case "OBJ": {
                TextFieldWidget tfw = addModelSettings();
                File path = new File(MultiblockedResourceLoader.location, "assets/multiblocked/obj");
                group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(this, "title", path, true,
                        DialogWidget.suffixFilter(".obj"), r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString("multiblocked:obj/" + r.getPath().replace(path.getPath(), "").substring(1).replace('\\', '/'));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1)).setHoverTooltip("select file"));
                if (current instanceof OBJRenderer) {
                    tfw.setCurrentString(((OBJRenderer) current).modelLocation.toString());
                }
                onUpdate = () -> {
                    if (tfw.getCurrentString().isEmpty()) {
                        setNewRenderer(null);
                    } else {
                        setNewRenderer(new OBJRenderer(new ResourceLocation(tfw.getCurrentString())));
                    }
                };
                break;
            }
            case "IModel": {
                TextFieldWidget tfw = addModelSettings();
                File path = new File(MultiblockedResourceLoader.location, "assets/multiblocked/models");
                group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(this, "title", path, true,
                        DialogWidget.suffixFilter(".json"), r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString("multiblocked:" + r.getPath().replace(path.getPath(), "").substring(1).replace(".json", "").replace('\\', '/'));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1)).setHoverTooltip("select file"));
                if (current instanceof IModelRenderer) {
                    tfw.setCurrentString(((IModelRenderer) current).modelLocation.toString());
                }
                onUpdate = () -> {
                    if (tfw.getCurrentString().isEmpty()) {
                        setNewRenderer(null);
                    } else {
                        setNewRenderer(new IModelRenderer(new ResourceLocation(tfw.getCurrentString())));
                    }
                };
                break;
            }
            case "Geo": {
                TextFieldWidget tfw = new TextFieldWidget(1, 1, 150, 20, true, null, null);
                File path = new File(MultiblockedResourceLoader.location, "assets/multiblocked/geo");
                group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(this, "title", path, true,
                        DialogWidget.suffixFilter(".geo.json"), r -> {
                    if (r != null && r.isFile()) {
                        tfw.setCurrentString(r.getName().replace(".geo.json", ""));
                    }
                })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1)).setHoverTooltip("select file"));
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

    private TextFieldWidget addModelSettings () {
        TextFieldWidget tfw = new TextFieldWidget(1,1,150,20,true, null, null);
        group.addWidget(tfw);
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
        return "null";
    }

}
