package n.e.k.o.economies;

import com.google.gson.Gson;
import n.e.k.o.economies.api.EcoKey;
import n.e.k.o.economies.api.EcoUser;
import n.e.k.o.economies.api.EcoValue;
import n.e.k.o.economies.api.NekoEconomiesAPI;
import n.e.k.o.economies.manager.CommandManager;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.StorageManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.storage.IStorage;
import n.e.k.o.economies.utils.CommandHelper;
import n.e.k.o.economies.utils.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod("nekoeconomies")
public class NekoEconomies {

    private static final Logger logger = LogManager.getLogger();
    private final Config config;
    private final IStorage storage;
    private final EconomiesManager economiesManager;
    private final UserManager userManager;
    private final CommandHelper commandHelper;

    public NekoEconomies() {
        config = Config.init("NekoEconomies", logger);
        if (config == null) {
            logger.error("Failed initializing config.");
            economiesManager = null;
            commandHelper = null;
            userManager = null;
            storage = null;
            return;
        }
        economiesManager = new EconomiesManager(this, config, logger);
        if (!economiesManager.init()) {
            logger.error("Failed initializing economiesManager.");
            commandHelper = null;
            userManager = null;
            storage = null;
            return;
        }
        userManager = new UserManager(this, economiesManager, config, logger);
        storage = StorageManager.getStorage(this, userManager, economiesManager, logger, config);
        if (storage == null) {
            logger.error("Failed initializing storage.");
            commandHelper = null;
            return;
        }
        userManager.setStorage(storage);
        commandHelper = new CommandHelper(economiesManager, config);
        NekoEconomiesAPI.init(userManager, economiesManager, config, logger);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(userManager);
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        var commandManager = new CommandManager(this, userManager, economiesManager, storage, commandHelper, config, logger);
        commandManager.init(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        logger.info(config.welcome);
        storage.load();
        logger.info(config.storage_loaded);
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        logger.info("Saving data...");
        storage.save();
        logger.info("Saved!");
    }

    public static Map<EcoKey, EcoValue> mapStringToCurrencies(EcoUser user, String balances, EconomiesManager economiesManager) {
        var currencies = new HashMap<EcoKey, EcoValue>();
        Map<String, String> map = new Gson().fromJson(balances, Map.class);
        for (var entry : map.entrySet()) {
            var balance = entry.getValue();
            var ecoKey = economiesManager.getEcoKey(entry.getKey());
            var ecoValue = new EcoValue(user, ecoKey, balance);
            currencies.put(ecoKey, ecoValue);
        }
        return currencies;
    }

    public static Map<String, String> currenciesToMapString(Map<EcoKey, EcoValue> balances) {
        var currencies = new HashMap<String, String>();
        for (var ecoKey : balances.keySet())
            if (!ecoKey.isOverridePixelmonCurrency())
                currencies.put(ecoKey.getId(), balances.get(ecoKey).getBalanceString());
        return currencies;
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static CompletableFuture<Void> runAsync(Runnable r) {
        return CompletableFuture.runAsync(() -> executor.execute(r));
    }

    private static final Queue<Runnable> rQueue = new ConcurrentLinkedQueue<>();

    public static void runSync(Runnable r) {
        if (r == null) return;
        rQueue.add(r);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        while (!rQueue.isEmpty())
            try {
                rQueue.poll().run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
    }

}
