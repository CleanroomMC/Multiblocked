package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components.PartWidget;
import com.cleanroommc.multiblocked.util.FileUtility;
import com.google.gson.JsonElement;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PartBuilderWidget extends WidgetGroup {
    DraggableScrollableWidgetGroup containers;
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
        }).setHoverBorderTexture(1, -1).setHoverTooltip("open folder"));
        this.addWidget(new ButtonWidget(200 - 4 - 20, 51, 20, 20, new ResourceTexture("multiblocked:textures/gui/add.png"), cd -> {
            for (Widget widget : widgets) {
                widget.setVisible(false);
                widget.setActive(false);
            }
            new PartWidget(this, new PartDefinition(new ResourceLocation("mod_id:component_id")), jsonObject -> {
                for (Widget widget : widgets) {
                    widget.setVisible(true);
                    widget.setActive(true);
                }
                if (jsonObject != null) {
                    FileUtility.saveJson(new File(Multiblocked.location, "definition/part/" + jsonObject.get("location").getAsString().replace(":", "_") + ".json"), jsonObject);
                }
                updateList();
            });
        }).setHoverBorderTexture(1, -1).setHoverTooltip("create a new part block"));
        updateList();
    }

    protected void updateList() {
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
                    .setSelectedTexture(-2, 0xff00aa00)
                    .addWidget(new ImageWidget(0, 0, 150, 20, new ColorRectTexture(0x4faaaaaa)))
                    .addWidget(new ButtonWidget(134, 4, 12, 12, new ResourceTexture("multiblocked:textures/gui/option.png"), cd -> {
                        JsonElement jsonElement = FileUtility.loadJson(file);
                        if (jsonElement != null) {
                            try {
                                PartDefinition definition = Multiblocked.GSON.fromJson(jsonElement, PartDefinition.class);
                                for (Widget widget : widgets) {
                                    widget.setVisible(false);
                                    widget.setActive(false);
                                }
                                new PartWidget(this, definition, jsonObject -> {
                                    for (Widget widget : widgets) {
                                        widget.setVisible(true);
                                        widget.setActive(true);
                                    }
                                    if (jsonObject != null) {
                                        FileUtility.saveJson(file, jsonObject);
                                    }
                                });
                            } catch (Exception ignored) {}
                        }
                    }).setHoverBorderTexture(1, -1).setHoverTooltip("setting"))
                    .addWidget(new ImageWidget(32, 0, 100, 20, new TextTexture(file.getName().replace(".json", "")).setWidth(100).setType(TextTexture.TextType.ROLL)))
                    .addWidget(new ImageWidget(4, 2, 18, 18, new ItemStackTexture(Items.PAPER)));
            files.add(widgetGroup);
            containers.addWidget(widgetGroup);
        }
    }
}
