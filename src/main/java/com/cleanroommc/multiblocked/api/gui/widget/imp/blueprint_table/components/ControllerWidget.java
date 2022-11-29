package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.*;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.RecipeMapBuilderWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.TemplateBuilderWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.JsonBlockPatternWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import com.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ControllerWidget extends ComponentWidget<ControllerDefinition>{
    protected JsonBlockPattern pattern;
    protected final WidgetGroup S4;
    protected final WidgetGroup S5;
    protected final SceneWidget sceneWidget;
    protected final Set<DummyComponentTileEntity> tiles = new HashSet<>();
    protected boolean isFormed;
    protected String recipeMap;

    public ControllerWidget(WidgetGroup group, ControllerDefinition definition, JsonBlockPattern pattern, String recipeMap, Consumer<JsonObject> onSave) {
        super(group, definition, onSave);
        this.pattern = pattern;
        this.recipeMap = recipeMap;
        int x = 47;
        SimplePredicate predicate = this.pattern.predicates.get("controller");
        if (predicate instanceof PredicateComponent) {
            ((PredicateComponent) predicate).definition = definition;
        }

        Widget consumeCatalystWidget;
        S1.addWidget(consumeCatalystWidget = GuiUtils.createSelector(x + 140, 165, "consumeCatalyst", "multiblocked.gui.widget.controller.consume", definition.consumeCatalyst.name(), Arrays.stream(ControllerDefinition.CatalystState.values()).map(Enum::name).collect(Collectors.toList()), r -> definition.consumeCatalyst = ControllerDefinition.CatalystState.valueOf(r)));
        consumeCatalystWidget.setVisible(definition.getCatalyst() != null);

        Widget noNeedControllerWidget;
        S1.addWidget(noNeedControllerWidget = GuiUtils.createBoolSwitch(x + 140, 180, "noNeedController", "multiblocked.gui.widget.controller.no_controller", definition.noNeedController, r -> definition.noNeedController = r));
        noNeedControllerWidget.setVisible(definition.getCatalyst() != null && !definition.getCatalyst().isEmpty());

        IItemHandlerModifiable handler;
        PhantomSlotWidget phantomSlotWidget = new PhantomSlotWidget(handler = new ItemStackHandler(1), 0, x + 250, 145);
        S1.addWidget(phantomSlotWidget);
        phantomSlotWidget.setClearSlotOnRightClick(true)
                .setChangeListener(() -> {
                    definition.setCatalyst(handler.getStackInSlot(0));
                    noNeedControllerWidget.setVisible(!handler.getStackInSlot(0).isEmpty());
                })
                .setBackgroundTexture(new ColorBorderTexture(1, -1))
                .setHoverTooltip("multiblocked.gui.widget.controller.catalyst")
                .setVisible(definition.getCatalyst() != null);
        handler.setStackInSlot(0, definition.getCatalyst() == null ? ItemStack.EMPTY : definition.getCatalyst());

        S1.addWidget(GuiUtils.createBoolSwitch(x + 140, 150, "needCatalyst", "multiblocked.gui.widget.controller.need_catalyst", definition.getCatalyst() != null, r -> {
            definition.setCatalyst(!r ? null : ItemStack.EMPTY);
            phantomSlotWidget.setVisible(r);
            consumeCatalystWidget.setVisible(r);
            noNeedControllerWidget.setVisible(r && !handler.getStackInSlot(0).isEmpty());
        }));

        tabContainer.addTab((TabButton) new TabButton(111, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S4"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S4"))
                        .setHoverTooltip("multiblocked.gui.widget.controller.s3"),
                S4 = new WidgetGroup(0, 0, getSize().width, getSize().height));
        S4.addWidget(new ImageWidget(50, 66, 138, 138, new GuiTextureGroup(new ColorBorderTexture(3, -1), new ColorRectTexture(0xaf444444))));
        S4.addWidget(sceneWidget = new SceneWidget(50, 66, 138, 138, null)
                .useCacheBuffer()
                .setRenderFacing(false)
                .setRenderSelect(false));
        ResourceTexture PAGE = new ResourceTexture("multiblocked:textures/gui/structure_page.png");
        sceneWidget.addWidget(new SwitchWidget(138 - 20, 138 - 20, 16, 16, this::onFormedSwitch)
                .setPressed(isFormed)
                .setTexture(PAGE.getSubTexture(222 / 256.0, 0, 16 / 256.0, 16 / 256.0), PAGE.getSubTexture(222 / 256.0, 16 / 256.0, 16 / 256.0, 16 / 256.0))
                .setHoverTooltip("multiblocked.structure_page.switch"));
        S4.addWidget(new TextBoxWidget(200, 0, 175, Collections.singletonList("")).setFontColor(-1).setShadow(true));
        S4.addWidget(new ButtonWidget(200, 66, 100, 20,
                new GuiTextureGroup(ResourceBorderTexture.BAR, new TextTexture("multiblocked.gui.label.pattern_settings", -1).setDropShadow(true)), cd -> {
            new JsonBlockPatternWidget(this, this.pattern.copy(), this::savePattern);
        }).setHoverBorderTexture(1, -1));
        updateScene(this.pattern);

        tabContainer.addTab((TabButton) new TabButton(111 + 23, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S5"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S5"))
                        .setHoverTooltip("multiblocked.gui.widget.controller.s4"),
                S5 = new WidgetGroup(0, 0, getSize().width, getSize().height));
        S5.addWidget(new LabelWidget(80, 55, "multiblocked.gui.label.recipe_map"));
        S5.addWidget(new TextFieldWidget(80, 70, 100, 15, true, () -> this.recipeMap, s -> this.recipeMap = s));
        S5.addWidget(new RecipeMapBuilderWidget(this, 188, 50, 150, 170).setOnRecipeMapSelected(recipeMap1 -> this.recipeMap = recipeMap1.name));
    }

    @Override
    protected JsonObject getJsonObj() {
        JsonObject jsonObject = super.getJsonObj();
        jsonObject.add("basePattern", Multiblocked.GSON.toJsonTree(pattern));
        jsonObject.addProperty("recipeMap", this.recipeMap == null ? RecipeMap.EMPTY.name : this.recipeMap);
        return jsonObject;
    }

    private void onFormedSwitch(ClickData clickData, boolean isPressed) {
        isFormed = isPressed;
        if (thread == null) {
            tiles.forEach(t->t.isFormed = isFormed);
        }
        sceneWidget.needCompileCache();
    }


    @SideOnly(Side.CLIENT)
    Thread thread;

    @SideOnly(Side.CLIENT)
    private void updateScene(JsonBlockPattern jsonPattern) {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        TrackedDummyWorld world = new TrackedDummyWorld();
        tiles.clear();
        sceneWidget.createScene(world);
        ImageWidget imageWidget;
        sceneWidget.addWidget(imageWidget = new ImageWidget(0, 0, sceneWidget.getSize().width, sceneWidget.getSize().height));
        imageWidget.setVisible(jsonPattern.pattern.length * jsonPattern.pattern[0].length * jsonPattern.pattern[0][0].length() > 1000);
        thread = new Thread(()->{
            int[] centerOffset = jsonPattern.getCenterOffset();
            String[][] pattern = jsonPattern.pattern;
            Set<BlockPos> posSet = new HashSet<>();
            int offset = Math.max(pattern.length, Math.max(pattern[0].length, pattern[0][0].length()));
            int sum = jsonPattern.pattern.length * jsonPattern.pattern[0].length * jsonPattern.pattern[0][0].length();
            AtomicDouble progress = new AtomicDouble(0);
            imageWidget.setImage(new TextTexture("building scene!").setSupplier(()-> "building scene! " + String.format("%.1f", progress.get()) + "%%").setWidth(sceneWidget.getSize().width));
            int count = 0;
            for (int i = 0; i < pattern.length; i++) {
                for (int j = 0; j < pattern[0].length; j++) {
                    for (int k = 0; k < pattern[0][0].length(); k++) {
                        if (Thread.interrupted()) {
                            sceneWidget.waitToRemoved(imageWidget);
                            return;
                        }
                        count++;
                        progress.set(count * 100.0 / sum);
                        char symbol = pattern[i][j].charAt(k);
                        BlockPos pos = jsonPattern.getActualPosOffset(k - centerOffset[2], j - centerOffset[1], i - centerOffset[0], EnumFacing.NORTH).add(offset, offset, offset);
                        world.addBlock(pos, new BlockInfo(MbdComponents.DummyComponentBlock));
                        DummyComponentTileEntity  tileEntity = (DummyComponentTileEntity) world.getTileEntity(pos);
                        ComponentDefinition definition = null;
                        assert tileEntity != null;
                        boolean disableFormed = false;
                        if (jsonPattern.symbolMap.containsKey(symbol)) {
                            Set<BlockInfo> candidates = new HashSet<>();
                            for (String s : jsonPattern.symbolMap.get(symbol)) {
                                SimplePredicate predicate = jsonPattern.predicates.get(s);
                                if (predicate instanceof PredicateComponent && ((PredicateComponent) predicate).definition != null) {
                                    definition = ((PredicateComponent) predicate).definition;
                                    disableFormed |= predicate.disableRenderFormed;
                                    break;
                                } else if (predicate != null && predicate.candidates != null) {
                                    candidates.addAll(Arrays.asList(predicate.candidates.get()));
                                    disableFormed |= predicate.disableRenderFormed;
                                }
                            }
                            definition = TemplateBuilderWidget.getComponentDefinition(definition, candidates);
                        }
                        if (definition != null) {
                            tileEntity.setDefinition(definition);
                            if (disableFormed) {
                                definition.getIdleStatus().setRenderer(new BlockStateRenderer(Blocks.AIR.getDefaultState()));
                            }
                        }
                        tileEntity.isFormed = isFormed;
                        tileEntity.setWorld(world);
                        tileEntity.validate();
                        posSet.add(pos);
                        tiles.add(tileEntity);
                    }
                }
            }
            Minecraft.getMinecraft().addScheduledTask(()->{
                sceneWidget.setRenderedCore(posSet, null);
                sceneWidget.waitToRemoved(imageWidget);
            });
            thread = null;
        });
        thread.start();
    }

    private void savePattern(JsonBlockPattern patternResult) {
        if (patternResult != null) {
            pattern = patternResult;
            pattern.cleanUp();
            updateScene(this.pattern);
        }
    }

    @Override
    protected void updateRegistryName(String s) {
        super.updateRegistryName(s);
        SimplePredicate predicate = pattern.predicates.get("controller");
        if (predicate instanceof PredicateComponent) {
            ((PredicateComponent) predicate).location = location;
        }
    }
}
