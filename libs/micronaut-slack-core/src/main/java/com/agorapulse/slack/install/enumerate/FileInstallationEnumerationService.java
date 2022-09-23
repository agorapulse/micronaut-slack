package com.agorapulse.slack.install.enumerate;

import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.model.builtin.DefaultBot;
import com.slack.api.bolt.util.JsonOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class FileInstallationEnumerationService implements InstallationEnumerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileInstallationEnumerationService.class);

    public static final String DEFAULT_ROOT_DIR = System.getProperty("user.home") + File.separator + ".slack-app";

    private final AppConfig config;
    private final String rootDir;
    private final boolean historicalDataEnabled;


    public FileInstallationEnumerationService(AppConfig config, String rootDir, boolean historicalDataEnabled) {
        this.config = config;
        this.rootDir = rootDir;
        this.historicalDataEnabled = historicalDataEnabled;
    }

    @Override
    public Stream<Bot> findAllBots() {
        try {
            return Files.walk(Paths.get(getBotBaseDir()))
                    .filter(p -> p.toFile().isFile() && (!historicalDataEnabled || p.toString().endsWith("-latest")))
                    .<Bot>map(p -> {
                        try {
                            return JsonOps.fromJson(loadFileContent(p.toAbsolutePath().toString()), DefaultBot.class);
                        } catch (IOException e) {
                            LOGGER.warn("Failed to load bot user from path {}", p);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull);

        } catch (IOException e) {
            LOGGER.warn("Failed to load all bot users");
            return Stream.empty();
        }
    }

    private String getBotBaseDir() throws IOException {
        String dir = getBaseDir() + File.separator + "bot";
        Path dirPath = Paths.get(dir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        return dir;
    }

    private String getBaseDir() {
        return rootDir + File.separator + config.getClientId() + File.separator + "installation";
    }

    private String loadFileContent(String filepath) throws IOException {
        String content = Files.readAllLines(Paths.get(filepath))
                .stream()
                .collect(joining());
        if (content == null || content.trim().isEmpty() || content.trim().equals("null")) {
            return null;
        }
        return content;
    }

}
