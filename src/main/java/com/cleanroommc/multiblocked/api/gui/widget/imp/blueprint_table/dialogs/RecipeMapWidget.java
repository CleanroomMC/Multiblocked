package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.*;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ProgressWidget;
import com.cleanroommc.multiblocked.api.recipe.Content;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.recipe.RecipeCondition;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.api.registry.MbdRecipeConditions;
import com.cleanroommc.multiblocked.util.Size;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RecipeMapWidget extends DialogWidget {
    protected RecipeMap recipeMap;
    protected final DraggableScrollableWidgetGroup recipesList;
    protected final WidgetGroup configurator;
    protected final WidgetGroup recipeIO;
    protected WidgetGroup contentCandidates;
    protected final List<RecipeItem> recipes;
    protected final Consumer<RecipeMap> onSave;
    protected WidgetGroup conditionGroup;

    public RecipeMapWidget(WidgetGroup parent, RecipeMap recipeMap, Consumer<RecipeMap> onSave) {
        super(parent, true);
        setParentInVisible();
        this.recipeMap = recipeMap;
        this.onSave = onSave;
        this.recipes = new ArrayList<>();
        this.addWidget(new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/blueprint_page.png")));
        this.addWidget(new LabelWidget(20, 25, "ID:"));
        this.addWidget(new TextFieldWidget(40, 20, 130, 15,  true, null, s -> recipeMap.name = s).setCurrentString(recipeMap.name));
        this.addWidget(new ButtonWidget(180, 17, 40, 20, this::onSave).setButtonTexture(
                ResourceBorderTexture.BUTTON_COMMON, new TextTexture("multiblocked.gui.tips.save_1", -1).setDropShadow(true)).setHoverBorderTexture(1, -1));
        this.addWidget(new ImageWidget(250, 3, 130, 128, ResourceBorderTexture.BORDERED_BACKGROUND_BLUE));
        this.addWidget(recipesList = new DraggableScrollableWidgetGroup(250, 7, 130, 120));
        this.addWidget(configurator = new WidgetGroup(250, 132, 130, 120));
        this.addWidget(recipeIO = new WidgetGroup(50, 50, 176, 100));
        this.addWidget(new ButtonWidget(230, 10, 20, 20, new ResourceTexture("multiblocked:textures/gui/add.png"), cd -> {
            Recipe recipe = new Recipe(UUID.randomUUID().toString(), ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of(), ImmutableList.of(), 1);
            recipes.add(new RecipeItem(recipe, false));
            recipesList.addWidget(recipes.get(recipes.size() - 1));
        }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.dialogs.recipe_map.add_recipe"));
        this.addWidget(new ButtonWidget(230, 32, 20, 20, new GuiTextureGroup(new ResourceTexture("multiblocked:textures/gui/add.png"), new ItemStackTexture(Items.COAL)), cd -> {
            Recipe recipe = new Recipe(UUID.randomUUID().toString(), ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of(), ImmutableList.of(), 1);
            recipes.add(new RecipeItem(recipe, true));
            recipesList.addWidget(recipes.get(recipes.size() - 1));
        }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.dialogs.recipe_map.add_fuel_tip"));
        if (recipeMap.isFuelRecipeMap()) {
            for (Recipe recipe : recipeMap.fuelRecipes) {
                recipes.add(new RecipeItem(recipe, true));
                recipesList.addWidget(recipes.get(recipes.size() - 1));
            }
        }
        for (Recipe recipe : recipeMap.recipes.values()) {
            recipes.add(new RecipeItem(recipe, false));
            recipesList.addWidget(recipes.get(recipes.size() - 1));
        }
        this.addWidget(conditionGroup = new WidgetGroup(50, 170, 176, 70));
    }

    public boolean isFuelRecipeMap() {
        return recipes.stream().anyMatch(r -> r.isFuel);
    }

    @Override
    public void close() {
        super.close();
        onSave.accept(null);
    }

    private void onSave(ClickData clickData) {
        if (recipeMap.isFuelRecipeMap()) {
            recipeMap.fuelRecipes.clear();
        }
        recipeMap.recipes.clear();
        recipeMap.inputCapabilities.clear();
        recipeMap.outputCapabilities.clear();
        for (RecipeItem recipeItem : recipes) {
            if (!recipeItem.isFuel) {
                recipeMap.addRecipe(recipeItem.getRecipe());
            } else {
                recipeMap.addFuelRecipe(recipeItem.getRecipe());
            }
        }
        onSave.accept(recipeMap);
        super.close();
    }

    @Override
    public Widget mouseClicked(int mouseX, int mouseY, int button) {
        Widget widget;
        if (contentCandidates != null && contentCandidates.isMouseOverElement(mouseX, mouseY)) {
            widget = contentCandidates;
            contentCandidates.mouseClicked(mouseX, mouseY, button);
        } else {
            widget = super.mouseClicked(mouseX, mouseY, button);
        }
        if (contentCandidates != null) {
            if (widgets.contains(contentCandidates)) {
                removeWidget(contentCandidates);
                contentCandidates = null;
            } else {
                addWidget(contentCandidates);
            }
        }
        return widget;
    }

    class RecipeItem extends WidgetGroup implements DraggableScrollableWidgetGroup.ISelected {
        private final boolean isFuel;
        private Recipe recipe;
        private boolean isSelected;
        private String uid;
        private int duration;
        private final Map<MultiblockCapability<?>, List<ContentWidget<?>>> inputs;
        private final Map<MultiblockCapability<?>, List<ContentWidget<?>>> outputs;
        private final List<RecipeCondition> conditions;
        private ContentWidget<?> selectedContent;
        private ButtonWidget removed;

        public RecipeItem(Recipe recipe, boolean isFuel) {
            super(5, 5 + recipesList.widgets.size() * 22, 120, 20);
            this.isFuel = isFuel;
            this.uid = recipe.uid;
            this.recipe = recipe;
            this.duration = recipe.duration;
            inputs = new HashMap<>();
            outputs = new HashMap<>();
            conditions = new ArrayList<>();
            this.addWidget(new ImageWidget(0, 0, 120, 20, new ColorRectTexture(0x5f49F75C)));
            this.addWidget(new ButtonWidget(104, 4, 12, 12, new ResourceTexture("multiblocked:textures/gui/remove.png"), cd -> {
                boolean find = false;
                for (Widget widget : recipesList.widgets) {
                    if (widget == RecipeItem.this) {
                        find = true;
                    } else if (find) {
                        widget.addSelfPosition(0, -22);
                    }
                }
                if (isSelected) {
                    configurator.clearAllWidgets();
                    recipeIO.clearAllWidgets();
                }
                recipes.remove(this);
                recipesList.waitToRemoved(RecipeItem.this);
            }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.dialogs.recipe_map.remove_recipe"));
            for (RecipeCondition condition : recipe.conditions) {
                conditions.add(condition.createTemplate().deserialize(condition.serialize()));
            }
        }

        @Override
        public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(mouseX, mouseY, partialTicks);
            if (inputs.isEmpty() && outputs.isEmpty()) updateIOContentWidgets();
            List<ContentWidget<?>> in = inputs.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
            for (int i = 0; i < Math.min(2, in.size()); i++) {
                ContentWidget<?> widget = in.get(i);
                int x = getPosition().x - widget.getPosition().x + i * 20;
                int y = getPosition().y - widget.getPosition().y;
                GlStateManager.translate(x, y, 0);
                widget.drawInBackground(-1, -1, partialTicks);
                GlStateManager.translate(-x, -y, 0);
            }

            if (isFuel) {
                new TextTexture("multiblocked.gui.dialogs.recipe_map.fuel_recipe").setType(TextTexture.TextType.LEFT).draw(mouseX, mouseY, getPosition().x + 60, getPosition().y + 6, 100, 10);
            } else {
                List<ContentWidget<?>> out = outputs.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
                for (int i = 0; i < Math.min(2, out.size()); i++) {
                    ContentWidget<?> widget = out.get(i);
                    int x = getPosition().x - widget.getPosition().x + i * 20 + 60;
                    int y = getPosition().y - widget.getPosition().y;
                    GlStateManager.translate(x, y, 0);
                    widget.drawInBackground(-1, -1, partialTicks);
                    GlStateManager.translate(-x, -y, 0);
                }
            }

            if (isSelected) {
                DrawerHelper.drawBorder(getPosition().x + 1, getPosition().y + 1, getSize().width - 2, getSize().height - 2, 0xffF76255, 2);
            }
        }

        @Override
        public boolean allowSelected(int mouseX, int mouseY, int button) {
            return isMouseOverElement(mouseX, mouseY);
        }

        @Override
        public void onSelected() {
            if (!isSelected) {
                isSelected = true;
                updateRecipeWidget();
            }
        }

        @Override
        public void onUnSelected() {
            isSelected = false;
            updateRecipe();
        }

        private void updateRecipe() {
            recipe = new Recipe(uid,
                    rebuild(inputs, false),
                    rebuild(outputs, false),
                    rebuild(inputs, true),
                    rebuild(outputs, true),
                    ImmutableList.copyOf(conditions),
                    duration);
        }

        private ImmutableMap<MultiblockCapability<?>, ImmutableList<Content>> rebuild(Map<MultiblockCapability<?>, List<ContentWidget<?>>> contents, boolean perTick) {
            ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Content>> builder = new ImmutableMap.Builder<>();
            for (Map.Entry<MultiblockCapability<?>, List<ContentWidget<?>>> entry : contents.entrySet()) {
                MultiblockCapability<?> capability = entry.getKey();
                if (capability != null && !entry.getValue().isEmpty()) {
                    ImmutableList.Builder<Content> listBuilder = new ImmutableList.Builder<>();
                    for (ContentWidget<?> content : entry.getValue()) {
                        if (content.getPerTick() == perTick) {
                            listBuilder.add(new Content(content.getContent(), content.getChance(), content.getSlotName()));
                        }
                    }
                    ImmutableList<Content> list = listBuilder.build();
                    if (!list.isEmpty()) {
                        builder.put(capability, listBuilder.build());
                    }
                }
            }
            return builder.build();
        }

        public Recipe getRecipe() {
            if (!isSelected) return recipe;
            updateRecipe();
            return recipe;
        }

        private void updateIOContentWidgets() {
            inputs.clear();
            outputs.clear();
            for (Map.Entry<MultiblockCapability<?>, ImmutableList<Content>> entry : recipe.inputs.entrySet()) {
                MultiblockCapability<?> capability = entry.getKey();
                for (Content in : entry.getValue()) {
                    ContentWidget<?> contentWidget = (ContentWidget<?>) capability.createContentWidget()
                            .setOnPhantomUpdate(w -> onContentSelectedOrUpdate(true, capability, w))
                            .setContent(IO.IN, in, false)
                            .setOnSelected(w -> onContentSelectedOrUpdate(true, capability, (ContentWidget<?>) w))
                            .setSelectedTexture(-1, 0);
                    this.inputs.computeIfAbsent(capability, c -> new ArrayList<>()).add(contentWidget);
                }
            }
            for (Map.Entry<MultiblockCapability<?>, ImmutableList<Content>> entry : recipe.tickInputs.entrySet()) {
                MultiblockCapability<?> capability = entry.getKey();
                for (Content in : entry.getValue()) {
                    ContentWidget<?> contentWidget = (ContentWidget<?>) capability.createContentWidget()
                            .setOnPhantomUpdate(w -> onContentSelectedOrUpdate(true, capability, w))
                            .setContent(IO.IN, in, true)
                            .setOnSelected(w -> onContentSelectedOrUpdate(true, capability, (ContentWidget<?>) w))
                            .setSelectedTexture(-1, 0);
                    this.inputs.computeIfAbsent(capability, c -> new ArrayList<>()).add(contentWidget);
                }
            }

            for (Map.Entry<MultiblockCapability<?>, ImmutableList<Content>> entry : recipe.outputs.entrySet()) {
                MultiblockCapability<?> capability = entry.getKey();
                for (Content out : entry.getValue()) {
                    ContentWidget<?> contentWidget= (ContentWidget<?>) capability
                            .createContentWidget().setOnPhantomUpdate(w -> onContentSelectedOrUpdate(false, capability, w))
                            .setContent(IO.OUT, out, false)
                            .setOnSelected(w -> onContentSelectedOrUpdate(false, capability, (ContentWidget<?>) w));
                    this.outputs.computeIfAbsent(capability, c -> new ArrayList<>()).add(contentWidget);
                }
            }
            for (Map.Entry<MultiblockCapability<?>, ImmutableList<Content>> entry : recipe.tickOutputs.entrySet()) {
                MultiblockCapability<?> capability = entry.getKey();
                for (Content out : entry.getValue()) {
                    ContentWidget<?> contentWidget= (ContentWidget<?>) capability
                            .createContentWidget().setOnPhantomUpdate(w -> onContentSelectedOrUpdate(false, capability, w))
                            .setContent(IO.OUT, out, true)
                            .setOnSelected(w -> onContentSelectedOrUpdate(false, capability, (ContentWidget<?>) w));
                    this.outputs.computeIfAbsent(capability, c -> new ArrayList<>()).add(contentWidget);
                }
            }
        }

        public void updateRecipeWidget() {
            RecipeMapWidget.this.configurator.clearAllWidgets();
            WidgetGroup group = RecipeMapWidget.this.recipeIO;
            WidgetGroup conditionGroup = RecipeMapWidget.this.conditionGroup;
            conditionGroup.clearAllWidgets();
            group.clearAllWidgets();
            group.addWidget(new ImageWidget(-10, -10, group.getSize().width + 20, group.getSize().height + 20, ResourceBorderTexture.BORDERED_BACKGROUND));
            if (!isFuel) {
                group.addWidget(new LabelWidget(5, 5,"multiblocked.gui.label.uid"));
                group.addWidget(new TextFieldWidget(30, 5,  120, 10, () -> uid, s -> uid = s).setHoverTooltip("multiblocked.gui.tips.unique"));
                inputs.clear();
            } else {
                group.addWidget(new ImageWidget(5, -3, 180, 20, new TextTexture("multiblocked.gui.dialogs.recipe_map.fuel_tip").setWidth(180).setType(TextTexture.TextType.LEFT)));
                group.addWidget(new TextFieldWidget(5, 30,  40, 10, null, s -> recipeMap.fuelThreshold = Integer.parseInt(s)).setNumbersOnly(1, Integer.MAX_VALUE).setCurrentString(recipeMap.fuelThreshold + "")
                        .setHoverTooltip("multiblocked.gui.dialogs.recipe_map.fuel_threshold"));
            }
            outputs.clear();
            DraggableScrollableWidgetGroup inputs = new DraggableScrollableWidgetGroup(isFuel ? ((176 - 64) / 2) : 5, 20, 64, 64).setBackground(new ColorRectTexture(0x3f000000));
            DraggableScrollableWidgetGroup outputs = new DraggableScrollableWidgetGroup(176 - 64 - 5, 20, 64, 64).setBackground(new ColorRectTexture(0x3f000000));
            group.addWidget(inputs);
            if (!isFuel) {
                group.addWidget(outputs);
                ProgressWidget progressWidget = new ProgressWidget(ProgressWidget.JEIProgress, 78, 42, 20, 20, recipeMap.progressTexture);
                group.addWidget(progressWidget);
                group.addWidget(new ButtonWidget(78, 42, 20, 20, null, cd-> new ResourceTextureWidget(RecipeMapWidget.this, texture -> {
                    if (texture != null) {
                        recipeMap.progressTexture = texture;
                        progressWidget.setProgressBar(texture.getSubTexture(0.0, 0.0, 1.0, 0.5), texture.getSubTexture(0.0, 0.5, 1.0, 0.5));
                    }
                })).setHoverTexture(new ColorRectTexture(0xaf888888)).setHoverTooltip("multiblocked.gui.dialogs.recipe_map.progress"));
            } else {
                ProgressWidget progressWidget = new ProgressWidget(ProgressWidget.JEIProgress, 5, 42, 20, 20, recipeMap.fuelTexture);
                progressWidget.setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP);
                group.addWidget(progressWidget);
                group.addWidget(new ButtonWidget(5, 42, 20, 20, null, cd-> new ResourceTextureWidget(RecipeMapWidget.this, texture -> {
                    if (texture != null) {
                        recipeMap.fuelTexture = texture;
                        progressWidget.setProgressBar(texture.getSubTexture(0.0, 0.0, 1.0, 0.5), texture.getSubTexture(0.0, 0.5, 1.0, 0.5));
                    }
                })).setHoverTexture(new ColorRectTexture(0xaf888888)).setHoverTooltip("multiblocked.gui.dialogs.recipe_map.fuel_progress"));
            }
            group.addWidget(new LabelWidget(5, 90, "multiblocked.gui.label.duration"));
            group.addWidget(new TextFieldWidget(60, 90,  60, 10, true, () -> duration + "", s -> duration = Integer.parseInt(s)).setNumbersOnly(1, Integer.MAX_VALUE));
            group.addWidget(new LabelWidget(122, 90, "multiblocked.gui.label.ticks"));
            updateIOContentWidgets();
            int index = 0;
            for (List<ContentWidget<?>> widgets : this.inputs.values()) {
                for (ContentWidget<?> widget : widgets) {
                    inputs.addWidget(widget.setSelfPosition(2 + 20 * (index % 3), 2 + 20 * (index / 3)));
                    index++;
                }
            }
            final int X = 2 + 20 * (index % 3);
            final int Y = 2 + 20 * (index / 3);
            inputs.addWidget(new ButtonWidget(X + 2, Y + 2, 16 , 16,
                    new ResourceTexture("multiblocked:textures/gui/add.png"),
                    cd -> addContent(this.inputs,
                            X - inputs.getScrollXOffset() + group.getSelfPosition().x + 5,
                            Y - inputs.getScrollYOffset() + group.getSelfPosition().y + 5)).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.dialogs.recipe_map.add_in"));
            if (index > 8) {
                inputs.setSize(new Size(64 + 4, 64));
                inputs.setYScrollBarWidth(4).setYBarStyle(null, new ColorRectTexture(-1));
            }

            if (!isFuel) {
                index = 0;
                for (List<ContentWidget<?>> widgets : this.outputs.values()) {
                    for (ContentWidget<?> widget : widgets) {
                        outputs.addWidget(widget.setSelfPosition(2 + 20 * (index % 3), 2 + 20 * (index / 3)));
                        index++;
                    }
                }
                final int X2 = 2 + 20 * (index % 3);
                final int Y2 = 2 + 20 * (index / 3);
                outputs.addWidget(new ButtonWidget(X2 + 2, Y2 + 2, 16 , 16,
                        new ResourceTexture("multiblocked:textures/gui/add.png"), cd -> addContent(this.outputs,
                        X2 - outputs.getScrollXOffset() + group.getSelfPosition().x + 176 - 74 + 5,
                        Y2 - outputs.getScrollYOffset() + group.getSelfPosition().y + 5)).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.dialogs.recipe_map.add_out"));
                if (index > 8) {
                    outputs.setSize(new Size(64 + 4, 64));
                    outputs.setYScrollBarWidth(4).setYBarStyle(null, new ColorRectTexture(-1));
                }
            }

            DraggableScrollableWidgetGroup conditionsGroup = new DraggableScrollableWidgetGroup(0, 0, 86, 70)
                    .setBackground(new ColorRectTexture(0x6f444444));
            DraggableScrollableWidgetGroup candidateGroup = new DraggableScrollableWidgetGroup(-28, 0, 20, 70)
                    .setBackground(new ColorRectTexture(0x6f444444));
            WidgetGroup configurator = new WidgetGroup(90, 0, 70, 70);
            conditionGroup.addWidget(new ImageWidget(-5, -5, conditionGroup.getSize().width + 10, conditionGroup.getSize().height + 10, ResourceBorderTexture.BORDERED_BACKGROUND));
            conditionGroup.addWidget(new ImageWidget(-32, -4, 28, 78, ResourceBorderTexture.BORDERED_BACKGROUND_BLUE));
            conditionGroup.addWidget(conditionsGroup);
            conditionGroup.addWidget(candidateGroup);
            conditionGroup.addWidget(configurator);

            for (RecipeCondition condition : MbdRecipeConditions.RECIPE_CONDITIONS_REGISTRY.values()) {
                candidateGroup.addWidget(new ButtonWidget(2, candidateGroup.getAllWidgetSize() * 18, 16, 16, condition.getValidTexture(), cd -> {
                    conditions.add(condition.createTemplate().deserialize(condition.serialize()));
                    updateRecipeWidget();
                }).setHoverTooltip(condition.getTranlationKey()));
            }

            for (RecipeCondition condition : conditions) {
                SelectableWidgetGroup conditionWidget = (SelectableWidgetGroup) new SelectableWidgetGroup(0, conditionsGroup.getAllWidgetSize() * 20, conditionsGroup.getSize().width, 20)
                        .setOnSelected(selected -> {
                            configurator.clearAllWidgets();
                            condition.openConfigurator(configurator);
                        })
                        .setSelectedTexture(-2, 0xff00aa00)
                        .addWidget(new ImageWidget(0, 0, conditionsGroup.getSize().width, 20, new ColorRectTexture(0x4faaaaaa)))
                        .addWidget(new ButtonWidget(conditionsGroup.getSize().width - 15, 4, 12, 12, new ResourceTexture("multiblocked:textures/gui/remove.png"), cd -> {
                            conditions.remove(condition);
                            updateRecipeWidget();
                        }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.remove"))
                        .addWidget(new ImageWidget(22, 0, conditionsGroup.getSize().width - 22 - 17, 20, new TextTexture("").setSupplier(()->condition.getTooltips().getFormattedText()).setWidth(conditionsGroup.getSize().width - 22 - 17).setType(TextTexture.TextType.ROLL)))
                        .addWidget(new ImageWidget(2, 2, 18, 18, condition.getValidTexture()));
                conditionsGroup.addWidget(conditionWidget);
            }
        }

        private void addContent(Map<MultiblockCapability<?>, List<ContentWidget<?>>> contents, int mouseX, int mouseY) {
            if (contentCandidates != null) RecipeMapWidget.this.waitToRemoved(contentCandidates);
            Collection<MultiblockCapability<?>> capabilities = MbdCapabilities.CAPABILITY_REGISTRY.values();
            int size = capabilities.size();
            contentCandidates = new WidgetGroup(mouseX + 10 - Math.min(size, 5) * 10, mouseY + 25, Math.min(size, 5) * 20, (1 + size / 5) * 20);
            contentCandidates.addWidget(new ImageWidget(-2, -2, contentCandidates.getSize().width + 4, contentCandidates.getSize().height + 4,
                    new ResourceTexture("multiblocked:textures/gui/darkened_slot.png")));
            int i = 0;
            for (MultiblockCapability<?> capability : capabilities) {
                contentCandidates.addWidget(capability.createContentWidget()
                        .setContent(IO.IN, capability.defaultContent(), 1, false)
                        .setOnMouseClicked(contentWidget -> {
                            contents.computeIfAbsent(capability, c -> new ArrayList<>()).add(contentWidget);
                            updateRecipe();
                            updateRecipeWidget();
                        })
                        .setSelfPosition(i % 5 * 20, (i / 5) * 20));
                i++;
            }
        }

        private void onContentSelectedOrUpdate(boolean input, MultiblockCapability<?> capability, ContentWidget<?> widget) {
            if (selectedContent != null && selectedContent != widget) {
                selectedContent.onUnSelected();
                selectedContent.waitToRemoved(removed);
            }
            if (selectedContent != widget) {
                widget.addWidget(removed = (ButtonWidget) new ButtonWidget(14, 0, 6, 6, cd -> {
                    (input ? inputs: outputs).get(capability).remove(widget);
                    updateRecipe();
                    updateRecipeWidget();
                }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/remove.png")).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.remove"));
                selectedContent = widget;
            }
            WidgetGroup group = RecipeMapWidget.this.configurator;
            group.clearAllWidgets();
            group.addWidget(new ImageWidget(0, 0, group.getSize().width, group.getSize().height, ResourceBorderTexture.BORDERED_BACKGROUND));
            widget.openConfigurator(group);
        }

    }
}
