package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.AspectStackWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.ContentWidget;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import io.github.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;

public class AspectThaumcraftCapability extends MultiblockCapability<AspectStack> {
    public static final AspectThaumcraftCapability CAP = new AspectThaumcraftCapability();

    private AspectThaumcraftCapability() {
        super("tc6_aspect", new Color(0xCB00C8).getRGB());
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof IAspectContainer;
    }

    @Override
    public AspectThaumcraftCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new AspectThaumcraftCapabilityProxy(tileEntity);
    }

    @Override
    public AspectStack copyInner(AspectStack content) {
        return content.copy();
    }

    @Override
    public ContentWidget<AspectStack> createContentWidget() {
        return new AspectStackWidget();
    }

    public static class AspectThaumcraftCapabilityProxy extends CapabilityProxy<AspectStack> {

        public AspectThaumcraftCapabilityProxy(TileEntity tileEntity) {
            super(AspectThaumcraftCapability.CAP, tileEntity);
        }

        public IAspectContainer getCapability() {
            return (IAspectContainer)getTileEntity();
        }

        @Override
        protected List<AspectStack> handleRecipeInner(IO io, Recipe recipe, List<AspectStack> left, boolean simulate) {
            IAspectContainer capability = getCapability();
            if (capability == null) return left;
            Iterator<AspectStack> iterator = left.iterator();
            if (io == IO.IN) {
                while (iterator.hasNext()) {
                    AspectStack aspectStack = iterator.next();
                    Aspect aspect = aspectStack.aspect;
                    int amount = aspectStack.amount;
                    if (!ArrayUtils
                            .contains(capability.getAspects().getAspects(), aspect)) return left;
                    int stored = capability.getAspects().getAmount(aspect);
                    aspectStack.amount = Math.max(0, stored - amount);
                    if (!simulate) {
                        capability.takeFromContainer(aspect, stored - aspectStack.amount);
                    }
                    if (aspectStack.amount <= 0) {
                        iterator.remove();
                    }
                }
            } else if (io == IO.OUT){
                while (iterator.hasNext()) {
                    AspectStack aspectStack = iterator.next();
                    Aspect aspect = aspectStack.aspect;
                    int amount = aspectStack.amount;
                    int ll = capability.addToContainer(aspect, amount);
                    aspectStack.amount = ll;
                    if (simulate && amount - ll > 0) {
                        capability.takeFromContainer(aspect, amount - ll);
                    }
                    if (aspectStack.amount <= 0) {
                        iterator.remove();
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }

    }
}
