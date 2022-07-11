package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.ParticleStackWidget;
import com.cleanroommc.multiblocked.jei.IJeiIngredientAdapter;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import lach_01298.qmd.block.QMDBlocks;
import lach_01298.qmd.jei.ingredient.ParticleType;
import lach_01298.qmd.particle.ITileParticleStorage;
import lach_01298.qmd.particle.ParticleStack;
import lach_01298.qmd.particle.ParticleStorage;
import lach_01298.qmd.particle.Particles;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ParticleQMDCapability extends MultiblockCapability<ParticleStack> {
    public static final ParticleQMDCapability CAP = new ParticleQMDCapability();

    private ParticleQMDCapability() {
        super("qmd_particle", new Color(0xCDD59DBC, true).getRGB(), new ParticleJeiAdapter());
    }

    @Override
    public ParticleStack defaultContent() {
        return new ParticleStack(Particles.antidown);
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof ITileParticleStorage;
    }

    @Override
    public ParticleQMDCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new ParticleQMDCapabilityProxy(tileEntity);
    }

    @Override
    public ParticleStack copyInner(ParticleStack content) {
        return content.copy();
    }

    @Override
    public ContentWidget<ParticleStack> createContentWidget() {
        return new ParticleStackWidget();
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[]{
                BlockInfo.fromBlockState(QMDBlocks.acceleratorBeamPort.getDefaultState()),
                BlockInfo.fromBlockState(QMDBlocks.acceleratorSynchrotronPort.getDefaultState()),
                BlockInfo.fromBlockState(QMDBlocks.beamline.getDefaultState()),
                BlockInfo.fromBlockState(QMDBlocks.particleChamberBeamPort.getDefaultState()),
        };
    }

    @Override
    public ParticleStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObj = jsonElement.getAsJsonObject();
        return new ParticleStack(
                Particles.getParticleFromName(jsonObj.get("particle").getAsString()),
                jsonObj.get("amount").getAsInt(),
                jsonObj.get("energy").getAsLong(),
                jsonObj.get("focus").getAsDouble());
    }

    @Override
    public JsonElement serialize(ParticleStack particleStack, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("particle", particleStack.getParticle().getName());
        jsonObj.addProperty("amount", particleStack.getAmount());
        jsonObj.addProperty("focus", particleStack.getFocus());
        jsonObj.addProperty("energy", particleStack.getMeanEnergy());
        return jsonObj;
    }

    public static class ParticleQMDCapabilityProxy extends CapabilityProxy<ParticleStack> {

        public ParticleQMDCapabilityProxy(TileEntity tileEntity) {
            super(ParticleQMDCapability.CAP, tileEntity);
        }

        public ITileParticleStorage getCapability() {
            return (ITileParticleStorage)getTileEntity();
        }

        @Override
        protected List<ParticleStack> handleRecipeInner(IO io, Recipe recipe, List<ParticleStack> left, boolean simulate) {
            ITileParticleStorage capability = getCapability();
            if (capability == null) return left;
            Iterator<ParticleStack> iterator = left.iterator();
            if (io == IO.IN) {
                while (iterator.hasNext()) {
                    ParticleStack particleStack = iterator.next();
                    for (ParticleStorage storage : capability.getParticleBeams()) {
                        ParticleStack stored = storage.getParticleStack();
                        if (stored != null
                                && storage.canExtractParticle(null)
                                && stored.getParticle() == particleStack.getParticle()
                                && stored.getMeanEnergy() >= particleStack.getMeanEnergy()) {
                            int leftAmount = particleStack.getAmount() - stored.getAmount();
                            if (!simulate) {
                                storage.extractParticle(null, particleStack.getParticle(), particleStack.getAmount());
                            }
                            particleStack.setAmount(leftAmount);
                            if (leftAmount <= 0) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                }
            } else if (io == IO.OUT){
                while (iterator.hasNext()) {
                    ParticleStack particleStack = iterator.next();
                    for (ParticleStorage storage : capability.getParticleBeams()) {
                        if (storage.canReciveParticle(null, particleStack)) {
                            if (simulate) {
                                storage.reciveParticle(null, particleStack);
                            }
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }

        ParticleStack[] lastStacks = new ParticleStack[0];
        long[] lastMaxEnergy = new long[0];
        long[] lastMinEnergy = new long[0];
        int[] lastCapacity = new int[0];

        @Override
        protected boolean hasInnerChanged() {
            ITileParticleStorage capability = getCapability();
            if (capability == null) return false;
            List<? extends ParticleStorage> storages = capability.getParticleBeams();
            boolean same = true;
            if (lastStacks.length == storages.size()) {
                for (int i = 0; i < storages.size(); i++) {
                    ParticleStorage tank = storages.get(i);
                    ParticleStack content = tank.getParticleStack();
                    ParticleStack lastContent = lastStacks[i];
                    if (content == null) {
                        if (lastContent != null) {
                            same = false;
                            break;
                        }
                    } else {
                        if (lastContent == null) {
                            same = false;
                            break;
                        } else if (content.getParticle() != lastContent.getParticle() || content.getFocus() != lastContent.getFocus() || content.getMeanEnergy() != lastContent.getMeanEnergy() || content.getAmount() != lastContent.getAmount()) {
                            same = false;
                            break;
                        }
                    }
                    long cap = tank.getMaxEnergy();
                    long lastCap = lastMaxEnergy[i];
                    if (cap != lastCap) {
                        same = false;
                        break;
                    }
                    cap = tank.getMinEnergy();
                    lastCap = lastMinEnergy[i];
                    if (cap != lastCap) {
                        same = false;
                        break;
                    }
                    cap = tank.getCapacity();
                    lastCap = lastCapacity[i];
                    if (cap != lastCap) {
                        same = false;
                        break;
                    }
                }
            } else {
                same = false;
            }

            if (same) {
                return false;
            }
            lastStacks = new ParticleStack[storages.size()];
            lastMaxEnergy = new long[storages.size()];
            lastMinEnergy = new long[storages.size()];
            lastCapacity = new int[storages.size()];
            for (int i = 0; i < storages.size(); i++) {
                ParticleStorage tank = storages.get(i);
                lastStacks[i] = tank.getParticleStack() == null ? null : tank.getParticleStack().copy();
                lastMaxEnergy[i] = tank.getMaxEnergy();
                lastMinEnergy[i] = tank.getMinEnergy();
                lastCapacity[i] = tank.getCapacity();
            }
            return true;
        }
    }

    public static class ParticleJeiAdapter implements IJeiIngredientAdapter<ParticleStack, ParticleStack> {

        @Override
        public Class<ParticleStack> getInternalIngredientType() {
            return ParticleStack.class;
        }

        @Override
        public IIngredientType<ParticleStack> getJeiIngredientType() {
            return ParticleType.Particle;
        }

        @Override
        public List<ParticleStack> apply(ParticleStack particleStack) {
            return Collections.singletonList(particleStack);
        }
    }
}
