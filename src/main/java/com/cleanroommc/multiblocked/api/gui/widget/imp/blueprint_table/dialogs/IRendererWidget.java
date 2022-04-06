package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.*;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.*;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
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
    private final IRenderer originalRenderer;
    private Runnable onUpdate;

    public IRendererWidget(WidgetGroup parent, IRenderer renderer, Consumer<IRenderer> onSave) {
        super(parent, true);
        this.onSave = onSave;
        this.originalRenderer = renderer;
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
                        Arrays.asList("null", "BlockState", "B3D", "OBJ", "IModel", "TParticle", "Geo") :
                        Arrays.asList("null", "BlockState", "B3D", "OBJ", "IModel", "TParticle"), -1)
                .setValue(getType(renderer))
                .setOnChanged(this::onChangeRenderer)
                .setButtonBackground(new ColorBorderTexture(1, -1), new ColorRectTexture(0xff444444))
                .setBackground(new ColorRectTexture(0xff999999))
                .setHoverTooltip("renderer"));
        if (onSave == null) return;
        this.addWidget(new ButtonWidget(285, 30, 40, 20, cd -> {
            if (tileEntity != null) {
                onSave.accept(tileEntity.getRenderer());
            }
            super.close();
        })
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("save", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("save"));
    }

    @Override
    public void close() {
        super.close();
        if (onSave != null) {
            onSave.accept(originalRenderer);
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
                File path = new File(Multiblocked.location, "assets/multiblocked/b3d");
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
                File path = new File(Multiblocked.location, "assets/multiblocked/obj");
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
                File path = new File(Multiblocked.location, "assets/multiblocked/models");
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
            case "TParticle" : {
                TextFieldWidget tfw = addModelSettings();
                TextureParticleRenderer renderer = new TextureParticleRenderer(new ResourceLocation(""));
                if (current instanceof  TextureParticleRenderer) {
                    renderer.texture = ((TextureParticleRenderer) current).texture;
                    renderer.light = ((TextureParticleRenderer) current).light;
                    renderer.scale = ((TextureParticleRenderer) current).scale;
                    renderer.isShader =((TextureParticleRenderer) current).isShader;
                    renderer.isAddBlend = ((TextureParticleRenderer) current).isAddBlend;
                    renderer.isBackLayer = ((TextureParticleRenderer) current).isBackLayer;
                    renderer.renderRange = ((TextureParticleRenderer) current).renderRange;
                }
                File png = new File(Multiblocked.location, "assets/multiblocked/textures");
                File shader = new File(Multiblocked.location, "assets/multiblocked/shaders");
                group.addWidget(new ButtonWidget(155, 1, 20, 20, cd -> DialogWidget.showFileDialog(this, "title", renderer.isShader ? shader : png, true,
                        renderer.isShader ? DialogWidget.suffixFilter(".frag") : DialogWidget.suffixFilter(".png"), r -> {
                            if (r != null && r.isFile()) {
                                if (renderer.isShader) {
                                    tfw.setCurrentString("multiblocked:" + r.getPath().replace(shader.getPath(), "").substring(1).replace(".frag", "").replace('\\', '/'));
                                } else {
                                    tfw.setCurrentString("multiblocked:" + r.getPath().replace(png.getPath(), "textures").replace('\\', '/'));
                                }
                            }
                        })).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/darkened_slot.png"), new TextTexture("F", -1)).setHoverTooltip("select file"));
                if (current instanceof IModelRenderer) {
                    tfw.setCurrentString(((IModelRenderer) current).modelLocation.toString());
                }
                group.addWidget(createBoolSwitch(1, 25, "isShader", "use Shader", renderer.isShader, r->{
                    if (renderer.isShader != r) {
                        renderer.isShader = r;
                        tfw.setCurrentString("");
                    }
                }));
                group.addWidget(createBoolSwitch(1, 40, "isAddBlend", "addition blend mode", renderer.isShader, r -> renderer.isAddBlend = r));
                group.addWidget(createBoolSwitch(1, 55, "isBackLayer", "render in the back layer", renderer.isBackLayer, r -> renderer.isBackLayer = r));
                group.addWidget(new TextFieldWidget(1,75,70,10,true, null, num->renderer.scale = Float.parseFloat(num))
                        .setNumbersOnly(0f, 100f).setCurrentString(renderer.scale+"").setHoverTooltip("particle scale: from 0 to 100"));
                group.addWidget(new LabelWidget(75, 75, "Scale"));
                group.addWidget(new TextFieldWidget(1,90,70,10,true, null, num->renderer.light = Integer.parseInt(num))
                        .setNumbersOnly(-1, 15).setCurrentString(renderer.light+"").setHoverTooltip("lighting map \n -1: follow the environment \n 0-15: lighting"));
                group.addWidget(new LabelWidget(75, 90, "Light"));
                group.addWidget(new TextFieldWidget(1,105,70,10,true, null, num->renderer.renderRange = Integer.parseInt(num))
                        .setNumbersOnly(-1, 1000).setCurrentString(renderer.renderRange+"").setHoverTooltip("render range (do not render if out of eye range) \n -1: always range"));
                group.addWidget(new LabelWidget(75, 105, "Render Range"));
                onUpdate = () -> {
                    if (tfw.getCurrentString().isEmpty()) {
                        setNewRenderer(null);
                    } else {
                        TextureParticleRenderer newRenderer = new TextureParticleRenderer(new ResourceLocation(tfw.getCurrentString()));
                        newRenderer.light = renderer.light;
                        newRenderer.scale = renderer.scale;
                        newRenderer.isShader = renderer.isShader;
                        newRenderer.isAddBlend = renderer.isAddBlend;
                        newRenderer.isBackLayer = renderer.isBackLayer;
                        newRenderer.renderRange = renderer.renderRange;
                        setNewRenderer(newRenderer);
                    }
                };
                break;
            }
            case "Geo": {
                TextFieldWidget tfw = new TextFieldWidget(1, 1, 150, 20, true, null, null);
                File path = new File(Multiblocked.location, "assets/multiblocked/geo");
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

    protected WidgetGroup createBoolSwitch(int x, int y, String text, String tips, boolean init, Consumer<Boolean> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new SwitchWidget(0, 0, 15, 15, (cd, r)->onPressed.accept(r))
                .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0,1,0.5))
                .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0.5,1,0.5))
                .setHoverTexture(new ColorBorderTexture(1, 0xff545757))
                .setPressed(init)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(20, 3, ()->text).setTextColor(-1).setDrop(true));
        return widgetGroup;
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
