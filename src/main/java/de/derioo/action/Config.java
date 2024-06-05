package de.derioo.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.derioo.action.config.Entry;
import de.derioo.action.config.ShortConfig;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Jacksonized
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        defaultImpl = Config.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ShortConfig.class, name = "short"),
        @JsonSubTypes.Type(value = Config.class, name = "default"),
})
public class Config {

    @Builder.Default
    List<Entry> entries = new ArrayList<>();

    @JsonProperty("commit-message")
    @Builder.Default
    String globalCommitMessage = "Sync GitHub files";

    public static Config fromFileString(String s) throws IOException {
        return new YAMLMapper().readValue(new File(s), Config.class);
    }



    @Getter
    @Setter
    @AllArgsConstructor
    @Jacksonized
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SingleFileLocation {

        @Builder.Default
        @JsonIgnoreProperties(ignoreUnknown = true)
        String repo = System.getenv("INPUT_REPOSITORY");
        String file;


        public @Nullable Content file(@NotNull GitHub gitHub) throws IOException {
            try {
                return new Content(gitHub.getRepository(repo).getFileContent(file));
            } catch (IOException e) {
                try {
                    return new Content(new ArrayList<>(gitHub.getRepository(repo).getDirectoryContent(file)));
                } catch (IOException x) {
                    return null;
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
            this.dir = new ArrayList<>();
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

        public List<GHContent> all() {
            if (isFile()) return Collections.singletonList(singleContent);
            return dir;
        }

        private List<GHContent> listInnerFiles(@NotNull GHContent dir) {
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
