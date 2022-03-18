package io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.IRendererWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import io.github.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.function.Consumer;

public class ComponentWidget extends WidgetGroup {
    protected final ComponentDefinition definition;
    protected ResourceLocation location;
    protected final TabContainer tabContainer;
    protected final WidgetGroup S1;

    public ComponentWidget(ComponentDefinition definition) {
        super(0, 0, 384, 256);
        setClientSideWidget();
        this.definition = definition;
        this.location = definition.location;
        this.addWidget(new ImageWidget(0, 0, 384, 256, new ResourceTexture("multiblocked:textures/gui/component.png")));
        this.addWidget(tabContainer = new TabContainer(0, 0, 384, 256));
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
    }

    protected void updateRegistryName(String s) {
        location = (s != null && !s.isEmpty()) ? new ResourceLocation(s) : location;
    }

    public WidgetGroup createBoolSwitch(int x, int y, String text, String tips, boolean init, Consumer<Boolean> onPressed) {
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

    public WidgetGroup createScene(int x, int y, String text, String tips, IRenderer init, Consumer<IRenderer> onUpdate) {
        TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlock(BlockPos.ORIGIN, new BlockInfo(MultiblockComponents.DummyComponentBlock));
        DummyComponentTileEntity tileEntity = (DummyComponentTileEntity) world.getTileEntity(BlockPos.ORIGIN);
        tileEntity.setDefinition(new PartDefinition(new ResourceLocation(Multiblocked.MODID, "component_widget")));
        tileEntity.getDefinition().baseRenderer = init;
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 90, 90);
        widgetGroup.addWidget(new LabelWidget(0, 0, ()->text).setTextColor(-1).setDrop(true));
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
