package de.derioo.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.kohsuke.github.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Main {


    public static void main(String[] args) {
        Config config = Config.fromFileString(System.getenv("INPUT_CONFIG"));
        String token = System.getenv("INPUT_GITHUB_TOKEN");
        try {
            GitHub gitHub = GitHub.connectUsingOAuth(token);
            for (Config.Entry entry : config.getEntries()) {
                Config.Entry.SingleFileLocation from = entry.getFrom();
                Config.Entry.SingleFileLocation to = entry.getTo();
                GHRepository toRepository = gitHub.getRepository(to.getRepo());
                Config.Content selectedFrom = from.file(gitHub);
                List<GHContent> toCopy = new ArrayList<>();

                if (selectedFrom.isFile()) {
                    toCopy.add(selectedFrom.getSingleContent());
                } else {
                    toCopy.addAll(selectedFrom.getDir());
                }
                for (GHContent ghContent : new ArrayList<>(toCopy)) {
                    if (ghContent.isDirectory()) {
                        toCopy.remove(ghContent);
                        continue;
                    }
                    String path = from.getFile();
                    String contentPath = ghContent.getPath();
                    contentPath = contentPath.replaceFirst(path, "");
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

                try {
                    to.file(gitHub);
                } catch (IOException ignored) {
                    if (to.getFile().matches("(.+)\\.(.+)"))
                        updateOrCreate(toRepository.getFullName(), "".getBytes(StandardCharsets.UTF_8), null, to.getFile(), "create empty file", token);
                }

                try {
                    Config.Content file = to.file(gitHub);
                    if (file.isFile()) {
                        for (GHContent ghContent : toCopy) {
                            try {
                                updateOrCreate(toRepository.getFullName(), ghContent.read().readAllBytes(), file.getSingleContent().getSha(), to.getFile(), "Sync files", token);
                            } catch (IOException e) {
                                System.out.println("##");
                                throw new RuntimeException(e);
                            }
                        }
                        continue;
                    }
                    continue;
                } catch (IOException ignored) {
                }
                buildFolder(token, from, to, toRepository, toCopy);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildFolder(String token, Config.Entry.SingleFileLocation from, Config.Entry.SingleFileLocation to, GHRepository toRepository, List<GHContent> toCopy) {
        for (GHContent ghContent : toCopy) {
            try {
                String rel = ghContent.getPath().replaceFirst(from.getFile(), "");
                updateOrCreate(toRepository.getFullName(), ghContent.read().readAllBytes(), ghContent.getSha(), to.getFile() + rel, "Sync files", token);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static void updateOrCreate(String repoName, byte[] content, String sha, String path, String commitMessage, String token) throws IOException {
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        OkHttpClient client = new OkHttpClient();

        String url = "https://api.github.com/repos/" + repoName + "/contents/" + path;

        String base64Content = Base64.getEncoder().encodeToString(content);


        CommitRequest.Commiter author = new CommitRequest.Commiter("bot", "41898282+github-actions[bot]@users.noreply.github.com");
        CommitRequest json = new CommitRequest(commitMessage, base64Content, sha, author, author);

        RequestBody body = RequestBody.create(
                new ObjectMapper().writeValueAsString(json),
                MediaType.parse("application/json; charset=utf-8")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
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
                            
                            We could not copy the files, because the access token provided is the default runner token
                            or the PAT doesnt have the repo scope set to true!
                            
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
