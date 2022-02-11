package io.github.cleanroommc.multiblocked;

import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.block.ItemComponent;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.definition.PartDefinition;
import io.github.cleanroommc.multiblocked.api.pattern.FactoryBlockPattern;
import io.github.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import io.github.cleanroommc.multiblocked.api.recipe.RecipeMap;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.api.tile.BlueprintTableTileEntity;
import io.github.cleanroommc.multiblocked.client.renderer.impl.BlockStateRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.OBJRenderer;
import io.github.cleanroommc.multiblocked.common.capability.AspectThaumcraftCapability;
import io.github.cleanroommc.multiblocked.common.capability.GasMekanismCapability;
import io.github.cleanroommc.multiblocked.common.capability.HeatMekanismCapability;
import io.github.cleanroommc.multiblocked.common.capability.ManaBotainaCapability;
import io.github.cleanroommc.multiblocked.common.recipe.content.AspectStack;
import io.github.cleanroommc.multiblocked.events.Listeners;
import io.github.cleanroommc.multiblocked.network.MultiblockedNetworking;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import thaumcraft.api.aspects.Aspect;

import java.util.function.Function;

import static io.github.cleanroommc.multiblocked.api.pattern.Predicates.air;
import static io.github.cleanroommc.multiblocked.api.pattern.Predicates.any;
import static io.github.cleanroommc.multiblocked.api.pattern.Predicates.anyCapability;
import static io.github.cleanroommc.multiblocked.api.pattern.Predicates.blocks;

@Mod.EventBusSubscriber(modid = Multiblocked.MODID)
public class CommonProxy {

    public void preInit() {
        MinecraftForge.EVENT_BUS.register(Listeners.class);
        MultiblockedNetworking.init();
        MultiblockCapabilities.registerCapabilities();
    }

    public void init() {

    }

    public void postInit() {

    }

    public static void registerComponents(){
        // register blueprint table
        BlueprintTableTileEntity.registerBlueprintTable();
        
        // create a part component.
        PartDefinition partDefinition = new PartDefinition(new ResourceLocation(Multiblocked.MODID, "test_part"));
        partDefinition.formedRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/emitter"))
                .setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED, true);
        partDefinition.baseRenderer = new BlockStateRenderer(Blocks.BEDROCK.getDefaultState());
        partDefinition.isOpaqueCube = false;
        partDefinition.allowRotate = false;
        MultiblockComponents.registerComponent(partDefinition);

        // create a recipeMap.
        RecipeMap recipeMap = new RecipeMap("test_recipe_map");
        recipeMap.start()
                .inputItems(new ItemsIngredient(2, new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.IRON_INGOT)))
                .outputItems(new ItemStack(Items.APPLE, 10))
                .inputFluids(new FluidStack(FluidRegistry.LAVA, 2000))
                .outputMana(100)
                .outputAspects(new AspectStack(Aspect.AURA, 50))
                .duration(60) // 60 tick -> 3s
                .buildAndRegister();
        recipeMap.start()
                .inputGas(new GasStack(GasRegistry.getGas(0), 150))
                .inputAspects(new AspectStack(Aspect.AURA, 50))
                .outputHeat(100)
                .duration(60) // 60 tick -> 3s
                .buildAndRegister();
        // create a controller component.
        ControllerDefinition controllerDefinition = new ControllerDefinition(new ResourceLocation(Multiblocked.MODID,"test_block"), recipeMap);
        controllerDefinition.basePattern = FactoryBlockPattern.start()
                .aisle("TXX", "   ")
                .aisle("C#A", "QPW")
                .aisle("BYD", "   ")
                .where(' ', any())
                .where('P', partDefinition.selfPredicate())
                .where('X', blocks(Blocks.STONE))
                .where('#', air())
                .where('Q', anyCapability(IO.IN, GasMekanismCapability.CAP))
                .where('W', anyCapability(IO.OUT, HeatMekanismCapability.CAP))
                .where('A', anyCapability(IO.IN, MultiblockCapabilities.ITEM)) // if and only if available IN-Item-Capability here. (item inputBus)
                .where('T', anyCapability(IO.IN, MultiblockCapabilities.FLUID)) // if and only if available IN-Item-Capability here. (item inputBus)
                .where('B', anyCapability(IO.OUT, ManaBotainaCapability.CAP)) // if and only if available IN-Item-Capability here. (item inputBus)
                .where('D', anyCapability(IO.BOTH, AspectThaumcraftCapability.CAP)) // if and only if available IN-Item-Capability here. (item inputBus)
                .where('C', blocks(Blocks.CHEST)) // tho not define a specific Capability here. it will still be detected according to the recipeMap, so will create a proxy of the BOTH-Item-Capability here. (item in/outputBus)
                .where('Y', controllerDefinition.selfPredicate(true))
                .build();
        controllerDefinition.formedRenderer = new OBJRenderer(new ResourceLocation(Multiblocked.MODID,"models/obj/energy_core_model.obj"))
                .setRenderLayer(BlockRenderLayer.SOLID, true);
        controllerDefinition.baseRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"test_model"))
                .setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED, true);
        controllerDefinition.isOpaqueCube = false;
//        controllerDefinition.disableOthersRendering = true;
        MultiblockComponents.registerComponent(controllerDefinition);

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        registerComponents();
        MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.values().forEach(registry::register);
        MultiblockComponents.registerTileEntity();
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (BlockComponent block : MultiblockComponents.COMPONENT_BLOCKS_REGISTRY
                .values()) {
            registry.register(createItemBlock(block, ItemComponent::new));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(block.getRegistryName());
        return itemBlock;
    }

}

