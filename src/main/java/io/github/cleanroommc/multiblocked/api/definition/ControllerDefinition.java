package io.github.cleanroommc.multiblocked.api.definition;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.item.MCItemStack;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.IPatternSupplier;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.IStructureFormed;
import io.github.cleanroommc.multiblocked.api.crafttweaker.functions.IStructureInvalid;
import io.github.cleanroommc.multiblocked.api.pattern.MultiblockShapeInfo;
import io.github.cleanroommc.multiblocked.api.tile.ControllerTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenProperty;
import stanhebben.zenscript.annotations.ZenSetter;

import java.util.Collections;
import java.util.List;

/**
 * Definition of a controller, which define its structure, logic, recipe chain and so on.
 */
@ZenClass("mods.multiblocked.definition.ControllerDefinition")
@ZenRegister
public class ControllerDefinition extends ComponentDefinition {
    @ZenProperty
    public IPatternSupplier patternSupplier;
    @ZenProperty
    public IStructureFormed structureFormed;
    @ZenProperty
    public IStructureInvalid structureInvalid;
    public ItemStack catalyst;
    @ZenProperty
    public boolean consumeCatalyst;
    @ZenProperty
    public List<MultiblockShapeInfo> designs;

    public ControllerDefinition(ResourceLocation location, IPatternSupplier patternSupplier) {
        super(location, ControllerTileEntity.class);
        this.patternSupplier = patternSupplier;
    }

    public List<MultiblockShapeInfo> getDesigns() {
        return designs == null ? Collections.emptyList() : designs;
    }

    @Optional.Method(modid = Multiblocked.MODID_CT)
    @ZenGetter("catalyst")
    public IItemStack getCatalyst() {
        return catalyst == null ? null : new MCItemStack(catalyst);
    }

    @Optional.Method(modid = Multiblocked.MODID_CT)
    @ZenSetter("catalyst")
    public void setCatalyst(IItemStack catalyst) {
        this.catalyst = catalyst == null ? null : CraftTweakerMC.getItemStack(catalyst);
    }
}
