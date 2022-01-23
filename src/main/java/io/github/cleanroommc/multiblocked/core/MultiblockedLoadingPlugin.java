package io.github.cleanroommc.multiblocked.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

public class MultiblockedLoadingPlugin implements IFMLLoadingPlugin {

    public MultiblockedLoadingPlugin() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.multiblocked.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"io.github.cleanroommc.multiblocked.core.ASMTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
