package n.e.k.o.economies.manager;

import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.storage.FlatFileStorage;
import n.e.k.o.economies.storage.IStorage;
import n.e.k.o.economies.storage.MysqlStorage;
import n.e.k.o.economies.storage.StorageConverter;
import n.e.k.o.economies.utils.Config;
import org.apache.logging.log4j.Logger;

public class StorageManager {

    public static IStorage getStorage(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, Logger logger, Config config) {
        return getStorage(nekoEconomies, userManager, economiesManager, logger, config, false);
    }

    public static IStorage getStorage(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, Logger logger, Config config, boolean isConverting) {
        var storage = config.settings.storage;
        if (storage.converter.enabled && !isConverting) {
            var fromStorage = getStorage(nekoEconomies, userManager, economiesManager, logger, config, true);
            var convertResult = StorageConverter.doConversion(storage, fromStorage, nekoEconomies, userManager, economiesManager, config, logger);
            if (convertResult instanceof String) {
                logger.error(convertResult);
                return null;
            } else if (convertResult instanceof IStorage) {
                if (config.overwriteConfig(fromStorage, logger)) {
                    logger.info("Conversion successful! Config file was updated to reflect the new values.");
                    ((IStorage) convertResult).save();
                }
                else {
                    var isFile = fromStorage instanceof FlatFileStorage;
                    String fromName = isFile ? "flatfile" : "mysql";
                    String toName = isFile ? "mysql" : "flatfile";
                    logger.warn("Failed overwriting config file! Remember to swap the values in your config file now (" + fromName + " -> " + toName + ") and disable the converter.");
                }
                return (IStorage) convertResult;
            }
        }
        if (storage.flatfile.enabled && storage.mysql.enabled)
            logger.error("Can't use both 'flatfile' and 'mysql' storage at the same time.");
        else
        if (storage.flatfile.enabled)
            return new FlatFileStorage(nekoEconomies, userManager, economiesManager, config, logger);
        else
        if (storage.mysql.enabled) {
            var mysql = new MysqlStorage(nekoEconomies, userManager, economiesManager, config, logger);
            if (!mysql.init()) return null;
            return mysql;
        }
        return null;
    }

}
