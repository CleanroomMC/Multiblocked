package io.github.cleanroommc.multiblocked.api.definition;

import io.github.cleanroommc.multiblocked.api.definition.functions.IPatternSupplier;
import io.github.cleanroommc.multiblocked.api.definition.functions.IStructureFormed;
import io.github.cleanroommc.multiblocked.api.definition.functions.IStructureInvalid;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockShapeInfo;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.IRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

/**
 * Definition of a controller, which define its structure, logic, recipe chain and so on.
 */
public class ControllerDefinition extends ComponentDefinition {
    public final IPatternSupplier patternSupplier;
    public IStructureFormed structureFormed;
    public IStructureInvalid structureInvalid;
    public ItemStack catalyst;
    public boolean consumeCatalyst;
    public List<MultiblockShapeInfo> designs;

    public ControllerDefinition(ResourceLocation location, IPatternSupplier patternSupplier) {
        super(location, ControllerTileEntity.class);
        this.patternSupplier = patternSupplier;
    }

    public List<MultiblockShapeInfo> getDesigns() {
        return designs == null ? Collections.emptyList() : designs;
    }
}
