package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.registry.MbdRenderers;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.ICustomRenderer;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        world.addBlock(BlockPos.ORIGIN, new BlockInfo(MbdComponents.DummyComponentBlock));
        tileEntity = (DummyComponentTileEntity) world.getTileEntity(BlockPos.ORIGIN);
        setNewRenderer(renderer);
        this.addWidget(new ImageWidget(35, 59, 138, 138, new GuiTextureGroup(new ColorBorderTexture(3, -1), new ColorRectTexture(0xaf444444))));
        this.addWidget(new SceneWidget(35, 59,  138, 138, world)
                .setRenderedCore(Collections.singleton(BlockPos.ORIGIN), null)
                .setRenderSelect(false)
                .setRenderFacing(false));
        this.addWidget(group = new DraggableScrollableWidgetGroup(181, 80, 180, 120));
        this.addWidget(new ButtonWidget(285, 55, 40, 20, this::onUpdate)
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("multiblocked.gui.tips.update", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("multiblocked.gui.tips.update"));
        this.addWidget(new ButtonWidget(330, 55, 45, 20, cd -> Minecraft.getMinecraft().scheduleResourcesRefresh())
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("multiblocked.gui.tips.refresh", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("multiblocked.gui.dialogs.renderer.refresh"));
        this.addWidget(new SelectorWidget(181, 55, 100, 20, getRendererList(), -1)
                .setValue(getType(renderer))
                .setOnChanged(this::onChangeRenderer)
                .setButtonBackground(new ColorBorderTexture(1, -1), new ColorRectTexture(0xff444444))
                .setBackground(new ColorRectTexture(0xff999999))
                .setHoverTooltip("multiblocked.gui.dialogs.renderer.renderer"));
        onChangeRenderer(getType(renderer));
        if (onSave == null) return;
        this.addWidget(new ButtonWidget(285, 30, 40, 20, cd -> {
            if (tileEntity != null) {
                onSave.accept(tileEntity.getRenderer());
            }
            super.close();
        })
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("multiblocked.gui.tips.save_1", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("multiblocked.gui.tips.save"));
    }

    private List<String> getRendererList() {
        List<String> list = new ArrayList<>();
        list.add("multiblocked.renderer.null");
        MbdRenderers.RENDERER_REGISTRY.values().stream().map(ICustomRenderer::getUnlocalizedName).forEach(list::add);
        return list;
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
        definition.getBaseStatus().setRenderer(newRenderer);
        tileEntity.setDefinition(definition);
    }

    private void onUpdate(ClickData clickData) {
        if (onUpdate != null) onUpdate.run();
    }

    private void onChangeRenderer(String s) {
        group.clearAllWidgets();
        onUpdate = null;
        IRenderer current = tileEntity.getRenderer();
        String[] split = s.split("\\.");
        s = split[split.length - 1];
        if (s.equals("null")) {
            onUpdate = () -> setNewRenderer(null);
        } else {
            ICustomRenderer renderer = MbdRenderers.getRenderer(s);
            if (renderer != null) {
                Supplier<IRenderer> supplier = renderer.createConfigurator(this, group, current);
                if (supplier != null) {
                    onUpdate = () -> setNewRenderer(supplier.get());
                }
            }
        }
    }

    public static String getType(IRenderer renderer) {
        if (renderer instanceof ICustomRenderer) {
            return ((ICustomRenderer) renderer).getUnlocalizedName();
        }
        return "multiblocked.renderer.null";
    }

}
