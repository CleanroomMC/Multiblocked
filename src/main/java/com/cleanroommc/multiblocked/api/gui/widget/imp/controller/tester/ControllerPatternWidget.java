package com.cleanroommc.multiblocked.api.gui.widget.imp.controller.tester;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.controller.PageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import com.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.tile.ControllerTileTesterEntity;
import com.cleanroommc.multiblocked.util.FileUtility;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.PacketBuffer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ControllerPatternWidget extends PageWidget {

    private static final ResourceTexture PAGE = new ResourceTexture("multiblocked:textures/gui/json_loader_page.png");
    private final ControllerTileTesterEntity controller;
    private final DraggableScrollableWidgetGroup jsonList;
    private File selected;

    public ControllerPatternWidget(ControllerTileTesterEntity controller, TabContainer tabContainer) {
        super(PAGE, tabContainer); //176, 256
        this.controller = controller;
        this.addWidget(new ImageWidget(5, 5, 176 - 10, 150, ResourceBorderTexture.BORDERED_BACKGROUND_BLUE));
        this.addWidget(jsonList = new DraggableScrollableWidgetGroup(10, 10, 176 - 20, 150 - 10));
        this.addWidget(new ButtonWidget(5, 160, 20, 20, new ResourceTexture("multiblocked:textures/gui/save.png"), cd->{
            if (!cd.isRemote) return;
            try {
                File dir = new File(Multiblocked.location, "definition/controller");
                Desktop.getDesktop().open(dir.isDirectory() ? dir : dir.getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).setHoverBorderTexture(1, -1).setHoverTooltip("open folder"));
        this.addWidget(new ButtonWidget(30, 160, 120, 20, null, this::loadJson).setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("load controller", -1).setDropShadow(true)).setHoverBorderTexture(1, -1)).setHoverTooltip("open folder");
        updateList();
    }

    private void loadJson(ClickData clickData) {
        if (selected != null) {
            JsonElement jsonElement = FileUtility.loadJson(selected);
            if (jsonElement != null) {
                try {
                    String recipeMap = jsonElement.getAsJsonObject().get("recipeMap").getAsString();
                    JsonBlockPattern pattern = Multiblocked.GSON.fromJson(jsonElement.getAsJsonObject().get("basePattern"), JsonBlockPattern.class);
                    ControllerDefinition definition = Multiblocked.GSON.fromJson(jsonElement, ControllerDefinition.class);
                    definition.basePattern = pattern.build();
                    for (File file : Optional.ofNullable(new File(Multiblocked.location, "recipe_map").listFiles((f, n) -> n.endsWith(".json"))).orElse(new File[0])) {
                        JsonObject config = (JsonObject) FileUtility.loadJson(file);
                        if (config != null && config.get("name").getAsString().equals(recipeMap)) {
                            definition.recipeMap = Multiblocked.GSON.fromJson(config, RecipeMap.class);
                        }
                    }
                    controller.setDefinition(definition);
                } catch (Exception e) {
                    Multiblocked.LOGGER.error("tester: error while loading the controller json {}", selected.getName(), e);
                }
            }
        }
    }

    private void updateList() {
        jsonList.clearAllWidgets();
        selected = null;
        File path = new File(Multiblocked.location, "definition/controller");
        if (!path.isDirectory()) {
            if (!path.mkdirs()) {
                return;
            }
        }
        for (File file : Optional.ofNullable(path.listFiles()).orElse(new File[0])) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                jsonList.addWidget(new SelectableWidgetGroup(5, 1 + jsonList.widgets.size() * 22, jsonList.getSize().width - 10, 20)
                        .setSelectedTexture(-2, 0xff00aa00)
                        .setOnSelected(W -> {
                            selected = file;
                            writeClientAction(-1, buffer -> {
                                buffer.writeBoolean(selected != null);
                                if (selected != null) {
                                    buffer.writeString(file.getName());
                                }
                            });
                        })
                        .addWidget(new ImageWidget(0, 0, jsonList.getSize().width - 10, 20, new ColorRectTexture(0x4f444444)))
                        .addWidget(new ImageWidget(0, 0, jsonList.getSize().width - 10, 20, new TextTexture(file.getName().replace(".json", "")).setWidth(jsonList.getSize().width - 10).setType(TextTexture.TextType.ROLL))));
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) {
            if (buffer.readBoolean()) {
                selected = new File(Multiblocked.location, "definition/controller/" + buffer.readString(Short.MAX_VALUE));
            } else {
                selected = null;
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }
}
