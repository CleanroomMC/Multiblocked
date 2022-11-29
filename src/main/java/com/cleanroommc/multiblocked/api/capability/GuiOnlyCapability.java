package com.cleanroommc.multiblocked.api.capability;

import com.cleanroommc.multiblocked.api.capability.proxy.CapabilityProxy;
import com.cleanroommc.multiblocked.api.capability.trait.CapabilityTrait;
import com.cleanroommc.multiblocked.api.pattern.util.BlockInfo;
import com.google.gson.*;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/11/29
 * @implNote GuiOnlyCapability
 */
public class GuiOnlyCapability extends MultiblockCapability<Double> {
    Supplier<CapabilityTrait> supplier;

    public GuiOnlyCapability(String name, Supplier<CapabilityTrait> supplier) {
        super(name, 0xffafafaf);
        this.supplier = supplier;
    }

    @Override
    public Double defaultContent() {
        return 0d;
    }

    @Override
    public boolean isBlockHasCapability(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return false;
    }

    @Override
    public Double copyInner(Double content) {
        return 0d;
    }

    @Override
    protected CapabilityProxy<? extends Double> createProxy(@Nonnull IO io, @Nonnull TileEntity tileEntity) {
        return null;
    }

    @Override
    public BlockInfo[] getCandidates() {
        return new BlockInfo[0];
    }

    @Override
    public boolean hasTrait() {
        return true;
    }

    @Override
    public CapabilityTrait createTrait() {
        return supplier.get();
    }

    @Override
    public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return 0d;
    }

    @Override
    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(0d);
    }
}
