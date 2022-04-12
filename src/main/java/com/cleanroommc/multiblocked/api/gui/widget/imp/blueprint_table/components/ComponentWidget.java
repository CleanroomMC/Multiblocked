package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.CapabilityTrait;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextBoxWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.IRendererWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.ResourceTextureWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.function.Consumer;

public class ComponentWidget<T extends ComponentDefinition> extends DialogWidget {
    protected final T definition;
    protected ResourceLocation location;
    protected final TabContainer tabContainer;
    protected final WidgetGroup S1;
    protected final WidgetGroup S2;
    protected final WidgetGroup JSON;
    private final DraggableScrollableWidgetGroup tfGroup;
    private final TextBoxWidget textBox;
    private final Consumer<JsonObject> onSave;
    private boolean isPretty;

    public ComponentWidget(WidgetGroup group, T definition, Consumer<JsonObject> onSave) {
        super(group, true);
        this.onSave = onSave;
        this.definition = definition;
        this.location = definition.location;
        this.addWidget(new ImageWidget(0, 0, 384, 256, new ResourceTexture("multiblocked:textures/gui/component.png")));
        this.addWidget(tabContainer = new TabContainer(0, 0, 384, 256).setOnChanged(this::onTabChanged));
        tabContainer.addTab(new TabButton(42, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S1"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S1"))
                        .setHoverTooltip("Step 1: basic setup"),
                S1 = new WidgetGroup(0, 0, getSize().width, getSize().height));
        int x = 47;
        S1.addWidget(new LabelWidget(x, 57, ()->"Registry Name:").setDrop(true).setTextColor(-1));
        S1.addWidget(new TextFieldWidget(x + 80, 54, 150, 15, true, null, this::updateRegistryName).setCurrentString(this.location.toString()));
        S1.addWidget(createBoolSwitch(x, 75, "allowRotate", "allow rotation", definition.allowRotate, r -> definition.allowRotate = r));
        S1.addWidget(createBoolSwitch(x, 90, "showInJei", "show in jei", definition.showInJei, r -> definition.showInJei = r));
        S1.addWidget(createBoolSwitch(x, 105, "isOpaqueCube", "is opaque block", definition.isOpaqueCube, r -> definition.isOpaqueCube = r));
        S1.addWidget(createScene(x - 2, 125, "baseRenderer", "basic renderer", definition.baseRenderer, r -> definition.baseRenderer = r));
        S1.addWidget(createScene(x + 98, 125, "formedRenderer", "formed renderer", definition.formedRenderer, r -> definition.formedRenderer = r));
        S1.addWidget(createScene(x + 198, 125, "workingRenderer", "working renderer", definition.workingRenderer, r -> definition.workingRenderer = r));

        tabContainer.addTab(new TabButton(65, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S2"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S2"))
                        .setHoverTooltip("Step 2: trait setup"),
                S2 = new WidgetGroup(0, 0, getSize().width, getSize().height));
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
                    }).setHoverTooltip("set background texture"));
                    
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
                        .setHoverTooltip(capability.name));
                widgetGroup.addWidget(new LabelWidget(40, 3, capability.getUnlocalizedName()));
                S2.addWidget(widgetGroup);
                y += 15;
            }
        }

        tabContainer.addTab(new TabButton(235, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("J"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("F"))
                        .setHoverTooltip("Finish"),
                JSON = new WidgetGroup(0, 0, getSize().width, getSize().height));
        JSON.addWidget(new SwitchWidget(50, 54, 16, 16, (cd,r) -> {
            isPretty = r;
            updatePatternJson();
        }).setHoverBorderTexture(1, -1).setTexture(new ResourceTexture("multiblocked:textures/gui/pretty.png"), new ResourceTexture("multiblocked:textures/gui/pretty_active.png")).setHoverTooltip("pretty format"));
        JSON.addWidget(new ButtonWidget(70, 54, 16, 16, cd -> GuiScreen.setClipboardString(isPretty ? Multiblocked.prettyJson(getComponentJson()) : getComponentJson())).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/copy.png")).setHoverBorderTexture(1, -1).setHoverTooltip("copy to clipboard"));
        JSON.addWidget(new ImageWidget(47, 75, 285, 136, new ColorBorderTexture(1, 0xafafaf00)));
        JSON.addWidget(tfGroup = new DraggableScrollableWidgetGroup(47, 75, 285, 136)
                .setBackground(new ColorRectTexture(0x8f111111))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(-1)));
        tfGroup.addWidget(textBox = new TextBoxWidget(0, 0, 285, Collections.singletonList("")).setFontColor(-1).setShadow(true));

        this.addWidget(new ButtonWidget(260, 26, 80, 20, null, cd -> {
            if (onSave != null) onSave.accept(getJsonObj());
            super.close();
        }).setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("Save Pattern", -1).setDropShadow(true)).setHoverBorderTexture(1, -1));
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
        JsonObject jsonObject = (JsonObject) Multiblocked.GSON.toJsonTree(definition);
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

    protected void updateRegistryName(String s) {
        location = (s != null && !s.isEmpty()) ? new ResourceLocation(s) : location;
    }

    protected WidgetGroup createBoolSwitch(int x, int y, String text, String tips, boolean init, Consumer<Boolean> onPressed) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 100, 15);
        widgetGroup.addWidget(new SwitchWidget(0, 0, 15, 15, (cd, r)->onPressed.accept(r))
                .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0,1,0.5))
                .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/boolean.png").getSubTexture(0,0.5,1,0.5))
                .setHoverTexture(new ColorBorderTexture(1, 0xff545757))
                .setPressed(init)
                .setHoverTooltip(tips));
        widgetGroup.addWidget(new LabelWidget(20, 3, text));
        return widgetGroup;
    }

    @SideOnly(Side.CLIENT)
    protected WidgetGroup createScene(int x, int y, String text, String tips, IRenderer init, Consumer<IRenderer> onUpdate) {
        TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlock(BlockPos.ORIGIN, new BlockInfo(MbdComponents.DummyComponentBlock));
        DummyComponentTileEntity tileEntity = (DummyComponentTileEntity) world.getTileEntity(BlockPos.ORIGIN);
        tileEntity.setDefinition(new PartDefinition(new ResourceLocation(Multiblocked.MODID, "component_widget")));
        tileEntity.getDefinition().baseRenderer = init;
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 90, 90);
        widgetGroup.addWidget(new LabelWidget(0, 0, text));
        widgetGroup.addWidget(new ImageWidget(0, 12,  90, 80, new ColorBorderTexture(2, 0xff4A82F7)));
        widgetGroup.addWidget(new SceneWidget(0, 12,  90, 80, world)
                .setRenderedCore(Collections.singleton(BlockPos.ORIGIN), null)
                .setRenderSelect(false)
                .setRenderFacing(false));
        widgetGroup.addWidget(new ButtonWidget(90-15, 12, 15, 15, new ResourceTexture("multiblocked:textures/gui/option.png"),(cd) ->
                new IRendererWidget(this, tileEntity.getRenderer(), r -> {
                    tileEntity.getDefinition().baseRenderer = r;
                    onUpdate.accept(r);
                })).setHoverBorderTexture(1, -1).setHoverTooltip(tips));

        return widgetGroup;
    }
}
