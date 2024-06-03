package de.derioo.action.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.derioo.action.Config;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Entry.Simple.class, name = "simple"),
        @JsonSubTypes.Type(value = AbstractMap.SimpleEntry.class, name = "detailed")
})
public abstract class Entry {

    public abstract Config.SingleFileLocation to();

    public abstract Config.SingleFileLocation from();

    public abstract String commitMessage();

    public abstract List<String> ignore();

    @Getter
    @Setter
    @AllArgsConstructor
    @Jacksonized
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Default extends Entry {

        Config.SingleFileLocation from;
        Config.SingleFileLocation to;

        @JsonProperty("commit-message")
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Builder.Default()
        String commitMessage = null;

        @Builder.Default()
        @JsonIgnoreProperties(ignoreUnknown = true)
        List<String> ignore = new ArrayList<>();


        @Override
        public Config.SingleFileLocation to() {
            return to;
        }

        @Override
        public Config.SingleFileLocation from() {
            return from;
        }

        @Override
        public String commitMessage() {
            return commitMessage;
        }

        @Override
        public List<String> ignore() {
            return ignore;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Simple extends Entry {

        String from;
        String to;

        @Override
        public Config.SingleFileLocation to() {
            return new Config.SingleFileLocation(System.getenv("INPUT_REPOSITORY"), to);
        }

        @Override
        public Config.SingleFileLocation from() {
            return new Config.SingleFileLocation(System.getenv("INPUT_REPOSITORY"), from);
        }

        @Override
        public String commitMessage() {
            return null;
        }

        @Override
        public List<String> ignore() {
            return new ArrayList<>();
        }
    }

}
