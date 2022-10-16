package n.e.k.o.economies.storage;

import n.e.k.o.economies.eco.EcoUser;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.utils.Config;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class FlatFileStorage implements IStorage {

    private final NekoEconomies nekoEconomies;
    private final UserManager userManager;
    private final EconomiesManager economiesManager;
    private final Config config;
    private final Logger logger;

    private final int FOLDER_CHAR_LENGTH = 1;

    public FlatFileStorage(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.userManager = userManager;
        this.economiesManager = economiesManager;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public boolean save() {
        for (var user : userManager.getAllUsers())
            save(user);
        return true;
    }

    @Override
    public void save(EcoUser user) {
        try {
            var uuid = user.getUuid();
            var prefix = uuid.toString().substring(0, FOLDER_CHAR_LENGTH);
            var subFolder = new File(config.folder, prefix);
            if (!subFolder.exists() && !subFolder.mkdirs()) {
                logger.error("Failed creating folder '" + subFolder.getAbsolutePath() + "'.");
                return;
            }
            var userFile = new File(subFolder, uuid + ".json");
            var exists = userFile.exists();
            if (!exists)
                try {
                    if (!userFile.createNewFile()) {
                        logger.error("Failed creating user file '" + userFile.getAbsolutePath() + "'.");
                        return;
                    }
                }
                catch (IOException e) {
                    logger.error(e.getMessage());
                    logger.error(e);
                    return;
                }
            var hasUpdate = user.hasUpdate();
            if (!exists || hasUpdate) {
                String output = gson.toJson(NekoEconomies.currenciesToMapString(user.balances));
                Files.write(userFile.toPath(), output.getBytes(StandardCharsets.UTF_8));
                if (hasUpdate)
                    user.setSaved();
            }
        } catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
            t.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Void> saveAsync() {
        return nekoEconomies.runAsync(this::save);
    }

    private static final Pattern uuidMatcher = Pattern.compile("[a-f\\d]{8}(?:-[a-f\\d]{4}){4}[a-f\\d]{8}(\\.json)");

    @Override
    public boolean load() {
        var subFolders = config.folder.listFiles(f -> f.isDirectory() && f.getName().length() == FOLDER_CHAR_LENGTH);
        if (subFolders == null || subFolders.length == 0) return false;
        for (var subFolder : subFolders) {
            var userFiles = subFolder.listFiles(f -> f.isFile() && uuidMatcher.matcher(f.getName()).find());
            if (userFiles == null || userFiles.length == 0) continue;
            for (var userFile : userFiles) {
                try {
                    System.out.println("fileName: " + userFile.getName());
                    var matcher = uuidMatcher.matcher(userFile.getName());
                    if (matcher.find()) {
                        var match = matcher.group();
                        System.out.println("match: " + match);
                        var uuid = match.contains(".") ? match.substring(0, match.lastIndexOf('.')) : match;
                        System.out.println("uuid: '" + uuid + "'");
                        var user = new EcoUser(UUID.fromString(uuid), economiesManager, config);
                        user.balances.putAll(NekoEconomies.mapStringToCurrencies(user, Files.readString(userFile.toPath()), economiesManager));
                        userManager.add(user);
                    }
                } catch (Throwable t) {
                    logger.error(t.getMessage());
                    logger.error(t);
                    t.printStackTrace();
                }
            }
        }
        return userManager.getAllUsers().size() > 0;
    }

    @Override
    public CompletableFuture<Void> loadAsync() {
        return nekoEconomies.runAsync(this::load);
    }

    @Override
    public boolean clear() {
        var subFolders = config.folder.listFiles(f -> f.isDirectory() && f.getName().length() == FOLDER_CHAR_LENGTH);
        if (subFolders == null || subFolders.length == 0) return true;
        final int[] failed = {0};
        for (var subFolder : subFolders)
            try {
                Files.walkFileTree(subFolder.toPath(), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        if (exc != null) {
                            logger.error("Failed deleting file in folder '" + dir.toFile().getAbsolutePath() + "'.");
                            logger.error(exc.getMessage());
                            logger.error(exc);
                        }
                        return getFileVisitResult(dir, failed);
                    }
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        return getFileVisitResult(file, failed);
                    }
                });
            } catch (IOException e) {
                logger.warn(e.getMessage());
                logger.warn(e);
                logger.warn("Can't delete folder: '" + subFolder.getAbsolutePath() + "'");
                failed[0]++;
            }
        return failed[0] == 0;
    }

    private FileVisitResult getFileVisitResult(Path file, int[] failed) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            logger.warn(e.getMessage());
            logger.warn(e);
            logger.warn("Can't delete folder: '" + file.toFile().getAbsolutePath() + "'");
            failed[0]++;
        }
        return FileVisitResult.CONTINUE;
    }

}
