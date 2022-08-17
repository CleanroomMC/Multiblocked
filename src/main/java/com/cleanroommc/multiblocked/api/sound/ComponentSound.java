package com.cleanroommc.multiblocked.api.sound;

import com.cleanroommc.multiblocked.api.tile.ComponentTileEntity;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2022/8/16
 * @implNote ComponentSound
 */
@SideOnly(Side.CLIENT)
public class ComponentSound extends MovingSound {
    @Nullable
    public final ComponentTileEntity<?> component;
    public final SoundState soundState;

    protected ComponentSound(SoundEvent soundEvent, SoundState soundState, @Nullable ComponentTileEntity<?> component) {
        super(soundEvent, SoundCategory.BLOCKS);
        this.component = component;
        this.soundState = soundState;
        this.repeat = soundState.loop;
        this.repeatDelay = soundState.delay;
        this.volume = soundState.volume;
        this.pitch = soundState.pitch;
        if (component == null) {
            this.attenuationType = AttenuationType.NONE;
            return;
        }
        this.xPosF = component.getPos().getX() + 0.5f;
        this.yPosF = component.getPos().getY() + 0.5f;
        this.zPosF = component.getPos().getZ() + 0.5f;
    }

    @Override
    public void update() {
        if (component != null) {
            World level = component.getWorld();
            if (!component.getStatus().equals(soundState.status) || component.isInvalid() || level.getTileEntity(component.getPos()) != component) {
                stopSound();
            }
        }
    }

    public void stopSound() {
        donePlaying = true;
        repeat = false;
    }

}
