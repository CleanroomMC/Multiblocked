package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.api.block.BlockComponent;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.pattern.util.RelativeDirection;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import com.cleanroommc.multiblocked.client.renderer.scene.WorldSceneRenderer;
import com.cleanroommc.multiblocked.client.util.RenderBufferUtils;
import com.cleanroommc.multiblocked.client.util.RenderUtils;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.cleanroommc.multiblocked.util.BlockPosFace;
import com.cleanroommc.multiblocked.util.CycleItemStackHandler;
import com.cleanroommc.multiblocked.util.Position;
import com.cleanroommc.multiblocked.util.Size;
import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import com.cleanroommc.multiblocked.api.gui.widget.Widget;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DialogWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.DraggableScrollableWidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextBoxWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateAnyCapability;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateBlocks;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateStates;
import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JsonBlockPatternWidget extends DialogWidget {
    public static BlockComponent symbolBlock;
    public JsonBlockPattern pattern;
    public TabContainer container;
    public BlockPatternSceneWidget sceneWidget;
    public SelectorWidget[] selectors;
    public TextFieldWidget[] repeats;
    public DraggableScrollableWidgetGroup symbolSelector;
    public DraggableScrollableWidgetGroup predicateGroup;
    public DraggableScrollableWidgetGroup tfGroup;
    public TextBoxWidget textBox;
    public boolean needUpdatePredicateSelector;
    public boolean isPretty;

    public JsonBlockPatternWidget(WidgetGroup parent, JsonBlockPattern pattern, Consumer<JsonBlockPattern> onClose) {
        super(parent, true);
        setParentInVisible();
        this.setOnClosed(()->onClose.accept(null));
        this.pattern = pattern;
        this.addWidget(new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/json_block_pattern.png")));
        this.addWidget(sceneWidget = new BlockPatternSceneWidget());
        this.addWidget(container = new TabContainer(0, 0, 384, 256));
        this.addWidget(new ButtonWidget(280, 29, 70, 20, cd -> {
            if (onClose != null) onClose.accept(pattern);
            parent.waitToRemoved(this);
        }).setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("Save Pattern", -1).setDropShadow(true)).setHoverBorderTexture(1, -1));

        // patternTab
        ResourceTexture tabPattern = new ResourceTexture("multiblocked:textures/gui/tab_pattern.png");
        WidgetGroup patternTab;
        container.addTab(new TabButton(171, 29, 20, 20)
                        .setTexture(tabPattern.getSubTexture(0, 0, 1, 0.5), tabPattern.getSubTexture(0, 0.5, 1, 0.5))
                        .setHoverTooltip("Pattern"),
                patternTab = new WidgetGroup(0, 0, getSize().width, getSize().height));

        int bgColor = 0x8f111111;

        patternTab.addWidget(new LabelWidget(174, 92, () -> "Repeat:").setTextColor(-1).setDrop(true));
        repeats = new TextFieldWidget[2];
        patternTab.addWidget(new ImageWidget(266, 86, 29, 18, new ResourceTexture("multiblocked:textures/gui/repeat.png")).setHoverTooltip("repetition of aisle (1 <= min <= max <= 1)"));
        patternTab.addWidget(repeats[0] = new TextFieldWidget(215, 87, 40, 15, true, () -> sceneWidget.selected == null ? "" : pattern.aisleRepetitions[sceneWidget.selected.a][0] + "", s -> {
            if (sceneWidget.selected != null && sceneWidget.centerOffset[0] != sceneWidget.selected.a) {
                pattern.aisleRepetitions[sceneWidget.selected.a][0] = Integer.parseInt(s);
                if (pattern.aisleRepetitions[sceneWidget.selected.a][0] > pattern.aisleRepetitions[sceneWidget.selected.a][1]) {
                    pattern.aisleRepetitions[sceneWidget.selected.a][1] = pattern.aisleRepetitions[sceneWidget.selected.a][0];
                }
            }
        }).setNumbersOnly(1, Integer.MAX_VALUE));

        patternTab.addWidget(repeats[1] = new TextFieldWidget(305, 87, 40, 15, true, () -> sceneWidget.selected == null ? "" : pattern.aisleRepetitions[sceneWidget.selected.a][1] + "", s -> {
            if (sceneWidget.selected != null && sceneWidget.centerOffset[0] != sceneWidget.selected.a) {
                pattern.aisleRepetitions[sceneWidget.selected.a][1] = Integer.parseInt(s);
                if (pattern.aisleRepetitions[sceneWidget.selected.a][0] > pattern.aisleRepetitions[sceneWidget.selected.a][1]) {
                    pattern.aisleRepetitions[sceneWidget.selected.a][0] = pattern.aisleRepetitions[sceneWidget.selected.a][1];
                }
            }
        }).setNumbersOnly(0, Integer.MAX_VALUE));
        repeats[0].setActive(false); repeats[1].setActive(false); repeats[0].setHoverTooltip("min"); repeats[1].setHoverTooltip("max");

        patternTab.addWidget(symbolSelector = new DraggableScrollableWidgetGroup(215, 105, 130, 35).setBackground(new ColorRectTexture(bgColor)));
        patternTab.addWidget(new ButtonWidget(174, 105, 35, 35, (cd) -> {
            char next = (char) (pattern.symbolMap.keySet().stream().max(Comparator.comparingInt(a -> a)).get() + 1);
            pattern.symbolMap.put(next, new HashSet<>());
            updateSymbolButton();
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/button_wood.png"), new TextTexture("Add", -1).setDropShadow(true).setWidth(40)).setHoverBorderTexture(1, -1));
        updateSymbolButton();
        patternTab.addWidget(new LabelWidget(174, 143, ()->"tips: you cant modify controller").setDrop(true).setTextColor(-1));

        List<String> candidates = Arrays.stream(RelativeDirection.values()).map(Enum::name).collect(Collectors.toList());
        patternTab.addWidget(new LabelWidget(174, 70, () -> "Dir:").setTextColor(-1).setDrop(true));
        patternTab.addWidget(new ImageWidget(193, 60, 20, 20, new ResourceTexture("multiblocked:textures/gui/axis.png")).setHoverTooltip("relative directions"));
        patternTab.addWidget(new ImageWidget(215, 57, 40, 10, new TextTexture("Char", -1).setDropShadow(true)));
        patternTab.addWidget(new ImageWidget(260, 57, 40, 10, new TextTexture("String", -1).setDropShadow(true)));
        patternTab.addWidget(new ImageWidget(305, 57, 40, 10, new TextTexture("Aisle", -1).setDropShadow(true)));
        selectors = new SelectorWidget[3];
        patternTab.addWidget(selectors[0] = new SelectorWidget(215, 67, 40, 15, candidates, -1).setOnChanged(s->this.onDirChange(0, s)).setButtonBackground(new ColorRectTexture(bgColor)).setValue(pattern.structureDir[0].name()));
        patternTab.addWidget(selectors[1] = new SelectorWidget(260, 67, 40, 15, candidates, -1).setOnChanged(s->this.onDirChange(1, s)).setButtonBackground(new ColorRectTexture(bgColor)).setValue(pattern.structureDir[1].name()));
        patternTab.addWidget(selectors[2] = new SelectorWidget(305, 67, 40, 15, candidates, -1).setOnChanged(s->this.onDirChange(2, s)).setButtonBackground(new ColorRectTexture(bgColor)).setValue(pattern.structureDir[2].name()));

        //predicateTab
        ResourceTexture tabPredicate = new ResourceTexture("multiblocked:textures/gui/tab_predicate.png");
        WidgetGroup predicateTab;
        container.addTab(new TabButton(171 + 25, 29, 20, 20)
                        .setTexture(tabPredicate.getSubTexture(0, 0, 1, 0.5),
                                tabPredicate.getSubTexture(0, 0.5, 1, 0.5))
                        .setHoverTooltip("Predicate"),
                predicateTab = new WidgetGroup(0, 0, getSize().width, getSize().height));
        DraggableScrollableWidgetGroup predicatesContainer = new DraggableScrollableWidgetGroup(171, 52, 179, 136 - 52)
                .setBackground(new ColorRectTexture(bgColor))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(-1));
        predicateTab.addWidget(predicatesContainer);
        AtomicReference<PredicateWidget> selectedPredicate = new AtomicReference<>();
        SelectorWidget sw;
        TextFieldWidget fw;
        predicateTab.addWidget(sw = new SelectorWidget(172, 138, 60, 16, Arrays.asList("capability", "blocks", "states", "component"), -1)
                .setButtonBackground(new ColorRectTexture(bgColor)).setValue("blocks"));
        predicateTab.addWidget(fw = new TextFieldWidget(232, 138, 50, 16, true, null, null));
        predicateTab.addWidget(new ButtonWidget(285, 138, 16, 16, cd -> {
            if (sw.getValue() == null) return;
            SimplePredicate predicate = null;
            switch (sw.getValue()) {
                case "capability":
                    predicate = new PredicateAnyCapability();
                    break;
                case "blocks":
                    predicate = new PredicateBlocks();
                    break;
                case "states":
                    predicate = new PredicateStates();
                    break;
                case "component":
                    predicate = new PredicateComponent();
                    break;
            }
            String predicateName = fw.getCurrentString();
            if (predicate != null && !pattern.predicates.containsKey(predicateName)) {
                pattern.predicates.put(fw.getCurrentString(), predicate);
                predicatesContainer.addWidget(new PredicateWidget(0, predicatesContainer.widgets.size() * 21, predicate, predicateName, selectedPredicate));
            }
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/add.png")).setHoverBorderTexture(1, -1).setHoverTooltip("create predicate"));
        predicateTab.addWidget(new ButtonWidget(350 - 36, 138, 16, 16, cd -> {
            if (selectedPredicate.get() == null) return;
            String name = selectedPredicate.get().name;
            if (sceneWidget.selected != null && !name.equals("controller")) {
                pattern.symbolMap.get(sceneWidget.selected.symbol).add(name);
                needUpdatePredicateSelector = true;
            }
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/move_down.png")).setHoverBorderTexture(1, -1).setHoverTooltip("add selected predicate to symbol"));
        predicateTab.addWidget(new ButtonWidget(350 - 18, 138, 16, 16, cd -> {
            if (selectedPredicate.get() == null) return;
            String name = selectedPredicate.get().name;
            if (sceneWidget.selected != null) {
                pattern.symbolMap.get(sceneWidget.selected.symbol).remove(name);
            }
            pattern.symbolMap.values().forEach(set->set.remove(name));
            pattern.predicates.remove(name);
            needUpdatePredicateSelector = true;
            boolean found = false;
            for (Widget widget : predicatesContainer.widgets) {
                if (found) {
                    widget.addSelfPosition(0, -21);
                    widget.setVisible(true);
                } else if (widget == selectedPredicate.get()) {
                    predicatesContainer.waitToRemoved(widget);
                    found = true;
                }
            }
            for (SymbolTileEntity tile : sceneWidget.tiles.values()) {
                tile.updateRenderer();
            }
        }).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/remove.png")).setHoverBorderTexture(1, -1).setHoverTooltip("remove selected predicate"));
        pattern.predicates.forEach((predicateName, predicate) -> {
            if (predicateName.equals("controller")) return;
            predicatesContainer.addWidget(new PredicateWidget(0, predicatesContainer.widgets.size() * 21, predicate, predicateName, selectedPredicate));
        });

        //textFieldTab
        ResourceTexture tabTextField = new ResourceTexture("multiblocked:textures/gui/tab_text_field.png");
        WidgetGroup textFieldTab;
        container.addTab(new TabButton(171 + 50, 29, 20, 20)
                        .setTexture(tabTextField.getSubTexture(0, 0, 1, 0.5),
                                tabTextField.getSubTexture(0, 0.5, 1, 0.5))
                        .setHoverTooltip("Json"),
                textFieldTab = new WidgetGroup(0, 0, getSize().width, getSize().height));
        textFieldTab.addWidget(new ImageWidget(171, 52, 179, 20, ResourceBorderTexture.BAR));
        textFieldTab.addWidget(new SwitchWidget(173, 54, 16, 16, (cd,r) -> {
            isPretty = r;
            updatePatternJson();
        }).setHoverBorderTexture(1, -1).setTexture(new ResourceTexture("multiblocked:textures/gui/pretty.png"), new ResourceTexture("multiblocked:textures/gui/pretty_active.png")).setHoverTooltip("pretty format"));
        textFieldTab.addWidget(new ButtonWidget(193, 54, 16, 16, cd -> GuiScreen.setClipboardString(isPretty ? Multiblocked.prettyJson(getPatternJson()) : getPatternJson())).setButtonTexture(new ResourceTexture("multiblocked:textures/gui/copy.png")).setHoverBorderTexture(1, -1).setHoverTooltip("copy to clipboard"));
        textFieldTab.addWidget(tfGroup = new DraggableScrollableWidgetGroup(171, 72, 179, 136 - 52)
                .setBackground(new ColorRectTexture(bgColor))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(-1)));
        tfGroup.addWidget(textBox = new TextBoxWidget(0, 0, 175, Collections.singletonList("")).setFontColor(-1).setShadow(true));
        container.setOnChanged((a, b)->{
            if (b == textFieldTab) {
                updatePatternJson();
            }
        });

        //information
        addWidget(new LabelWidget(31, 166, () -> "Symbol Character: " + (sceneWidget.selected == null ? "" : ("'" + sceneWidget.selected.symbol + "'"))).setTextColor(-1));
        addWidget(new LabelWidget(31, 178, () -> {
            if (sceneWidget.selected == null) return "Aisle Repetition: ";
            return String.format("Aisle Index: %d", sceneWidget.selected.a);
        }).setTextColor(-1));
        addWidget(new LabelWidget(31, 190, () -> {
            if (sceneWidget.selected == null) return "Aisle Repetition: ";
            int[] repeat = pattern.aisleRepetitions[sceneWidget.selected.a];
            return String.format("Aisle Repetition: (%d, %d)", repeat[0], repeat[1]);
        }).setTextColor(-1));
        addWidget(predicateGroup = new DraggableScrollableWidgetGroup(171, 166, 179, 58)
                .setBackground(new ColorRectTexture(bgColor))
                .setYScrollBarWidth(4)
                .setYBarStyle(null, new ColorRectTexture(-1)));
        updatePredicateSelector();
    }

    public void updatePatternJson() {
        textBox.setContent(Collections.singletonList(isPretty ? Multiblocked.prettyJson(getPatternJson()) : getPatternJson()));
        tfGroup.computeMax();
    }

    public String getPatternJson() {
        return pattern.toJson();
    }

    public static int getColor(char symbol) {
        switch (symbol) {
            case '@' : return 0xff0000ff;
            case ' ' : return 0xff4EEDF7;
            case '-' : return 0xff1E2DF7;
            default: {
                return EnumDyeColor.values()[(symbol - 'A') % EnumDyeColor.values().length].colorValue | 0xff000000;
            }
        }
    }

    private void updatePredicateSelector() {
        predicateGroup.clearAllWidgets();
        if (sceneWidget.selected != null && pattern.symbolMap.containsKey(sceneWidget.selected.symbol)) {
            for (String predicateName : pattern.symbolMap.get(sceneWidget.selected.symbol)) {
                SimplePredicate predicate =  pattern.predicates.get(predicateName);
                if (predicate != null) {
                    predicateGroup.addWidget(new PredicateWidget(0, predicateGroup.widgets.size() * 21, predicate, predicateName, name -> {
                        if (sceneWidget.selected != null) {
                            pattern.symbolMap.get(sceneWidget.selected.symbol).remove(name);
                            needUpdatePredicateSelector = true;
                        }
                    }));
                }
            }
            for (SymbolTileEntity tile : sceneWidget.tiles.values()) {
                tile.updateRenderer();
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (needUpdatePredicateSelector) {
            updatePredicateSelector();
            needUpdatePredicateSelector = false;
        }
    }

    private void updateSymbolButton() {
        symbolSelector.clearAllWidgets();
        int i = 0;
        for (final char c : pattern.symbolMap.keySet().stream().sorted().collect(Collectors.toList())) {
            symbolSelector.addWidget(new SymbolButton(13 * (i % 9) + 8, 13 * (i / 9) + 6, 10, 10, c, sceneWidget, cd -> {
                if (sceneWidget.selected != null && c != '@' && sceneWidget.selected.symbol != '@') {
                    sceneWidget.selected.symbol = c;
                    sceneWidget.selected.updateRenderer();
                    String old = pattern.pattern[sceneWidget.selected.a][sceneWidget.selected.b];
                    String newString = old.substring(0, sceneWidget.selected.c) + c + old.substring(sceneWidget.selected.c + 1);
                    pattern.pattern[sceneWidget.selected.a][sceneWidget.selected.b] = newString;
                    sceneWidget.updateCharacterView();
                    BlockPos pos = sceneWidget.selected.getPos();
                    sceneWidget.onSelected(pos, EnumFacing.NORTH);
                    sceneWidget.onSelected(pos, EnumFacing.NORTH);
                }
            }).setHoverTooltip(c == '@' ? "controller" : c == ' ' ? "any" : c == '-' ? "air" : null));
            i++;
        }
    }

    private void onDirChange(int index, String candidate) {
        RelativeDirection dir = RelativeDirection.valueOf(candidate);
        RelativeDirection[] newDirs = new RelativeDirection[3];
        newDirs[index] = dir;
        for (int i = 0; i < pattern.structureDir.length; i++) {
            if (pattern.structureDir[i].isSameAxis(dir) && i != index) {
                newDirs[i] = pattern.structureDir[index];
            } else if (i != index) {
                newDirs[i] = pattern.structureDir[i];
            }
            selectors[i].setValue(newDirs[i].name());
        }
        pattern.changeDir(newDirs[0], newDirs[1], newDirs[2]);
        sceneWidget.reloadBlocks();
    }

    public static void registerBlock() {
        PartDefinition definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "symbol"), SymbolTileEntity.class);
        definition.isOpaqueCube = false;
        definition.showInJei = false;
        MbdComponents.registerComponent(definition);
        symbolBlock = MbdComponents.COMPONENT_BLOCKS_REGISTRY.get(definition.location);
        symbolBlock.setCreativeTab(null);
    }

    public class BlockPatternSceneWidget extends SceneWidget {
        public Map<BlockPos, SymbolTileEntity> tiles = new HashMap<>();
        public Set<SymbolTileEntity> sameSymbol = new HashSet<>();
        public Set<SymbolTileEntity> sameAisle = new HashSet<>();
        public SymbolTileEntity selected;
        public int[] centerOffset = new int[3];
        public int offset;
        public TextTexture texture;
        public int aisleRender = -1;
        public int viewMode;
        public DraggableScrollableWidgetGroup characterView;
        public WidgetGroup layerSwitch;

        @SideOnly(Side.CLIENT)
        TrackedDummyWorld world;
        public BlockPatternSceneWidget() {
            super(31, 31, 125, 125, null);
            texture = new TextTexture("", -1).setWidth(125).setType(TextTexture.TextType.ROLL);
            addWidget(characterView = new DraggableScrollableWidgetGroup(0, 0, 125, 125));
            reloadBlocks();
            addWidget(layerSwitch = new WidgetGroup(0, 0, 125, 125));
            layerSwitch.addWidget(new ImageWidget(5, 0, 125, 20, texture));
            layerSwitch.addWidget(new ButtonWidget(5, 50, 10, 10, new ResourceTexture("multiblocked:textures/gui/up.png"), cd -> addAisle(1)).setHoverTooltip("next aisle"));
            layerSwitch. addWidget(new LabelWidget(5, 60, () -> aisleRender == -1 ? "all" : aisleRender + "").setTextColor(-1));
            layerSwitch.addWidget(new ButtonWidget(5, 70, 10, 10, new ResourceTexture("multiblocked:textures/gui/down.png"), cd -> addAisle(-1)).setHoverTooltip("last aisle"));

            addWidget(new ButtonWidget(110, 110, 10, 10, new ResourceTexture("multiblocked:textures/gui/button_view.png"), this::switchPatternView).setHoverTooltip("switch view"));
        }

        public void updateCharacterView () {
            characterView.clearAllWidgets();
            int x = 5, y = 5;
            for (int i = 0; i < pattern.pattern.length; i++) {
                for (int j = 0; j < pattern.pattern[0].length; j++) {
                    for (int k = 0; k < pattern.pattern[0][0].length(); k++) {
                        char c = pattern.pattern[i][j].charAt(k);
                        BlockPos pos = pattern.getActualPosOffset(k - centerOffset[2], j - centerOffset[1], i - centerOffset[0], EnumFacing.NORTH).add(offset, offset, offset);
                        characterView.addWidget(new SymbolButton(x, y, 10, 10, c, this, cd -> onSelected(pos, EnumFacing.NORTH)));
                        x += 13;
                    }
                    x += 5;
                }
                y += 13;
                x = 5;
            }
            characterView.setVisible(viewMode == 2);
        }

        private void switchPatternView(ClickData clickData) {
            viewMode = (viewMode + 1) % 3;
            if (viewMode == 2) {
                characterView.setVisible(true);
                characterView.setActive(true);
                layerSwitch.setVisible(false);
                layerSwitch.setActive(false);
            } else {
                characterView.setVisible(false);
                characterView.setActive(false);
                layerSwitch.setVisible(true);
                layerSwitch.setActive(true);
            }
            sceneWidget.needCompileCache();
        }

        private void addAisle(int add) {
            if (aisleRender + add >= -1 && aisleRender + add < pattern.pattern.length) {
                aisleRender += add;
            }
            sceneWidget.needCompileCache();
        }

        public void updateTips(String tips) {
            texture.updateText(tips);
        }

        public void reloadBlocks() {
            updateTips("");
            aisleRender = -1;
            selected = null;
            tiles.clear();
            sameSymbol.clear();
            sameAisle.clear();
            createScene(world = new TrackedDummyWorld());
            useCacheBuffer();
            world.setRenderFilter(pos -> {
                if (aisleRender > -1) {
                    return tiles.containsKey(pos) && tiles.get(pos).a == aisleRender;
                }
                return true;
            });
            centerOffset = pattern.getCenterOffset();
            String[][] pattern = JsonBlockPatternWidget.this.pattern.pattern;
            Set<BlockPos> posSet = new HashSet<>();
            offset = Math.max(pattern.length, Math.max(pattern[0].length, pattern[0][0].length()));
            for (int i = 0; i < pattern.length; i++) {
                for (int j = 0; j < pattern[0].length; j++) {
                    for (int k = 0; k < pattern[0][0].length(); k++) {
                        char c = pattern[i][j].charAt(k);
                        BlockPos pos = JsonBlockPatternWidget.this.pattern.getActualPosOffset(k - centerOffset[2], j - centerOffset[1], i - centerOffset[0], EnumFacing.NORTH).add(offset, offset, offset);
                        world.addBlock(pos, new BlockInfo(symbolBlock.getDefaultState()));
                        SymbolTileEntity tileEntity = (SymbolTileEntity) world.getTileEntity(pos);
                        assert tileEntity != null;
                        tileEntity.init(c, JsonBlockPatternWidget.this, i, j, k);
                        tileEntity.setDefinition(symbolBlock.definition);
                        tileEntity.setWorld(world);
                        tileEntity.validate();
                        posSet.add(pos);
                        tiles.put(pos, tileEntity);
                        tileEntity.updateRenderer();
                    }
                }
            }
            setRenderedCore(posSet, null);
            setOnSelected(this::onSelected);
            setRenderFacing(false);
            updateCharacterView();
            needCompileCache();
        }

        @Override
        public Widget mouseClicked(int mouseX, int mouseY, int button) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void renderBlockOverLay(WorldSceneRenderer renderer) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();

            if (viewMode == 1) { // render pattern style
                net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                float lastBrightnessX = OpenGlHelper.lastBrightnessX;
                float lastBrightnessY = OpenGlHelper.lastBrightnessY;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

                if (selected != null) {
                    for (SymbolTileEntity tile : sameSymbol) {
                        drawSymbolTE(tessellator, buffer, tile, getColor(tile.symbol), 1);
                    }
                }

                if (selected != null) {
                    GlStateManager.depthMask(false);
                }
                for (SymbolTileEntity tile : tiles.values()) {
                    if (aisleRender > -1 && tile.a != aisleRender) continue;
                    if (sameSymbol.contains(tile)) continue;
                    float dd = Math.abs(System.currentTimeMillis() % 3000);
                    drawSymbolTE(tessellator, buffer, tile, getColor(tile.symbol), selected == null ? 1 : ((((dd > 1500) ? (3000 - dd) : dd) / 1500f) * 0.3f));
                }
                if (selected != null) {
                    GlStateManager.depthMask(true);
                }

                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
                net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
            }

            super.renderBlockOverLay(renderer);

            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();

            if (selected != null) {
                int[] minPos = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
                int[] maxPos = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
                for (SymbolTileEntity symbol : sameAisle) {
                    BlockPos pos = symbol.getPos();
                    if (pos.getX() > maxPos[0]) maxPos[0] = pos.getX();
                    if (pos.getY() > maxPos[1]) maxPos[1] = pos.getY();
                    if (pos.getZ() > maxPos[2]) maxPos[2] = pos.getZ();

                    if (pos.getX() < minPos[0]) minPos[0] = pos.getX();
                    if (pos.getY() < minPos[1]) minPos[1] = pos.getY();
                    if (pos.getZ() < minPos[2]) minPos[2] = pos.getZ();
                }
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                RenderUtils.renderCubeFace(buffer, minPos[0] - 0.01, minPos[1] - 0.01, minPos[2] - 0.01, maxPos[0] + 1.01, maxPos[1] + 1.01, maxPos[2] + 1.01, 0.3f, 0.5f, 0.7f, 1);
                tessellator.draw();

                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
            GlStateManager.enableTexture2D();

        }

        private void drawSymbolTE(Tessellator tessellator, BufferBuilder buffer, SymbolTileEntity tile, int color, float a) {
            float r = ((color & 0xFF0000) >> 16) / 255f;
            float g = ((color & 0xFF00) >> 8) / 255f;
            float b = ((color & 0xFF)) / 255f;
            float scale = 0.8f;
            GlStateManager.translate((tile.getPos().getX() + 0.5), (tile.getPos().getY() + 0.5), (tile.getPos().getZ() + 0.5));
            GlStateManager.scale(scale, scale, scale);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            RenderBufferUtils.renderCubeFace(buffer, -0.5f, -0.5f, -0.5f, 0.5, 0.5, 0.5, r, g, b, a, true);
            tessellator.draw();

            GlStateManager.scale(1 / scale, 1 / scale, 1 / scale);
            GlStateManager.translate(-(tile.getPos().getX() + 0.5), -(tile.getPos().getY() + 0.5), -(tile.getPos().getZ() + 0.5));
        }

        private void onSelected(BlockPos pos, EnumFacing facing) {
            if (selected == tiles.get(pos)) {
                this.selectedPosFace = null;
                selected = null;
                sameSymbol.clear();
                sameAisle.clear();
                updateTips("");
                repeats[0].setActive(false); repeats[1].setActive(false);
            } else {
                this.selectedPosFace = new BlockPosFace(pos, facing);
                selected = tiles.get(pos);
                sameSymbol.clear();
                sameAisle.clear();
                for (SymbolTileEntity symbol : tiles.values()) {
                    if (symbol.symbol == selected.symbol) {
                        sameSymbol.add(symbol);
                    }
                    if (selected.a == symbol.a) {
                        sameAisle.add(symbol);
                    }
                }
                repeats[0].setActive(true); repeats[1].setActive(true);
            }
            needUpdatePredicateSelector = true;
        }

        @Override
        public Widget mouseReleased(int mouseX, int mouseY, int button) {
            if (viewMode == 2) {
                clickPosFace = null;
                dragging = false;
                return null;
            }
            Widget widget = null;
            if (button != 1) {
                widget=super.mouseReleased(mouseX, mouseY, button);
            }
            if (widget == null && isMouseOverElement(mouseX, mouseY) && hoverPosFace == null && selectedPosFace != null && button == 0) {
                onSelected(selectedPosFace.pos, selectedPosFace.facing);
                return this;
            }
            return widget;
        }
    }

    public class PredicateWidget extends WidgetGroup {
        public SimplePredicate predicate;
        public CycleItemStackHandler itemHandler;
        public String name;
        public boolean isSelected;
        public AtomicReference<PredicateWidget> atomicReference;

        public PredicateWidget(int x, int y, SimplePredicate predicate, String name, int xC) {
            super(x, y, 175, 20);
            setClientSideWidget();
            this.predicate = predicate;
            this.name = name;

            itemHandler = new CycleItemStackHandler(Collections.singletonList(predicate.getCandidates()));
            addWidget(new ImageWidget(0, 0, 179, 20, new ResourceTexture("multiblocked:textures/gui/predicate_selector_bar.png")));
            addWidget(new SlotWidget(itemHandler, 0, 1, 1, false, false));
            addWidget(new ImageWidget(20, 0, 120, 20, new TextTexture(name, 0xaf000000).setWidth(120).setType(TextTexture.TextType.ROLL))); // 106
            if (name.equals("controller") || name.equals("air") || name.equals("any")) return;
            addWidget(new ButtonWidget(xC, 3, 14, 14, new ResourceTexture("multiblocked:textures/gui/option.png"), cd -> {
                DialogWidget dialogWidget = new DialogWidget(JsonBlockPatternWidget.this, true).setOnClosed(() -> JsonBlockPatternWidget.this.sceneWidget.tiles.values().forEach(SymbolTileEntity::updateRenderer));
                dialogWidget.addWidget(new ImageWidget(0, 0, 384, 256, new ColorRectTexture(0xaf333333)));
                int yOffset = 30;
                int xOffset = 30;
                for (WidgetGroup widget : predicate.getConfigWidget(new ArrayList<>())) {
                    widget.addSelfPosition(xOffset, yOffset);
                    dialogWidget.addWidget(widget);
                    yOffset += widget.getSize().height + 5;
                }
            }).setHoverBorderTexture(1, -1).setHoverTooltip("configuration"));
        }

        public PredicateWidget(int x, int y, SimplePredicate predicate, String name, Consumer<String> closeCallBack) {
            this(x, y, predicate, name, 144);
            if (name.equals("controller")) return;
            addWidget(new ButtonWidget(160, 3, 14, 14, new ResourceTexture("multiblocked:textures/gui/remove.png"), cd -> {if(closeCallBack != null) closeCallBack.accept(name);}).setHoverBorderTexture(1, -1).setHoverTooltip("remove predicate"));
        }

        public PredicateWidget(int x, int y, SimplePredicate predicate, String name, AtomicReference<PredicateWidget> atomicReference) {
            this(x, y, predicate, name, 160);
            this.atomicReference = atomicReference;
        }

        @Override
        public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(mouseX, mouseY, partialTicks);
            if (isSelected) {
                DrawerHelper.drawBorder(getPosition().x + 1, getPosition().y + 1, getSize().width - 2, getSize().height - 2, 0xff00aa00, 1);
            }
        }

        @Override
        public Widget mouseClicked(int mouseX, int mouseY, int button) {
            if (isMouseOverElement(mouseX, mouseY) && atomicReference != null) {
                if (atomicReference.get() == this) {
                    atomicReference.set(null);
                    this.isSelected = false;
                } else {
                    if (atomicReference.get() != null) {
                        atomicReference.get().isSelected = false;
                    }
                    atomicReference.set(this);
                    this.isSelected = true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            itemHandler.updateStacks(Collections.singletonList(predicate.getCandidates()));
        }
    }

    public static class SymbolButton extends ButtonWidget{

        private final BlockPatternSceneWidget sceneWidget;
        private final char c;

        public SymbolButton(int xPosition, int yPosition, int width, int height, char c, BlockPatternSceneWidget sceneWidget, Consumer<ClickData> onPressed) {
            super(xPosition, yPosition, width, height, new TextTexture(c + "", getColor(c)), onPressed);
            this.c = c;
            this.sceneWidget = sceneWidget;
        }

        @Override
        public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
            Position position = getPosition();
            Size size = getSize();
            DrawerHelper.drawBorder(position.x, position.y, size.width, size.height, sceneWidget.selected != null && sceneWidget.selected.symbol == c ? 0xff00ff00 : -1, 1);
            super.drawInBackground(mouseX, mouseY, partialTicks);
        }
    }

    public static class SymbolTileEntity extends PartTileEntity<PartDefinition> {
        public char symbol;
        public IRenderer renderer;
        public JsonBlockPatternWidget widget;
        public int a,b,c;

        public SymbolTileEntity() {
            super();
        }

        public void init(char symbol, JsonBlockPatternWidget widget, int a, int b, int c) {
            this.symbol = symbol;
            this.widget = widget;
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public void updateRenderer() {
            if (widget.pattern.symbolMap.containsKey(symbol)) {
                Set<BlockInfo> candidates = new HashSet<>();
                for (String s : widget.pattern.symbolMap.get(symbol)) {
                    SimplePredicate predicate = widget.pattern.predicates.get(s);
                    if (predicate instanceof PredicateComponent && ((PredicateComponent) predicate).definition != null) {
                        renderer = ((PredicateComponent) predicate).definition.baseRenderer;
                        candidates = null;
                        break;
                    } else if (predicate != null && predicate.candidates != null) {
                        candidates.addAll(Arrays.asList(predicate.candidates.get()));
                    }
                }
                if (candidates != null) {
                    if (candidates.size() == 1) {
                        renderer = new BlockStateRenderer(candidates.toArray(new BlockInfo[0])[0].getBlockState());
                    } else if (!candidates.isEmpty()){
                        renderer = new CycleBlockStateRenderer(candidates.toArray(new BlockInfo[0]));
                    } else {
                        renderer = null;
                    }
                }
            }
            if (widget.sceneWidget != null) {
                widget.sceneWidget.needCompileCache();
            }
        }

        @Override
        public boolean isFormed() {
            return false;
        }

        @Override
        public IRenderer getRenderer() {
            return widget == null ? null : widget.sceneWidget.viewMode == 1 ? null : renderer;
        }
        
    }
}
