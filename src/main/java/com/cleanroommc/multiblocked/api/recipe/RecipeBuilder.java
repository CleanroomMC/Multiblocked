package com.cleanroommc.multiblocked.api.recipe;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.crafttweaker.functions.IPredicateFunction;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.common.capability.*;
import com.cleanroommc.multiblocked.common.recipe.conditions.*;
import com.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import com.cleanroommc.multiblocked.common.recipe.content.Starlight;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import lach_01298.qmd.particle.ParticleStack;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenConstructor;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.*;

@SuppressWarnings("unchecked")
@ZenClass("mods.multiblocked.recipe.RecipeBuilder")
@ZenRegister
public class RecipeBuilder {

    @ZenProperty
    public final RecipeMap recipeMap;
    public final Map<MultiblockCapability<?>, ImmutableList.Builder<Content>> inputBuilder = new HashMap<>();
    public final Map<MultiblockCapability<?>, ImmutableList.Builder<Content>> tickInputBuilder = new HashMap<>();
    public final Map<MultiblockCapability<?>, ImmutableList.Builder<Content>> outputBuilder = new HashMap<>();
    public final Map<MultiblockCapability<?>, ImmutableList.Builder<Content>> tickOutputBuilder = new HashMap<>();
    public final Map<String, Object> data = new HashMap<>();
    @ZenProperty
    protected int duration;
    protected ITextComponent text;
    protected StringBuilder keyBuilder = new StringBuilder(); // to make each recipe has a unique identifier and no need to set name yourself.
    protected boolean perTick;
    protected String fixedName;
    protected String slotName;
    public final List<RecipeCondition> conditions = new ArrayList<>();

    @ZenConstructor
    public RecipeBuilder(RecipeMap recipeMap) {
        this.recipeMap = recipeMap;
    }

    @ZenMethod
    public RecipeBuilder copy() {
        RecipeBuilder copy = new RecipeBuilder(recipeMap);
        inputBuilder.forEach((k, v)->{
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            copy.inputBuilder.put(k, builder.addAll(v.build()));
        });
        outputBuilder.forEach((k, v)->{
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            copy.outputBuilder.put(k, builder.addAll(v.build()));
        });
        tickInputBuilder.forEach((k, v)->{
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            copy.tickInputBuilder.put(k, builder.addAll(v.build()));
        });
        tickOutputBuilder.forEach((k, v)->{
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            copy.tickOutputBuilder.put(k, builder.addAll(v.build()));
        });
        data.forEach(copy.data::put);
        copy.conditions.addAll(conditions);
        copy.duration = this.duration;
        copy.keyBuilder = new StringBuilder(keyBuilder.toString());
        copy.fixedName = null;
        return copy;
    }

    @ZenMethod
    public RecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public RecipeBuilder data(String key, Object object) {
        this.data.put(key, object);
        return this;
    }

    @ZenMethod
    @Optional.Method(modid = Multiblocked.MODID_CT)
    public RecipeBuilder data(String key, IData object) {
        this.data.put(key, object);
        return this;
    }

    public RecipeBuilder text(ITextComponent text) {
        this.text = text;
        return this;
    }

    @ZenMethod
    @Optional.Method(modid = Multiblocked.MODID_CT)
    public RecipeBuilder text(crafttweaker.api.text.ITextComponent text) {
        this.text = CraftTweakerMC.getITextComponent(text);
        return this;
    }

    @ZenMethod
    public RecipeBuilder perTick(boolean perTick) {
        this.perTick = perTick;
        return this;
    }

    @ZenMethod
    public RecipeBuilder name(String name) {
        this.fixedName = name;
        return this;
    }

    @ZenMethod
    public RecipeBuilder slotName(String slotName) {
        this.slotName = slotName != null && !slotName.isEmpty() ? slotName : null;
        return this;
    }

    public RecipeBuilder input(MultiblockCapability<?> capability, float chance, Object... obj) {
        keyBuilder.append(chance);
        (perTick ? tickInputBuilder : inputBuilder).computeIfAbsent(capability, c -> ImmutableList.builder()).addAll(Arrays.stream(obj).map(o->new Content(o, chance, slotName)).iterator());
        return this;
    }

