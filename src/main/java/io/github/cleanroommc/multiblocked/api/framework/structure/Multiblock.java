package io.github.cleanroommc.multiblocked.api.framework.structure;

import com.google.gson.*;
import io.github.cleanroommc.multiblocked.persistence.MultiblockWorldSavedData;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import io.github.cleanroommc.multiblocked.Multiblocked;
import io.github.cleanroommc.multiblocked.api.framework.structure.definition.IDefinition;
import io.github.cleanroommc.multiblocked.api.framework.structure.definition.NameDefinition;
import io.github.cleanroommc.multiblocked.util.Utils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The definition of a "Multiblock"
 */
public class Multiblock {

    public static final JsonDeserializer<Multiblock> DESERIALIZER = new Deserializer();

    private static Map<String, Multiblock> VALID_MULTIBLOCKS;
    private static Map<IDefinition, Collection<Multiblock>> CONTROLLER_MAPPING;

    // TODO: Preliminary, streamline this
    public static void loadMultiblocks() {
        VALID_MULTIBLOCKS = new Object2ReferenceOpenHashMap<>();
        CONTROLLER_MAPPING = new Reference2ObjectOpenHashMap<>();
        try {
            File multiblockedFolder = new File(Launch.minecraftHome, "config/multiblocked/definitions/structure");
            multiblockedFolder.mkdirs();
            File[] jsons = multiblockedFolder.listFiles((file, name) -> name.endsWith(".json"));
            if (jsons == null) {
                return;
            }
            for (File json : jsons) {
                Reader reader = Files.newBufferedReader(json.toPath());
                Multiblock multiblock = Multiblocked.GSON.fromJson(reader, Multiblock.class);
                Multiblocked.LOGGER.info("{} registered", multiblock.unlocalizedName);
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Multiblock get(String unlocalizedName) {
        return VALID_MULTIBLOCKS.get(unlocalizedName);
    }

    public static Stream<Multiblock> getAll() {
        return CONTROLLER_MAPPING.values().stream().flatMap(Collection::stream);
    }

    public static Stream<ItemStack> getAllCatalysts() {
        return CONTROLLER_MAPPING.values().stream().flatMap(Collection::stream).map(Multiblock::getCatalyst);
    }

    public static Stream<IDefinition> getAllControllers() {
        return CONTROLLER_MAPPING.values().stream().flatMap(Collection::stream).map(Multiblock::getController);
    }

    public static boolean isValidController(IBlockState state) {
        return CONTROLLER_MAPPING.containsKey(state);
    }

    public static Collection<Multiblock> getCandidates(IBlockState state, ItemStack stack) {
        return CONTROLLER_MAPPING.entrySet().stream()
                .filter(entry -> entry.getKey().test(state))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(multiblock -> Utils.areStacksSimilar(stack, multiblock.getCatalyst()))
                .collect(Collectors.toList());
    }

    private final String unlocalizedName;
    private final ItemStack catalyst;
    private final boolean toConsumeCatalyst, disallowOverlap, externalIO;
    private final Layout layout;

    protected Multiblock(String unlocalizedName, ItemStack catalyst, boolean toConsumeCatalyst, boolean disallowOverlap, boolean externalIO, Layout layout) {
        this.unlocalizedName = unlocalizedName;
        this.catalyst = catalyst;
        this.toConsumeCatalyst = toConsumeCatalyst;
        this.disallowOverlap = disallowOverlap;
        this.externalIO = externalIO;
        this.layout = layout;
        if (VALID_MULTIBLOCKS.put(this.unlocalizedName, this) != null) {
            Multiblocked.LOGGER.throwing(Level.FATAL, new IllegalArgumentException(unlocalizedName + " already has a registered Multiblock!"));
        }
        CONTROLLER_MAPPING.computeIfAbsent(layout.getController(), k -> new ReferenceOpenHashSet<>()).add(this);
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public TextComponentTranslation getLocalizedName() {
        return new TextComponentTranslation(unlocalizedName);
    }

    public String getNametagText(MultiblockInstance instance) {
        MultiblockInstance.Status status = instance.getStatus();
        if (status == null) {
            return new TextComponentTranslation(unlocalizedName).getFormattedText();
        }
        return new TextComponentTranslation(unlocalizedName).getFormattedText() + " " + status.getString();
    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public boolean toConsumeCatalyst() {
        return toConsumeCatalyst;
    }

    public boolean disallowOverlap() {
        return disallowOverlap;
    }

    public boolean acceptsExternalIO() {
        return externalIO;
    }

    public Layout getLayout() {
        return layout;
    }

    public IDefinition getController() {
        return layout.getController();
    }

    @Nullable
    public Pair<Vec3i, NameDefinition> getNameTag() {
        return layout.getNameTag();
    }

    public boolean check(World world, BlockPos controllerPos, EnumFacing facing) {
        Set<BlockPos> correctPositions = new ObjectOpenHashSet<>(layout.getPositions().size());
        for (Map.Entry<Vec3i, IDefinition> entry : layout.getPositions().entrySet()) {
            BlockPos pos = Utils.rotate(controllerPos, entry.getKey(), facing);
            if (!entry.getValue().test(world.getBlockState(pos))) {
                return false;
            }
            correctPositions.add(pos);
        }
        return MultiblockWorldSavedData.getOrCreate(world).getInstances()
                .stream()
                .filter(instance -> instance.getMultiblock().disallowOverlap())
                .map(MultiblockInstance::getPositions)
                .flatMap(Collection::stream)
                .noneMatch(correctPositions::contains);
    }

    public boolean fastCheck(World world, BlockPos controllerPos, EnumFacing facing) {
        for (Map.Entry<Vec3i, IDefinition> entry : layout.getPositions().entrySet()) {
            BlockPos pos = Utils.rotate(controllerPos, entry.getKey(), facing);
            if (!entry.getValue().test(world.getBlockState(pos))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return unlocalizedName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Multiblock) {
            return ((Multiblock) obj).unlocalizedName.equals(this.unlocalizedName);
        }
        return false;
    }

    public static class Deserializer implements JsonDeserializer<Multiblock> {

        @Override
        public Multiblock deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            String unlocalizedName = "multiblocked.multiblock.name." + JsonUtils.getString(jsonObj, "name");
            ItemStack catalyst = context.deserialize(JsonUtils.getJsonObject(jsonObj, "catalyst"), ItemStack.class);
            boolean consumeCatalyst = JsonUtils.getBoolean(jsonObj, "consume_catalyst", false);
            boolean disallowOverlap = JsonUtils.getBoolean(jsonObj, "disallow_overlap", false);
            boolean externalIO = JsonUtils.getBoolean(jsonObj, "external_io", true);
            Layout layout = context.deserialize(JsonUtils.getJsonObject(jsonObj, "layout"), Layout.class);
            return new Multiblock(unlocalizedName, catalyst, consumeCatalyst, disallowOverlap, externalIO, layout);
        }

    }

}
