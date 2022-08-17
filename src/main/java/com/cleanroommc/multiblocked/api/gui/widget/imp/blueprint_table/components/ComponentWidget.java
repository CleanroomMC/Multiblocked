package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.block.CustomProperties;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.definition.StatusProperties;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IRendererWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IShapeWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.ISoundWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.ResourceTextureWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.sound.SoundState;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.renderer.scene.WorldSceneRenderer;
import com.cleanroommc.multiblocked.client.util.RenderBufferUtils;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentWidget<T extends ComponentDefinition> extends DialogWidget {
    protected final T definition;
    protected ResourceLocation location;
    protected final TabContainer tabContainer;
    protected final WidgetGroup S1;
    protected final WidgetGroup S2;
    protected final WidgetGroup S3;
    protected final WidgetGroup JSON;
    private final DraggableScrollableWidgetGroup tfGroup;
    private final DraggableScrollableWidgetGroup statusList;
    private final WidgetGroup statusSettings;
    private final TextBoxWidget textBox;
    private final Consumer<JsonObject> onSave;
    private boolean isPretty;

    public ComponentWidget(WidgetGroup group, T definition, Consumer<JsonObject> onSave) {
        super(group, true);
        setParentInVisible();
        this.onSave = onSave;
        this.definition = definition;
        this.location = definition.location;
        this.addWidget(new ImageWidget(0, 0, 384, 256, new ResourceTexture("multiblocked:textures/gui/component.png")));
        this.addWidget(tabContainer = new TabContainer(0, 0, 384, 256).setOnChanged(this::onTabChanged));
        tabContainer.addTab((TabButton) new TabButton(42, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S1"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S1"))
                        .setHoverTooltip("multiblocked.gui.widget.component.s1"),
                S1 = new WidgetGroup(0, 0, getSize().width, getSize().height));
        int x = 47;
        S1.addWidget(new LabelWidget(x, 57, "multiblocked.gui.label.registry_name"));
        S1.addWidget(new TextFieldWidget(x + 80, 54, 150, 15,  true, null, this::updateRegistryName).setCurrentString(this.location.toString()));
        if (definition instanceof ControllerDefinition) {
            S1.addWidget(GuiUtils.createSelector(x, 75, "rotateState", "multiblocked.gui.widget.component.rotate_state", definition.properties.rotationState == CustomProperties.RotationState.NONE ? "NONE" : CustomProperties.RotationState.NON_Y_AXIS.name(),
                    Stream.of(CustomProperties.RotationState.NONE, CustomProperties.RotationState.NON_Y_AXIS).map(Enum::name)
                            .collect(Collectors.toList()), r -> definition.properties.rotationState = CustomProperties.RotationState.valueOf(r)));
        } else {
            S1.addWidget(GuiUtils.createSelector(x, 75, "rotateState", "multiblocked.gui.widget.component.rotate_state", definition.properties.rotationState.name(), Arrays.stream(CustomProperties.RotationState.values()).map(Enum::name).collect(Collectors.toList()), r -> definition.properties.rotationState = CustomProperties.RotationState.valueOf(r)));
        }
        S1.addWidget(GuiUtils.createBoolSwitch(x, 90, "showInJei", "multiblocked.gui.widget.component.jei", definition.properties.showInJei, r -> definition.properties.showInJei = r));
        S1.addWidget(GuiUtils.createBoolSwitch(x, 105, "isOpaqueCube", "multiblocked.gui.widget.component.opaque", definition.properties.isOpaque, r -> definition.properties.isOpaque = r));
        S1.addWidget(GuiUtils.createBoolSwitch(x, 120, "hasCollision", "multiblocked.gui.widget.component.collision", definition.properties.hasCollision, r -> definition.properties.hasCollision = r));
        S1.addWidget(GuiUtils.createFloatField(x, 135, "destroyTime", "multiblocked.gui.widget.component.destroy_time", definition.properties.destroyTime, 0, Float.MAX_VALUE, r -> definition.properties.destroyTime = r));
        S1.addWidget(GuiUtils.createFloatField(x, 150, "explosionResistance", "multiblocked.gui.widget.component.explosion_resistance", definition.properties.explosionResistance, 0, Float.MAX_VALUE, r -> definition.properties.explosionResistance = r));
        S1.addWidget(GuiUtils.createIntField(x, 165, "harvestLevel", "multiblocked.gui.widget.component.harvest", definition.properties.harvestLevel, 0, Integer.MAX_VALUE, r -> definition.properties.harvestLevel = r));
        S1.addWidget(GuiUtils.createIntField(x, 180, "stackSize", "multiblocked.gui.widget.component.stack_size", definition.properties.stackSize, 1, 64, r -> definition.properties.stackSize = r));
        S1.addWidget(GuiUtils.createStringField(x, 195, "tabGroup", "multiblocked.gui.widget.component.tab_group", definition.properties.tabGroup, r -> definition.properties.tabGroup = r));


        tabContainer.addTab(new TabButton(65, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S2"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S2"))
                        .setHoverTooltip("multiblocked.gui.widget.component.status"),
                S2 = new WidgetGroup(0, 0, getSize().width, getSize().height));
        WidgetGroup statuesGroup = new WidgetGroup(20, 50, 150, 170);
        S2.addWidget(statuesGroup);
        S2.addWidget(new ImageWidget(170, 50, 168, 170, ResourceBorderTexture.BORDERED_BACKGROUND));
        S2.addWidget(statusSettings = new WidgetGroup(170, 50, 168, 170));

        statuesGroup.addWidget(new ImageWidget(20, 0, 150 - 20, 70, ResourceBorderTexture.BORDERED_BACKGROUND_BLUE));
        statuesGroup.addWidget(statusList = new DraggableScrollableWidgetGroup(20, 4, 150 - 20, 70 - 8));
        statuesGroup.addWidget(new ButtonWidget(0, 3, 20, 20, new ResourceTexture("multiblocked:textures/gui/add.png"), cd->{
            int i = 1;
            while (definition.status.containsKey("new_status_" + i)) {
                i++;
            }
            String name = "new_status_" + i;
            definition.status.put(name, new StatusProperties(name));
            updateStatusList();
        }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.widget.component.status.create"));
        updateStatusList();


        tabContainer.addTab(new TabButton(88, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S3"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S3"))
                        .setHoverTooltip("multiblocked.gui.widget.component.s2"),
                S3 = new WidgetGroup(0, 0, getSize().width, getSize().height));
        int y = 55;
        for (MultiblockCapability<?> capability : MbdCapabilities.CAPABILITY_REGISTRY.values()) {
            if (capability.hasTrait()) {
                WidgetGroup widgetGroup = new WidgetGroup(47, y, 100, 15);
                Runnable configurator = () -> {
                    DialogWidget dialog = new DialogWidget(group, true);
                    CapabilityTrait trait = capability.createTrait();
                    trait.serialize(definition.traits.get(capability.name));

                    // set background
                    int xOffset = (384 - 176) / 2;
                    dialog.addWidget(new ImageWidget(0, 0, 384, 256, new ColorRectTexture(0xaf000000)));
                    ImageWidget imageWidget;
                    dialog.addWidget(imageWidget = new ImageWidget(xOffset, 0, 176, 256, null));
                    imageWidget.setImage(new ResourceTexture(JsonUtils.getString(definition.traits, "background", "multiblocked:textures/gui/custom_gui.png")));
                    dialog.addWidget(new ButtonWidget(xOffset - 20,10, 20, 20, new ResourceTexture("multiblocked:textures/gui/option.png"), cd2 -> {
                        new ResourceTextureWidget(dialog, texture -> {
                            if (texture != null) {
                                imageWidget.setImage(texture);
                            }
                        });
                    }).setHoverTooltip("multiblocked.gui.widget.component.set_bg"));

                    // open trait settings
                    trait.openConfigurator(dialog);

                    // save when closed
                    dialog.setOnClosed(() -> {
                        String background = ((ResourceTexture)imageWidget.getImage()).imageLocation.toString();
                        if (!background.equals("multiblocked:textures/gui/custom_gui.png")) {
                            definition.traits.addProperty("background", background);
                        }
                        definition.traits.add(capability.name, trait.deserialize());
                    });
                };
                ButtonWidget buttonWidget = new ButtonWidget(20, 0, 15, 15, new ResourceTexture("multiblocked:textures/gui/option.png"), cd -> {
                    if (definition.traits.has(capability.name)) {
                        configurator.run();
                    }
                });
                buttonWidget.setVisible(definition.traits.has(capability.name));
                widgetGroup.addWidget(buttonWidget);
                widgetGroup.addWidget(new SwitchWidget(0, 0, 15, 15, (cd, r)-> {
                    if (r) {
                        configurator.run();
                    } else {
                        definition.traits.remove(capability.name);
                    }
                    buttonWidget.setVisible(r);
                })
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0,1,0.5))
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0.5,1,0.5))
                        .setHoverTexture(new ColorBorderTexture(1, 0xff545757))
                        .setPressed(definition.traits.has(capability.name))
                        .setHoverTooltip(capability.getUnlocalizedName()));
                widgetGroup.addWidget(new LabelWidget(40, 3, capability.getUnlocalizedName()));
                S3.addWidget(widgetGroup);
                y += 15;
            }
        }

        tabContainer.addTab(new TabButton(235, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("J"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("F"))
                        .setHoverTooltip("multiblocked.gui.widget.component.f"),
                JSON = new WidgetGroup(0, 0, getSize().width, getSize().height));
        JSON.addWidget(new SwitchWidget(50, 54, 16, 16, (cd,r) -> {
            isPretty = r;
            updatePatternJson();
        }).setHoverBorderTexture(1, -1).setTexture(new ResourceTexture("multiblocked:textures/gui/pretty.png"), new ResourceTexture("multiblocked:textures/gui/pretty_active.png")).setHoverTooltip("multiblocked.gui.tips.pretty"));
        JSON.addWidget(new ButtonWidget(70, 54, 16, 16, cd -> GuiScreen.setClipboardString(isPretty ? Multiblocked.prettyJson(getComponentJson()) : getComponentJson())).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/copy.png")).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.copy"));
        JSON.addWidget(new ImageWidget(47, 75, 285, 136, new ColorBorderTexture(1, 0xafafaf00)));
        JSON.addWidget(tfGroup = new DraggableScrollableWidgetGroup(47, 75, 285, 136)
                .setBackground(new ColorRectTexture(0x8f111111))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(-1)));
        tfGroup.addWidget(textBox = new TextBoxWidget(0, 0, 285, Collections.singletonList("")).setFontColor(-1).setShadow(true));

        this.addWidget(new ButtonWidget(260, 26, 80, 20, null, cd -> {
            if (onSave != null) onSave.accept(getJsonObj());
            super.close();
        }).setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("multiblocked.gui.label.save_pattern", -1).setDropShadow(true)).setHoverBorderTexture(1, -1));
    }

    @Override
    public void close() {
        super.close();
        if (onSave != null) onSave.accept(null);
    }

    protected void onTabChanged(WidgetGroup oldTag, WidgetGroup newTag) {
        if (newTag == JSON) {
            updatePatternJson();
        }
    }

    protected JsonObject getJsonObj() {
        JsonObject jsonObject = definition.toJson(new JsonObject());
        jsonObject.addProperty("location", location.toString());
        return jsonObject;
    }

    private String getComponentJson() {
        return Multiblocked.GSON.toJson(getJsonObj());
    }

    private void updatePatternJson() {
        textBox.setContent(Collections.singletonList(isPretty ? Multiblocked.prettyJson(getComponentJson()) : getComponentJson()));
        tfGroup.computeMax();
    }

    protected void updateStatusList() {
        statusList.clearAllWidgets();
        statusSettings.clearAllWidgets();
        for (StatusProperties status : definition.status.values()) {
            SelectableWidgetGroup group = new SelectableWidgetGroup(5, 1 + statusList.widgets.size() * 22, statusList.getSize().width - 10, 20);
            group.setSelectedTexture(-2, 0xff00aa00)
                    .setOnSelected(W -> {
                        statusSettings.clearAllWidgets();
                        DraggableWidgetGroup panel = new DraggableWidgetGroup(0, 0, statusSettings.getSize().width, statusSettings.getSize().height);
                        WidgetGroup scene = createScene(() -> status.renderer != null,  status::getRenderer, status::getShape, status::setRenderer);
                        statusSettings.addWidget(scene);
                        statusSettings.addWidget(panel);
                        panel.addWidget(new LabelWidget(4, 7, "multiblocked.gui.label.registry_name"));
                        panel.addWidget(new LabelWidget(4, 24, "Parent:"));
                        if (status.builtin) {
                            panel.addWidget(new LabelWidget(80, 7, status.getName()));
                            panel.addWidget(new LabelWidget(50, 24, status.getParent() == null ? "null" : status.getParent().getName()));
                        } else {
                            List<String> candidates = new ArrayList<>();
                            candidates.add("null");
                            candidates.addAll(definition.status.values().stream().filter(s->s!=status).map(StatusProperties::getName).collect(Collectors.toList()));
                            panel.addWidget(new TextFieldWidget(80, 4, 80, 15,  true, null, s -> {
                                String lastName = status.getName();
                                status.setName(s);
                                definition.status.remove(lastName);
                                definition.status.put(s, status);
                            })
                                    .setCurrentString(status.getName())
                                    .setHoverTooltip("multiblocked.gui.widget.component.status.name"));
                            panel.addWidget(new SelectorWidget(50, 22, 110, 15, candidates, -1)
                                    .setValue(status.getParent() == null ? "null" : status.getParent().getName())
                                    .setOnChanged(newParent -> status.setParent(definition.status.getOrDefault(newParent, null)))
                                    .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                                    .setBackground(new ColorRectTexture(0xffaaaaaa))
                                    .setHoverTooltip("multiblocked.gui.widget.component.status.parent"));
                        }
                        panel.addWidget(createStatusBoolSwitch(4, 40, "renderer", "multiblocked.gui.widget.component.status.renderer", status.renderer != null, widgetGroup -> {
                            if (widgetGroup == null) {
                                status.renderer = null;
                            } else {
                                status.setRenderer(status.renderer == null ? null : status.getRenderer());
                            }
                        }));
                        panel.addWidget(createStatusBoolSwitch(4, 60, "lightEmissive", "multiblocked.gui.widget.component.status.light_emissive", status.lightEmissive != null, widgetGroup -> {
                            if (widgetGroup == null) {
                                status.lightEmissive = null;
                            } else {
                                status.setLightEmissive(status.lightEmissive == null ? 0 : status.getLightEmissive());
                                widgetGroup.addWidget(GuiUtils.createIntField(30, 0, "", "", status.getLightEmissive(), 0, 15, status::setLightEmissive));
                            }
                        }));
                        panel.addWidget(createStatusBoolSwitch(4, 80, "shape", "multiblocked.gui.widget.component.status.shape", status.shape != null, widgetGroup -> {
                            if (widgetGroup == null) {
                                status.shape = null;
                            } else {
                                status.setShape(status.shape == null ? Collections.singletonList(Block.FULL_BLOCK_AABB) : status.getShape());
                                widgetGroup.addWidget(new ButtonWidget(20, 0, 15, 15, new ResourceTexture("multiblocked:textures/gui/option.png"),(cd) -> {
                                    scene.setVisible(false);
                                    new IShapeWidget(this, status.getRenderer(), status.getShape(), s -> {
                                        status.setShape(s);
                                        scene.setVisible(true);
                                    });
                                }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.settings"));
                            }
                        }));
                        panel.addWidget(createStatusBoolSwitch(4, 100, "sound", "multiblocked.gui.widget.component.status.sound", status.sound != null, widgetGroup -> {
                            if (widgetGroup == null) {
                                status.sound = null;
                            } else {
                                status.setSound(status.sound == null ? SoundState.EMPTY : status.getSound());
                                widgetGroup.addWidget(new ButtonWidget(20, 0, 15, 15, new ResourceTexture("multiblocked:textures/gui/option.png"),(cd) ->
                                        new ISoundWidget(this, status.getSound(), status::setSound))
                                        .setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.settings"));
                            }
                        }));
                    })
                    .addWidget(new ImageWidget(0, 0, 120, 20, new ColorRectTexture(0x4faaaaaa)))
                    .addWidget(new ImageWidget(2, 0, statusList.getSize().width - 14, 20, new TextTexture(status.getName())
                            .setSupplier(status::getName)
                            .setWidth(statusList.getSize().width - 14).setType(TextTexture.TextType.ROLL)));
            if (!status.builtin) {
                group.addWidget(new ButtonWidget(104, 4, 12, 12, new ResourceTexture("multiblocked:textures/gui/remove.png"), cd -> {
                    definition.status.remove(status.getName());
                    updateStatusList();
                }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.remove"));
            }
            statusList.addWidget(group);
        }
    }

    protected void updateRegistryName(String s) {
        location = (s != null && !s.isEmpty()) ? new ResourceLocation(s) : location;
    }

    protected WidgetGroup createStatusBoolSwitch(int x, int y, String text, String tips, boolean init, Consumer<WidgetGroup> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        AtomicReference<WidgetGroup> group = new AtomicReference<>();
        widgetGroup.addWidget(new SwitchWidget(0, 0, 15, 15, (cd, r) -> {
            if (r) {
                group.set(new WidgetGroup(60, 0, 100, 100));
                widgetGroup.addWidget(group.get());
                onPressed.accept(group.get());
            } else {
                widgetGroup.removeWidget(group.get());
                onPressed.accept(null);
            }
        })
                .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0,1,0.5))
                .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0.5,1,0.5))
                .setHoverTexture(new ColorBorderTexture(1, 0xff545757))
                .setPressed(init)
                .setHoverTooltip(tips));
        if (init) {
            group.set(new WidgetGroup(60, 0, 100, 100));
            widgetGroup.addWidget(group.get());
            onPressed.accept(group.get());
        }
        widgetGroup.addWidget(new LabelWidget(20, 3, text));
        return widgetGroup;
    }

    @SideOnly(Side.CLIENT)
    protected WidgetGroup createScene(Supplier<Boolean> custom, Supplier<IRenderer> init, Supplier<List<AxisAlignedBB>> shape, Consumer<IRenderer> onUpdate) {
        final int width = 90, height = 90;
        TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlock(BlockPos.ORIGIN, BlockInfo.fromBlockState(MbdComponents.DummyComponentBlock.getDefaultState()));
        DummyComponentTileEntity tileEntity = (DummyComponentTileEntity) world.getTileEntity(BlockPos.ORIGIN);
        tileEntity.setDefinition(new PartDefinition(new ResourceLocation(Multiblocked.MODID, "component_widget")));
        tileEntity.getDefinition().getBaseStatus().setRenderer(init.get());
        Widget buttonWidget = new ButtonWidget(width - 17, 2, 15, 15, new ResourceTexture("multiblocked:textures/gui/option.png"),(cd) ->
                new IRendererWidget(this, tileEntity.getRenderer(), r -> {
                    tileEntity.getDefinition().getBaseStatus().setRenderer(r);
                    onUpdate.accept(r);
                })).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.settings");
        buttonWidget.setVisible(custom.get());
        WidgetGroup widgetGroup = new WidgetGroup(-110, 75, width, height) {
            @Override
            public void updateScreen() {
                super.updateScreen();
                IRenderer currentRenderer = tileEntity.getRenderer();
                IRenderer newRenderer = init.get();
                if (currentRenderer != newRenderer) {
                    tileEntity.getDefinition().getBaseStatus().setRenderer(newRenderer);
                }
                buttonWidget.setVisible(custom.get());
            }
        };
        widgetGroup.addWidget(new ImageWidget(0, 0,  width, height, new ColorBorderTexture(2, 0xff4A82F7)));
        widgetGroup.addWidget(new SceneWidget(0, 0,  width, height, world) {
            @Override
            @SideOnly(Side.CLIENT)
            public void renderBlockOverLay(WorldSceneRenderer renderer) {
                super.renderBlockOverLay(renderer);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                Tessellator tessellator = Tessellator.getInstance();
                GlStateManager.disableTexture2D();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
                GlStateManager.glLineWidth(2);

                for (AxisAlignedBB aabb : shape.get()) {
                    RenderBufferUtils.renderCubeFrame(buffer, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, 0, 0, 0, 1);
                }

                tessellator.draw();
            }
        }
                .setRenderedCore(Collections.singleton(BlockPos.ORIGIN), null)
                .setRenderSelect(false)
                .setRenderFacing(false));
        widgetGroup.addWidget(buttonWidget);
        return widgetGroup;
    }
}
