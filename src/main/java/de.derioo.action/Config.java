package de.derioo.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Jacksonized
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Config {

    List<Entry> entries;

    public static Config fromFileString(String s) {
        try {
            return new YAMLMapper().readValue(new File(s), Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Jacksonized
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Entry {

        SingleFileLocation from;
        SingleFileLocation to;

        @Builder.Default()
        @JsonIgnoreProperties(ignoreUnknown = true)
        List<String> ignore = new ArrayList<>();

        @Getter
        @Setter
        @AllArgsConstructor
        @Jacksonized
        @Builder
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class SingleFileLocation {
            String repo;
            String file;



            public Content file(@NotNull GitHub gitHub) throws IOException {
                try {
                    return new Content(gitHub.getRepository(repo).getFileContent(file));
                } catch (IOException e) {
                    try {
                       return new Content(new ArrayList<>(gitHub.getRepository(repo).getDirectoryContent(file)));
                    } catch (IOException x) {
                        e.printStackTrace();
                        throw new IOException(x);
                    }
                }
            }


        }
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Content {

        GHContent singleContent;

        List<GHContent> dir;

        public Content(GHContent singleContent) {
            this.singleContent = singleContent;
        }

        public Content(List<GHContent> dir) {
            this.singleContent = null;
            this.dir = dir;
            for (GHContent ghContent : new ArrayList<>(dir)) {
                if (ghContent.isDirectory()) {
                    dir.addAll(listInnerFiles(ghContent));
                }
            }
        }

        private List<GHContent> listInnerFiles(GHContent dir) {
            List<GHContent> files = new ArrayList<>();
            try {
                for (GHContent ghContent : dir.listDirectoryContent()) {
                    if (ghContent.isDirectory()) {
                        files.addAll(listInnerFiles(ghContent));
                    }
                    files.add(ghContent);
                }
                return files;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isFile() {
            return singleContent != null;
        }
    }


}
