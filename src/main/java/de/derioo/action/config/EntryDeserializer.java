package de.derioo.action.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class EntryDeserializer extends JsonDeserializer<Entry> {

    @Override
    public Entry deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        JsonNode root = p.getCodec().readTree(p);
        System.out.println(p.getValueAsString());
        System.out.println(root.toString());
        if (root.has("from") && root.get("from").isTextual() &&
                root.has("to") && root.get("to").isTextual()) {

            return ctx.readValue(p, Entry.Simple.class);
        } else {
            return ctx.readValue(p, Entry.class);
        }
    }
}