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
     * matching recipe.
     *
     * @param io the IO type of this recipe. must be one of the {@link IO#IN} or {@link IO#OUT}
     * @param recipe recipe.
     * @param left requirements of this recipe.
     * @return left requirements for continue searching of this proxy type.
     * <br>
     *      null - nothing left. matching successful.
     */
    protected abstract K matchingRecipeInner(IO io, Recipe recipe, K left);

    /**
     * searching successful. consumption
     * @param io the IO type of this recipe. must be one of the {@link IO#IN} or {@link IO#OUT}
     * @param recipe recipe.
     * @return left to do.
     * <br>
     *     null - nothing left. consumption finish.
     */
    protected abstract K handleRecipeInputInner(IO io, Recipe recipe, K left);

    /**
     * recipe logic finish. harvest
     * @param io the IO type of this recipe. must be one of the {@link IO#IN} or {@link IO#OUT}
     * @param recipe recipe.
     * @return left to do.
     * <br>
     *     null - nothing left. harvest finish.
     */
    protected abstract K handleRecipeOutputInner(IO io, Recipe recipe, K left);

    @SuppressWarnings("unchecked")
    public final K searchingRecipe(IO io, Recipe recipe, Object left) {
        return matchingRecipeInner(io, recipe, (K) left);
    }

    @SuppressWarnings("unchecked")
    public final K handleRecipeInput(IO io, Recipe recipe, Object left) {
        return handleRecipeInputInner(io, recipe, (K) left);
    }

    @SuppressWarnings("unchecked")
    public final K handleRecipeOutput(IO io, Recipe recipe, Object left) {
        return handleRecipeOutputInner(io, recipe, (K) left);
    }
}
