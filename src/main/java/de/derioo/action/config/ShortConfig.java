package de.derioo.action.config;

import de.derioo.action.Config;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Jacksonized
@Builder
@Getter
public class ShortConfig extends Config {
    public ShortConfig(@NotNull List<ShortEntry> entries, String globalCommitMessage) {
        super(entries.stream().map(shortEntry -> (Entry) shortEntry).toList(), globalCommitMessage);
    }

    public static class ShortEntry extends Entry {

        public ShortEntry(String from, String to) {
            super(new SingleFileLocation(System.getenv("INPUT_REPOSITORY"), from),
                    new SingleFileLocation(System.getenv("INPUT_REPOSITORY"), to),
                    null, new ArrayList<>());
        }
    }
}
