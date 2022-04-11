package com.cleanroommc.multiblocked.common.capability;

import com.cleanroommc.multiblocked.api.capability.CapCapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.capability.MultiblockCapability;
import com.cleanroommc.multiblocked.api.gui.texture.TextTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.recipe.ContentWidget;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.cleanroommc.multiblocked.api.recipe.Recipe;
import com.cleanroommc.multiblocked.api.registry.MbdCapabilities;
import com.cleanroommc.multiblocked.common.capability.widget.NumberContentWidget;
import com.cleanroommc.multiblocked.util.world.DummyWorld;
import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FEMultiblockCapability extends MultiblockCapability<Integer> {

    public FEMultiblockCapability() {
        super("forge_energy", new Color(0xCB0000).getRGB());
    }

    @Override
    public Integer defaultContent() {
        return 500;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return !getCapability(CapabilityEnergy.ENERGY, tileEntity).isEmpty();
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public FECapabilityProxy createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return new FECapabilityProxy(tileEntity);
    }

    @Override
    public ContentWidget<? super Integer> createContentWidget() {
        return new NumberContentWidget().setContentTexture(new TextTexture("FE", color)).setUnit("FE");
    }

    @Override
    public BlockInfo[] getCandidates() {
        List<BlockInfo> list = new ArrayList<>();
        DummyWorld dummyWorld = new DummyWorld();
        for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if (block.getRegistryName() != null) {
                String path = block.getRegistryName().getPath();
                if (path.contains("energy") || path.contains("rf")) {
                    try {
                        if (block.hasTileEntity(block.getDefaultState())) {
                            TileEntity tileEntity = block.createTileEntity(dummyWorld, block.getDefaultState());
                            if (tileEntity != null && isBlockHasCapability(IO.BOTH, tileEntity)) {
                                list.add(new BlockInfo(block.getDefaultState(), tileEntity));
                            }
                        }
                    } catch (Throwable ignored) { }
                }
            }
        }
        return list.toArray(new BlockInfo[0]);
    }

    @Override
    public Integer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return jsonElement.getAsInt();
    }

    @Override
    public JsonElement serialize(Integer integer, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(integer);
    }

    public static class FECapabilityProxy extends CapCapabilityProxy<IEnergyStorage, Integer> {

        public FECapabilityProxy(TileEntity tileEntity) {
            super(MbdCapabilities.FE, tileEntity, CapabilityEnergy.ENERGY);
        }

        @Override
        protected List<Integer> handleRecipeInner(IO io, Recipe recipe, List<Integer> left, boolean simulate) {
            IEnergyStorage capability = getCapability();
            if (capability == null) return left;
            int sum = left.stream().reduce(0, Integer::sum);
            if (io == IO.IN) {
                sum = sum - capability.extractEnergy(sum, simulate);
            } else if (io == IO.OUT) {
                sum = sum - capability.receiveEnergy(sum, simulate);
            }
            return sum <= 0 ? null : Collections.singletonList(sum);
        }

    }
}
