package com.cleanroommc.multiblocked;

import com.cleanroommc.multiblocked.api.capability.IO;
import com.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import com.cleanroommc.multiblocked.api.definition.PartDefinition;
import com.cleanroommc.multiblocked.api.gui.texture.ItemStackTexture;
import com.cleanroommc.multiblocked.api.gui.widget.imp.blueprint_table.JsonBlockPatternWidget;
import com.cleanroommc.multiblocked.api.item.ItemMultiblockBuilder.BuilderRecipeLogic;
import com.cleanroommc.multiblocked.api.pattern.FactoryBlockPattern;
import com.cleanroommc.multiblocked.api.pattern.Predicates;
import com.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import com.cleanroommc.multiblocked.api.recipe.RecipeMap;
import com.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import com.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import com.cleanroommc.multiblocked.api.registry.MultiblockedItems;
import com.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import com.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.GeoComponentRenderer;
import com.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import com.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import com.cleanroommc.multiblocked.events.Listeners;
import com.cleanroommc.multiblocked.network.MultiblockedNetworking;
import com.cleanroommc.multiblocked.common.capability.AspectThaumcraftCapability;
import com.cleanroommc.multiblocked.common.capability.GasMekanismCapability;
import com.cleanroommc.multiblocked.common.capability.HeatMekanismCapability;
import com.cleanroommc.multiblocked.common.capability.ManaBotainaCapability;
import com.cleanroommc.multiblocked.common.capability.ParticleQMDCapability;
import com.cleanroommc.multiblocked.proxies.thaumicjei.IThaumicJEIProxy;
import lach_01298.qmd.particle.ParticleStack;
import lach_01298.qmd.particle.Particles;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import thaumcraft.api.aspects.Aspect;

@Mod.EventBusSubscriber(modid = Multiblocked.MODID)
public class CommonProxy {

    public static IThaumicJEIProxy thaumicJEIProxy = null;

    public void preInit() {
        if (Multiblocked.isModLoaded(Multiblocked.MODID_JEI) && Multiblocked.isModLoaded(Multiblocked.MODID_TC6)) {
            try {
                if (Multiblocked.isModLoaded(Multiblocked.MODID_THAUMJEI)) {
                    thaumicJEIProxy = Class.forName("com.cleanroommc.multiblocked.proxies.thaumicjei.ActiveThaumicJEIProxy").asSubclass(IThaumicJEIProxy.class).newInstance();
                } else {
                    thaumicJEIProxy = Class.forName("com.cleanroommc.multiblocked.proxies.thaumicjei.DummyThaumicJEIProxy").asSubclass(IThaumicJEIProxy.class).newInstance();
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException exception) {
                throw new LoaderException("Failed to load the proxy for Thaumic JEI");
            }
        }

        MinecraftForge.EVENT_BUS.register(Listeners.class);
        MultiblockedNetworking.init();
        MultiblockCapabilities.registerCapabilities();
    }

    public void init() {

    }

    public void postInit() {

    }

    public static void registerComponents(){
        // register any capability block
        MultiblockCapabilities.registerAnyCapabilityBlocks();
        // register blueprint table
        BlueprintTableTileEntity.registerBlueprintTable();
        // register JsonBlock
        JsonBlockPatternWidget.registerBlock();

        // create a part component.
        PartDefinition partDefinition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "test_part"));
        MultiblockComponents.registerComponent(partDefinition);
        partDefinition.formedRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/emitter"));
        partDefinition.baseRenderer = new BlockStateRenderer(Blocks.BEDROCK.getDefaultState());
        partDefinition.isOpaqueCube = false;
        partDefinition.allowRotate = false;