    public RecipeBuilder output(MultiblockCapability<?> capability, float chance, Object... obj) {
        keyBuilder.append(chance);
        (perTick ? tickOutputBuilder : outputBuilder).computeIfAbsent(capability, c -> ImmutableList.builder()).addAll(Arrays.stream(obj).map(o->new Content(o, chance, slotName)).iterator());
        return this;
    }

    public RecipeBuilder addCondition(RecipeCondition condition) {
        conditions.add(condition);
        return this;
    }

    //region Forge Energy
    @ZenMethod
    public RecipeBuilder inputFE(int forgeEnergy) {
        return inputFE(1, forgeEnergy);
    }

    @ZenMethod
    public RecipeBuilder outputFE(int forgeEnergy) {
        return outputFE(1, forgeEnergy);
    }

    @ZenMethod
    public RecipeBuilder inputFE(float chance, int forgeEnergy) {
        keyBuilder.append(MbdCapabilities.FE.name).append(forgeEnergy);
        return input(MbdCapabilities.FE, chance, forgeEnergy);
    }

    @ZenMethod
    public RecipeBuilder outputFE(float chance, int forgeEnergy) {
        keyBuilder.append(MbdCapabilities.FE.name).append(forgeEnergy);
        return output(MbdCapabilities.FE, chance, forgeEnergy);
    }
    //endregion

    //region Items
    public RecipeBuilder inputItems(ItemsIngredient... inputs) {
        return inputItems(1, inputs);
    }

    public RecipeBuilder outputItems(ItemStack... outputs) {
        return outputItems(1, outputs);
    }

    public RecipeBuilder inputItems(float chance, ItemsIngredient... inputs) {
        keyBuilder.append(MbdCapabilities.ITEM.name);
        for (ItemsIngredient input : inputs) {
            keyBuilder.append(input.hashCode());
        }
        return input(MbdCapabilities.ITEM, chance, (Object[]) inputs);
    }

    public RecipeBuilder outputItems(float chance, ItemStack... outputs) {
        keyBuilder.append(MbdCapabilities.ITEM.name);
        for (ItemStack output : outputs) {
            keyBuilder.append(output.getCount());
            ResourceLocation name = output.getItem().getRegistryName();
            keyBuilder.append(name == null ? "" : name.hashCode());
        }
        return output(MbdCapabilities.ITEM, chance, Arrays.stream(outputs).map(ItemsIngredient::new).toArray());
    }
    //endregion

    //region Fluids
    public RecipeBuilder inputFluids(FluidStack... inputs) {
        return inputFluids(1, inputs);
    }

    public RecipeBuilder outputFluids(FluidStack... outputs) {
        return outputFluids(1, outputs);
    }

    public RecipeBuilder inputFluids(float chance, FluidStack... inputs) {
        keyBuilder.append(MbdCapabilities.FLUID.name);
        for (FluidStack input : inputs) {
            keyBuilder.append(input.amount);
            keyBuilder.append(input.getUnlocalizedName());
        }
        return input(MbdCapabilities.FLUID, chance, (Object[]) inputs);
    }

    public RecipeBuilder outputFluids(float chance, FluidStack... outputs) {
        keyBuilder.append(MbdCapabilities.FLUID.name);
        for (FluidStack output : outputs) {
            keyBuilder.append(output.amount);
            keyBuilder.append(output.getUnlocalizedName());
        }
        return output(MbdCapabilities.FLUID, chance, (Object[]) outputs);
    }
    //endregion

    //region Entities
    public RecipeBuilder inputEntities(float chance, EntityIngredient... inputs) {
        return input(EntityMultiblockCapability.CAP, chance, (Object[]) inputs);
    }

    public RecipeBuilder outputEntities(float chance, EntityIngredient... outputs) {
        return output(EntityMultiblockCapability.CAP, chance, (Object[]) outputs);
    }

    public RecipeBuilder inputEntities(EntityIngredient... inputs) {
        return inputEntities(1, inputs);
    }

    public RecipeBuilder outputEntities(EntityIngredient... outputs) {
        return outputEntities(1, outputs);
    }
    //endregion

    //region Bot Mana
    @Optional.Method(modid = Multiblocked.MODID_BOT)
    @ZenMethod
    public RecipeBuilder inputMana(int mana) {
        return inputMana(1, mana);
    }

