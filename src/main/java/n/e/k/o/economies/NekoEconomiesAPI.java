package n.e.k.o.economies;

import n.e.k.o.economies.eco.EcoKey;
import n.e.k.o.economies.eco.EcoUser;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.utils.Config;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class NekoEconomiesAPI {

    private static NekoEconomiesAPI instance = null;
    private static final Object instanceLock = new Object();
    private static final List<CompletableFuture<NekoEconomiesAPI>> asyncs = new ArrayList<>();
    private final UserManager userManager;
    private final EconomiesManager economiesManager;
    private final Config config;
    private final Logger logger;

    public NekoEconomiesAPI(UserManager userManager, EconomiesManager economiesManager, Config config, Logger logger) {
        this.userManager = userManager;
        this.economiesManager = economiesManager;
        this.config = config;
        this.logger = logger;
    }

    public static NekoEconomiesAPI init(UserManager userManager, EconomiesManager economiesManager, Config config, Logger logger) {
        synchronized (instanceLock) {
            if (instance != null)
                throw new RuntimeException("NekoEconomiesAPI has already been initialized!");
            instance = new NekoEconomiesAPI(userManager, economiesManager, config, logger);
            if (!asyncs.isEmpty()) {
                for (CompletableFuture<NekoEconomiesAPI> async : asyncs)
                    async.complete(instance);
                asyncs.clear();
            }
            return instance;
        }
    }

    public static NekoEconomiesAPI get() {
        synchronized (instanceLock) {
            return instance;
        }
    }

    public static CompletableFuture<NekoEconomiesAPI> getAsync() {
        synchronized (instanceLock) {
            if (instance != null)
                return CompletableFuture.completedFuture(instance);
            CompletableFuture<NekoEconomiesAPI> async = new CompletableFuture<>();
            asyncs.add(async);
            return async;
        }
    }

    public EcoUser getOrCreateUser(UUID uuid) {
        return userManager.getUser(uuid);
    }

    public EcoKey getCurrency(String id) {
        return economiesManager.getEcoKey(id);
    }

    public EcoKey getDefaultCurrency() {
        return economiesManager.getDefaultCurrency();
    }

    public List<EcoKey> getAllCurrencies() {
        return economiesManager.getAllCurrencies();
    }

}
