package com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.components;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.definition.ComponentDefinition;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.GuiTextureGroup;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceBorderTexture;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextBoxWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.TextFieldWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.RecipeMapBuilderWidget;
import com.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.registry.MbdComponents;
import com.cleanroommc.multiblocked.api.tile.DummyComponentTileEntity;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
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
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.dialogs.JsonBlockPatternWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.tab.TabButton;
import com.cleanroommc.multiblocked.api.pattern.JsonBlockPattern;
import com.google.gson.JsonObject;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
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

public class ControllerWidget extends ComponentWidget<ControllerDefinition>{
    protected JsonBlockPattern pattern;
    protected final WidgetGroup S3;
    protected final WidgetGroup S4;
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
        S1.addWidget(createBoolSwitch(x + 100, 90, "consumeCatalyst", "consume Catalyst", definition.consumeCatalyst, r -> definition.consumeCatalyst = r));

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
        handler.setStackInSlot(0, definition.catalyst == null ? ItemStack.EMPTY : definition.catalyst);
        labelWidget.setVisible(definition.catalyst != null);

        S1.addWidget(createBoolSwitch(x + 100, 75, "needCatalyst", "If no catalyst is needed, the structure will try to formed per second.", definition.catalyst != null, r -> {
            definition.catalyst = !r ? null : ItemStack.EMPTY;
            phantomSlotWidget.setVisible(r);
            labelWidget.setVisible(r);
        }));

        tabContainer.addTab(new TabButton(88, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S3"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S3"))
                        .setHoverTooltip("Step 3: structure pattern setup"),
                S3 = new WidgetGroup(0, 0, getSize().width, getSize().height));
        S3.addWidget(new ImageWidget(50, 66, 138, 138, new GuiTextureGroup(new ColorBorderTexture(3, -1), new ColorRectTexture(0xaf444444))));
        S3.addWidget(sceneWidget = new SceneWidget(50, 66, 138, 138, null)
                .useCacheBuffer()
                .setRenderFacing(false)
                .setRenderSelect(false));
        ResourceTexture PAGE = new ResourceTexture("multiblocked:textures/gui/structure_page.png");
        sceneWidget.addWidget(new SwitchWidget(138 - 20, 138 - 20, 16, 16, this::onFormedSwitch)
                .setPressed(isFormed)
                .setTexture(PAGE.getSubTexture(176 / 256.0, 184 / 256.0, 16 / 256.0, 16 / 256.0), PAGE.getSubTexture(176 / 256.0, 200 / 256.0, 16 / 256.0, 16 / 256.0))
                .setHoverTooltip("multiblocked.structure_page.switch"));
        S3.addWidget(new TextBoxWidget(200, 0, 175, Collections.singletonList("")).setFontColor(-1).setShadow(true));
        S3.addWidget(new ButtonWidget(200, 66, 100, 20,
                new GuiTextureGroup(ResourceBorderTexture.BAR, new TextTexture("Pattern Setting", -1).setDropShadow(true)), cd -> {
            new JsonBlockPatternWidget(this, this.pattern.copy(), this::savePattern);
        }).setHoverBorderTexture(1, -1));
        updateScene();

        tabContainer.addTab(new TabButton(111, 26, 20, 20)
                        .setPressedTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0.5, 1, 0.5), new TextTexture("S4"))
                        .setBaseTexture(new ResourceTexture("multiblocked:textures/gui/switch_common.png").getSubTexture(0, 0, 1, 0.5), new TextTexture("S4"))
                        .setHoverTooltip("Step 4: machine recipe map"),
                S4 = new WidgetGroup(0, 0, getSize().width, getSize().height));
        S4.addWidget(new LabelWidget(80, 55, "RecipeMap: "));
        S4.addWidget(new TextFieldWidget(80, 70, 100, 15, true, () -> this.recipeMap, s -> this.recipeMap = s));
        S4.addWidget(new RecipeMapBuilderWidget(this, 188, 50, 150, 170).setOnRecipeMapSelected(recipeMap1 -> this.recipeMap = recipeMap1.name));
    }

    @Override
    protected JsonObject getJsonObj() {
        JsonObject jsonObject = super.getJsonObj();
        jsonObject.add("basePattern", Multiblocked.GSON.toJsonTree(pattern));
        jsonObject.addProperty("recipeMap", this.recipeMap == null ? RecipeMap.EMPTY.name : this.recipeMap);
        if (definition.catalyst == null) {
            jsonObject.add("catalyst", null);
        }
        return jsonObject;
    }

    private void onFormedSwitch(ClickData clickData, boolean isPressed) {
        isFormed = isPressed;
        tiles.forEach(t->t.isFormed = isFormed);
        sceneWidget.needCompileCache();
    }

    @SideOnly(Side.CLIENT)
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
                    world.addBlock(pos, new BlockInfo(MbdComponents.DummyComponentBlock));
                    DummyComponentTileEntity  tileEntity = (DummyComponentTileEntity) world.getTileEntity(pos);
                    ComponentDefinition definition = null;
                    assert tileEntity != null;
                    boolean disableFormed = false;
                    if (this.pattern.symbolMap.containsKey(symbol)) {
                        Set<BlockInfo> candidates = new HashSet<>();
                        for (String s : this.pattern.symbolMap.get(symbol)) {
                            SimplePredicate predicate = this.pattern.predicates.get(s);
                            if (predicate instanceof PredicateComponent && ((PredicateComponent) predicate).definition != null) {
                                definition = ((PredicateComponent) predicate).definition;
                                disableFormed |= predicate.disableRenderFormed;
                                break;
                            } else if (predicate != null && predicate.candidates != null) {
                                candidates.addAll(Arrays.asList(predicate.candidates.get()));
                                disableFormed |= predicate.disableRenderFormed;
                            }
                        }
                        if (candidates.size() == 1) {
                            definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "i_renderer"));
                            definition.baseRenderer = new BlockStateRenderer(candidates.toArray(new BlockInfo[0])[0].getBlockState());
                        } else if (!candidates.isEmpty()) {
                            definition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "i_renderer"));
                            definition.baseRenderer = new CycleBlockStateRenderer(candidates.toArray(new BlockInfo[0]));
                        }
                    }
                    if (definition != null) {
                        tileEntity.setDefinition(definition);
                        if (disableFormed) {
                            definition.formedRenderer = new BlockStateRenderer(Blocks.AIR.getDefaultState());
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
        sceneWidget.setRenderedCore(posSet, null);
    }

    private void savePattern(JsonBlockPattern patternResult) {
        if (patternResult != null) {
            pattern = patternResult;
            pattern.cleanUp();
            updateScene();
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
