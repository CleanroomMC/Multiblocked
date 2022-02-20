package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.ContentWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.ParticleStackWidget;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import lach_01298.qmd.particle.ITileParticleStorage;
import lach_01298.qmd.particle.ParticleStack;
import lach_01298.qmd.particle.ParticleStorage;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;

public class ParticleQMDCapability extends MultiblockCapability<ParticleStack> {
    public static final ParticleQMDCapability CAP = new ParticleQMDCapability();

    private ParticleQMDCapability() {
        super("qmd_particle", new Color(0xCDD59DBC, true).getRGB());
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
                        if (stored != null && stored.getParticle() == particleStack.getParticle() && storage.canExtractParticle(null)) {
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

    }
}
