package n.e.k.o.economies.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.commands.enums.CommandCtx;
import n.e.k.o.economies.api.EcoUser;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.storage.IStorage;
import n.e.k.o.economies.utils.CommandHelper;
import n.e.k.o.economies.utils.Config;
import n.e.k.o.economies.utils.StringColorUtils;
import net.minecraft.command.CommandSource;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;

public class BalanceCommand implements Command<CommandSource> {

    private final NekoEconomies nekoEconomies;
    private final UserManager userManager;
    private final EconomiesManager economiesManager;
    private final IStorage storage;
    private final CommandHelper commandHelper;
    private final Config config;
    private final Logger logger;

    public BalanceCommand(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, IStorage storage, CommandHelper commandHelper, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.userManager = userManager;
        this.economiesManager = economiesManager;
        this.storage = storage;
        this.commandHelper = commandHelper;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public int run(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        if (!commandHelper.canExecuteCommand(source, true))
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
            if (!commandHelper.canExecuteCommand(source, CommandCtx.PLAYER, true))
                return SINGLE_SUCCESS;
            otherPlayer = userManager.getUser(source.asPlayer().getUniqueID());
        }

        if (otherPlayer.player != null)
            logger.info("Checking player: " + otherPlayer.player.getName().getString());
        else
            logger.info("Checking player: " + otherPlayer.uuid);

        for (var currency : economiesManager.getAllCurrencies()) {
            var value = otherPlayer.getCurrency(currency);
            source.sendFeedback(StringColorUtils.getColoredString("  [" + currency.getId() + "] " + value.getBalanceString(3)), true);
        }

        return SINGLE_SUCCESS;
    }

}
