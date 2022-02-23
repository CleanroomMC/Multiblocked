package io.github.cleanroommc.multiblocked.api.pattern;

import crafttweaker.annotations.ZenRegister;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import io.github.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import io.github.cleanroommc.multiblocked.api.tile.part.PartTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.impl.CycleBlockStateRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import stanhebben.zenscript.annotations.ZenClass;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@ZenClass("mods.multiblocked.pattern.BlockPattern")
@ZenRegister
public class BlockPattern {

    static EnumFacing[] FACINGS = {EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.UP, EnumFacing.DOWN};
    public final int[][] aisleRepetitions;
    public final RelativeDirection[] structureDir;
    protected final TraceabilityPredicate[][][] blockMatches; //[z][y][x]
    protected final int fingerLength; //z size
    protected final int thumbLength; //y size
    protected final int palmLength; //x size

    // x, y, z, minZ, maxZ
    private int[] centerOffset = null;

    public BlockPattern(TraceabilityPredicate[][][] predicatesIn, RelativeDirection[] structureDir, int[][] aisleRepetitions) {
        this.blockMatches = predicatesIn;
        this.fingerLength = predicatesIn.length;
        this.structureDir = structureDir;
        this.aisleRepetitions = aisleRepetitions;

        if (this.fingerLength > 0) {
            this.thumbLength = predicatesIn[0].length;

            if (this.thumbLength > 0) {
                this.palmLength = predicatesIn[0][0].length;
            } else {
                this.palmLength = 0;
            }
        } else {
            this.thumbLength = 0;
            this.palmLength = 0;
        }

        initializeCenterOffsets();
    }

    private void initializeCenterOffsets() {
        loop:
        for (int x = 0; x < this.palmLength; x++) {
            for (int y = 0; y < this.thumbLength; y++) {
                for (int z = 0, minZ = 0, maxZ = 0; z < this.fingerLength; minZ += aisleRepetitions[z][0], maxZ += aisleRepetitions[z][1], z++) {
                    TraceabilityPredicate predicate = this.blockMatches[z][y][x];
                    if (predicate.isCenter) {
                        centerOffset = new int[]{x, y, z, minZ, maxZ};
                        break loop;
                    }
                }
            }
        }
        if (centerOffset == null) {
            throw new IllegalArgumentException("Didn't found center predicate");
        }
    }

