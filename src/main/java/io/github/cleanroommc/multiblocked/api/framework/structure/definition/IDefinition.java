package io.github.cleanroommc.multiblocked.api.framework.structure.definition;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;

public interface IDefinition extends Predicate<IBlockState> {

    JsonDeserializer<IDefinition> DESERIALIZER = new Deserializer();

    default boolean isController() {
        return false;
    }

    class Deserializer implements JsonDeserializer<IDefinition> {

        static final Type type = new TypeToken<Map<String, String>>(){}.getType();

        private Deserializer() { }

        @Override
        public IDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            String blockName = JsonUtils.getString(jsonObj, "block", "");
            if (blockName.isEmpty()) {
                if (JsonUtils.getBoolean(jsonObj, "nametag", false)) {
                    return NameDefinition.INSTANCE;
                }
                throw new IllegalArgumentException("Block " + blockName + " does not exist!");
            }
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
            if (block == null) {
                throw new IllegalArgumentException("Block " + blockName + " does not exist!");
            }
            boolean isController = JsonUtils.getBoolean(jsonObj, "controller", false);
            if (block == Blocks.AIR) {
                if (isController) {
                    throw new IllegalArgumentException("Controller block cannot be AIR.");
                }
                return BlockDefinition.getAir();
            }
            BlockDefinition definition;
            JsonElement meta = jsonObj.get("meta");
            if (meta == null) {
                JsonElement properties = jsonObj.get("properties");
                if (properties == null) {
                    definition = new BlockDefinition(block, isController);
                } else {
                    definition = new BlockPropertiesDefinition(block, context.deserialize(properties, type), isController);
                }
            } else {
                definition = new BlockMetaDefinition(block, meta.getAsInt(), isController);
            }
            return definition;
        }

    }

}
