package io.github.cleanroommc.multiblocked.api.framework.structure;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.Pair;
import io.github.cleanroommc.multiblocked.api.framework.structure.definition.IDefinition;
import io.github.cleanroommc.multiblocked.api.framework.structure.definition.NameDefinition;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Layout {

    public static final JsonDeserializer<Layout> DESERIALIZER = new Deserializer();

    private final Map<Vec3i, IDefinition> positions;
    private final IDefinition controller;

    private Pair<Vec3i, NameDefinition> name;

    public Layout(Map<Vec3i, IDefinition> positions) {
        this.positions = positions;
        IDefinition controller = null;
        for (Map.Entry<Vec3i, IDefinition> entry : positions.entrySet()) {
            if (controller != null && this.name != null) {
                break;
            }
            IDefinition value = entry.getValue();
            if (value.isController()) {
                controller = value;
            } else if (value instanceof NameDefinition) {
                this.name = Pair.of(entry.getKey(), (NameDefinition) value);
            }
        }
        this.controller = controller;
    }

    public IDefinition getController() {
        return controller;
    }

    public Map<Vec3i, IDefinition> getPositions() {
        return positions;
    }

    public Pair<Vec3i, NameDefinition> getNameTag() {
        return name;
    }

    public static class Deserializer implements JsonDeserializer<Layout> {

        static final Type definitionsType = new TypeToken<Map<String, IDefinition>>(){}.getType();

        private Deserializer() { }

        @Override
        public Layout deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            JsonArray patternArray = JsonUtils.getJsonArray(jsonObj, "pattern");
            List<List<String>> rawPattern = new ArrayList<>();
            for (JsonElement element : patternArray) {
                List<String> rows = new ArrayList<>();
                rawPattern.add(rows);
                for (JsonElement jsonElement : (JsonArray) element) {
                    rows.add(jsonElement.getAsString());
                }
            }
            Map<Vec3i, IDefinition> positions = getRelativePositions(rawPattern, getBlockDefinitions(jsonObj, context));
            return new Layout(positions);
        }

        private Map<Vec3i, IDefinition> getRelativePositions(List<List<String>> rawPattern, Char2ObjectMap<IDefinition> blockDefinitions) {
            Vec3i controllerPos = Vec3i.NULL_VECTOR;
            Map<Vec3i, IDefinition> relativePositions = new Object2ObjectOpenHashMap<>();
            char controller = blockDefinitions.char2ObjectEntrySet().stream().filter(e -> e.getValue().isController()).map(Char2ObjectMap.Entry::getCharKey).findFirst().get();
            boolean gotController = false;
            for (int y = 0; y < rawPattern.size(); y++) {
                List<String> layer = rawPattern.get(y);
                for (int z = 0; z < layer.size(); z++) {
                    String row = layer.get(z);
                    for (int x = 0; x < row.length(); x++) {
                        char block = row.charAt(x);
                        if (gotController && block != ' ') {
                            IDefinition definition = blockDefinitions.get(block);
                            if (definition == null) {
                                throw new IllegalArgumentException("Character " + block + " isn't attached to any block definitions.");
                            }
                            relativePositions.put(new Vec3i(controllerPos.getX() - x, y - controllerPos.getY(), controllerPos.getZ() - z), definition);
                        } else if (block == controller) {
                            relativePositions.put(controllerPos = new Vec3i(x, y, z), blockDefinitions.get(controller));
                            gotController = true;
                            // Restart loop
                            y = -1;
                            x = row.length();
                            z = layer.size();
                            break;
                        }
                    }
                }
            }
            return relativePositions;
        }

        private Char2ObjectMap<IDefinition> getBlockDefinitions(JsonObject jsonObj, JsonDeserializationContext context) {
            Map<String, IDefinition> rawDefinitions = context.deserialize(JsonUtils.getJsonObject(jsonObj, "definitions"), definitionsType);
            Char2ObjectMap<IDefinition> definitions = new Char2ObjectOpenHashMap<>(rawDefinitions.size());
            boolean controllerFound = false;
            for (Map.Entry<String, IDefinition> entry : rawDefinitions.entrySet()) {
                String key = entry.getKey();
                IDefinition value = entry.getValue();
                if (value.isController()) {
                    if (controllerFound) {
                        throw new IllegalArgumentException("There can only be one controller per multiblock!");
                    } else {
                        controllerFound = true;
                    }
                }
                definitions.put(key.charAt(0), value);
            }
            return definitions;
        }

    }

}
