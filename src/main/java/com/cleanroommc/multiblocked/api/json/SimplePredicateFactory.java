package com.cleanroommc.multiblocked.api.json;

import com.cleanroommc.multiblocked.api.pattern.predicates.SimplePredicate;
import com.cleanroommc.multiblocked.api.registry.MbdPredicates;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class SimplePredicateFactory implements TypeAdapterFactory {
    public static final SimplePredicateFactory INSTANCE = new SimplePredicateFactory();

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (SimplePredicate.class.isAssignableFrom(type.getRawType())) {
            return (TypeAdapter<T>) new SimplePredicateTypeAdapter(gson);
        }
        return null;
    }

    private static final class SimplePredicateTypeAdapter extends TypeAdapter<SimplePredicate> {

        private final Gson gson;

        private SimplePredicateTypeAdapter(final Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(final JsonWriter out, final SimplePredicate src) {
            if (src == null) {
                gson.toJson(JsonNull.INSTANCE, out);
                return;
            }
            gson.toJson(src.toJson(new JsonObject()), out);
        }

        @Override
        public SimplePredicate read(final JsonReader in) {
            final JsonElement jsonElement = gson.fromJson(in, JsonElement.class);
            if (jsonElement.isJsonNull()) return null;
            JsonObject jsonObj = jsonElement.getAsJsonObject();
            final String type = jsonObj.get("type").getAsString();
            return MbdPredicates.createPredicate(type, jsonObj);
        }

    }

}
