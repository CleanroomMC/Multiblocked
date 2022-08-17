package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.*;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.*;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.IRenderer;
import com.cleanroommc.multiblocked.client.renderer.scene.WorldSceneRenderer;
import com.cleanroommc.multiblocked.client.util.RenderBufferUtils;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class IShapeWidget extends DialogWidget {
    public Consumer<List<AxisAlignedBB>> onSave;
    public final DummyComponentTileEntity tileEntity;
    private final DraggableScrollableWidgetGroup container;
    private final List<AxisAlignedBB> aabbs;
    private List<AxisAlignedBB> shape;

    public IShapeWidget(WidgetGroup parent, IRenderer renderer, List<AxisAlignedBB> shape, Consumer<List<AxisAlignedBB>> onSave) {
        super(parent, true);
        this.onSave = onSave;
        this.shape = shape;
        this.addWidget(new ImageWidget(0, 0, getSize().width, getSize().height, new ColorRectTexture(0xaf000000)));
        TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlock(BlockPos.ORIGIN, BlockInfo.fromBlockState(MbdComponents.DummyComponentBlock.getDefaultState()));
        tileEntity = (DummyComponentTileEntity) world.getTileEntity(BlockPos.ORIGIN);
        setNewRenderer(renderer);
        this.addWidget(new ImageWidget(35, 59, 138, 138, new GuiTextureGroup(new ColorBorderTexture(3, -1), new ColorRectTexture(0xaf444444))));
        this.addWidget(new SceneWidget(35, 59,  138, 138, world) {
            @Override
            @SideOnly(Side.CLIENT)
            public void renderBlockOverLay(WorldSceneRenderer renderer) {
                super.renderBlockOverLay(renderer);

                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


                Tessellator tessellator = Tessellator.getInstance();
                GlStateManager.disableTexture2D();
                BufferBuilder buffer = tessellator.getBuffer();
                buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
                GlStateManager.glLineWidth(3);

                for (AxisAlignedBB aabb : IShapeWidget.this.shape) {
                    RenderBufferUtils.renderCubeFrame(buffer, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, 1, 0, 0, 1);
                }

                tessellator.draw();

                GlStateManager.enableDepth();
            }
        }
                .setRenderedCore(Collections.singleton(BlockPos.ORIGIN), null)
                .setRenderSelect(false)
                .setRenderFacing(false));
        this.addWidget(new ButtonWidget(210, 55, 40, 20, this::onUpdate)
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("multiblocked.gui.tips.update", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("multiblocked.gui.tips.update"));
        container = new DraggableScrollableWidgetGroup(180, 80, 185, 120).setBackground(new ColorRectTexture(0xffaaaaaa));
        this.addWidget(container);
        this.addWidget(new ButtonWidget(320, 55, 40, 20, cd -> {
            onSave.accept(this.shape);
            super.close();
        })
                .setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("multiblocked.gui.tips.save_1", -1).setDropShadow(true))
                .setHoverBorderTexture(1, -1)
                .setHoverTooltip("multiblocked.gui.tips.save"));
        aabbs = new ArrayList<>(this.shape);

        this.addWidget(new ButtonWidget(180, 55, 20, 20, new ResourceTexture("multiblocked:textures/gui/add.png"), cd->{
            aabbs.add(new AxisAlignedBB(0, 0, 0, 0, 0, 0));
            updateShapeList();
        }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.shape.add"));
        updateShapeList();
    }

    private void updateShapeList() {
        container.clearAllWidgets();
        for (int i = 0; i < aabbs.size(); i++) {
            WidgetGroup group = new WidgetGroup(2, container.widgets.size() * 35, container.getSize().width - 4, 30);
            group.addWidget(new ImageWidget(0, 0, group.getSize().width, group.getSize().height, new ColorRectTexture(0x5f444444)));
            final int finalI = i;

            int x = 0;
            group.addWidget(new LabelWidget(x, 3, "minX"));
            group.addWidget(new TextFieldWidget(x + 25, 3, 25, 10, true, null, s->{
                AxisAlignedBB aabb = aabbs.get(finalI);
                aabbs.set(finalI, new AxisAlignedBB(Float.parseFloat(s), aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ));
            }).setNumbersOnly(0f, 2f).setCurrentString(aabbs.get(finalI).minX + ""));
            x += 55;

            group.addWidget(new LabelWidget(x, 3, "minY"));
            group.addWidget(new TextFieldWidget(x + 25, 3, 25, 10, true, null, s->{
                AxisAlignedBB aabb = aabbs.get(finalI);
                aabbs.set(finalI, new AxisAlignedBB(aabb.minX, Float.parseFloat(s), aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ));
            }).setNumbersOnly(0f, 2f).setCurrentString(aabbs.get(finalI).minY + ""));
            x += 55;

            group.addWidget(new LabelWidget(x, 3, "minZ"));
            group.addWidget(new TextFieldWidget(x + 25, 3, 25, 10, true, null, s->{
                AxisAlignedBB aabb = aabbs.get(finalI);
                aabbs.set(finalI, new AxisAlignedBB(aabb.minX, aabb.minY, Float.parseFloat(s), aabb.maxX, aabb.maxY, aabb.maxZ));
            }).setNumbersOnly(0f, 2f).setCurrentString(aabbs.get(finalI).minZ + ""));
            x = 0;

            group.addWidget(new LabelWidget(x, 20, "maxX"));
            group.addWidget(new TextFieldWidget(x + 25, 20, 25, 10, true, null, s->{
                AxisAlignedBB aabb = aabbs.get(finalI);
                aabbs.set(finalI, new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, Float.parseFloat(s), aabb.maxY, aabb.maxZ));
            }).setNumbersOnly(0f, 2f).setCurrentString(aabbs.get(finalI).maxX + ""));
            x += 55;

            group.addWidget(new LabelWidget(x, 20, "maxY"));
            group.addWidget(new TextFieldWidget(x + 25, 20, 25, 10, true, null, s->{
                AxisAlignedBB aabb = aabbs.get(finalI);
                aabbs.set(finalI, new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, Float.parseFloat(s), aabb.maxZ));
            }).setNumbersOnly(0f, 2f).setCurrentString(aabbs.get(finalI).maxY + ""));
            x += 55;

            group.addWidget(new LabelWidget(x, 20, "maxZ"));
            group.addWidget(new TextFieldWidget(x + 25, 20, 25, 10, true, null, s->{
                AxisAlignedBB aabb = aabbs.get(finalI);
                aabbs.set(finalI, new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, Float.parseFloat(s)));
            }).setNumbersOnly(0f, 2f).setCurrentString(aabbs.get(finalI).maxZ + ""));
            container.addWidget(group);
            x += 55;

            group.addWidget(new ButtonWidget(x, 8, 15, 15, new ResourceTexture("multiblocked:textures/gui/remove.png"), cd -> {
                aabbs.remove(finalI);
                updateShapeList();
            }).setHoverBorderTexture(1, -1).setHoverTooltip("multiblocked.gui.tips.remove"));
        }
    }

    @Override
    public void close() {
        super.close();
        if (onSave != null) {
            onSave.accept(shape);
        }
    }

    public void setNewRenderer(IRenderer newRenderer) {
        PartDefinition definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "i_renderer"));
        definition.getBaseStatus().setRenderer(newRenderer);
        tileEntity.setDefinition(definition);
    }

    private void onUpdate(ClickData clickData) {
        shape = new ArrayList<>(aabbs);
    }

}
