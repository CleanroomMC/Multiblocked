package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.RecipeMapWidget;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.util.FileUtility;
import com.google.gson.JsonElement;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class RecipeMapBuilderWidget extends WidgetGroup {
    private DraggableScrollableWidgetGroup recipeMapList;
    private File selected;
    private Consumer<RecipeMap> onRecipeMapSelected;
    private final WidgetGroup parent;

    public RecipeMapBuilderWidget(WidgetGroup parent, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.setClientSideWidget();
        this.parent = parent;
        if (!Multiblocked.isClient()) return;
        this.addWidget(new ImageWidget(20, 0, width - 20, height, ResourceBorderTexture.BORDERED_BACKGROUND_BLUE));
        this.addWidget(recipeMapList = new DraggableScrollableWidgetGroup(20, 4, width - 20, height - 8));
        this.addWidget(new ButtonWidget(0, 5, 20, 20, new ResourceTexture("multiblocked:textures/gui/save.png"), cd->{
            try {
                File dir = new File(Multiblocked.location, "recipe_map");
                Desktop.getDesktop().open(dir.isDirectory() ? dir : dir.getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).setHoverBorderTexture(1, -1).setHoverTooltip("open folder"));
        this.addWidget(new ButtonWidget(0, 26, 20, 20, new ResourceTexture("multiblocked:textures/gui/add.png"), cd->{
            new RecipeMapWidget(parent, new RecipeMap(UUID.randomUUID().toString()),recipeMap -> {
                if (recipeMap != null) {
                    File path = new File(Multiblocked.location, "recipe_map/" + recipeMap.name + ".json");
                    JsonElement element = Multiblocked.GSON.toJsonTree(recipeMap);
                    FileUtility.saveJson(path, element);
                    updateRecipeMapList();
                }
            });
        }).setHoverBorderTexture(1, -1).setHoverTooltip("create a new RecipeMap"));
        updateRecipeMapList();
    }

    public RecipeMapBuilderWidget setOnRecipeMapSelected(Consumer<RecipeMap> onRecipeMapSelected) {
        this.onRecipeMapSelected = onRecipeMapSelected;
        return this;
    }

    private void updateRecipeMapList() {
        recipeMapList.clearAllWidgets();
        if (onRecipeMapSelected != null) {
            onRecipeMapSelected.accept(RecipeMap.EMPTY);
        }
        selected = null;
        File path = new File(Multiblocked.location, "recipe_map");
        if (!path.isDirectory()) {
            if (!path.mkdirs()) {
                return;
            }
        }
        for (File file : Optional.ofNullable(path.listFiles()).orElse(new File[0])) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                recipeMapList.addWidget(new SelectableWidgetGroup(5, 1 + recipeMapList.widgets.size() * 22, getSize().width - 30, 20)
                        .setSelectedTexture(-2, 0xff00aa00)
                        .setOnSelected(W -> {
                            selected = file;
                            if (onRecipeMapSelected != null) {
                                onRecipeMapSelected.accept(Multiblocked.GSON.fromJson(FileUtility.loadJson(file), RecipeMap.class));
                            }
                        })
                        .addWidget(new ImageWidget(0, 0, 120, 20, new ColorRectTexture(0x4faaaaaa)))
                        .addWidget(new ButtonWidget(104, 4, 12, 12, new ResourceTexture("multiblocked:textures/gui/option.png"), cd -> new RecipeMapWidget(parent, Multiblocked.GSON.fromJson(FileUtility.loadJson(file), RecipeMap.class), recipeMap -> {
                            if (recipeMap != null) {
                                if (selected == file) {
                                    if (onRecipeMapSelected != null) {
                                        onRecipeMapSelected.accept(recipeMap);
                                    }
                                }
                                JsonElement element = Multiblocked.GSON.toJsonTree(recipeMap);
                                FileUtility.saveJson(file, element);
                            }
                        })).setHoverBorderTexture(1, -1).setHoverTooltip("setting"))
                        .addWidget(new ImageWidget(2, 0, 96, 20, new TextTexture(file.getName()).setWidth(96).setType(
                                TextTexture.TextType.ROLL))));
            }
        }
    }

}
