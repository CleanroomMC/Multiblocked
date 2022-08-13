package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.ContentModifier;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.common.capability.widget.AspectStackWidget;
import com.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import com.cleanroommc.multiblocked.jei.IJeiIngredientAdapter;
import com.cleanroommc.multiblocked.jei.ingredient.AspectListIngredient;
import com.google.gson.*;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.blocks.BlocksTC;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class AspectThaumcraftCapability extends MultiblockCapability<AspectStack> {
    public static final AspectThaumcraftCapability CAP = new AspectThaumcraftCapability();

    private AspectThaumcraftCapability() {
        super("tc6_aspect", new Color(0xCB00C8).getRGB(), new AspectJeiAdapter());
    }

    @Override
    public AspectStack defaultContent() {
        return new AspectStack(Aspect.AIR, 1);
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
    public AspectStack copyInnerByModifier(AspectStack content, ContentModifier modifier) {
        AspectStack copy = content.copy();
        copy.amount = ((int) modifier.apply(content.amount));
        return copy;
    }

    @Override
    public ContentWidget<AspectStack> createContentWidget() {
        return new AspectStackWidget();
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[]{BlockInfo.fromBlockState(BlocksTC.jarNormal.getDefaultState()), BlockInfo.fromBlockState(BlocksTC.jarVoid.getDefaultState())};
    }

    @Override
    public AspectStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new AspectStack(Aspect.getAspect(jsonElement.getAsJsonObject().get("aspect").getAsString()), jsonElement.getAsJsonObject().get("amount").getAsInt());
    }

    @Override
    public JsonElement serialize(AspectStack aspectStack, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("aspect", aspectStack.aspect.getTag());
        jsonObj.addProperty("amount", aspectStack.amount);
        return jsonObj;
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
            if (capability == null || capability.getAspects() == null) return left;
            Iterator<AspectStack> iterator = left.iterator();
            if (io == IO.IN) {
                while (iterator.hasNext()) {
                    AspectStack aspectStack = iterator.next();
                    Aspect aspect = aspectStack.aspect;
                    int amount = aspectStack.amount;
                    if (!ArrayUtils.contains(capability.getAspects().getAspects(), aspect)) continue;
                    int stored = capability.getAspects().getAmount(aspect);
                    aspectStack.amount = Math.max(0, amount - stored);
                    if (!simulate) {
                        if (!capability.takeFromContainer(aspect, Math.min(stored, amount))) {
                            aspectStack.amount = amount;
                        }
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

        AspectList lastAspectList;

        @Override
        protected boolean hasInnerChanged() {
            IAspectContainer capability = getCapability();
            if (capability == null) return false;
            AspectList aspectList = capability.getAspects();
            if (lastAspectList != null) {
                return !aspectList.aspects.equals(lastAspectList.aspects);
            }
            lastAspectList = aspectList.copy();
            return true;
        }

    }

    public static class AspectJeiAdapter implements IJeiIngredientAdapter<AspectStack, AspectList> {

        @Override
        public Class<AspectStack> getInternalIngredientType() {
            return AspectStack.class;
        }

        @Override
        public IIngredientType<AspectList> getJeiIngredientType() {
            return AspectListIngredient.INSTANCE;
        }

        @Override
        public Stream<AspectList> apply(AspectStack aspectStack) {
            return Stream.of(aspectStack.toAspectList());
        }
    }
}
