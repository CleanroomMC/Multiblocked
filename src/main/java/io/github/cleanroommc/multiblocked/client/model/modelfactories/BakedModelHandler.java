package io.github.cleanroommc.multiblocked.client.model.modelfactories;

import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BakedModelHandler {

    @SubscribeEvent
    public void onModelsBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(BlockComponent.MODEL_LOCATION, BlockComponentBakedModel.INSTANCE);
    }

}

