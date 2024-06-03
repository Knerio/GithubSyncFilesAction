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

        if (root.has("from") && root.get("from").isTextual() &&
                root.has("to") && root.get("to").isTextual()) {
            System.out.println(root.toString());
            return new ObjectMapper().readValue(root.toString(), Entry.Simple.class);
        } else {
            return new ObjectMapper().readValue(root.toString(), Entry.Default.class);
        }
    }
}