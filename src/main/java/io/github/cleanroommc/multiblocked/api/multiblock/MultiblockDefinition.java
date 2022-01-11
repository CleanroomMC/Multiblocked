package io.github.cleanroommc.multiblocked.api.multiblock;

import io.github.cleanroommc.multiblocked.api.multiblock.functions.IPatternSupplier;
import io.github.cleanroommc.multiblocked.api.multiblock.functions.IStructureFormed;
import io.github.cleanroommc.multiblocked.api.multiblock.functions.IStructureInvalid;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockShapeInfo;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

/**
 * Definition of a multiblock, which define its structure, logic, recipe chain and so on.
 */
public class MultiblockDefinition {
    public final ResourceLocation location;
    public final IPatternSupplier patternSupplier;
    public IStructureFormed structureFormed;
    public IStructureInvalid structureInvalid;
    public IRenderer controllerRenderer;
    public ItemStack catalyst;
    public boolean consumeCatalyst;
    public List<MultiblockShapeInfo> designs;

    public MultiblockDefinition(ResourceLocation location, IPatternSupplier patternSupplier) {
        this.location = location;
        this.patternSupplier = patternSupplier;
    }

    public List<MultiblockShapeInfo> getDesigns() {
        return designs == null ? Collections.emptyList() : designs;
    }
}
