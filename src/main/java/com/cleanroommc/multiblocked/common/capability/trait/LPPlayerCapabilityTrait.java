package com.cleanroommc.multiblocked.common.capability.trait;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.cleanroommc.multiblocked.api.capability.trait.PlayerCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.LPBloodMagicCapability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class LPPlayerCapabilityTrait extends PlayerCapabilityTrait {

    public LPPlayerCapabilityTrait() {
        super(LPBloodMagicCapability.CAP);
    }

    public SoulNetwork getCapability() {
        EntityPlayer player = getPlayer();
        return player == null ? null : NetworkHelper.getSoulNetwork(player);
    }

    public int updateLP(int inputLp, boolean simulate) {
        EntityPlayer player = getPlayer();
        if (player instanceof EntityPlayerMP) {
            SoulNetwork soulNetwork = getCapability();
            if (soulNetwork != null) {
                int stored = soulNetwork.getCurrentEssence();
                int lp = Math.max(0, stored + inputLp);
                if (!simulate) {
                    soulNetwork.setCurrentEssence(lp); // auto sync
                }
                return Math.abs(lp - (stored + inputLp));
            }
        }
        return Math.abs(inputLp);
    }
}
