package io.github.cleanroommc.multiblocked.common.capability;

import io.github.cleanroommc.multiblocked.api.capability.CapabilityProxy;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import io.github.cleanroommc.multiblocked.api.gui.texture.ColorRectTexture;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.ContentWidget;
import io.github.cleanroommc.multiblocked.api.gui.widget.imp.recipe.content.NumberContentWidget;
import io.github.cleanroommc.multiblocked.api.recipe.Recipe;
import net.minecraft.tileentity.TileEntity;
import vazkii.botania.api.mana.IManaReceiver;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Collections;
import java.util.List;

public class ManaBotainaCapability extends MultiblockCapability {
    public static final ManaBotainaCapability CAP = new ManaBotainaCapability();

    private ManaBotainaCapability() {
        super("bot_mana", new Color(0x06D2D9).getRGB());
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return tileEntity instanceof IManaReceiver;
    }

    @Override
    public ManaBotainaCapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new ManaBotainaCapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<?> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new ColorRectTexture(this.color)).setUnit("mana");
    }

    public static class ManaBotainaCapabilityProxy extends CapabilityProxy<Integer> {

        public ManaBotainaCapabilityProxy(TileEntity tileEntity) {
            super(tileEntity);
        }

        public IManaReceiver getCapability() {
            return (IManaReceiver)getTileEntity();
        }

        @Override
        protected List<Integer> handleRecipeInner(IO io, Recipe recipe, List<Integer> left, boolean simulate) {
            IManaReceiver capability = getCapability();
            if (capability == null) return left;
            int sum = left.stream().reduce(0, Integer::sum);
            if (io == IO.IN) {
                int stored = capability.getCurrentMana();
                if (!simulate) {
                    capability.recieveMana(-stored);
                }
                sum = sum - stored;
            } else if (io == IO.OUT) {
                if (capability.isFull()) {
                    return left;
                }
                if (!simulate) {
                    capability.recieveMana(sum);
                }
                return null;
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

        @Override
        protected Integer copyInner(Integer content) {
            return content;
        }
    }
}
