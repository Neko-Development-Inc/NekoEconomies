package n.e.k.o.economies.utils;

import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class Config {

    public transient File folder = null;
    public transient File configFile = null;

    public String welcome;
    public Settings settings;

    public static class Settings {

        public Strings strings;
        public Permissions permissions;
        public Commands commands;

        public static class Strings {
            public String no_permission;
            public String requires_player;
            public String requires_console;

            public Reload reload;
            public ArrayList<String> help;

            public static class Reload {
                public String plugin_reloaded;
                public String plugin_not_reloaded;
            }
        }

        public static class Permissions {
            public String access;
            public String reload;
            public String help;
        }

        public static class Commands {
            public String root;
            public ArrayList<String> aliases;
        }
    }

    public static Config init(String modName, Logger logger) {
        File folder = NekoFolder.getOrCreateConfigFolder(modName);
        if (folder == null)
            return null;
        try {
            File configFile = new File(folder, "config.json");
            if (!configFile.exists())
                try (InputStream is = Config.class.getResourceAsStream("/nekoeconomies/config.json")) {
                    assert is != null;
                    Files.copy(is, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            Config config = new Gson().fromJson(new FileReader(configFile), Config.class);
            config.folder = folder;
            config.configFile = configFile;
            return config;
        }
        catch (Throwable t) {
            logger.error(t);
            t.printStackTrace();
        }
        return null;
    }

}
