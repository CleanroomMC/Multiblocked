package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.trait.PlayerCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.EMCProjectECapability;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class EMCPlayerCapabilityTrait extends PlayerCapabilityTrait {

    public long emc;
    public EntityPlayer player;

    public EMCPlayerCapabilityTrait() {
        super(EMCProjectECapability.CAP);
    }

    public IKnowledgeProvider getCapability() {
        return player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
    }

    public long updateEMC(long emc, boolean simulate) {
        player = getPlayer();
        if (player instanceof EntityPlayerMP) {
            IKnowledgeProvider emcCap = getCapability();
            if (emcCap != null) {
                if (emcCap.getEmc() + emc > 0) {
                    this.emc = emcCap.getEmc() + emc;
                } else {
                    this.emc = 0;
                    return Math.abs(emcCap.getEmc() + emc);
                }
                if (!simulate) {
                    emcCap.setEmc(this.emc);
                    emcCap.sync((EntityPlayerMP) player); // send to client
                }
            }
        }
        return 0L;
    }
}