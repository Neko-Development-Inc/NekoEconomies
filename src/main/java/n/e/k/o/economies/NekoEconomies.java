package n.e.k.o.economies;

import n.e.k.o.economies.commands.enums.CommandCheckType;
import n.e.k.o.economies.commands.enums.CommandCtx;
import n.e.k.o.economies.manager.CommandManager;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.utils.Config;
import n.e.k.o.economies.utils.StringColorUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("nekoeconomies")
public class NekoEconomies {

    private static final Logger logger = LogManager.getLogger();
    private final Config config;
    private final EconomiesManager economiesManager;

    public NekoEconomies() {
        config = Config.init("NekoEconomies", logger);
        if (config == null) {
            logger.error("Failed initializing config.");
            economiesManager = null;
            return;
        }
        economiesManager = new EconomiesManager(this, config, logger);
        if (!economiesManager.init()) {
            logger.error("Failed initializing economiesManager.");
            return;
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        logger.info(config.welcome);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {

    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        CommandManager commandManager = new CommandManager(this, config, logger);
        commandManager.init(event.getDispatcher());
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
        boolean isOverPowered = source.hasPermissionLevel(4); // lol
        boolean isConsole = isOverPowered && !(source.getEntity() instanceof ServerPlayerEntity);
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

        Entity entity = source.getEntity();
        if (!isOverPowered && !(entity instanceof ServerPlayerEntity)) { // This probably won't ever happen?
            if (sendFeedback)
                source.sendFeedback(StringColorUtils.getColoredString(config.settings.strings.no_permission), true);
            return CommandCheckType.NO_PERMISSION;
        }

        if (isOverPowered)
            return CommandCheckType.SUCCESS;

        ServerPlayerEntity player = (ServerPlayerEntity) entity;
        if (PermissionAPI.hasPermission(player, node))
            return CommandCheckType.SUCCESS;

        if (sendFeedback)
            source.sendFeedback(StringColorUtils.getColoredString(config.settings.strings.no_permission), true);

        return CommandCheckType.NO_PERMISSION;
    }

}