        // create a recipeMap.
        RecipeMap recipeMap = new RecipeMap("test_recipe_map");
        recipeMap.start()
                .inputItems(0.85f, new ItemsIngredient(2, new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.IRON_INGOT)))
                .outputItems(new ItemStack(Items.APPLE, 10))
                .inputFluids(new FluidStack(FluidRegistry.LAVA, 2000))
                .outputMana(0.125f, 100)
                .outputAspects(new AspectStack(Aspect.AURA, 50))
                .duration(2000) // 60 tick -> 3s
                .buildAndRegister();
        recipeMap.start()
                .inputGas(0.3f, new GasStack(GasRegistry.getGas(0), 150))
                .inputAspects(new AspectStack(Aspect.AURA, 50))
                .outputHeat(100)
                .duration(200) // 60 tick -> 3s
                .buildAndRegister();
        recipeMap.start()
                .inputAspects(new AspectStack(Aspect.AIR, 50))
                .outputParticles(new ParticleStack(Particles.antialpha, 32, 132, 123),
                        new ParticleStack(Particles.charm, 11, 21, 222))
                .duration(200) // 60 tick -> 3s
                .buildAndRegister();
        // create a controller component.
        ControllerDefinition controllerDefinition = new ControllerDefinition(new ResourceLocation(Multiblocked.MODID,"test_block"));
        MultiblockComponents.registerComponent(controllerDefinition);
        controllerDefinition.recipeMap = recipeMap;
        FactoryBlockPattern factory = FactoryBlockPattern.start()
                .aisle("TXX", " E ")
                .aisle("C#A", "QPW")
                .aisle("BYD", "   ")
                .where(' ', Predicates.any())
                .where('P', Predicates.component(partDefinition))
                .where('X', Predicates.blocks(Blocks.STONE))
                .where('#', Predicates.air())
                .where('E', Predicates.anyCapability(IO.OUT, ParticleQMDCapability.CAP))
                .where('Q', Predicates.anyCapability(IO.IN, GasMekanismCapability.CAP))
                .where('W', Predicates.anyCapability(IO.OUT, HeatMekanismCapability.CAP))
                .where('A', Predicates.anyCapability(IO.IN, MultiblockCapabilities.ITEM)) // if and only if available IN-Item-Capability here. (item inputBus)
                .where('T', Predicates.anyCapability(IO.IN, MultiblockCapabilities.FLUID)) // if and only if available IN-Item-Capability here. (item inputBus)
                .where('B', Predicates.anyCapability(IO.OUT, ManaBotainaCapability.CAP)) // if and only if available IN-Item-Capability here. (item inputBus)
                .where('D', Predicates.anyCapability(IO.BOTH, AspectThaumcraftCapability.CAP)) // if and only if available IN-Item-Capability here. (item inputBus)
                .where('C', Predicates.blocks(Blocks.CHEST)) // tho not define a specific Capability here. it will still be detected according to the recipeMap, so will create a proxy of the BOTH-Item-Capability here. (item in/outputBus)
                .where('Y', Predicates.component(controllerDefinition));
        controllerDefinition.basePattern = factory.build();
//        controllerDefinition.formedRenderer = OBJRenderer.get(new ResourceLocation(Multiblocked.MODID,"models/obj/energy_core_model.obj"), BlockRenderLayer.SOLID);
        controllerDefinition.formedRenderer = new GeoComponentRenderer("botarium");
        controllerDefinition.baseRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"test_model"));
        controllerDefinition.isOpaqueCube = false;
        recipeMap.categoryTexture = new ItemStackTexture(controllerDefinition.getStackForm());

//        String result = Multiblocked.GSON.toJson(controllerDefinition);
//        controllerDefinition = Multiblocked.GSON.fromJson(result, ControllerDefinition.class);
//        System.out.println();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        registerComponents();
        IForgeRegistry<Block> registry = event.getRegistry();
        MultiblockComponents.registerBlocks(registry);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        MultiblockComponents.COMPONENT_ITEMS_REGISTRY.values().forEach(registry::register);
        MultiblockedItems.registerItems(registry);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        ForgeRegistries.RECIPES.register(new BuilderRecipeLogic().setRegistryName(Multiblocked.MODID, "builder"));
    }

}

