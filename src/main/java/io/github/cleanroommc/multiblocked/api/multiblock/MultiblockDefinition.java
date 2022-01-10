package io.github.cleanroommc.multiblocked.api.multiblock;

import io.github.cleanroommc.multiblocked.api.multiblock.functions.IPatternSupplier;
import io.github.cleanroommc.multiblocked.api.multiblock.functions.IStructureFormed;
import io.github.cleanroommc.multiblocked.api.multiblock.functions.IStructureInvalid;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockShapeInfo;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Definition of a multiblock, which define its structure, logic, recipe chain and so on.
 */
public class MultiblockDefinition {
    public final String unlocalizedName;
    public final IPatternSupplier patternSupplier;
    public IStructureFormed structureFormed;
    public IStructureInvalid structureInvalid;
    public ItemStack catalyst;
    public boolean consumeCatalyst;
    public List<MultiblockShapeInfo> designs;

    public MultiblockDefinition(String unlocalizedName, IPatternSupplier patternSupplier) {
        this.unlocalizedName = unlocalizedName;
        this.patternSupplier = patternSupplier;
    }

    public ItemStack getCatalyst() {
        return catalyst == null ? ItemStack.EMPTY : catalyst;
    }

    public List<MultiblockShapeInfo> getDesigns() {
        return designs == null ? Collections.emptyList() : designs;
    }
}
