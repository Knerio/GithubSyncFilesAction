package de.derioo.action.config;

import de.derioo.action.Config;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShortConfig extends Config {
    public ShortConfig(@NotNull List<ShortEntry> entries, String globalCommitMessage) {
        super(entries.stream().map(shortEntry -> (Entry) shortEntry).toList(), globalCommitMessage);
    }

    public static class ShortEntry extends Entry {

        public ShortEntry(String from, String to) {
            super(SingleFileLocation
                    .builder()
                    .file(from)
                    .build(),
                    SingleFileLocation
                    .builder()
                    .file(to)
                    .build(), null, new ArrayList<>());
        }
    }
}
