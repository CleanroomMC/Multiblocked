package com.cleanroommc.multiblocked.api.gui.widget.imp.controller.structure;

import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import com.cleanroommc.multiblocked.api.gui.texture.IGuiTexture;
import com.cleanroommc.multiblocked.api.gui.texture.ResourceTexture;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.pattern.MultiblockShapeInfo;
import com.cleanroommc.multiblocked.api.pattern.MultiblockState;
import com.cleanroommc.multiblocked.api.pattern.TraceabilityPredicate;
import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import com.cleanroommc.multiblocked.client.util.TrackedDummyWorld;
import com.cleanroommc.multiblocked.util.CycleItemStackHandler;
import com.cleanroommc.multiblocked.util.ItemStackKey;
import com.cleanroommc.multiblocked.api.gui.util.ClickData;
import com.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ButtonWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.ImageWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SceneWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SlotWidget;
import com.cleanroommc.multiblocked.api.gui.widget.imp.SwitchWidget;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class PatternWidget extends WidgetGroup {
    private static final TrackedDummyWorld world = new TrackedDummyWorld();
    private static BlockPos LAST_POS = new BlockPos(50, 50, 50);

    private static final ResourceTexture PAGE = new ResourceTexture("multiblocked:textures/gui/structure_page.png");
    private static final Map<ControllerDefinition, PatternWidget> CACHE = new HashMap<>();
    private static final IGuiTexture BACKGROUND = PAGE.getSubTexture(0, 0, 176 / 256.0, 1);
    private static final IGuiTexture RIGHT_BUTTON = PAGE.getSubTexture(176 / 256.0, 0, 5 / 256.0, 17 / 256.0);
    private static final IGuiTexture RIGHT_BUTTON_HOVER = PAGE.getSubTexture(181 / 256.0, 0, 5 / 256.0, 17 / 256.0);
    private static final IGuiTexture LEFT_BUTTON = PAGE.getSubTexture(176 / 256.0, 17 / 256.0, 5 / 256.0, 17 / 256.0);
    private static final IGuiTexture LEFT_BUTTON_HOVER = PAGE.getSubTexture(181 / 256.0, 17 / 256.0, 5 / 256.0, 17 / 256.0);
    private static final IGuiTexture TIPS = PAGE.getSubTexture(176 / 256.0, 32 / 256.0, 20 / 256.0, 20 / 256.0);
    private static final IGuiTexture FORMED_BUTTON = PAGE.getSubTexture(176 / 256.0, 184 / 256.0, 16 / 256.0, 16 / 256.0);
    private static final IGuiTexture FORMED_BUTTON_PRESSED = PAGE.getSubTexture(176 / 256.0, 200 / 256.0, 16 / 256.0, 16 / 256.0);

    private final SceneWidget sceneWidget;
    private final ButtonWidget leftButton;
    private final ButtonWidget rightButton;
    private final SwitchWidget switchWidget;
    public final ControllerDefinition controllerDefinition;
    public final MBPattern[] patterns;
    public final List<ItemStack> allItemStackInputs;
    private final List<SimplePredicate> predicates;
    private int index;
    private SlotWidget[] slotWidgets;
    private SlotWidget[] candidates;

    private PatternWidget(ControllerDefinition controllerDefinition) {
        super(0, 0, 176, 256);
        setClientSideWidget();
        allItemStackInputs = new ArrayList<>();
        predicates = new ArrayList<>();
        addWidget(sceneWidget = new SceneWidget(6, 51, 164, 143, world)
                .useCacheBuffer()
                .setOnSelected(this::onPosSelected)
                .setRenderFacing(false)
                .setRenderFacing(false));
        this.controllerDefinition = controllerDefinition;
        HashSet<ItemStackKey> drops = new HashSet<>();
        drops.add(new ItemStackKey(this.controllerDefinition.getStackForm()));
        this.patterns = controllerDefinition.getDesigns(world).stream()
                .map(it -> initializePattern(it, drops))
                .toArray(MBPattern[]::new);
        drops.forEach(it -> allItemStackInputs.add(it.getItemStack()));
        addWidget(leftButton = new ButtonWidget(7, 198, 5, 17, LEFT_BUTTON, (x)->reset(index - 1)).setHoverTexture(LEFT_BUTTON_HOVER));
        addWidget(rightButton = new ButtonWidget(164, 198, 5, 17, RIGHT_BUTTON, (x)->reset(index + 1)).setHoverTexture(RIGHT_BUTTON_HOVER));
        addWidget(switchWidget = (SwitchWidget) new SwitchWidget(150, 173, 16, 16, this::onFormedSwitch)
                .setTexture(FORMED_BUTTON, FORMED_BUTTON_PRESSED)
                .setHoverTooltip("multiblocked.structure_page.switch"));
        addWidget(new ImageWidget(149, 27, 20, 20, TIPS).setHoverTooltip(controllerDefinition.getDescription()));
        addWidget(new ImageWidget(7, 7, 162, 16,
                new TextTexture(controllerDefinition.location.getPath() + ".name", -1)
                        .setType(TextTexture.TextType.ROLL)
                        .setWidth(162)
                        .setDropShadow(true)));
        addWidget(new ImageWidget(17, 196, 142, 18,
                new TextTexture("multiblocked.structure_page.candidates", 0xff000000)));
    }

    public static PatternWidget getPatternWidget(ControllerDefinition controllerDefinition) {
        PatternWidget patternWidget = CACHE.computeIfAbsent(controllerDefinition, PatternWidget::new);
        patternWidget.reset(0);
        return patternWidget;
    }

    private void reset(int index) {
        if (index >= patterns.length || index < 0) return;
        this.index = index;
        MBPattern pattern = patterns[index];
        sceneWidget.setRenderedCore(pattern.blockMap.keySet(), null);
        if (slotWidgets != null) {
            for (SlotWidget slotWidget : slotWidgets) {
                removeWidget(slotWidget);
            }
        }
        slotWidgets = new SlotWidget[Math.min(pattern.parts.size(), 18)];
        IItemHandler itemHandler = new ItemStackHandler(pattern.parts);
        for (int i = 0; i < slotWidgets.length; i++) {
            slotWidgets[i] = new SlotWidget(itemHandler, i, 7 + (i % 9) * 18, 214 + (i / 9) * 18, false, false);
            addWidget(slotWidgets[i]);
        }
        leftButton.setVisible(index > 0);
        rightButton.setVisible(index < patterns.length - 1);
        updateClientSlots();
        switchWidget.setPressed(pattern.controllerBase.isFormed());
    }

    private void onFormedSwitch(ClickData clickData, Boolean isPressed) {
        MBPattern pattern = patterns[index];
        ControllerTileEntity controllerBase = pattern.controllerBase;
        if (isPressed) {
            controllerBase.state = new MultiblockState(world, controllerBase.getPos());
            controllerBase.getPattern().checkPatternAt(controllerBase.state, true);
            controllerBase.onStructureFormed();
            if (controllerBase.isFormed() && controllerBase.getDefinition().disableOthersRendering) {
                long controllerLong = controllerBase.getPos().toLong();
                Set<BlockPos> modelDisabled = new HashSet<>();
                for (long blockPos : controllerBase.state.cache) {
                    if (controllerLong == blockPos) continue;
                    modelDisabled.add(BlockPos.fromLong(blockPos));
                }
                world.setRenderFilter(pos -> !modelDisabled.contains(pos));
            }
        } else {
            if (controllerBase.getDefinition().disableOthersRendering) {
                world.setRenderFilter(null);
            }
            controllerBase.state = null;
            controllerBase.onStructureInvalid();
        }
        sceneWidget.needCompileCache();
    }

    private void onPosSelected(BlockPos pos, EnumFacing facing) {
        TraceabilityPredicate predicate = patterns[index].predicateMap.get(pos);
        if (predicate != null) {
            predicates.clear();
            predicates.addAll(predicate.common);
            predicates.addAll(predicate.limited);
            predicates.removeIf(p -> p.candidates == null);
            if (candidates != null) {
                for (SlotWidget candidate : candidates) {
                    removeWidget(candidate);
                }
            }
            List<List<ItemStack>> candidateStacks = new ArrayList<>();
            List<List<String>> predicateTips = new ArrayList<>();
            for (SimplePredicate simplePredicate : predicates) {
                List<ItemStack> itemStacks = simplePredicate.getCandidates();
                if (!itemStacks.isEmpty()) {
                    candidateStacks.add(itemStacks);
                    predicateTips.add(simplePredicate.getToolTips(predicate));
                }
            }
            candidates = new SlotWidget[candidateStacks.size()];
            CycleItemStackHandler itemHandler =
                    new CycleItemStackHandler(candidateStacks);
            for (int i = 0; i < candidateStacks.size(); i++) {
                int finalI = i;
                candidates[i] = new SlotWidget(itemHandler, i, 8 + (i / 6) * 18, 55 + (i % 6) * 18, false, false)
                        .setBackgroundTexture(new ColorRectTexture(0x4fffffff))
                        .setOnAddedTooltips((slot, list) -> list.addAll(predicateTips.get(finalI)));
                addWidget(candidates[i]);
            }
            updateClientSlots();
        }
    }

    private void updateClientSlots() {
        if (gui == null || gui.getModularUIGui() == null) return;
        gui.getModularUIGui().inventorySlots.inventorySlots.clear();
        for (SlotWidget slotWidget : getNativeWidgets()) {
            gui.getModularUIGui().inventorySlots.inventorySlots.add(slotWidget.getHandle());
        }
    }

    public static BlockPos locateNextRegion(int range) {
        BlockPos pos = LAST_POS;
        LAST_POS = LAST_POS.add(range, 0, range);
        return pos;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        BACKGROUND.draw(mouseX, mouseY, x, y, width, height);
        super.drawInBackground(mouseX, mouseY, partialTicks);
    }

    private MBPattern initializePattern(MultiblockShapeInfo shapeInfo, HashSet<ItemStackKey> blockDrops) {
        Map<BlockPos, BlockInfo> blockMap = new HashMap<>();
        ControllerTileEntity controllerBase = null;
        BlockPos multiPos = locateNextRegion(500);

        BlockInfo[][][] blocks = shapeInfo.getBlocks();
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int z = 0; z < column.length; z++) {
                    TileEntity tileEntity = column[z].getTileEntity();
                    IBlockState blockState = column[z].getBlockState();
                    if (tileEntity == null && blockState.getBlock().hasTileEntity(blockState)) {
                        tileEntity = blockState.getBlock().createTileEntity(world, blockState);
                    }
                    if (tileEntity instanceof ControllerTileEntity) {
                        controllerBase = (ControllerTileEntity) tileEntity;
                    }
                    blockMap.put(multiPos.add(x, y, z), new BlockInfo(blockState, tileEntity));
                }
            }
        }

        world.addBlocks(blockMap);

        Map<ItemStackKey, PartInfo> parts = gatherBlockDrops(blockMap);
        blockDrops.addAll(parts.keySet());

        Map<BlockPos, TraceabilityPredicate> predicateMap = new HashMap<>();
        if (controllerBase != null) {
            controllerBase.state = new MultiblockState(world, controllerBase.getPos());
            controllerBase.getPattern().checkPatternAt(controllerBase.state, true);
            controllerBase.onStructureFormed();
            if (controllerBase.isFormed() && controllerBase.getDefinition().disableOthersRendering) {
                long controllerLong = controllerBase.getPos().toLong();
                Set<BlockPos> modelDisabled = new HashSet<>();
                for (long blockPos : controllerBase.state.cache) {
                    if (controllerLong == blockPos) continue;
                    modelDisabled.add(BlockPos.fromLong(blockPos));
                }
                world.setRenderFilter(pos -> !modelDisabled.contains(pos));
            }
            predicateMap = controllerBase.state.getMatchContext().get("predicates");
        }
        return new MBPattern(blockMap, parts.values().stream().sorted((one, two) -> {
            if (one.isController) return -1;
            if (two.isController) return +1;
            if (one.isTile && !two.isTile) return -1;
            if (two.isTile && !one.isTile) return +1;
            if (one.blockId != two.blockId) return two.blockId - one.blockId;
            return two.amount - one.amount;
        }).map(PartInfo::getItemStack).toArray(ItemStack[]::new), predicateMap, controllerBase);
    }

    private Map<ItemStackKey, PartInfo> gatherBlockDrops(Map<BlockPos, BlockInfo> blocks) {
        Map<ItemStackKey, PartInfo> partsMap = new HashMap<>();
        for (Map.Entry<BlockPos, BlockInfo> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            IBlockState blockState = ((World) PatternWidget.world).getBlockState(pos);
            ItemStack itemStack = blockState.getBlock().getPickBlock(blockState, new RayTraceResult(
                    new Vec3d(0.5, 1, 0.5).add(pos.getX(), pos.getY(), pos.getZ()),
                    EnumFacing.UP,
                    pos), PatternWidget.world, pos, Minecraft.getMinecraft().player);

            ItemStackKey itemStackKey = new ItemStackKey(itemStack);
            PartInfo partInfo = partsMap.get(itemStackKey);
            if (partInfo == null) {
                partInfo = new PartInfo(itemStackKey, entry.getValue());
                partsMap.put(itemStackKey, partInfo);
            }
            ++partInfo.amount;
        }
        return partsMap;
    }

    private static class PartInfo {
        final ItemStackKey itemStackKey;
        boolean isController = false;
        boolean isTile = false;
        final int blockId;
        int amount = 0;

        PartInfo(final ItemStackKey itemStackKey, final BlockInfo blockInfo) {
            this.itemStackKey = itemStackKey;
            this.blockId = Block.getIdFromBlock(blockInfo.getBlockState().getBlock());
            TileEntity tileEntity = blockInfo.getTileEntity();
            if (tileEntity != null) {
                this.isTile = true;
                if (tileEntity instanceof ControllerTileEntity)
                    this.isController = true;
            }
        }

        ItemStack getItemStack() {
            ItemStack result = this.itemStackKey.getItemStack();
            result.setCount(this.amount);
            return result;
        }
    }

    private static class MBPattern {
        final NonNullList<ItemStack> parts;
        final Map<BlockPos, TraceabilityPredicate> predicateMap;
        final Map<BlockPos, BlockInfo> blockMap;
        final ControllerTileEntity controllerBase;

        public MBPattern(Map<BlockPos, BlockInfo> blockMap, ItemStack[] parts, Map<BlockPos, TraceabilityPredicate> predicateMap, ControllerTileEntity controllerBase) {
            this.parts = NonNullList.from(ItemStack.EMPTY, parts);
            this.blockMap = blockMap;
            this.predicateMap = predicateMap;
            this.controllerBase = controllerBase;
        }
    }
}
