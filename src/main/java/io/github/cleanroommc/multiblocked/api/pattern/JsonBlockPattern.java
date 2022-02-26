package io.github.cleanroommc.multiblocked.api.pattern;

import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateBlocks;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.PredicateComponent;
import io.github.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import io.github.cleanroommc.multiblocked.api.pattern.util.RelativeDirection;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JsonBlockPattern {
    public RelativeDirection[] structureDir;
    public String[][] pattern;
    public int[][] aisleRepetitions;
    public Map<String, SimplePredicate> predicates; // 0-any, 1-air, 2-controller
    public Map<Character, List<String>> symbolMap;

    public JsonBlockPattern() {
        predicates = new HashMap<>();
        symbolMap = new HashMap<>();
        structureDir = new RelativeDirection[] {RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT};
        predicates.put("any", SimplePredicate.ANY);
        predicates.put("air", SimplePredicate.AIR);
    }

    public JsonBlockPattern(World world, ResourceLocation location, BlockPos controllerPos, EnumFacing facing, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this();
        pattern = new String[1 + maxX - minX][ 1 + maxY - minY];
        if (facing == EnumFacing.WEST) {
            structureDir = new RelativeDirection[] {RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.BACK};
        } else if (facing == EnumFacing.EAST) {
            structureDir = new RelativeDirection[] {RelativeDirection.RIGHT, RelativeDirection.UP, RelativeDirection.FRONT};
        } else if (facing == EnumFacing.NORTH) {
            structureDir = new RelativeDirection[] {RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.RIGHT};
        } else if (facing == EnumFacing.SOUTH) {
            structureDir = new RelativeDirection[] {RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT};
        }
        aisleRepetitions = new int[pattern.length][2];
        for (int[] aisleRepetition : aisleRepetitions) {
            aisleRepetition[0] = 1;
            aisleRepetition[1] = 1;
        }
        symbolMap.put(' ', Collections.singletonList("any")); // 0-any
        symbolMap.put('-', Collections.singletonList("air")); // 1-air

        predicates.put("controller", new PredicateComponent(location)); // 2-controller
        symbolMap.put('@', Collections.singletonList("controller"));

        Map<Block, Character> map = new HashMap<>();
        map.put(Blocks.AIR, ' ');

        char c = 'A'; // auto

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                StringBuilder builder = new StringBuilder();
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (controllerPos.equals(pos)) {
                        builder.append('@'); // controller
                    } else {
                        Block block = world.getBlockState(pos).getBlock();
                        if (!map.containsKey(block)) {
                            map.put(block, c);
                            String name = Objects.requireNonNull(block.getRegistryName()).toString();
                            predicates.put(name, new PredicateBlocks(block));
                            symbolMap.put(c, Collections.singletonList(name));
                            c++;
                        }
                        builder.append(map.get(block));
                    }
                }
                pattern[x - minX][y - minY] = builder.toString();
            }
        }
    }

    public void changeDir (RelativeDirection charDir, RelativeDirection stringDir, RelativeDirection aisleDir) {
        if (charDir.isSameAxis(stringDir) || stringDir.isSameAxis(aisleDir) || aisleDir.isSameAxis(charDir)) return;
        char[][][] newPattern = new char
                [structureDir[0].isSameAxis(aisleDir) ? pattern[0][0].length() : structureDir[1].isSameAxis(aisleDir) ? pattern[0].length : pattern.length]
                [structureDir[0].isSameAxis(stringDir) ? pattern[0][0].length() : structureDir[1].isSameAxis(stringDir) ? pattern[0].length : pattern.length]
                [structureDir[0].isSameAxis(charDir) ? pattern[0][0].length() : structureDir[1].isSameAxis(charDir) ? pattern[0].length : pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[0].length; j++) {
                for (int k = 0; k < pattern[0][0].length(); k++) {
                    char c = pattern[i][j].charAt(k);
                    int x = 0, y = 0, z = 0;
                    if (structureDir[2].isSameAxis(aisleDir)) {
                        if (structureDir[2] == aisleDir) {
                            x = i;
                        } else {
                            x = pattern.length - i - 1;
                        }
                    } else if (structureDir[2].isSameAxis(stringDir)) {
                        if (structureDir[2] == stringDir) {
                            y = i;
                        } else {
                            y = pattern.length - i - 1;
                        }
                    } else if (structureDir[2].isSameAxis(charDir)) {
                        if (structureDir[2] == charDir) {
                            z = i;
                        } else {
                            z = pattern.length - i - 1;
                        }
                    }

                    if (structureDir[1].isSameAxis(aisleDir)) {
                        if (structureDir[1] == aisleDir) {
                            x = j;
                        } else {
                            x = pattern[0].length - j - 1;
                        }
                    } else if (structureDir[1].isSameAxis(stringDir)) {
                        if (structureDir[1] == stringDir) {
                            y = j;
                        } else {
                            y = pattern[0].length - j - 1;
                        }
                    } else if (structureDir[1].isSameAxis(charDir)) {
                        if (structureDir[1] == charDir) {
                            z = j;
                        } else {
                            z = pattern[0].length - j - 1;
                        }
                    }

                    if (structureDir[0].isSameAxis(aisleDir)) {
                        if (structureDir[0] == aisleDir) {
                            x = k;
                        } else {
                            x = pattern[0][0].length() - k - 1;
                        }
                    } else if (structureDir[0].isSameAxis(stringDir)) {
                        if (structureDir[0] == stringDir) {
                            y = k;
                        } else {
                            y = pattern[0][0].length() - k - 1;
                        }
                    } else if (structureDir[0].isSameAxis(charDir)) {
                        if (structureDir[0] == charDir) {
                            z = k;
                        } else {
                            z = pattern[0][0].length() - k - 1;
                        }
                    }
                    newPattern[x][y][z] = c;
                }
            }
        }

        pattern = new String[newPattern.length][newPattern[0].length];
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[0].length; j++) {
                StringBuilder builder = new StringBuilder();
                for (char c : newPattern[i][j]) {
                    builder.append(c);
                }
                pattern[i][j] = builder.toString();
            }
        }

        aisleRepetitions = new int[pattern.length][2];
        for (int[] aisleRepetition : aisleRepetitions) {
            aisleRepetition[0] = 1;
            aisleRepetition[1] = 1;
        }

        structureDir = new RelativeDirection[]{charDir, stringDir, aisleDir};
    }

    public BlockPattern build() {
        int[] centerOffset = new int[5];
        TraceabilityPredicate[][][] predicate = (TraceabilityPredicate[][][]) Array.newInstance(TraceabilityPredicate.class, this.pattern.length, this.pattern[0].length, this.pattern[0][0].length());
        for (int i = 0, minZ = 0, maxZ = 0; i < this.pattern.length; minZ += aisleRepetitions[i][0], maxZ += aisleRepetitions[i][1], i++) {
            for (int j = 0; j < this.pattern[0].length; j++) {
                for (int k = 0; k < this.pattern[0][0].length(); k++) {
                    List<String> saves = symbolMap.get(this.pattern[i][j].charAt(k));
                    if (saves == null || saves.isEmpty()) {
                        predicate[i][j][k] = Predicates.any();
                    } else {
                        predicate[i][j][k] = new TraceabilityPredicate();
                        for (String p : saves) {
                            predicate[i][j][k] = predicate[i][j][k].or(new TraceabilityPredicate(predicates.get(p)));
                            if (p.equals("controller")) {
                                predicate[i][j][k].setCenter();
                                centerOffset = new int[]{k, j, i, minZ, maxZ};
                            }
                        }
                    }
                }
            }
        }
        return new BlockPattern(predicate, structureDir, aisleRepetitions, centerOffset);
    }

    public BlockPos getActualPosOffset(int x, int y, int z, EnumFacing facing) {
        int[] c0 = new int[]{x, y, z}, c1 = new int[3];
        remapping(c0, c1, facing);
        return new BlockPos(c1[0], c1[1], c1[2]);
    }

    public int[] getActualPatternOffset(BlockPos pos, EnumFacing facing) {
        int[] c0 = new int[]{pos.getX(), pos.getY(), pos.getZ()}, c1 = new int[3];
        remapping(c0, c1, facing);
        return c1;
    }

    public void remapping(int[] c0, int[] c1, EnumFacing facing){
        for (int i = 0; i < 3; i++) {
            EnumFacing realFacing = structureDir[i].getActualFacing(facing);
            if (realFacing == EnumFacing.UP) {
                c1[1] = c0[i];
            } else if (realFacing == EnumFacing.DOWN) {
                c1[1] = -c0[i];
            } else if (realFacing == EnumFacing.WEST) {
                c1[0] = -c0[i];
            } else if (realFacing == EnumFacing.EAST) {
                c1[0] = c0[i];
            } else if (realFacing == EnumFacing.NORTH) {
                c1[2] = -c0[i];
            } else if (realFacing == EnumFacing.SOUTH) {
                c1[2] = c0[i];
            }
        }
    }

}
