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
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Entry {

    Config.SingleFileLocation from;
    Config.SingleFileLocation to;

    @Builder.Default
    @JsonProperty("commit-message")
    String commitMessage = null;

    @Builder.Default
    @JsonIgnoreProperties(ignoreUnknown = true)
    List<String> ignored = new ArrayList<>();

}