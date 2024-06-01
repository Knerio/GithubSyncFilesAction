package de.derioo.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class Main {


    public static void main(String[] args) {
        try {
            Config config = Config.fromFileString(System.getenv("INPUT_CONFIG"));
            String token = System.getenv("INPUT_GITHUB_TOKEN");

            GitHub gitHub = GitHub.connectUsingOAuth(token);
            for (Config.Entry entry : config.getEntries()) {
                final Config.Entry.SingleFileLocation from = entry.getFrom();
                final Config.Entry.SingleFileLocation to = entry.getTo();
                final GHRepository toRepository = gitHub.getRepository(to.getRepo());
                final Config.Content selectedFrom = Objects.requireNonNull(from.file(gitHub), "Provided from doesnt exists");
                final List<GHContent> toCopy = new ArrayList<>(selectedFrom.all());

                for (GHContent ghContent : new ArrayList<>(toCopy)) {
                    if (ghContent.isDirectory()) {
                        toCopy.remove(ghContent);
                        continue;
                    }

                    String path = from.getFile();
                    String contentPath = ghContent.getPath().replaceFirst(path, "");

                    if (contentPath.isEmpty()) {
                        contentPath = ghContent.getName();
                    }

                    if (contentPath.startsWith("/")) contentPath = contentPath.substring(1);

                    for (String ignored : entry.getIgnore()) {
                        ignored = "glob:" + ignored;
                        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(ignored);
                        if (matcher.matches(Path.of(contentPath))) toCopy.remove(ghContent);
                    }
                }

                if (to.file(gitHub) == null) {
                    if (to.getFile().matches("(.+)\\.(.+)")) {
                        createOrUpdateSingleFile(toCopy, toRepository, null, to, config, entry, token);
                        continue;
                    }
                }
                Config.Content file = to.file(gitHub);
                if (Objects.isNull(file)) {
                    buildFolder(token, from, to, toRepository, toCopy, entry, config);
                    continue;
                }

                if (file.isFile()) {
                    createOrUpdateSingleFile(toCopy, toRepository, file.getSingleContent().getSha(), to, config, entry, token);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createOrUpdateSingleFile(@NotNull List<GHContent> toCopy, GHRepository toRepository, String sha, Config.Entry.SingleFileLocation to, Config config, Config.Entry entry, String token) {
        for (GHContent ghContent : toCopy) {
            try (InputStream stream = ghContent.read()) {
                updateOrCreate(toRepository.getFullName(), stream.readAllBytes(), sha, to.getFile(), getCommitMessage(config, entry), token);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void buildFolder(String token, Config.Entry.SingleFileLocation from, Config.Entry.SingleFileLocation to, GHRepository toRepository, @NotNull List<GHContent> toCopy, Config.Entry entry, Config config) {
        for (GHContent ghContent : toCopy) {
            try (InputStream stream = ghContent.read()) {
                String rel = ghContent.getPath().replaceFirst(from.getFile(), "");
                updateOrCreate(toRepository.getFullName(), stream.readAllBytes(), ghContent.getSha(), to.getFile() + rel, getCommitMessage(config, entry), token);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static String getCommitMessage(Config config, Config.@NotNull Entry entry) {
        if (entry.getCommitMessage() != null) return entry.getCommitMessage();
        return config.getGlobalCommitMessage();
    }

    public static void updateOrCreate(String repoName, byte[] content, String sha, @NotNull String path, String commitMessage, String token) throws IOException {
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        OkHttpClient client = new OkHttpClient();


        CommitRequest.Commiter author = new CommitRequest.Commiter("bot", "41898282+github-actions[bot]@users.noreply.github.com");
        CommitRequest json = new CommitRequest(commitMessage, Base64.getEncoder().encodeToString(content), sha, author, author);

        RequestBody body = RequestBody.create(
                new ObjectMapper().writeValueAsString(json),
                MediaType.parse("application/json; charset=utf-8")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.github.com/repos/" + repoName + "/contents/" + path)
                .addHeader("Authorization", "token " + token)
                .addHeader("X-GitHub-Api-Version", "2022-11-28")
                .addHeader("Accept", "application/vnd.github+json")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                if (responseBody.startsWith("{\"message\":\"Resource not accessible by integration\"")) {
                    System.err.println("""
                            ---------------------------
                                                        
                            We could not copy the files, because the access token provided doesnt have contents to write set
                            or the PAT doesnt have the repo scope set to true!
                            See https://github.com/Knerio/GithubSyncFilesAction for help
                                                        
                            ---------------------------
                            """);
                }
                throw new IOException("Unexpected response " + response);
            }
            System.out.println(responseBody);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Jacksonized
    private static class CommitRequest {

        String message;
        String content;

        String sha;


        Commiter commiter;

        Commiter author;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        @Jacksonized
        private static class Commiter {

            String name;
            String email;

        }
    }

}
