package com.cleanroommc.multiblocked.client.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IParticleHandler {
    /**
     * Run any pre render gl code here.
     * You can also start drawing quads.
     */
    @SideOnly(Side.CLIENT)
    void preDraw(BufferBuilder buffer);

    /**
     * Run any post render gl code here.
     * This is where you would draw if you started drawing in preDraw
     */
    @SideOnly(Side.CLIENT)
    void postDraw(BufferBuilder buffer);

    IParticleHandler DEFAULT_FX_HANDLER = new IParticleHandler() {
        @Override
        public void preDraw(BufferBuilder buffer) {

        }

        @Override
        public void postDraw(BufferBuilder buffer) {
        }
    };
}
