package io.github.cleanroommc.multiblocked.common.capability.proxy;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;

import java.util.Iterator;
import java.util.List;

public class AspectThaumcraftCapabilityProxy extends CapabilityProxy<AspectList> {

    public AspectThaumcraftCapabilityProxy(TileEntity tileEntity) {
        super(tileEntity);
    }

    public IAspectContainer getCapability() {
        return (IAspectContainer)getTileEntity();
    }

    @Override
    protected List<AspectList> handleRecipeInner(IO io, Recipe recipe, List<AspectList> left, boolean simulate) {
        IAspectContainer capability = getCapability();
        if (capability == null) return left;
        Iterator<AspectList> iterator = left.iterator();
        if (io == IO.IN) {
            while (iterator.hasNext()) {
                AspectList aspectList = iterator.next();
                Aspect aspect = aspectList.getAspects()[0];
                int amount = aspectList.getAmount(aspect);
                if (!ArrayUtils.contains(capability.getAspects().getAspects(), aspect)) return left;
                int stored = capability.getAspects().getAmount(aspect);
                aspectList.aspects.put(aspect, Math.max(0, stored - amount));
                if (!simulate) {
                    capability.takeFromContainer(aspect, stored - aspectList.getAmount(aspect));
                }
                if (aspectList.getAmount(aspect) <= 0) {
                    iterator.remove();
                }
            }
        } else if (io == IO.OUT){
            while (iterator.hasNext()) {
                AspectList aspectList = iterator.next();
                Aspect aspect = aspectList.getAspects()[0];
                int amount = aspectList.getAmount(aspect);
                int ll = capability.addToContainer(aspect, amount);
                aspectList.aspects.put(aspect, Math.max(0, ll));
                if (simulate && amount - ll > 0) {
                    capability.takeFromContainer(aspect, amount - ll);
                }
                if (aspectList.getAmount(aspect) <= 0) {
                    iterator.remove();
                }
            }
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    protected AspectList copyInner(AspectList content) {
        return content.copy();
    }
}
