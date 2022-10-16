package n.e.k.o.economies.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import n.e.k.o.economies.storage.FlatFileStorage;
import n.e.k.o.economies.storage.IStorage;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class Config {

    public transient File folder = null;
    public transient File configFile = null;

    public String welcome;
    public String storage_loaded;
    public Settings settings;

    public static class Settings {

        public ArrayList<Currency> currencies;
        public Storage storage;
        public Strings strings;
        public Permissions permissions;
        public Commands commands;

        public static class Currency {
            public String id;
            public String displayName;
            public String defaultValue;
            public boolean isDefaultCurrency;
        }

        public static class Storage {
            public Converter converter;
            public Mysql mysql;
            public Flatfile flatfile;

            public static class Converter {
                public boolean enabled;
                public String from;
                public String to;
                public boolean clearFrom;
            }

            public static class Mysql {
                public boolean enabled;
                public String host;
                public int port;
                public String db;
                public String user;
                public String password;
                public boolean minify;
                public String CREATE_TABLE_USERS;
                public String GET_ALL_USERS;
                public String SAVE_USERS;
                public String DELETE_TABLE;
            }

            public static class Flatfile {
                public boolean enabled;
                public boolean minify;
            }
        }

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
            public String baltop;
            public String reload;
            public String help;
            public String admin;
        }

        public static class Commands {
            public Eco eco;
            public Balance balance;
            public BalanceTop baltop;

            public abstract static class CommandRoot {
                public String command;
                public ArrayList<String> aliases;
            }

            public static class Eco extends CommandRoot {
                public Set set;
                public Add add;
                public Subtract subtract;
                public Clear clear;
                public Help help;
                public Reload reload;

                public static class Set extends CommandRoot { }

                public static class Add extends CommandRoot { }

                public static class Subtract extends CommandRoot { }

                public static class Clear extends CommandRoot { }

                public static class Help extends CommandRoot { }

                public static class Reload extends CommandRoot { }
            }

            public static class Balance extends CommandRoot { }

            public static class BalanceTop extends CommandRoot { }
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

    public boolean overwriteConfig(IStorage storage, Logger logger) {
        try {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            Files.write(configFile.toPath(), gson.toJson(this).getBytes(StandardCharsets.UTF_8));
            if (settings.storage.converter.clearFrom) {
                boolean isFile = storage instanceof FlatFileStorage;
                if (storage.clear())
                    logger.info("Successfully cleared '" + (isFile ? "FlatFile" : "MySQL") + "' storage.");
                else
                    logger.info("Failed clearing '" + (isFile ? "FlatFile" : "MySQL") + "' storage.");
            }
        }
        catch (IOException e) {
            logger.error(e.getMessage());
            logger.error(e);
            return false;
        }
        return true;
    }

}
