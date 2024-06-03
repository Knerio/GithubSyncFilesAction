package de.derioo.action.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.derioo.action.Config;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import java.util.AbstractMap;
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
    String commitMessage;
    List<String> ignore;

    @Getter
    @Setter
    @Jacksonized
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Default extends Entry {

        @Builder.Default
        @JsonProperty("commit-message")
        String commitMessage = null;

        @Builder.Default
        @JsonIgnoreProperties(ignoreUnknown = true)
        List<String> ignore = new ArrayList<>();

        public Default(
                @JsonProperty("from") Config.SingleFileLocation from,
                @JsonProperty("to") Config.SingleFileLocation to,
                @JsonProperty("commit-message") String commitMessage,
                @JsonProperty("ignore") List<String> ignore) {
            super(from, to, commitMessage, ignore);
        }
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Simple extends Entry {

        public Simple(String from, String to) {
            super(
                    new Config.SingleFileLocation(System.getenv("INPUT_REPOSITORY"), from),
                    new Config.SingleFileLocation(System.getenv("INPUT_REPOSITORY"), to),
                    null,
                    new ArrayList<>()
            );
        }
    }
}