package io.github.cleanroommc.multiblocked.api.gui.ingredient;

import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraftforge.fml.common.Optional;

import java.util.List;

public interface IGhostIngredientTarget {

    @Optional.Method(modid = "jei")
    List<Target<?>> getPhantomTargets(Object ingredient);

}
