package n.e.k.o.economies.storage;

import n.e.k.o.economies.EcoUser;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.utils.Config;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
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
    private final Config config;
    private final Logger logger;

    public FlatFileStorage(NekoEconomies nekoEconomies, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public boolean save() {
        for (EcoUser user : UserManager.getAllUsers())
            save(user);
        return true;
    }

    @Override
    public void save(EcoUser user) {
        try {
            UUID uuid = user.getUuid();
            String prefix = uuid.toString().substring(0, 2);
            File subFolder = new File(config.folder, prefix);
            if (!subFolder.exists() && !subFolder.mkdirs()) {
                logger.error("Failed creating folder '" + subFolder.getAbsolutePath() + "'.");
                return;
            }
            File userFile = new File(subFolder, uuid + ".json");
            boolean exists = userFile.exists();
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
            boolean hasUpdate = user.hasUpdate();
            if (!exists || hasUpdate) {
                Files.write(userFile.toPath(), gson.toJson(user).getBytes(StandardCharsets.UTF_8));
                if (hasUpdate)
                    user.setSaved();
            }
        } catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
        }
    }

    @Override
    public CompletableFuture<Void> saveAsync() {
        return nekoEconomies.runAsync(this::save);
    }

    private static final Pattern uuidMatcher = Pattern.compile("[a-f\\d]{8}(?:-[a-f\\d]{4}){4}[a-f\\d]{8}(\\.json)");

    @Override
    public boolean load() {
        File[] subFolders = config.folder.listFiles(f -> f.isDirectory() && f.getName().length() == 2);
        if (subFolders == null || subFolders.length == 0) return false;
        for (File subFolder : subFolders) {
            File[] userFiles = subFolder.listFiles(f -> f.isFile() && uuidMatcher.matcher(f.getName()).find());
            if (userFiles == null || userFiles.length == 0) continue;
            for (File userFile : userFiles) {
                try {
                    EcoUser user = gson.fromJson(new FileReader(userFile), EcoUser.class);
                    UserManager.add(user);
                } catch (Throwable t) {
                    logger.error(t.getMessage());
                    logger.error(t);
                }
            }
        }
        return UserManager.getAllUsers().size() > 0;
    }

    @Override
    public CompletableFuture<Void> loadAsync() {
        return nekoEconomies.runAsync(this::load);
    }

    @Override
    public boolean clear() {
        File[] subFolders = config.folder.listFiles(f -> f.isDirectory() && f.getName().length() == 2);
        if (subFolders == null || subFolders.length == 0) return true;
        final int[] failed = {0};
        for (File subFolder : subFolders)
            try {
                Files.walkFileTree(subFolder.toPath(), new SimpleFileVisitor<Path>() {
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
