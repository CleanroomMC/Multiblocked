package com.cleanroommc.multiblocked.common.capability.trait;

import com.cleanroommc.multiblocked.api.capability.trait.PlayerCapabilityTrait;
import com.cleanroommc.multiblocked.common.capability.EMCProjectECapability;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class EMCPlayerCapabilityTrait extends PlayerCapabilityTrait {

    public long emc;
    public int clearTick;
    public EntityPlayer player;

    private boolean needsSync = false;

    public EMCPlayerCapabilityTrait() {
        super(EMCProjectECapability.CAP);
    }

    public long updateEMC(long emc, int clearTick, boolean simulate) {
        this.clearTick = clearTick;
        player = getPlayer();
        if (player instanceof EntityPlayerMP) {
            IKnowledgeProvider emcCap = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
            if (emcCap != null) {
                if (emcCap.getEmc() + emc > 0) {
                    this.emc = emcCap.getEmc() + emc;
                } else {
                    this.emc = 0;
                    return Math.abs(emcCap.getEmc() + emc);
                }
                if (!simulate) needsSync = true;
            }
        }
        return 0L;
    }

    @Override
    public boolean hasUpdate() {
        return true;
    }

    @Override
    public void update() {
        if (component.getTimer() % 20 == 0) {
            if (player == null) {
                player = getPlayer();
                if (player instanceof EntityPlayerMP) {
                    IKnowledgeProvider emcCap = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
                    if (emcCap != null) {
                        if (needsSync) {
                            emcCap.setEmc(this.emc);
                        }
                        this.emc = emcCap.getEmc();
                    }
                }
            } else {
                player = null;
            }
        }

        if (clearTick > 0) {
            clearTick--;
            if (clearTick == 0) {
                emc = 0;
            }
        }
    }
}