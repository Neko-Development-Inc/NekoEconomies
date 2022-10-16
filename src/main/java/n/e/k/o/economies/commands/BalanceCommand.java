package n.e.k.o.economies.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.commands.enums.CommandCtx;
import n.e.k.o.economies.eco.EcoUser;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.utils.Config;
import n.e.k.o.economies.utils.StringColorUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class BalanceCommand implements Command<CommandSource> {

    private final NekoEconomies nekoEconomies;
    private final UserManager userManager;
    private final EconomiesManager economiesManager;
    private final Config config;
    private final Logger logger;

    public BalanceCommand(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.userManager = userManager;
        this.economiesManager = economiesManager;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public int run(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        if (!nekoEconomies.canExecuteCommand(source, CommandCtx.PLAYER, true))
            return SINGLE_SUCCESS;

        source.sendFeedback(StringColorUtils.getColoredString("All your balances:"), true);

        EcoUser otherPlayer;
        try {
            String playerName = ctx.getArgument("player", String.class);
            GameProfile profile = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getGameProfileForUsername(playerName);
            if (profile == null || !profile.isComplete()) {
                source.sendFeedback(StringColorUtils.getColoredString("Player not found by name '" + playerName + "'."), true);
                return 0;
            }
            otherPlayer = userManager.getUser(profile.getId());
        } catch (IllegalArgumentException e) {
            otherPlayer = userManager.getUser(source.asPlayer().getUniqueID());
        }

        if (otherPlayer.player != null)
            System.out.println("Checking player: " + otherPlayer.player.getName().getString());
        else
            System.out.println("Checking player: " + otherPlayer.uuid);

        for (var currency : economiesManager.getAllCurrencies()) {
            var value = otherPlayer.getCurrency(currency);
            source.sendFeedback(StringColorUtils.getColoredString("  [" + currency.getId() + "] " + value.getBalanceString(3)), true);
        }

        return SINGLE_SUCCESS;
    }

    public void register(LiteralArgumentBuilder<CommandSource> builder) {
        var set = new BalanceSetCommand(nekoEconomies, userManager, config, logger);
        var commandStrings = new ArrayList<>(config.settings.commands.balance.set.aliases);
        commandStrings.add(0, config.settings.commands.balance.set.command);
        for (var strCommand : commandStrings) {
            var cmd = Commands.literal(strCommand)
                    .executes(set)
                    .requires(nekoEconomies::canExecuteCommand);
            set.register(cmd);
            builder.then(cmd);
        }
    }

}
