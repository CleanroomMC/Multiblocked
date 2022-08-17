package com.cleanroommc.multiblocked.api.sound;

import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2022/8/16
 * @implNote SoundState
 */
public class SoundState {
    public static final SoundState EMPTY = new SoundState(new ResourceLocation("multiblocked:empty"));
    private transient boolean init;
    private transient SoundEvent soundEvent;
    public transient String status;
    public final ResourceLocation sound;
    public boolean loop;
    public int delay;
    public float volume;
    public float pitch;

    public SoundState(ResourceLocation sound) {
        this.sound = sound;
        this.loop = false;
        this.delay = 0;
        this.volume = 1;
        this.pitch = 1;
    }

    public SoundState copy(ResourceLocation sound) {
        SoundState soundState = new SoundState(sound);
        soundState.loop = this.loop;
        soundState.delay = this.delay;
        soundState.volume = this.volume;
        soundState.pitch = this.pitch;
        return soundState;
    }

    public SoundState copy() {
        if (this == EMPTY) return EMPTY;
        return copy(sound);
    }

    @Nullable
    public SoundEvent getSoundEvent() {
        if (this == EMPTY) return null;
        if (soundEvent == null && !init) {
            init = true;
            soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(sound);
        }
        return soundEvent;
    }

    @SideOnly(Side.CLIENT)
    public ComponentSound playGUISound() {
        if (this == EMPTY) return null;
        SoundEvent sound = getSoundEvent();
        ComponentSound componentSound = null;
        if (sound != null) {
            Minecraft.getMinecraft().getSoundHandler().playSound(componentSound = new ComponentSound(sound, this, null));
        }
        return componentSound;
    }

    @SideOnly(Side.CLIENT)
    public ComponentSound playSound(ComponentTileEntity<?> component) {
        if (this == EMPTY) return null;
        SoundEvent sound = getSoundEvent();
        ComponentSound componentSound = null;
        if (sound != null) {
            Minecraft.getMinecraft().getSoundHandler().playSound(componentSound = new ComponentSound(sound, this, component));
        }
        return componentSound;
    }

    public boolean playLevelSound(World level, BlockPos pos) {
        if (this == EMPTY) return false;
        SoundEvent sound = getSoundEvent();
        if (sound == null) return false;
        level.playSound(null, pos, sound, SoundCategory.BLOCKS, volume, pitch);
        return true;
    }
}