    public boolean checkPatternAt(MultiblockState worldState, boolean savePredicate) {
        boolean findFirstAisle = false;
        int minZ = -centerOffset[4];
        worldState.clean();
        PatternMatchContext matchContext = worldState.matchContext;
        Map<SimplePredicate, Integer> globalCount = worldState.globalCount;
        Map<SimplePredicate, Integer> layerCount = worldState.layerCount;
        if (worldState.getController() == null) {
            worldState.setError(new PatternStringError("no controller found"));
            return false;
        }
        BlockPos centerPos = worldState.getController().getPos();
        EnumFacing facing = worldState.getController().getFrontFacing().getOpposite();
        Set<MultiblockCapability<?>> inputCapabilities = worldState.getController().getDefinition().recipeMap.inputCapabilities;
        Set<MultiblockCapability<?>> outputCapabilities = worldState.getController().getDefinition().recipeMap.outputCapabilities;
        //Checking aisles
        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
            //Checking repeatable slices
            loop:
            for (r = 0; (findFirstAisle ? r < aisleRepetitions[c][1] : z <= -centerOffset[3]); r++) {
                //Checking single slice
                layerCount.clear();

                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
                        worldState.setError(null);
                        TraceabilityPredicate predicate = this.blockMatches[c][b][a];
                        BlockPos pos = setActualRelativeOffset(x, y, z, facing).add(centerPos.getX(), centerPos.getY(), centerPos.getZ());
                        worldState.update(pos, predicate);
                        if (!predicate.isAny()) {
                            worldState.addPosCache(pos);
                            if (savePredicate) {
                                worldState.getMatchContext().getOrCreate("predicates", (Supplier<HashMap<BlockPos, TraceabilityPredicate>>) HashMap::new).put(pos, predicate);
                            }
                        }
                        boolean canPartShared = true;
                        TileEntity tileEntity = worldState.getTileEntity();
                        if (tileEntity instanceof PartTileEntity) { // add detected parts
                            if (!predicate.isAny()) {
                                PartTileEntity<?> partTileEntity = (PartTileEntity<?>) tileEntity;
                                if (partTileEntity.isFormed() && !partTileEntity.canShared()) { // check part can be shared
                                    canPartShared = false;
                                    worldState.setError(new PatternStringError("multiblocked.pattern.error.share"));
                                } else {
                                    worldState.getMatchContext()
                                            .getOrCreate("parts", LongOpenHashSet::new)
                                            .add(worldState.getPos().toLong());
                                }
                            }
                        }
                        if (!predicate.test(worldState) || !canPartShared) { // matching failed
                            if (findFirstAisle) {
                                if (r < aisleRepetitions[c][0]) {//retreat to see if the first aisle can start later
                                    r = c = 0;
                                    z = minZ++;
                                    matchContext.reset();
                                    findFirstAisle = false;
                                }
                            } else {
                                z++;//continue searching for the first aisle
                            }
                            continue loop;
                        }
                        if (tileEntity != null && !predicate.isAny()) {
                            Map<Long, EnumMap<IO, Set<MultiblockCapability<?>>>> capabilities = worldState.getMatchContext().getOrCreate("capabilities", Long2ObjectOpenHashMap::new);
                            if (!capabilities.containsKey(worldState.getPos().toLong())) {
                                // if predicate has no specific capability requirements. we will check abilities of every blocks
                                Set<MultiblockCapability<?>> bothFound = new HashSet<>();
                                for (MultiblockCapability<?> capability : inputCapabilities) { // IN
                                    if (outputCapabilities.contains(capability) && capability.isBlockHasCapability(IO.BOTH, tileEntity)) {
                                        bothFound.add(capability);
                                        capabilities.computeIfAbsent(worldState.getPos().toLong(), l-> new EnumMap<>(IO.class))
                                                .computeIfAbsent(IO.BOTH, xx->new HashSet<>())
                                                .add(capability);
                                    } else if (capability.isBlockHasCapability(IO.IN, tileEntity)) {
                                        capabilities.computeIfAbsent(worldState.getPos().toLong(), l-> new EnumMap<>(IO.class))
                                                .computeIfAbsent(IO.IN, xx->new HashSet<>())
                                                .add(capability);
                                    }
                                }
                                for (MultiblockCapability<?> capability : outputCapabilities) { // OUT
                                    if (!bothFound.contains(capability) && capability.isBlockHasCapability(IO.OUT, tileEntity)) {
                                        capabilities.computeIfAbsent(worldState.getPos().toLong(), l-> new EnumMap<>(IO.class))
                                                .computeIfAbsent(IO.OUT, xx->new HashSet<>())
                                                .add(capability);
                                    }
                                }
                            }
                        }
                    }
                }
                findFirstAisle = true;
                z++;

