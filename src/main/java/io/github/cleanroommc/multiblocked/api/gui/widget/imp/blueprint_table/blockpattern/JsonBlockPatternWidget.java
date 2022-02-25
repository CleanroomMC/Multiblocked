package io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.blockpattern;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SelectorWidget;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.DyeUtils;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonBlockPatternWidget extends WidgetGroup {
    public static BlockComponent symbolBlock;
    public JsonBlockPattern pattern;
    public TabContainer container;
    public WidgetGroup patternTab;
    public WidgetGroup predicateTab;
    public BlockPatternSceneWidget sceneWidget;
    public SelectorWidget[] selectors;

    public JsonBlockPatternWidget(JsonBlockPattern pattern) {
        super(0, 0, 384, 256);
        setClientSideWidget();
        if (Multiblocked.isClient()) {
            this.pattern = pattern;
            this.addWidget(0, new ImageWidget(0, 0, getSize().width, getSize().height, new ResourceTexture("multiblocked:textures/gui/json_block_pattern.png")));
            this.addWidget(sceneWidget = new BlockPatternSceneWidget());
            this.addWidget(container = new TabContainer(0, 0, 384, 256));

            ResourceTexture tabPattern = new ResourceTexture("multiblocked:textures/gui/tab_pattern.png");
            container.addTab((TabButton)new TabButton(171, 29, 20, 20)
                            .setTexture(tabPattern.getSubTexture(0, 0, 1, 0.5),
                                    tabPattern.getSubTexture(0, 0.5, 1, 0.5))
                            .setHoverTooltip("Pattern Settings"),
                    patternTab = new WidgetGroup(0, 0, getSize().width, getSize().height));
            List<String> candidates = Arrays.stream(RelativeDirection.values()).map(Enum::name).collect(Collectors.toList());
            patternTab.addWidget(new LabelWidget(35, 168, () -> "Char Direction").setTextColor(-1).setDrop(true));
            patternTab.addWidget(new LabelWidget(140, 168, () -> "String Direction").setTextColor(-1).setDrop(true));
            patternTab.addWidget(new LabelWidget(245, 168, () -> "Aisle Direction").setTextColor(-1).setDrop(true));
            selectors = new SelectorWidget[3];
            patternTab.addWidget(selectors[0] = new SelectorWidget(35, 180, 100, 15, candidates, -1).setOnChanged(s->this.onDirChange(0, s)).setButtonBackground(new ColorRectTexture(0xff111111)).setValue(pattern.structureDir[0].name()));
            patternTab.addWidget(selectors[1] = new SelectorWidget(140, 180, 100, 15, candidates, -1).setOnChanged(s->this.onDirChange(1, s)).setButtonBackground(new ColorRectTexture(0xff111111)).setValue(pattern.structureDir[1].name()));
            patternTab.addWidget(selectors[2] = new SelectorWidget(245, 180, 100, 15, candidates, -1).setOnChanged(s->this.onDirChange(2, s)).setButtonBackground(new ColorRectTexture(0xff111111)).setValue(pattern.structureDir[2].name()));

            ResourceTexture tabPredicate = new ResourceTexture("multiblocked:textures/gui/tab_predicate.png");
            container.addTab((TabButton)new TabButton(171 + 25, 29, 20, 20)
                            .setTexture(tabPredicate.getSubTexture(0, 0, 1, 0.5),
                                    tabPredicate.getSubTexture(0, 0.5, 1, 0.5))
                            .setHoverTooltip("Predicate Settings"),
                    predicateTab = new WidgetGroup(171, 29, 20, 20));

            container.setOnChanged(this::onTabChanged);
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

    private void onTabChanged(WidgetGroup oldTab, WidgetGroup newTab) {
        if (newTab == predicateTab) {
            sceneWidget.tiles.values().forEach(SymbolTileEntity::updateRenderer);
        }
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

        @SideOnly(Side.CLIENT)
        TrackedDummyWorld world;
        public BlockPatternSceneWidget() {
            super(31, 31, 125, 125, null);
            reloadBlocks();
            addWidget(new ImageWidget(0, 0, 125, 20, texture = new TextTexture("", -1).setWidth(125).setType(TextTexture.TextType.ROLL)));
            addWidget(new ButtonWidget(0, 50, 10, 10, new ColorRectTexture(-1), cd -> addAisle(1)));
            addWidget(new LabelWidget(0, 60, ()->{
                if (aisleRender == -1) return "all";
                return aisleRender + "";
            }).setTextColor(-1));
            addWidget(new ButtonWidget(0, 70, 10, 10, new ColorRectTexture(-1), cd -> addAisle(-1)));
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
                    }
                }
            }
            setRenderedCore(posSet, null);
            setOnSelected(this::onSelected);
            setRenderFacing(false);
        }

        @Override
        public void renderBlockOverLay(WorldSceneRenderer renderer) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.disableTexture2D();

            if (JsonBlockPatternWidget.this.container.focus == JsonBlockPatternWidget.this.patternTab) { // render pattern style
                if (selected != null) {
                    for (SymbolTileEntity tile : sameSymbol) {
                        drawSymbolTE(tessellator, buffer, tile, tile.getColor(), 1);
                    }
                }

                if (selected != null) {
                    GlStateManager.depthMask(false);
                }
                for (SymbolTileEntity tile : tiles.values()) {
                    if (aisleRender > -1 && tile.a != aisleRender) continue;
                    if (sameSymbol.contains(tile)) continue;
                    float dd = Math.abs(System.currentTimeMillis() % 3000);
                    drawSymbolTE(tessellator, buffer, tile, tile.getColor(), selected == null ? 1 : ((((dd > 1500) ? (3000 - dd) : dd) / 1500f) * 0.3f));
                }
                if (selected != null) {
                    GlStateManager.depthMask(true);
                }
            }

            super.renderBlockOverLay(renderer);

            if (selected != null) {
                RenderUtils.renderBlockOverLay(selected.getPos(), 1, 0, 0, 1.01f);
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
            } else {
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
                int[] repetition = pattern.aisleRepetitions[selected.a];
                updateTips(String.format("repeat(%d, %d)", repetition[0], repetition[1]));
            }
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
            return widget == null ? null : widget.container.focus == widget.patternTab ? null : renderer;
        }

        public int getColor() {
            switch (symbol) {
                case '@' : return 0xff0000ff;
                case ' ' : return 0xff4EEDF7;
                default: {
                    return EnumDyeColor.values()[(symbol - 'A') % EnumDyeColor.values().length].colorValue | 0xff000000;
                }
            }
        }

        @Override
        public boolean shouldRenderInPass(int pass) { // why i do render here!!

            return super.shouldRenderInPass(pass);
        }
    }
}
