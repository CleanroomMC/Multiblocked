package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.cleanroommc.multiblocked.api.gui.texture.ColorBorderTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.LabelWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.PhantomSlotWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.JsonBlockPatternWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import com.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ControllerWidget extends ComponentWidget{
    protected final JsonBlockPattern pattern;
    protected final WidgetGroup S2;
    protected final SceneWidget sceneWidget;
    protected final Set<DummyComponentTileEntity> tiles = new HashSet<>();
    protected boolean isFormed;

    public ControllerWidget(JsonBlockPattern pattern, ControllerDefinition definition) {
        super(definition);
        this.pattern = pattern;
        int x = 47;
        SimplePredicate predicate = pattern.predicates.get("controller");
        if (predicate instanceof PredicateComponent) {
            ((PredicateComponent) predicate).definition = definition;
        }
        S1.addWidget(createBoolSwitch(x + 100, 90, "consumeCatalyst", "consume Catalyst", definition.consumeCatalyst, r -> definition.consumeCatalyst = r));
        S1.addWidget(createBoolSwitch(x + 100, 105, "disableOthersRendering", "disable Others Rendering", definition.disableOthersRendering, r -> definition.disableOthersRendering = r));

        IItemHandlerModifiable handler;
        PhantomSlotWidget phantomSlotWidget = new PhantomSlotWidget(handler = new ItemStackHandler(1), 0, x + 205, 73);
        LabelWidget labelWidget = new LabelWidget(x + 230, 78, () -> "Catalyst").setTextColor(-1).setDrop(true);
        S1.addWidget(phantomSlotWidget);
        S1.addWidget(labelWidget);
        phantomSlotWidget.setClearSlotOnRightClick(true)
                .setChangeListener(() -> definition.catalyst = handler.getStackInSlot(0))
                .setBackgroundTexture(new ColorBorderTexture(1, -1))
                .setHoverTooltip("Catalyst, if it is empty then right click can be formed")
                .setVisible(definition.catalyst != null);
        handler.setStackInSlot(0, definition.catalyst);
        labelWidget.setVisible(definition.catalyst != null);

        S1.addWidget(createBoolSwitch(x + 100, 75, "needCatalyst", "If no catalyst is needed, the structure will try to formed per second.", definition.catalyst != null, r -> {
            definition.catalyst = !r ? null : ItemStack.EMPTY;
            phantomSlotWidget.setVisible(r);
            labelWidget.setVisible(r);
        }));

        tabContainer.addTab(new TabButton(65, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S2"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S2"))
                        .setHoverTooltip("Step 2: structure pattern setup"),
                S2 = new WidgetGroup(0, 0, getSize().width, getSize().height));
        S2.addWidget(new ImageWidget(35, 59, 138, 138, new GuiTextureGroup(new ColorBorderTexture(3, -1), new ColorRectTexture(0xaf444444))));
        S2.addWidget(sceneWidget = new SceneWidget(35, 59, 138, 138, null).useCacheBuffer().setRenderFacing(false).setRenderSelect(false));
        ResourceTexture PAGE = new ResourceTexture("multiblocked:textures/gui/structure_page.png");
        sceneWidget.addWidget(new SwitchWidget(138 - 20, 138 - 20, 16, 16, this::onFormedSwitch)
                .setPressed(isFormed)
                .setTexture(PAGE.getSubTexture(176 / 256.0, 184 / 256.0, 16 / 256.0, 16 / 256.0), PAGE.getSubTexture(176 / 256.0, 200 / 256.0, 16 / 256.0, 16 / 256.0))
                .setHoverTooltip("multiblocked.structure_page.switch"));
        S2.addWidget(new ButtonWidget(181, 55, 20, 20, new ColorRectTexture(-1), cd -> new JsonBlockPatternWidget(this, pattern, this::savePattern)));
        updateScene();
    }

    private void onFormedSwitch(ClickData clickData, boolean isPressed) {
        isFormed = isPressed;
        tiles.forEach(t->t.isFormed = isFormed);
    }

    private void updateScene() {
        int[] centerOffset = this.pattern.getCenterOffset();
        String[][] pattern = this.pattern.pattern;
        Set<BlockPos> posSet = new HashSet<>();
        TrackedDummyWorld world = new TrackedDummyWorld();
        sceneWidget.createScene(world);
        tiles.clear();
        int offset = Math.max(pattern.length, Math.max(pattern[0].length, pattern[0][0].length()));
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[0].length; j++) {
                for (int k = 0; k < pattern[0][0].length(); k++) {
                    char symbol = pattern[i][j].charAt(k);
                    BlockPos pos = this.pattern.getActualPosOffset(k - centerOffset[2], j - centerOffset[1], i - centerOffset[0], EnumFacing.NORTH).add(offset, offset, offset);
                    world.addBlock(pos, new BlockInfo(MultiblockComponents.DummyComponentBlock));
                    DummyComponentTileEntity  tileEntity = (DummyComponentTileEntity) world.getTileEntity(pos);
                    ComponentDefinition definition = null;
                    assert tileEntity != null;
                    if (this.pattern.symbolMap.containsKey(symbol)) {
                        Set<IBlockState> candidates = new HashSet<>();
                        for (String s : this.pattern.symbolMap.get(symbol)) {
                            SimplePredicate predicate = this.pattern.predicates.get(s);
                            if (predicate instanceof PredicateComponent && ((PredicateComponent) predicate).definition != null) {
                                definition = ((PredicateComponent) predicate).definition;
                                break;
                            } else if (predicate != null && predicate.candidates != null) {
                                for (BlockInfo blockInfo : predicate.candidates.get()) {
                                    candidates.add(blockInfo.getBlockState());
                                }
                            }
                        }
                        if (!candidates.isEmpty()) {
                            definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "i_renderer"));
                            definition.baseRenderer = new CycleBlockStateRenderer(candidates.toArray(new IBlockState[0]));
                        }
                    }
                    if (definition != null) {
                        tileEntity.setDefinition(definition);
                    }
                    tileEntity.isFormed = isFormed;
                    tileEntity.setWorld(world);
                    tileEntity.validate();
                    posSet.add(pos);
                    tiles.add(tileEntity);
                }
            }
        }
        sceneWidget.setRenderedCore(posSet, null);
    }

    private void savePattern() {
        updateScene();
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
