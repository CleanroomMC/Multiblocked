package io.github.cleanroommc.multiblocked.api.capability;

import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;

import java.util.List;
import java.util.stream.Collectors;

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
    protected abstract List<K> matchingRecipeInner(IO io, Recipe recipe, List<K> left);

    /**
     * searching successful. consumption
     * @param io the IO type of this recipe. must be one of the {@link IO#IN} or {@link IO#OUT}
     * @param recipe recipe.
     * @return left to do.
     * <br>
     *     null - nothing left. consumption finish.
     */
    protected abstract List<K> handleRecipeInputInner(IO io, Recipe recipe, List<K> left);

    /**
     * recipe logic finish. harvest
     * @param io the IO type of this recipe. must be one of the {@link IO#IN} or {@link IO#OUT}
     * @param recipe recipe.
     * @return left to do.
     * <br>
     *     null - nothing left. harvest finish.
     */
    protected abstract List<K> handleRecipeOutputInner(IO io, Recipe recipe, List<K> left);


    /**
     * deep copy of this content. recipe need it for searching and such things
     */
    protected abstract K copyInner(K content);

    @SuppressWarnings("unchecked")
    public final K copyContent(Object content) {
        return copyInner((K) content);
    }

    public final List<K> searchingRecipe(IO io, Recipe recipe, List<?> left) {
        return matchingRecipeInner(io, recipe, left.stream().map(this::copyContent).collect(Collectors.toList()));
    }

    public final List<K> handleRecipeInput(IO io, Recipe recipe, List<?> left) {
        return handleRecipeInputInner(io, recipe, left.stream().map(this::copyContent).collect(Collectors.toList()));
    }

    public final List<K> handleRecipeOutput(IO io, Recipe recipe, List<?> left) {
        return handleRecipeOutputInner(io, recipe, left.stream().map(this::copyContent).collect(Collectors.toList()));
    }
}
