package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import io.github.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;

import java.util.Iterator;
import java.util.List;

public class AspectThaumcraftCapabilityProxy extends CapabilityProxy<AspectStack> {

    public AspectThaumcraftCapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
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
                if (!ArrayUtils.contains(capability.getAspects().getAspects(), aspect)) return left;
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

    @Override
    protected AspectStack copyInner(AspectStack content) {
        return content.copy();
    }
}
