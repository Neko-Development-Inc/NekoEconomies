package n.e.k.o.economies;

import com.google.gson.Gson;
import n.e.k.o.economies.commands.enums.CommandCheckType;
import n.e.k.o.economies.commands.enums.CommandCtx;
import n.e.k.o.economies.eco.EcoKey;
import n.e.k.o.economies.eco.EcoUser;
import n.e.k.o.economies.eco.EcoValue;
import n.e.k.o.economies.manager.CommandManager;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.StorageManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.storage.IStorage;
import n.e.k.o.economies.utils.Config;
import n.e.k.o.economies.utils.StringColorUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.server.permission.PermissionAPI;
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

    public NekoEconomies() {
        config = Config.init("NekoEconomies", logger);
        if (config == null) {
            logger.error("Failed initializing config.");
            economiesManager = null;
            userManager = null;
            storage = null;
            return;
        }
        economiesManager = new EconomiesManager(this, config, logger);
        if (!economiesManager.init()) {
            logger.error("Failed initializing economiesManager.");
            userManager = null;
            storage = null;
            return;
        }
        userManager = new UserManager(this, economiesManager, config, logger);
        storage = StorageManager.getStorage(this, userManager, economiesManager, logger, config);
        if (storage == null) {
            logger.error("Failed initializing storage.");
            return;
        }
        userManager.setStorage(storage);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(userManager);
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        var commandManager = new CommandManager(this, userManager, economiesManager, config, logger);
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
            currencies.put(ecoKey.getId(), balances.get(ecoKey).getBalanceString());
        return currencies;
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CompletableFuture<Void> runAsync(Runnable r) {
        return CompletableFuture.runAsync(() -> executor.execute(r));
    }

    private final Queue<Runnable> rQueue = new ConcurrentLinkedQueue<>();

    public void runSync(Runnable r) {
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

    public boolean canExecuteCommand(CommandSource source) {
        return _canExecuteCommand(source, CommandCtx.ANY, config.settings.permissions.access, false) == CommandCheckType.SUCCESS;
    }

    public boolean canExecuteCommand(CommandSource source, boolean sendFeedback) {
        return _canExecuteCommand(source, CommandCtx.ANY, config.settings.permissions.access, sendFeedback) == CommandCheckType.SUCCESS;
    }

    public boolean canExecuteCommand(CommandSource source, String node) {
        return _canExecuteCommand(source, CommandCtx.ANY, node, false) == CommandCheckType.SUCCESS;
    }

    public boolean canExecuteCommand(CommandSource source, String node, boolean sendFeedback) {
        return _canExecuteCommand(source, CommandCtx.ANY, node, sendFeedback) == CommandCheckType.SUCCESS;
    }

    public boolean canExecuteCommand(CommandSource source, CommandCtx who) {
        return _canExecuteCommand(source, who, config.settings.permissions.access, false) == CommandCheckType.SUCCESS;
    }

    public boolean canExecuteCommand(CommandSource source, CommandCtx who, String node) {
        return _canExecuteCommand(source, who, node, false) == CommandCheckType.SUCCESS;
    }

    public boolean canExecuteCommand(CommandSource source, CommandCtx who, boolean sendFeedback) {
        return _canExecuteCommand(source, who, config.settings.permissions.access, sendFeedback) == CommandCheckType.SUCCESS;
    }

    public boolean canExecuteCommand(CommandSource source, CommandCtx who, String node, boolean sendFeedback) {
        return _canExecuteCommand(source, who, node, sendFeedback) == CommandCheckType.SUCCESS;
    }

    public CommandCheckType _canExecuteCommand(CommandSource source, CommandCtx who, String node, boolean sendFeedback) {
        var isOverPowered = source.hasPermissionLevel(4); // lol
        var isConsole = isOverPowered && !(source.getEntity() instanceof ServerPlayerEntity);
        if (isConsole) {
            if (who.is(CommandCtx.PLAYER)) {
                if (sendFeedback)
                    source.sendFeedback(StringColorUtils.getColoredString(config.settings.strings.requires_player), true);
                return CommandCheckType.REQUIRES_PLAYER;
            }
            return CommandCheckType.SUCCESS;
        }
        else if (who.is(CommandCtx.CONSOLE)) {
            if (sendFeedback)
                source.sendFeedback(StringColorUtils.getColoredString(config.settings.strings.requires_console), true);
            return CommandCheckType.REQUIRES_CONSOLE;
        }

        var entity = source.getEntity();
        if (!isOverPowered && !(entity instanceof ServerPlayerEntity)) { // This probably won't ever happen?
            if (sendFeedback)
                source.sendFeedback(StringColorUtils.getColoredString(config.settings.strings.no_permission), true);
            return CommandCheckType.NO_PERMISSION;
        }

        if (isOverPowered)
            return CommandCheckType.SUCCESS;

        var player = (ServerPlayerEntity) entity;
        if (PermissionAPI.hasPermission(player, node))
            return CommandCheckType.SUCCESS;

        if (sendFeedback)
            source.sendFeedback(StringColorUtils.getColoredString(config.settings.strings.no_permission), true);

        return CommandCheckType.NO_PERMISSION;
    }

}
