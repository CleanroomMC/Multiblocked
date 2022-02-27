package io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.blockpattern;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.api.gui.util.ClickData;
import io.github.cleanroommc.multiblocked.api.gui.util.DrawerHelper;
import io.github.cleanroommc.multiblocked.api.gui.widget.Widget;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.*;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import io.github.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import io.github.cleanroommc.multiblocked.api.pattern.util.RelativeDirection;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.scene.WorldSceneRenderer;
import io.github.cleanroommc.multiblocked.client.util.RenderBufferUtils;
import io.github.cleanroommc.multiblocked.client.util.RenderUtils;
import io.github.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import io.github.cleanroommc.multiblocked.util.BlockPosFace;
import io.github.cleanroommc.multiblocked.util.CycleItemStackHandler;
import io.github.cleanroommc.multiblocked.util.Position;
import io.github.cleanroommc.multiblocked.util.Size;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JsonBlockPatternWidget extends WidgetGroup {
    public static BlockComponent symbolBlock;
    public JsonBlockPattern pattern;
    public TabContainer container;
    public WidgetGroup patternTab;
    public WidgetGroup predicateTab;
    public BlockPatternSceneWidget sceneWidget;
    public SelectorWidget[] selectors;
    public TextFieldWidget[] repeats;
    public DraggableScrollableWidgetGroup symbolSelector;
    public DraggableScrollableWidgetGroup predicateGroup;
    public boolean needUpdatePredicateSelector;

    public JsonBlockPatternWidget(JsonBlockPattern pattern) {
        super(0, 0, 384, 256);
        setClientSideWidget();
        if (Multiblocked.isClient()) {
            this.pattern = pattern;
            this.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/json_block_pattern.png")));
            this.addWidget(sceneWidget = new BlockPatternSceneWidget());
            this.addWidget(container = new TabContainer(0, 0, 384, 256));

            // patternTab
            ResourceTexture tabPattern = new ResourceTexture("multiblocked:textures/gui/tab_pattern.png");
            container.addTab((TabButton)new TabButton(171, 29, 20, 20)
                            .setTexture(tabPattern.getSubTexture(0, 0, 1, 0.5),
                                    tabPattern.getSubTexture(0, 0.5, 1, 0.5))
                            .setHoverTooltip("Pattern Settings"),
                    patternTab = new WidgetGroup(0, 0, getSize().width, getSize().height));

            int bgColor = 0x8f111111;

            patternTab.addWidget(new LabelWidget(174, 92, () -> "Repeat:").setTextColor(-1).setDrop(true));
            repeats = new TextFieldWidget[2];
            patternTab.addWidget(repeats[0] = new TextFieldWidget(215, 87, 40, 15, () -> sceneWidget.selected == null ? "" : pattern.aisleRepetitions[sceneWidget.selected.a][0] + "", s -> {
                if (sceneWidget.selected != null && s != null && !s.isEmpty() && sceneWidget.centerOffset[0] != sceneWidget.selected.a) {
                    pattern.aisleRepetitions[sceneWidget.selected.a][0] = Integer.parseInt(s);
                }
            }).setNumbersOnly(1, Integer.MAX_VALUE).setMaxLength(3).setBackground(new ColorRectTexture(bgColor)));
            patternTab.addWidget(repeats[1] = new TextFieldWidget(305, 87, 40, 15, () -> sceneWidget.selected == null ? "" : pattern.aisleRepetitions[sceneWidget.selected.a][1] + "", s -> {
                if (sceneWidget.selected != null && s != null && !s.isEmpty() && sceneWidget.centerOffset[0] != sceneWidget.selected.a) {
                    pattern.aisleRepetitions[sceneWidget.selected.a][1] = Integer.parseInt(s);
                }
            }).setNumbersOnly(1, Integer.MAX_VALUE).setMaxLength(3).setBackground(new ColorRectTexture(bgColor)));
            repeats[0].setActive(false); repeats[1].setActive(false); repeats[0].setHoverTooltip("min"); repeats[1].setHoverTooltip("max");

            patternTab.addWidget(symbolSelector = new DraggableScrollableWidgetGroup(215, 105, 130, 35).setBackground(new ColorRectTexture(bgColor)));
            patternTab.addWidget(new ButtonWidget(174, 105, 40, 20, null, (cd) -> {
                char next = (char) (pattern.symbolMap.keySet().stream().max(Comparator.comparingInt(a -> a)).get() + 1);
                pattern.symbolMap.put(next, new HashSet<>());
                updateSymbolButton();
            }).setButtonTexture(new ColorRectTexture(0xff454545), new TextTexture("Add Symbol", -1)));
            updateSymbolButton();
            patternTab.addWidget(new LabelWidget(174, 142, ()->"tips: you cant modify controller").setTextColor(-1));

            List<String> candidates = Arrays.stream(RelativeDirection.values()).map(Enum::name).collect(Collectors.toList());
            patternTab.addWidget(new LabelWidget(174, 70, () -> "Dir:").setTextColor(-1).setDrop(true));
            patternTab.addWidget(new ImageWidget(215, 57, 40, 10, new TextTexture("Char", -1).setDropShadow(true)));
            patternTab.addWidget(new ImageWidget(260, 57, 40, 10, new TextTexture("String", -1).setDropShadow(true)));
            patternTab.addWidget(new ImageWidget(305, 57, 40, 10, new TextTexture("Aisle", -1).setDropShadow(true)));
            selectors = new SelectorWidget[3];
            patternTab.addWidget(selectors[0] = new SelectorWidget(215, 67, 40, 15, candidates, -1).setOnChanged(s->this.onDirChange(0, s)).setButtonBackground(new ColorRectTexture(bgColor)).setValue(pattern.structureDir[0].name()));
            patternTab.addWidget(selectors[1] = new SelectorWidget(260, 67, 40, 15, candidates, -1).setOnChanged(s->this.onDirChange(1, s)).setButtonBackground(new ColorRectTexture(bgColor)).setValue(pattern.structureDir[1].name()));
            patternTab.addWidget(selectors[2] = new SelectorWidget(305, 67, 40, 15, candidates, -1).setOnChanged(s->this.onDirChange(2, s)).setButtonBackground(new ColorRectTexture(bgColor)).setValue(pattern.structureDir[2].name()));

            //predicateTab
            ResourceTexture tabPredicate = new ResourceTexture("multiblocked:textures/gui/tab_predicate.png");
            container.addTab((TabButton)new TabButton(171 + 25, 29, 20, 20)
                            .setTexture(tabPredicate.getSubTexture(0, 0, 1, 0.5),
                                    tabPredicate.getSubTexture(0, 0.5, 1, 0.5))
                            .setHoverTooltip("Predicate Settings"),
                    predicateTab = new WidgetGroup(171, 29, 20, 20));

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
            addWidget(predicateGroup = new DraggableScrollableWidgetGroup(168, 166, 185, 58).setBackground(new ColorRectTexture(bgColor)));
            updatePredicateSelector();
        }
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
                    predicateGroup.addWidget(new PredicateWidget(0, predicateGroup.widgets.size() * 20, predicate, predicateName, name -> {
                        if (sceneWidget.selected != null) {
                            pattern.symbolMap.get(sceneWidget.selected.symbol).remove(name);
                            needUpdatePredicateSelector = true;
                        }
                    }));
                }
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (needUpdatePredicateSelector) {
            updatePredicateSelector();
        }
    }

    private void updateSymbolButton() {
        symbolSelector.clearAllWidgets();
        int i = 0;
        for (final char c : pattern.symbolMap.keySet().stream().sorted().collect(Collectors.toList())) {
            symbolSelector.addWidget(new SymbolButton(13 * (i % 9) + 8, 13 * (i / 9) + 6, 10, 10, c, sceneWidget, cd -> {
                if (sceneWidget.selected != null && c != '@' && sceneWidget.selected.symbol != '@') {
                    sceneWidget.selected.symbol = c;
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
        MultiblockComponents.registerComponent(definition);
        symbolBlock = MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(definition.location);
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
            layerSwitch.addWidget(new ButtonWidget(5, 50, 10, 10, new ColorRectTexture(-1), cd -> addAisle(1)).setHoverTooltip("next aisle"));
            layerSwitch. addWidget(new LabelWidget(5, 60, () -> aisleRender == -1 ? "all" : aisleRender + "").setTextColor(-1));
            layerSwitch.addWidget(new ButtonWidget(5, 70, 10, 10, new ColorRectTexture(-1), cd -> addAisle(-1)).setHoverTooltip("last aisle"));

            addWidget(new ButtonWidget(110, 110, 10, 10, new ColorRectTexture(0xffff0000), this::switchPatternView)
                    .setHoverTooltip("switch view"));
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
        }

        private void addAisle(int add) {
            if (aisleRender + add >= -1 && aisleRender + add < pattern.pattern.length) {
                aisleRender += add;
            }
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
            world.setRenderFilter(pos -> {
                if (aisleRender > -1) {
                    return tiles.containsKey(pos) && tiles.get(pos).a == aisleRender;
                }
                return true;
            });
            String[][] pattern = JsonBlockPatternWidget.this.pattern.pattern;
            Set<BlockPos> posSet = new HashSet<>();
            offset = Math.max(pattern.length, Math.max(pattern[0].length, pattern[0][0].length()));
            for (int i = 0; i < pattern.length; i++) {
                for (int j = 0; j < pattern[0].length; j++) {
                    for (int k = 0; k < pattern[0][0].length(); k++) {
                        if (pattern[i][j].charAt(k) == '@') {
                            centerOffset = new int[]{i, j, k};
                            break;
                        }
                    }
                }
            }
            for (int i = 0; i < pattern.length; i++) {
                for (int j = 0; j < pattern[0].length; j++) {
                    for (int k = 0; k < pattern[0][0].length(); k++) {
                        char c = pattern[i][j].charAt(k);
                        BlockPos pos = JsonBlockPatternWidget.this.pattern.getActualPosOffset(k - centerOffset[2], j - centerOffset[1], i - centerOffset[0], EnumFacing.NORTH).add(offset, offset, offset);
                        world.addBlock(pos, new BlockInfo(symbolBlock.getDefaultState()));
                        SymbolTileEntity tileEntity = (SymbolTileEntity) world.getTileEntity(pos);
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

            if (viewMode == 0) { // render pattern style
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
                RenderUtils.renderCubeFace(buffer, minPos[0], minPos[1], minPos[2], maxPos[0] + 1, maxPos[1] + 1, maxPos[2] + 1, 0.3f, 0.5f, 0.7f, 1);
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

    public static class PredicateWidget extends WidgetGroup {
        public SimplePredicate predicate;
        public String name;

        public PredicateWidget(int x, int y, SimplePredicate predicate, String name, Consumer<String> closeCallBack) {
            super(x, y, 185, 20);
            setClientSideWidget();
            this.predicate = predicate;
            this.name = name;
            CycleItemStackHandler itemHandler = new CycleItemStackHandler(Collections.singletonList(predicate.getCandidates()));
            addWidget(new SlotWidget(itemHandler, 0, 1, 1, false, false));
            addWidget(new LabelWidget(20, 5, () -> String.format("%s|%s|%d-%d|%d-%d|%d",
                    predicate.type, name, predicate.minLayerCount, predicate.maxGlobalCount, predicate.minLayerCount, predicate.maxLayerCount, predicate.previewCount)));
            addWidget(new ButtonWidget(146, 1, 18, 18, new ColorRectTexture(0xff00ff00), null));
            addWidget(new ButtonWidget(166, 1, 18, 18, new ColorRectTexture(0xffff0000), clickData -> {if(closeCallBack != null) closeCallBack.accept(name);}));
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
                Set<IBlockState> candidates = new HashSet<>();
                for (String s : widget.pattern.symbolMap.get(symbol)) {
                    SimplePredicate predicate = widget.pattern.predicates.get(s);
                    if (predicate != null && predicate.candidates != null) {
                        for (BlockInfo blockInfo : predicate.candidates.get()) {
                            candidates.add(blockInfo.getBlockState());
                        }
                    }
                }
                renderer = new CycleBlockStateRenderer(candidates.toArray(new IBlockState[0]));
            }
        }

        @Override
        public boolean isFormed() {
            return false;
        }

        @Override
        public IRenderer getRenderer() {
            return widget == null ? null : widget.sceneWidget.viewMode == 0 ? null : renderer;
        }

        @Override
        public boolean shouldRenderInPass(int pass) { // why i do render here!!

            return super.shouldRenderInPass(pass);
        }
    }
}
