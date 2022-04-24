package com.cleanroommc.multiblocked.api.recipe;

import com.cleanroommc.multiblocked.Multiblocked;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.common.capability.*;
import com.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.IData;
import crafttweaker.api.minecraft.CraftTweakerMC;
import lach_01298.qmd.particle.ParticleStack;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenConstructor;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@ZenClass("mods.multiblocked.recipe.RecipeBuilder")
@ZenRegister
public class RecipeBuilder {

    @ZenProperty
    public final RecipeMap recipeMap;
    public final Map<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> inputBuilder = new HashMap<>();
    public final Map<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> tickInputBuilder = new HashMap<>();
    public final Map<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> outputBuilder = new HashMap<>();
    public final Map<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> tickOutputBuilder = new HashMap<>();
    public final Map<String, Object> data = new HashMap<>();
    @ZenProperty
    protected int duration;
    protected ITextComponent text;
    protected StringBuilder keyBuilder = new StringBuilder(); // to make each recipe has a unique identifier and no need to set name yourself.
    protected boolean perTick;

    @ZenConstructor
    public RecipeBuilder(RecipeMap recipeMap) {
        this.recipeMap = recipeMap;
    }

    @ZenMethod
    public RecipeBuilder copy() {
        RecipeBuilder copy = new RecipeBuilder(recipeMap);
        inputBuilder.forEach((k, v)->{
            ImmutableList.Builder<Tuple<Object, Float>> builder = ImmutableList.builder();
            copy.inputBuilder.put(k, builder.addAll(v.build()));
        });
        outputBuilder.forEach((k, v)->{
            ImmutableList.Builder<Tuple<Object, Float>> builder = ImmutableList.builder();
            copy.outputBuilder.put(k, builder.addAll(v.build()));
        });
        tickInputBuilder.forEach((k, v)->{
            ImmutableList.Builder<Tuple<Object, Float>> builder = ImmutableList.builder();
            copy.tickInputBuilder.put(k, builder.addAll(v.build()));
        });
        tickOutputBuilder.forEach((k, v)->{
            ImmutableList.Builder<Tuple<Object, Float>> builder = ImmutableList.builder();
            copy.tickOutputBuilder.put(k, builder.addAll(v.build()));
        });
        data.forEach(copy.data::put);
        copy.duration = this.duration;
        copy.keyBuilder = new StringBuilder(keyBuilder.toString());
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

    public RecipeBuilder input(MultiblockCapability<?> capability, float chance, Object... obj) {
        keyBuilder.append(chance);
        (perTick ? tickInputBuilder : inputBuilder).computeIfAbsent(capability, c -> ImmutableList.builder()).add(Arrays.stream(obj).map(o->new Tuple<>(o, chance)).toArray(Tuple[]::new));
        return this;
    }

    public RecipeBuilder output(MultiblockCapability<?> capability, float chance, Object... obj) {
        keyBuilder.append(chance);
        (perTick ? tickOutputBuilder : outputBuilder).computeIfAbsent(capability, c -> ImmutableList.builder()).add(Arrays.stream(obj).map(o->new Tuple<>(o, chance)).toArray(Tuple[]::new));
        return this;
    }

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

    @Optional.Method(modid = Multiblocked.MODID_GTCE)
    @ZenMethod
    public RecipeBuilder inputEU(int eu) {
        return inputMana(1, eu);
    }

    @Optional.Method(modid = Multiblocked.MODID_GTCE)
    @ZenMethod
    public RecipeBuilder outputEU(int eu) {
        return outputMana(1, eu);
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
    @Optional.Method(modid = Multiblocked.MODID_BG)
    @ZenMethod
    public RecipeBuilder inputLP(long lp) {
        return inputEMC(1, lp);
    }

    @Optional.Method(modid = Multiblocked.MODID_BG)
    @ZenMethod
    public RecipeBuilder outputLP(long lp) {
        return outputEMC(1, lp);
    }

    @Optional.Method(modid = Multiblocked.MODID_BG)
    @ZenMethod
    public RecipeBuilder inputLP(float chance, long lp) {
        keyBuilder.append(EMCProjectECapability.CAP.name).append(lp);
        return input(EMCProjectECapability.CAP, chance, lp);
    }

    @Optional.Method(modid = Multiblocked.MODID_BG)
    @ZenMethod
    public RecipeBuilder outputLP(float chance, long lp) {
        keyBuilder.append(EMCProjectECapability.CAP.name).append(lp);
        return output(EMCProjectECapability.CAP, chance, lp);
    }

    @Optional.Method(modid = Multiblocked.MODID_EMBERS)
    @ZenMethod
    public RecipeBuilder inputEmber(long ember) {
        return inputEMC(1, ember);
    }

    @Optional.Method(modid = Multiblocked.MODID_EMBERS)
    @ZenMethod
    public RecipeBuilder outputEmber(long ember) {
        return outputEMC(1, ember);
    }

    @Optional.Method(modid = Multiblocked.MODID_EMBERS)
    @ZenMethod
    public RecipeBuilder inputEmber(float chance, long ember) {
        keyBuilder.append(EMCProjectECapability.CAP.name).append(ember);
        return input(EMCProjectECapability.CAP, chance, ember);
    }

    @Optional.Method(modid = Multiblocked.MODID_EMBERS)
    @ZenMethod
    public RecipeBuilder outputEmber(float chance, long ember) {
        keyBuilder.append(EMCProjectECapability.CAP.name).append(ember);
        return output(EMCProjectECapability.CAP, chance, ember);
    }

    @ZenMethod
    public Recipe build() {
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> inputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> entry : this.inputBuilder.entrySet()) {
            inputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> outputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> entry : this.outputBuilder.entrySet()) {
            outputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> tickInputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> entry : this.tickInputBuilder.entrySet()) {
            tickInputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        ImmutableMap.Builder<MultiblockCapability<?>, ImmutableList<Tuple<Object, Float>>> tickOutputBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<MultiblockCapability<?>, ImmutableList.Builder<Tuple<Object, Float>>> entry : this.tickOutputBuilder.entrySet()) {
            tickOutputBuilder.put(entry.getKey(), entry.getValue().build());
        }
        return new Recipe(keyBuilder.toString(), inputBuilder.build(), outputBuilder.build(), tickInputBuilder.build(), tickOutputBuilder.build(), data.isEmpty() ? Recipe.EMPTY : ImmutableMap.copyOf(data), text, duration);
    }

    @ZenMethod
    public void buildAndRegister(){
        recipeMap.addRecipe(build());
    }
}
