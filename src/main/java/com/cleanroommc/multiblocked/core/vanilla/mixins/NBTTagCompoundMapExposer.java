package com.cleanroommc.multiblocked.core.vanilla.mixins;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(NBTTagCompound.class)
public interface NBTTagCompoundMapExposer {

    @Accessor
    Map<String, NBTBase> getTagMap();

}