                //Check layer-local matcher predicate
                for (Map.Entry<SimplePredicate, Integer> entry : layerCount.entrySet()) {
                    if (entry.getValue() < entry.getKey().minLayerCount) {
                        worldState.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 3));
                        return false;
                    }
                }
            }
            //Repetitions out of range
            if (r < aisleRepetitions[c][0] || !worldState.isFormed()) {
                if (worldState.isFormed()) {
                    worldState.setError(new PatternError());
                }
                return false;
            }
        }

        //Check count matches amount
        for (Map.Entry<SimplePredicate, Integer> entry : globalCount.entrySet()) {
            if (entry.getValue() < entry.getKey().minGlobalCount) {
                worldState.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 1));
                return false;
            }
        }

        worldState.setError(null);
        return true;
    }

    public void autoBuild(EntityPlayer player, MultiblockState worldState) {
        World world = player.world;
        int minZ = -centerOffset[4];
        worldState.clean();
        BlockPos centerPos = worldState.getController().getPos();
        EnumFacing facing = worldState.getController().getFrontFacing().getOpposite();
        Map<SimplePredicate, BlockInfo[]> cacheInfos = new HashMap<>();
        Map<SimplePredicate, Integer> cacheGlobal = worldState.globalCount;
        Map<BlockPos, Object> blocks = new HashMap<>();
        blocks.put(centerPos, worldState.getController());
        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
            for (r = 0; r < aisleRepetitions[c][0]; r++) {
                Map<SimplePredicate, Integer> cacheLayer = new HashMap<>();
                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
                        TraceabilityPredicate predicate = this.blockMatches[c][b][a];
                        BlockPos pos = setActualRelativeOffset(x, y, z, facing).add(centerPos.getX(), centerPos.getY(), centerPos.getZ());
                        worldState.update(pos, predicate);
                        if (!world.isAirBlock(pos)) {
                            blocks.put(pos, world.getBlockState(pos));
                            for (SimplePredicate limit : predicate.limited) {
                                limit.testLimited(worldState);
                            }
                        } else {
                            boolean find = false;
                            BlockInfo[] infos = new BlockInfo[0];
                            for (SimplePredicate limit : predicate.limited) {
                                if (limit.minLayerCount > 0) {
                                    if (!cacheLayer.containsKey(limit)) {
                                        cacheLayer.put(limit, 1);
                                    } else if (cacheLayer.get(limit) < limit.minLayerCount && (limit.maxLayerCount == -1 || cacheLayer.get(limit) < limit.maxLayerCount)) {
                                        cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                                if (!cacheInfos.containsKey(limit)) {
                                    cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                }
                                infos = cacheInfos.get(limit);
                                find = true;
                                break;
                            }
                            if (!find) {
                                for (SimplePredicate limit : predicate.limited) {
                                    if (limit.minGlobalCount > 0) {
                                        if (!cacheGlobal.containsKey(limit)) {
                                            cacheGlobal.put(limit, 1);
                                        } else if (cacheGlobal.get(limit) < limit.minGlobalCount && (limit.maxGlobalCount == -1 || cacheGlobal.get(limit) < limit.maxGlobalCount)) {
                                            cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        continue;
                                    }
                                    if (!cacheInfos.containsKey(limit)) {
                                        cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                    }
                                    infos = cacheInfos.get(limit);
                                    find = true;
                                    break;
                                }
                            }
                            if (!find) { // no limited
                                for (SimplePredicate limit : predicate.limited) {
                                    if (limit.maxLayerCount != -1 && cacheLayer.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxLayerCount)
                                        continue;
                                    if (limit.maxGlobalCount != -1 && cacheGlobal.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxGlobalCount)
                                        continue;
                                    if (!cacheInfos.containsKey(limit)) {
                                        cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                    }
                                    if (cacheLayer.containsKey(limit)) {
                                        cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                    } else {
                                        cacheLayer.put(limit, 1);
                                    }
                                    if (cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    } else {
                                        cacheGlobal.put(limit, 1);
                                    }
                                    infos = ArrayUtils.addAll(infos, cacheInfos.get(limit));
                                }
                                for (SimplePredicate common : predicate.common) {
                                    if (!cacheInfos.containsKey(common)) {
                                        cacheInfos.put(common, common.candidates == null ? null : common.candidates.get());
                                    }
                                    infos = ArrayUtils.addAll(infos, cacheInfos.get(common));
                                }
                            }

                            List<ItemStack> candidates = new ArrayList<>();
                            for (BlockInfo info : infos) {
                                if (info.getBlockState().getBlock() != Blocks.AIR) {
                                    IBlockState blockState = info.getBlockState();
                                    if (blockState.getBlock() instanceof BlockComponent && ((BlockComponent) blockState.getBlock()).definition.baseRenderer instanceof CycleBlockStateRenderer) {
                                        CycleBlockStateRenderer renderer = (CycleBlockStateRenderer) ((BlockComponent) blockState.getBlock()).definition.baseRenderer;
                                        for (IBlockState state : renderer.states) {
                                            candidates.add(new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, blockState.getBlock().damageDropped(state)));
                                        }
                                    } else {
                                        candidates.add(new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1, blockState.getBlock().damageDropped(blockState)));
                                    }
                                }
                            }

                            // check inventory
                            ItemStack found = null;
                            if (!player.isCreative()) {
                                for (ItemStack itemStack : player.inventory.mainInventory) {
                                    if (candidates.stream().anyMatch(candidate -> candidate.isItemEqual(itemStack)) && !itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock) {
                                        found = itemStack.copy();
                                        itemStack.setCount(itemStack.getCount() - 1);
                                        break;
                                    }
                                }
                                if (found == null) continue;
                            } else {
                                for (ItemStack candidate : candidates) {
                                    found = candidate.copy();
                                    if (!found.isEmpty() && found.getItem() instanceof ItemBlock) {
                                        break;
                                    }
                                    found = null;
                                }
                                if (found == null) continue;
                            }
                            ItemBlock itemBlock = (ItemBlock) found.getItem();
                            IBlockState state = itemBlock.getBlock().getStateFromMeta(itemBlock.getMetadata(found.getMetadata()));
                            blocks.put(pos, state);
                            world.setBlockState(pos, state);
                            TileEntity tileEntity = world.getTileEntity(pos);
                            if (tileEntity instanceof ComponentTileEntity) {
                                blocks.put(pos, tileEntity);
                            }
                        }
                    }
                }
                z++;
            }
        }
        EnumFacing[] facings = ArrayUtils.addAll(new EnumFacing[]{facing}, FACINGS); // follow controller first
        blocks.forEach((pos, block) -> { // adjust facing
            if (block instanceof ComponentTileEntity) {
                ComponentTileEntity<?> componentTileEntity = (ComponentTileEntity<?>) block;
                boolean find = false;
                for (EnumFacing enumFacing : facings) {
                    if (componentTileEntity.isValidFrontFacing(enumFacing)) {
                        if (!blocks.containsKey(pos.offset(enumFacing))) {
                            componentTileEntity.setFrontFacing(enumFacing);
                            find = true;
                            break;
                        }
                    }
                }
                if (!find) {
                    for (EnumFacing enumFacing : FACINGS) {
                        if (world.isAirBlock(pos.offset(enumFacing)) && componentTileEntity.isValidFrontFacing(enumFacing)) {
                            componentTileEntity.setFrontFacing(enumFacing);
                            break;
                        }
                    }
                }
            }
        });
    }

    public BlockInfo[][][] getPreview(int[] repetition) {
        Map<SimplePredicate, BlockInfo[]> cacheInfos = new HashMap<>();
        Map<SimplePredicate, Integer> cacheGlobal = new HashMap<>();
        Map<BlockPos, BlockInfo> blocks = new HashMap<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int l = 0, x = 0; l < this.fingerLength; l++) {
            for (int r = 0; r < repetition[l]; r++) {
                //Checking single slice
                Map<SimplePredicate, Integer> cacheLayer = new HashMap<>();
                for (int y = 0; y < this.thumbLength; y++) {
                    for (int z = 0; z < this.palmLength; z++) {
                        TraceabilityPredicate predicate = this.blockMatches[l][y][z];
                        boolean find = false;
                        BlockInfo[] infos = null;
                        for (SimplePredicate limit : predicate.limited) { // check layer and previewCount
                            if (limit.minLayerCount > 0) {
                                if (!cacheLayer.containsKey(limit)) {
                                    cacheLayer.put(limit, 1);
                                } else if (cacheLayer.get(limit) < limit.minLayerCount) {
                                    cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                } else {
                                    continue;
                                }
                                if (cacheGlobal.getOrDefault(limit, 0) < limit.previewCount) {
                                    if (!cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, 1);
                                    } else if (cacheGlobal.get(limit) < limit.previewCount) {
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    } else {
                                        continue;
                                    }
                                }
                            } else {
                                continue;
                            }
                            if (!cacheInfos.containsKey(limit)) {
                                cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                            }
                            infos = cacheInfos.get(limit);
                            find = true;
                            break;
                        }
                        if (!find) { // check global and previewCount
                            for (SimplePredicate limit : predicate.limited) {
                                if (limit.minGlobalCount == -1 && limit.previewCount == -1) continue;
                                if (cacheGlobal.getOrDefault(limit, 0) < limit.previewCount) {
                                    if (!cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, 1);
                                    } else if (cacheGlobal.get(limit) < limit.previewCount) {
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    } else {
                                        continue;
                                    }
                                } else if (limit.minGlobalCount > 0) {
                                    if (!cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, 1);
                                    } else if (cacheGlobal.get(limit) < limit.minGlobalCount) {
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                                if (!cacheInfos.containsKey(limit)) {
                                    cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                }
                                infos = cacheInfos.get(limit);
                                find = true;
                                break;
                            }
                        }
                        if (!find) { // check common with previewCount
                            for (SimplePredicate common : predicate.common) {
                                if (common.previewCount > 0) {
                                    if (!cacheGlobal.containsKey(common)) {
                                        cacheGlobal.put(common, 1);
                                    } else if (cacheGlobal.get(common) < common.previewCount) {
                                        cacheGlobal.put(common, cacheGlobal.get(common) + 1);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                                if (!cacheInfos.containsKey(common)) {
                                    cacheInfos.put(common, common.candidates == null ? null : common.candidates.get());
                                }
                                infos = cacheInfos.get(common);
                                find = true;
                                break;
                            }
                        }
                        if (!find) { // check without previewCount
                            for (SimplePredicate common : predicate.common) {
                                if (common.previewCount == -1) {
                                    if (!cacheInfos.containsKey(common)) {
                                        cacheInfos.put(common, common.candidates == null ? null : common.candidates.get());
                                    }
                                    infos = cacheInfos.get(common);
                                    find = true;
                                    break;
                                }
                            }
                        }
                        if (!find) { // check max
                            for (SimplePredicate limit : predicate.limited) {
                                if (limit.previewCount != -1) {
                                    continue;
                                } else if (limit.maxGlobalCount != -1 || limit.maxLayerCount != -1) {
                                    if (cacheGlobal.getOrDefault(limit, 0) < limit.maxGlobalCount) {
                                        if (!cacheGlobal.containsKey(limit)) {
                                            cacheGlobal.put(limit, 1);
                                        } else {
                                            cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                        }
                                    } else if (cacheLayer.getOrDefault(limit, 0) < limit.maxLayerCount) {
                                        if (!cacheLayer.containsKey(limit)) {
                                            cacheLayer.put(limit, 1);
                                        } else {
                                            cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                        }
                                    } else {
                                        continue;
                                    }
                                }

                                if (!cacheInfos.containsKey(limit)) {
                                    cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                }
                                infos = cacheInfos.get(limit);
                                break;
                            }
                        }
                        BlockInfo info = infos == null || infos.length == 0 ? BlockInfo.EMPTY : infos[0];
                        BlockPos pos = setActualRelativeOffset(z, y, x, EnumFacing.NORTH);

                        if (info.getBlockState().getBlock() instanceof BlockComponent) {
                            TileEntity tileEntity = info.getBlockState().getBlock().createTileEntity(null, info.getBlockState());
                            info = new BlockInfo(info.getBlockState(), tileEntity);
                        }
                        blocks.put(pos, info);
                        minX = Math.min(pos.getX(), minX);
                        minY = Math.min(pos.getY(), minY);
                        minZ = Math.min(pos.getZ(), minZ);
                        maxX = Math.max(pos.getX(), maxX);
                        maxY = Math.max(pos.getY(), maxY);
                        maxZ = Math.max(pos.getZ(), maxZ);
                    }
                }
                x++;
            }
        }
        BlockInfo[][][] result = (BlockInfo[][][]) Array.newInstance(BlockInfo.class, maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
        int finalMinX = minX;
        int finalMinY = minY;
        int finalMinZ = minZ;
        blocks.forEach((pos, info) -> {
            if (info.getTileEntity() instanceof ComponentTileEntity<?>) {
                ComponentTileEntity<?> componentTileEntity = (ComponentTileEntity<?>) info.getTileEntity();
                boolean find = false;
                for (EnumFacing enumFacing : FACINGS) {
                    if (componentTileEntity.isValidFrontFacing(enumFacing)) {
                        if (!blocks.containsKey(pos.offset(enumFacing))) {
                            componentTileEntity.setFrontFacing(enumFacing);
                            find = true;
                            break;
                        }
                    }
                }
                if (!find) {
                    for (EnumFacing enumFacing : FACINGS) {
                        BlockInfo blockInfo = blocks.get(pos.offset(enumFacing));
                        if (blockInfo != null && blockInfo.getBlockState().getBlock() == Blocks.AIR && componentTileEntity.isValidFrontFacing(enumFacing)) {
                            componentTileEntity.setFrontFacing(enumFacing);
                            break;
                        }
                    }
                }
            }
            result[pos.getX() - finalMinX][pos.getY() - finalMinY][pos.getZ() - finalMinZ] = info;
        });
        return result;
    }

    private BlockPos setActualRelativeOffset(int x, int y, int z, EnumFacing facing) {
        int[] c0 = new int[]{x, y, z}, c1 = new int[3];
        for (int i = 0; i < 3; i++) {
            switch (structureDir[i].getActualFacing(facing)) {
                case UP:
                    c1[1] = c0[i];
                    break;
                case DOWN:
                    c1[1] = -c0[i];
                    break;
                case WEST:
                    c1[0] = -c0[i];
                    break;
                case EAST:
                    c1[0] = c0[i];
                    break;
                case NORTH:
                    c1[2] = -c0[i];
                    break;
                case SOUTH:
                    c1[2] = c0[i];
                    break;
            }
        }
        return new BlockPos(c1[0], c1[1], c1[2]);
    }
}