    @Optional.Method(modid = Multiblocked.MODID_BOT)
    @ZenMethod
    public RecipeBuilder outputMana(int mana) {
        return outputMana(1, mana);
    }

    @Optional.Method(modid = Multiblocked.MODID_BOT)
    @ZenMethod
    public RecipeBuilder inputMana(float chance, int mana) {
        keyBuilder.append(ManaBotaniaCapability.CAP.name).append(mana);
        return input(ManaBotaniaCapability.CAP, chance, mana);
    }

    @Optional.Method(modid = Multiblocked.MODID_BOT)
    @ZenMethod
    public RecipeBuilder outputMana(float chance, int mana) {
        keyBuilder.append(ManaBotaniaCapability.CAP.name).append(mana);
        return output(ManaBotaniaCapability.CAP, chance, mana);
    }
    //endregion

    //region TC6 Aspects
    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public RecipeBuilder inputAspects(AspectStack... inputs) {
        return inputAspects(1, inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public RecipeBuilder outputAspects(AspectStack... outputs) {
        return outputAspects(1, outputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public RecipeBuilder inputAspects(float chance, AspectStack... inputs) {
        keyBuilder.append(AspectThaumcraftCapability.CAP.name);
        for (AspectStack input : inputs) {
            keyBuilder.append(input.amount);
            keyBuilder.append(input.aspect.getName());
        }
        return input(AspectThaumcraftCapability.CAP, chance, (Object[]) inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_TC6)
    public RecipeBuilder outputAspects(float chance, AspectStack... outputs) {
        keyBuilder.append(AspectThaumcraftCapability.CAP.name);
        for (AspectStack output : outputs) {
            keyBuilder.append(output.amount);
            keyBuilder.append(output.aspect.getName());
        }
        return output(AspectThaumcraftCapability.CAP, chance, (Object[]) outputs);
    }
    //endregion

    //region Mek Heat
    @Optional.Method(modid = Multiblocked.MODID_MEK)
    @ZenMethod
    public RecipeBuilder inputHeat(double heat) {
        return inputHeat(1, heat);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    @ZenMethod
    public RecipeBuilder outputHeat(double heat) {
        return outputHeat(1, heat);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    @ZenMethod
    public RecipeBuilder inputHeat(float chance, double heat) {
        keyBuilder.append(HeatMekanismCapability.CAP.name).append(heat);
        return input(HeatMekanismCapability.CAP, chance, heat);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    @ZenMethod
    public RecipeBuilder outputHeat(float chance, double heat) {
        keyBuilder.append(HeatMekanismCapability.CAP.name).append(heat);
        return output(HeatMekanismCapability.CAP, chance, heat);
    }
    //endregion

    //region Mek Gas
    @Optional.Method(modid = Multiblocked.MODID_MEK)
    @ZenMethod
    public RecipeBuilder inputLaser(double laser) {
        return inputLaser(1, laser);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    @ZenMethod
    public RecipeBuilder outputLaser(double laser) {
        return outputLaser(1, laser);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    @ZenMethod
    public RecipeBuilder inputLaser(float chance, double laser) {
        keyBuilder.append(LaserMekanismCapability.CAP.name).append(laser);
        return input(LaserMekanismCapability.CAP, chance, laser);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    @ZenMethod
    public RecipeBuilder outputLaser(float chance, double laser) {
        keyBuilder.append(LaserMekanismCapability.CAP.name).append(laser);
        return output(LaserMekanismCapability.CAP, chance, laser);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder inputGas(GasStack... inputs) {
        return inputGas(1, inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder outputGas(GasStack... outputs) {
        return outputGas(1, outputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder inputGas(float chance, GasStack... inputs) {
        keyBuilder.append(GasMekanismCapability.CAP.name);
        for (GasStack input : inputs) {
            keyBuilder.append(input.amount);
            keyBuilder.append(input.getGas().getID());
        }
        return input(GasMekanismCapability.CAP, chance, (Object[]) inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_MEK)
    public RecipeBuilder outputGas(float chance, GasStack... outputs) {
        keyBuilder.append(GasMekanismCapability.CAP.name);
        for (GasStack output : outputs) {
            keyBuilder.append(output.amount);
            keyBuilder.append(output.getGas().getID());
        }
        return output(GasMekanismCapability.CAP, chance, (Object[]) outputs);
    }
    //endregion

    //region QMD Particles
    @Optional.Method(modid = Multiblocked.MODID_QMD)
    public RecipeBuilder inputParticles(ParticleStack... inputs) {
        return inputParticles(1, inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_QMD)
    public RecipeBuilder outputParticles(ParticleStack... outputs) {
        return outputParticles(1, outputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_QMD)
    public RecipeBuilder inputParticles(float chance, ParticleStack... inputs) {
        keyBuilder.append(ParticleQMDCapability.CAP.name);
        for (ParticleStack input : inputs) {
            keyBuilder.append(input.getAmount());
            keyBuilder.append(input.getParticle().getName());
            keyBuilder.append(input.getMeanEnergy());
            keyBuilder.append(input.getFocus());
        }
        return input(ParticleQMDCapability.CAP, chance, (Object[]) inputs);
    }

    @Optional.Method(modid = Multiblocked.MODID_QMD)
    public RecipeBuilder outputParticles(float chance, ParticleStack... outputs) {
        keyBuilder.append(ParticleQMDCapability.CAP.name);
        for (ParticleStack output : outputs) {
            keyBuilder.append(output.getAmount());
            keyBuilder.append(output.getParticle().getName());
            keyBuilder.append(output.getMeanEnergy());
            keyBuilder.append(output.getFocus());
        }
        return output(ParticleQMDCapability.CAP, chance, (Object[]) outputs);
    }
    //endregion

    //region GTCE EU
    @Optional.Method(modid = Multiblocked.MODID_GTCE)
    @ZenMethod
    public RecipeBuilder inputEU(int eu) {
        return inputEU(1, eu);
    }

    @Optional.Method(modid = Multiblocked.MODID_GTCE)
    @ZenMethod
    public RecipeBuilder outputEU(int eu) {
        return outputEU(1, eu);
    }

    @Optional.Method(modid = Multiblocked.MODID_GTCE)
    @ZenMethod
    public RecipeBuilder inputEU(float chance, long eu) {
        keyBuilder.append(EnergyGTCECapability.CAP.name).append(eu);
        return input(EnergyGTCECapability.CAP, chance, eu);
    }

    @Optional.Method(modid = Multiblocked.MODID_GTCE)
    @ZenMethod
    public RecipeBuilder outputEU(float chance, long eu) {
        keyBuilder.append(EnergyGTCECapability.CAP.name).append(eu);
        return output(EnergyGTCECapability.CAP, chance, eu);
    }
    //endregion

    //region LC LE
    @Optional.Method(modid = Multiblocked.MODID_LC)
    @ZenMethod
    public RecipeBuilder inputLE(double le) {
        return inputLE(1, le);
    }

    @Optional.Method(modid = Multiblocked.MODID_LC)
    @ZenMethod
    public RecipeBuilder outputLE(double le) {
        return outputLE(1, le);
    }

    @Optional.Method(modid = Multiblocked.MODID_LC)
    @ZenMethod
    public RecipeBuilder inputLE(float chance, double le) {
        keyBuilder.append(LEMultiblockCapability.CAP.name).append(le);
        return input(LEMultiblockCapability.CAP, chance, le);
    }

    @Optional.Method(modid = Multiblocked.MODID_LC)
    @ZenMethod
    public RecipeBuilder outputLE(float chance, double le) {
        keyBuilder.append(LEMultiblockCapability.CAP.name).append(le);
        return output(LEMultiblockCapability.CAP, chance, le);
    }
    //endregion

    //region Prodigy Hot Air
    @Optional.Method(modid = Multiblocked.MODID_PRODIGY)
    @ZenMethod
    public RecipeBuilder inputHotAir(int hotAir) {
        return inputHotAir(1, hotAir);
    }

    @Optional.Method(modid = Multiblocked.MODID_PRODIGY)
    @ZenMethod
    public RecipeBuilder outputHotAir(int hotAir) {
        return outputHotAir(1, hotAir);
    }

    @Optional.Method(modid = Multiblocked.MODID_PRODIGY)
    @ZenMethod
    public RecipeBuilder inputHotAir(float chance, int hotAir) {
        keyBuilder.append(HotAirProdigyCapability.CAP.name).append(hotAir);
        return input(HotAirProdigyCapability.CAP, chance, hotAir);
    }

    @Optional.Method(modid = Multiblocked.MODID_PRODIGY)
    @ZenMethod
    public RecipeBuilder outputHotAir(float chance, int hotAir) {
        keyBuilder.append(HotAirProdigyCapability.CAP.name).append(hotAir);
        return output(HotAirProdigyCapability.CAP, chance, hotAir);
    }
    //endregion

    //region NA Aura
    @Optional.Method(modid = Multiblocked.MODID_NA)
    @ZenMethod
    public RecipeBuilder inputAura(int aura) {
        return inputAura(1, aura);
    }

    @Optional.Method(modid = Multiblocked.MODID_NA)
    @ZenMethod
    public RecipeBuilder outputAura(int aura) {
        return outputAura(1, aura);
    }

    @Optional.Method(modid = Multiblocked.MODID_NA)
    @ZenMethod
    public RecipeBuilder inputAura(float chance, int aura) {
        keyBuilder.append(AuraMultiblockCapability.CAP.name).append(aura);
        return input(AuraMultiblockCapability.CAP, chance, aura);
    }

    @Optional.Method(modid = Multiblocked.MODID_NA)
    @ZenMethod
    public RecipeBuilder outputAura(float chance, int aura) {
        keyBuilder.append(AuraMultiblockCapability.CAP.name).append(aura);
        return output(AuraMultiblockCapability.CAP, chance, aura);
    }
    //endregion

    //region EU2 Grid Power
    @Optional.Method(modid = Multiblocked.MODID_EU2)
    @ZenMethod
    public RecipeBuilder inputGP(float gp) {
        return inputGP(1, gp);
    }

    @Optional.Method(modid = Multiblocked.MODID_EU2)
    @ZenMethod
    public RecipeBuilder outputGP(float gp) {
        return outputGP(1, gp);
    }

    @Optional.Method(modid = Multiblocked.MODID_EU2)
    @ZenMethod
    public RecipeBuilder inputGP(float chance, float gp) {
        keyBuilder.append(GPExtraUtilities2Capability.CAP.name).append(gp);
        return input(GPExtraUtilities2Capability.CAP, chance, gp);
    }

    @Optional.Method(modid = Multiblocked.MODID_EU2)
    @ZenMethod
    public RecipeBuilder outputGP(float chance, float gp) {
        keyBuilder.append(GPExtraUtilities2Capability.CAP.name).append(gp);
        return output(GPExtraUtilities2Capability.CAP, chance, gp);
    }
    //endregion

    //region PE EMC
    @Optional.Method(modid = Multiblocked.MODID_PE)
    @ZenMethod
    public RecipeBuilder inputEMC(long emc) {
        return inputEMC(1, emc);
    }

    @Optional.Method(modid = Multiblocked.MODID_PE)
    @ZenMethod
    public RecipeBuilder outputEMC(long emc) {
        return outputEMC(1, emc);
    }

    @Optional.Method(modid = Multiblocked.MODID_PE)
    @ZenMethod
    public RecipeBuilder inputEMC(float chance, long emc) {
        keyBuilder.append(EMCProjectECapability.CAP.name).append(emc);
        return input(EMCProjectECapability.CAP, chance, emc);
    }

    @Optional.Method(modid = Multiblocked.MODID_PE)
    @ZenMethod
    public RecipeBuilder outputEMC(float chance, long emc) {
        keyBuilder.append(EMCProjectECapability.CAP.name).append(emc);
        return output(EMCProjectECapability.CAP, chance, emc);
    }
    //endregion

    //region BG Life Power
    @Optional.Method(modid = Multiblocked.MODID_BG)
    @ZenMethod
    public RecipeBuilder inputLP(int lp) {
        return inputLP(1, lp);
    }

    @Optional.Method(modid = Multiblocked.MODID_BG)
    @ZenMethod
    public RecipeBuilder outputLP(int lp) {
        return outputLP(1, lp);
    }

    @Optional.Method(modid = Multiblocked.MODID_BG)
    @ZenMethod
    public RecipeBuilder inputLP(float chance, int lp) {
        keyBuilder.append(LPBloodMagicCapability.CAP.name).append(lp);
        return input(LPBloodMagicCapability.CAP, chance, lp);
    }

    @Optional.Method(modid = Multiblocked.MODID_BG)
    @ZenMethod
    public RecipeBuilder outputLP(float chance, int lp) {
        keyBuilder.append(LPBloodMagicCapability.CAP.name).append(lp);
        return output(LPBloodMagicCapability.CAP, chance, lp);
    }
    //endregion

    //region Embers Ember
    @Optional.Method(modid = Multiblocked.MODID_EMBERS)
    @ZenMethod
    public RecipeBuilder inputEmber(double ember) {
        return inputEmber(1, ember);
    }

    @Optional.Method(modid = Multiblocked.MODID_EMBERS)
    @ZenMethod
    public RecipeBuilder outputEmber(double ember) {
        return outputEmber(1, ember);
    }

    @Optional.Method(modid = Multiblocked.MODID_EMBERS)
    @ZenMethod
    public RecipeBuilder inputEmber(float chance, double ember) {
        keyBuilder.append(EmberEmbersCapability.CAP.name).append(ember);
        return input(EmberEmbersCapability.CAP, chance, ember);
    }

    @Optional.Method(modid = Multiblocked.MODID_EMBERS)
    @ZenMethod
    public RecipeBuilder outputEmber(float chance, double ember) {
        keyBuilder.append(EmberEmbersCapability.CAP.name).append(ember);
        return output(EmberEmbersCapability.CAP, chance, ember);
    }
    //endregion

    //region TA Impetus
    @Optional.Method(modid = Multiblocked.MODID_TA)
    @ZenMethod
    public RecipeBuilder inputImpetus(int impetus) {
        return inputImpetus(1, impetus);
    }

    @Optional.Method(modid = Multiblocked.MODID_TA)
    @ZenMethod
    public RecipeBuilder outputImpetus(int impetus) {
        return outputImpetus(1, impetus);
    }

    @Optional.Method(modid = Multiblocked.MODID_TA)
    @ZenMethod
    public RecipeBuilder inputImpetus(float chance, long impetus) {
        keyBuilder.append(ImpetusThaumicAugmentationCapability.CAP.name).append(impetus);
        return input(ImpetusThaumicAugmentationCapability.CAP, chance, impetus);
    }

    @Optional.Method(modid = Multiblocked.MODID_TA)
    @ZenMethod
    public RecipeBuilder outputImpetus(float chance, long impetus) {
        keyBuilder.append(ImpetusThaumicAugmentationCapability.CAP.name).append(impetus);
        return output(ImpetusThaumicAugmentationCapability.CAP, chance, impetus);
    }
    //endregion

    //region PNC Pressure
    @Optional.Method(modid = Multiblocked.MODID_PNC)
    @ZenMethod
    public RecipeBuilder inputPressure(float chance, float pressure) {
        keyBuilder.append(PneumaticPressureCapability.CAP.name).append(pressure);
        return input(PneumaticPressureCapability.CAP, chance, pressure);
    }

    @Optional.Method(modid = Multiblocked.MODID_PNC)
    @ZenMethod
    public RecipeBuilder inputPressure(float pressure) {
        return inputPressure(1, pressure);
    }
    //endregion

    //region AS Starlight
    @Optional.Method(modid = Multiblocked.MODID_AS)
    @ZenMethod
    public RecipeBuilder inputStarlight(float chance, int starlight, @stanhebben.zenscript.annotations.Optional String constellation) {
        keyBuilder.append(StarlightAstralCapability.CAP.name).append(starlight).append(constellation);
        return input(StarlightAstralCapability.CAP, chance, new Starlight(starlight, ConstellationRegistry.getConstellationByName(constellation)));
    }

    @Optional.Method(modid = Multiblocked.MODID_AS)
    @ZenMethod
    public RecipeBuilder outputStarlight(float chance, int starlight, @stanhebben.zenscript.annotations.Optional String constellation) {
        keyBuilder.append(StarlightAstralCapability.CAP.name).append(starlight).append(constellation);
        return output(StarlightAstralCapability.CAP, chance, new Starlight(starlight, ConstellationRegistry.getConstellationByName(constellation)));
    }

    @Optional.Method(modid = Multiblocked.MODID_AS)
    @ZenMethod
    public RecipeBuilder inputStarlight(int starlight, @stanhebben.zenscript.annotations.Optional String constellation) {
        return inputStarlight(1, starlight, constellation);
    }

    @Optional.Method(modid = Multiblocked.MODID_AS)
    @ZenMethod
    public RecipeBuilder outputStarlight(int starlight, @stanhebben.zenscript.annotations.Optional String constellation) {
        return outputStarlight(1, starlight, constellation);
    }
    //endregion

    //region MM Mechanical Power
    @Optional.Method(modid = Multiblocked.MODID_MM)
    @ZenMethod
    public RecipeBuilder inputMystMechPower(float chance, double minimumPower) {
        keyBuilder.append(MystMechPowerCapability.CAP.name).append(minimumPower);
        return input(MystMechPowerCapability.CAP, 1, minimumPower);
    }

    @Optional.Method(modid = Multiblocked.MODID_MM)
    @ZenMethod
    public RecipeBuilder inputMystMechPower(double minimumPower) {
        return inputMystMechPower(1, minimumPower);
    }
    //endregion

    //region Conditions
    @ZenMethod
    public RecipeBuilder dimension(String dimension, boolean reverse) {
        return addCondition(new DimensionCondition(dimension).setReverse(reverse));
    }

    @ZenMethod
    public RecipeBuilder dimension(String dimension) {
        return dimension(dimension, false);
    }

    public RecipeBuilder biome(ResourceLocation biome, boolean reverse) {
        return addCondition(new BiomeCondition(biome).setReverse(reverse));
    }

    @ZenMethod
    public RecipeBuilder biome(ResourceLocation biome) {
        return biome(biome, false);
    }

    @ZenMethod
    public RecipeBuilder rain(float level, boolean reverse) {
        return addCondition(new RainingCondition(level).setReverse(reverse));
    }

    public RecipeBuilder rain(float level) {
        return rain(level, false);
    }

    @ZenMethod
    public RecipeBuilder thunder(float level, boolean reverse) {
        return addCondition(new ThunderCondition(level).setReverse(reverse));
    }

    @ZenMethod
    public RecipeBuilder thunder(float level) {
        return thunder(level, false);
    }

    @ZenMethod
    public RecipeBuilder posY(int min, int max, boolean reverse) {
        return addCondition(new PositionYCondition(min, max).setReverse(reverse));
    }

    @ZenMethod
    public RecipeBuilder posY(int min, int max) {
        return posY(min, max, false);
    }

    @ZenMethod
    public RecipeBuilder predicate(IPredicateFunction predicate, String tooltip, boolean reverse) {
        this.conditions.add(new PredicateCondition(tooltip, predicate).setReverse(reverse));
        return this;
    }

    @ZenMethod
    public RecipeBuilder predicate(IPredicateFunction predicate, boolean reverse) {
        return predicate(predicate, PredicateCondition.DEFAULT_TOOLTIP, reverse);
    }

    @ZenMethod
    public RecipeBuilder predicate(IPredicateFunction predicate, String tooltip) {
        return predicate(predicate, tooltip, false);
    }

    @ZenMethod
    public RecipeBuilder predicate(IPredicateFunction predicate) {
        return predicate(predicate, PredicateCondition.DEFAULT_TOOLTIP, false);
    }
    //endregion

    @ZenMethod
    public Recipe build() {
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Content>> inputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Content>> entry : this.inputBuilder.entrySet()) {
            inputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Content>> outputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Content>> entry : this.outputBuilder.entrySet()) {
            outputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Content>> tickInputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Content>> entry : this.tickInputBuilder.entrySet()) {
            tickInputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Content>> tickOutputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Content>> entry : this.tickOutputBuilder.entrySet()) {
            tickOutputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        return new Recipe(fixedName == null ? keyBuilder.toString() : fixedName, inputBuilder.build(), outputBuilder.build(), tickInputBuilder.build(), tickOutputBuilder.build(), ImmutableList.copyOf(conditions), data.isEmpty() ? Recipe.EMPTY : ImmutableMap.copyOf(data), text, duration);
    }

    @ZenMethod
    public void buildAndRegister() {
        buildAndRegister(false);
    }

    @ZenMethod
    public void buildAndRegister(boolean isFuel) {
        if (isFuel) {
            recipeMap.addFuelRecipe(build());
        } else {
            recipeMap.addRecipe(build());
        }
    }
}
