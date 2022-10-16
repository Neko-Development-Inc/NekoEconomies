package n.e.k.o.economies.storage;

import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.utils.Config;
import org.apache.logging.log4j.Logger;

public class StorageConverter
{

    public static Object doConversion(Config.Settings.Storage storage, IStorage fromStorage, NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, Config config, Logger logger)
    {
        var from = storage.converter.from;
        var to = storage.converter.to;
        switch (from) {
            case "mysql": {
                switch (to) {
                    case "mysql": return "Can't convert from 'mysql' to 'mysql'.";
                    case "flatfile": {
                        if (!fromStorage.load())
                            return "Failed converting from 'mysql' to 'flatfile', because the mysql database exist or wasn't loaded properly.";
                        var toStorage = new FlatFileStorage(nekoEconomies, userManager, economiesManager, config, logger);
                        userManager.setAllUnsaved();
                        toStorage.save();
                        storage.mysql.enabled = false;
                        storage.flatfile.enabled = true;
                        storage.converter.enabled = false;
                        return toStorage;
                    }
                }
                break;
            }
            case "flatfile": {
                switch (to) {
                    case "flatfile": return "Can't convert from 'flatfile' to 'flatfile'.";
                    case "mysql": {
                        if (!fromStorage.load())
                            return "Failed converting from 'flatfile' to 'mysql', because the flatfile didn't exist or wasn't loaded properly.";
                        var toStorage = new MysqlStorage(nekoEconomies, userManager, economiesManager, config, logger);
                        if (!toStorage.init())
                            return "Failed init on mysqlStorage.";
                        userManager.setAllUnsaved();
                        toStorage.save();
                        storage.flatfile.enabled = false;
                        storage.mysql.enabled = true;
                        storage.converter.enabled = false;
                        return toStorage;
                    }
                }
            }
        }
        return null;
    }

}
