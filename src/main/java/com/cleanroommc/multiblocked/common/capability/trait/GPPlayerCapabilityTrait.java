package com.cleanroommc.multiblocked.common.capability.trait;


import com.cleanroommc.multiblocked.api.capability.trait.PlayerCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.GPExtraUtilities2Capability;
import com.rwtema.extrautils2.power.Freq;
import com.rwtema.extrautils2.power.IPower;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.tile.XUTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class GPPlayerCapabilityTrait extends PlayerCapabilityTrait {
    public EntityPlayer player;
    public float power = 0;
    public int frequency;
    public int clearTick;

    public final IPower proxyProxy = new IPower() {
        @Override
        public float getPower() {
            return power;
        }

        @Override
        public IWorldPowerMultiplier getMultiplier() {
            return IWorldPowerMultiplier.CONSTANT;
        }

        @Override
        public int frequency() {
            return frequency;
        }

        @Override
        public void powerChanged(boolean b) {

        }

        @Nullable
        @Override
        public World world() {
            return component == null ? null : component.getWorld();
        }

        @Override
        public String getName() {
            return component == null ? "missing" : component.getUnlocalizedName();
        }

        @Override
        public boolean isLoaded() {
            return component != null && XUTile.isLoaded(component);
        }

        @Nullable
        @Override
        public BlockPos getLocation() {
            return component == null ? null : component.getPos();
        }
    };

    public GPPlayerCapabilityTrait() {
        super(GPExtraUtilities2Capability.CAP);
    }

    @Override
    public boolean hasUpdate() {
        return true;
    }

    public void updatePower(float power, int clearTick) {
        this.power = power;
        this.clearTick = Math.max(clearTick, this.clearTick);
    }

    @Override
    public void update() {
        if (component.getTimer() % 20 == 0 && player != getPlayer()) {
            if (player == null) {
                player = getPlayer();
                if (player instanceof EntityPlayerMP && PowerManager.instance.assignedValuesPlayer.containsKey(player)) {
                    frequency = Freq.getBasePlayerFreq((EntityPlayerMP) player);
                    PowerManager.instance.addPowerHandler(proxyProxy);
                }
            } else {
                player = null;
                PowerManager.instance.removePowerHandler(proxyProxy);
            }
        }

        if (clearTick > 0) {
            clearTick--;
            if (clearTick == 0) {
                power = 0;
            }
        }
    }
}
