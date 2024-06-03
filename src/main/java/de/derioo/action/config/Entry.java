package de.derioo.action.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.derioo.action.Config;
import lombok.*;
import lombok.experimental.*;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonDeserialize(using = EntryDeserializer.class)
public abstract class Entry {

    Config.SingleFileLocation from;
    Config.SingleFileLocation to;
    @JsonProperty("commit-message")
    String commitMessage;
    List<String> ignored;

    @Jacksonized
    @lombok.Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Default extends Entry {

        Config.SingleFileLocation from;
        Config.SingleFileLocation to;

        @Builder.Default
        @JsonProperty("commit-message")
        String commitMessage = null;

        @Builder.Default
        @JsonIgnoreProperties(ignoreUnknown = true)
        List<String> ignored = new ArrayList<>();

        public Default(
                @JsonProperty("from") Config.SingleFileLocation from,
                @JsonProperty("to") Config.SingleFileLocation to,
                @JsonProperty("commit-message") String commitMessage,
                @JsonProperty("ignored") List<String> ignored) {
            super(from, to, commitMessage, ignored);
        }
    }

    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Simple extends Entry {

        String from;
        String to;

        public Simple(
                @JsonProperty("from") String from,
                @JsonProperty("to") String to) {
            super(
                    new Config.SingleFileLocation(System.getenv("INPUT_REPOSITORY"), from),
                    new Config.SingleFileLocation(System.getenv("INPUT_REPOSITORY"), to),
                    null,
                    new ArrayList<>()
            );
        }
    }
}