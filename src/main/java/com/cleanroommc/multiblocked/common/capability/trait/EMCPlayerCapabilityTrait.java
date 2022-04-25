package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.trait.PlayerCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.EMCProjectECapability;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class EMCPlayerCapabilityTrait extends PlayerCapabilityTrait {

    public EMCPlayerCapabilityTrait() {
        super(EMCProjectECapability.CAP);
    }

    public IKnowledgeProvider getCapability() {
        EntityPlayer player = getPlayer();
        return player == null ? null : player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
    }

    public long updateEMC(long emc, boolean simulate) {
        EntityPlayer player = getPlayer();
        if (player instanceof EntityPlayerMP) {
            IKnowledgeProvider emcCap = getCapability();
            if (emcCap != null) {
                long stored = emcCap.getEmc();
                long emcL = Math.max(0, stored + emc);
                if (!simulate) {
                    emcCap.setEmc(emcL);
                    emcCap.sync((EntityPlayerMP) player); // send to client
                }
                return Math.abs(emcL - (stored + emc));
            }
        }
        return Math.abs(emc);
    }
}