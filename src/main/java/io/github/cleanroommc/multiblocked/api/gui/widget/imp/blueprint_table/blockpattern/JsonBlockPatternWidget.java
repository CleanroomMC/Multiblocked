package io.github.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.blockpattern;

import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabContainer;
import io.github.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import io.github.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import io.github.cleanroommc.multiblocked.client.util.RenderBufferUtils;
import io.github.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonBlockPatternWidget extends WidgetGroup {
    public static BlockComponent symbolBlock;
    public JsonBlockPattern pattern;
    public TabContainer container;
    public WidgetGroup patternTab;
    public WidgetGroup predicateTab;
    public BlockPatternSceneWidget sceneWidget;

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
                    patternTab = new WidgetGroup(171, 29, 20, 20));

            ResourceTexture tabPredicate = new ResourceTexture("multiblocked:textures/gui/tab_predicate.png");
            container.addTab((TabButton)new TabButton(171 + 25, 29, 20, 20)
                            .setTexture(tabPredicate.getSubTexture(0, 0, 1, 0.5),
                                    tabPredicate.getSubTexture(0, 0.5, 1, 0.5))
                            .setHoverTooltip("Predicate Settings"),
                    predicateTab = new WidgetGroup(171, 29, 20, 20));

            container.setOnChanged(this::onTabChanged);
        }
    }

    public static void registerBlock() {
        PartDefinition definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "symbol"), SymbolTileEntity.class);
        definition.isOpaqueCube = false;
        MultiblockComponents.registerComponent(definition);
        symbolBlock = MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.get(definition.location);
    }

    private void onTabChanged(WidgetGroup oldTab, WidgetGroup newTab) {
        if (newTab == predicateTab) {
            sceneWidget.tiles.forEach(SymbolTileEntity::updateRenderer);
        }
    }

    public class BlockPatternSceneWidget extends SceneWidget {
        public List<SymbolTileEntity> tiles = new ArrayList<>();
        @SideOnly(Side.CLIENT)
        TrackedDummyWorld world;
        public BlockPatternSceneWidget() {
            super(31, 31, 125, 125, null);
            if (Multiblocked.isClient()) {
                createScene(world = new TrackedDummyWorld());
                String[][] pattern = JsonBlockPatternWidget.this.pattern.pattern;
                Set<BlockPos> posSet = new HashSet<>();
                for (int i = 0; i < pattern.length; i++) {
                    for (int j = 0; j < pattern[0].length; j++) {
                        for (int k = 0; k < pattern[0][0].length(); k++) {
                            char c = pattern[i][j].charAt(k);
                            BlockPos pos = JsonBlockPatternWidget.this.pattern.getActualRelativeOffset(i, j, k, EnumFacing.NORTH);
                            world.addBlock(pos, new BlockInfo(symbolBlock.getDefaultState()));
                            SymbolTileEntity tileEntity = (SymbolTileEntity) world.getTileEntity(pos);
                            tileEntity.init(c, JsonBlockPatternWidget.this);
                            tileEntity.setDefinition(symbolBlock.definition);
                            tileEntity.setWorld(world);
                            tileEntity.validate();
                            posSet.add(pos);
                            tiles.add(tileEntity);
                        }
                    }
                }
                setRenderedCore(posSet, null);
            }
        }

    }

    public static class SymbolTileEntity extends PartTileEntity<PartDefinition> {
        public char symbol;
        public IRenderer renderer;
        public JsonBlockPatternWidget widget;

        public SymbolTileEntity() {
            super();
        }

        public void init(char symbol, JsonBlockPatternWidget widget) {
            this.symbol = symbol;
            this.widget = widget;
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
                case ' ' : return 0x00000000;
                default: return 0xffff0000;
            }
        }

        @Override
        public boolean shouldRenderInPass(int pass) { // why i do render here!!
            if (pass == 0) {
                int color = getColor();
                float a = ((color & 0xFF000000) >> 24) / 255f;
                float r = ((color & 0xFF0000) >> 16) / 255f;
                float g = ((color & 0xFF00) >> 8) / 255f;
                float b = ((color & 0xFF)) / 255f;
                float scale = 0.8f;
                GlStateManager.enableBlend();
                GlStateManager.translate((pos.getX() + 0.5), (pos.getY() + 0.5), (pos.getZ() + 0.5));
                GlStateManager.scale(scale, scale, scale);

                Tessellator tessellator = Tessellator.getInstance();
                GlStateManager.disableTexture2D();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                RenderBufferUtils.renderCubeFace(buffer, -0.5f, -0.5f, -0.5f, 0.5, 0.5, 0.5, r, g, b, a, true);
                tessellator.draw();

                GlStateManager.scale(1 / scale, 1 / scale, 1 / scale);
                GlStateManager.translate(-(pos.getX() + 0.5), -(pos.getY() + 0.5), -(pos.getZ() + 0.5));
                GlStateManager.enableTexture2D();

                GlStateManager.color(1, 1, 1, 1);
            }
            return false;
        }
    }
}
