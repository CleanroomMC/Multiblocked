package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components.PartWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.cleanroommc.multiblocked.util.FileUtility;
import com.google.gson.JsonElement;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PartBuilderWidget extends WidgetGroup {
    DraggableScrollableWidgetGroup containers;
    DummyComponentTileEntity tileEntity;
    List<SelectableWidgetGroup> files = new ArrayList<>();

    public PartBuilderWidget() {
        super(0, 0, 384, 256);
        setClientSideWidget();
        if (!Multiblocked.isClient()) return;
        this.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/blueprint_page.png")));
        this.addWidget(new ImageWidget(200 - 4, 30 - 4, 150 + 8, 190 + 8, ResourceBorderTexture.BORDERED_BACKGROUND_BLUE));
        this.addWidget(containers = new DraggableScrollableWidgetGroup(200, 30, 150, 190));
        this.addWidget(new ButtonWidget(200 - 4 - 20, 30, 20, 20, new ResourceTexture("multiblocked:textures/gui/save.png"), cd -> {
            if (cd.isRemote) {
                try {
                    File dir = new File(Multiblocked.location, "definition/part");
                    Desktop.getDesktop().open(dir.isDirectory() ? dir : dir.getParentFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.open_folder"));
        this.addWidget(new ButtonWidget(200 - 4 - 20, 51, 20, 20, new ResourceTexture("multiblocked:textures/gui/add.png"), cd -> {
            new PartWidget(this, new PartDefinition(new ResourceLocation("mod_id:component_id")), jsonObject -> {

                if (jsonObject != null) {
                    FileUtility.saveJson(new File(Multiblocked.location, "definition/part/" + jsonObject.get("location").getAsString().replace(":", "_") + ".json"), jsonObject);
                }
                updateList();
            });
        }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.builder.part.create"));
        initScene();
        updateList();
    }

    @SideOnly(Side.CLIENT)
    private void initScene() {
        TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlock(BlockPos.ORIGIN, new BlockInfo(MbdComponents.DummyComponentBlock));
        tileEntity = (DummyComponentTileEntity) world.getTileEntity(BlockPos.ORIGIN);
        this.addWidget(new ImageWidget(30, 59, 138, 138, new GuiTextureGroup(new ColorBorderTexture(3, -1), new ColorRectTexture(0xaf444444))));
        this.addWidget(new SceneWidget(30, 59,  138, 138, world)
                .setRenderedCore(Collections.singleton(BlockPos.ORIGIN), null)
                .setRenderSelect(false)
                .setRenderFacing(false));
    }

    private void setNewRenderer(IRenderer newRenderer) {
        PartDefinition definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "i_renderer"));
        definition.getBaseStatus().setRenderer(newRenderer);
        tileEntity.setDefinition(definition);
    }

    protected void updateList() {
        setNewRenderer(null);
        int size = files.size();
        files.forEach(containers::waitToRemoved);
        files.clear();
        File path = new File(Multiblocked.location, "definition/part");
        if (!path.isDirectory()) {
            if (!path.mkdirs()) {
                return;
            }
        }
        for (File file : Optional.ofNullable(path.listFiles((s, name) -> name.endsWith(".json"))).orElse(new File[0])) {
            SelectableWidgetGroup widgetGroup = (SelectableWidgetGroup) new SelectableWidgetGroup(0, (containers.widgets.size() - size) * 22, containers.getSize().width, 20)
                    .setOnSelected(group -> {
                        JsonElement jsonElement = FileUtility.loadJson(file);
                        if (jsonElement != null) {
                            try {
                                setNewRenderer(Multiblocked.GSON.fromJson(jsonElement, PartDefinition.class).getRenderer());
                            } catch (Exception ignored) {}
                        }
                    })
                    .setSelectedTexture(-2, 0xff00aa00)
                    .addWidget(new ImageWidget(0, 0, 150, 20, new ColorRectTexture(0x4faaaaaa)))
                    .addWidget(new ButtonWidget(134, 4, 12, 12, new ResourceTexture("multiblocked:textures/gui/option.png"), cd -> {
                        JsonElement jsonElement = FileUtility.loadJson(file);
                        if (jsonElement != null) {
                            try {
                                PartDefinition definition = Multiblocked.GSON.fromJson(jsonElement, PartDefinition.class);
                                new PartWidget(this, definition, jsonObject -> {
                                    if (jsonObject != null) {
                                        FileUtility.saveJson(file, jsonObject);
                                    }
                                });
                            } catch (Exception ignored) {}
                        }
                    }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.settings"))
                    .addWidget(new ImageWidget(32, 0, 100, 20, new TextTexture(file.getName().replace(".json", "")).setWidth(100).setType(TextTexture.TextType.ROLL)))
                    .addWidget(new ImageWidget(4, 2, 18, 18, new ItemStackTexture(Items.PAPER)));
            files.add(widgetGroup);
            containers.addWidget(widgetGroup);
        }
    }
}
