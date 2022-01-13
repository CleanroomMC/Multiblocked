package io.github.cleanroommc.multiblocked.api.capability;

import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;

/**
 * The Proxy of a specific capability that has been detected {@link MultiblockCapability}. Providing I/O and such features to a controller.
 */
public abstract class CapabilityProxy<K> {
    protected TileEntity tileEntity;

    public CapabilityProxy(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    /**
     * searching recipe.
     *
     * @param io the IO type of this proxy.
     * @param recipe recipe.
     * @param left requirements of this recipe.
     * @return left requirements for continue searching of this proxy type.
     * <br>
     *      null - nothing left.
     */
    public abstract K searchingRecipe(IO io, Recipe recipe, K left);

    /**
     * searching successful. consumption
     * @param io the IO type of this proxy.
     * @param recipe recipe.
     * @return left to do.
     * <br>
     *     null - nothing left.
     */
    protected abstract K handleRecipeInput(IO io, Recipe recipe, K left);

    /**
     * recipe logic finish. earning
     * @param io the IO type of this proxy.
     * @param recipe recipe.
     * @return left to do.
     * <br>
     *     null - nothing left.
     */
    protected abstract K handleRecipeOutput(IO io, Recipe recipe, K left);
}
