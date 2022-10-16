package n.e.k.o.economies.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import n.e.k.o.economies.commands.enums.CommandCheckType;
import n.e.k.o.economies.commands.enums.CommandCtx;
import n.e.k.o.economies.eco.EcoKey;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.utils.Config;
import n.e.k.o.economies.utils.StringColorUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.concurrent.CompletableFuture;

public class CommandHelper {

    private final EconomiesManager economiesManager;
    private final Config config;

    public CommandHelper(EconomiesManager economiesManager, Config config) {
        this.economiesManager = economiesManager;
        this.config = config;
    }

    public CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSource> ctx, SuggestionsBuilder builder) {
        for (String playerName : ServerLifecycleHooks.getCurrentServer().getOnlinePlayerNames())
            builder.suggest(playerName);
        return builder.buildFuture();
    }

    public CompletableFuture<Suggestions> suggestCurrencies(CommandContext<CommandSource> ctx, SuggestionsBuilder builder) {
        for (EcoKey currency : economiesManager.getAllCurrencies())
            builder.suggest(currency.getId());
        return builder.buildFuture();
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
