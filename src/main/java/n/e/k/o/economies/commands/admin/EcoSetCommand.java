package n.e.k.o.economies.commands.admin;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.api.EcoKey;
import n.e.k.o.economies.api.EcoUser;
import n.e.k.o.economies.api.EcoValue;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.storage.IStorage;
import n.e.k.o.economies.utils.CommandHelper;
import n.e.k.o.economies.utils.Config;
import n.e.k.o.economies.utils.StringColorUtils;
import net.minecraft.command.CommandSource;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;

public class EcoSetCommand implements Command<CommandSource> {

    private final NekoEconomies nekoEconomies;
    private final UserManager userManager;
    private final EconomiesManager economiesManager;
    private final IStorage storage;
    private final CommandHelper commandHelper;
    private final Config config;
    private final Logger logger;

    public EcoSetCommand(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, IStorage storage, CommandHelper commandHelper, Config config, Logger logger) {
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
        if (!commandHelper.canExecuteCommand(source, config.settings.permissions.admin, true))
            return SINGLE_SUCCESS;

        source.sendFeedback(StringColorUtils.getColoredString("BalanceSetCommand command"), true);

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

        double num;
        try {
            num = ctx.getArgument("num", Double.class);
        } catch (IllegalArgumentException e) {
            source.sendFeedback(StringColorUtils.getColoredString("No value set."), true);
            return 0;
        }

        EcoKey ecoKey;
        try {
            String strCurrency = ctx.getArgument("currency", String.class);
            ecoKey = economiesManager.getEcoKey(strCurrency);
            if (ecoKey == null) {
                source.sendFeedback(StringColorUtils.getColoredString("Currency not found by name '" + strCurrency + "'."), true);
                return 0;
            }
        } catch (IllegalArgumentException e) {
            ecoKey = economiesManager.getDefaultCurrency();
        }

        if (otherPlayer.player != null)
            logger.info("Setting balance for player " + otherPlayer.player.getName().getString() + " for currency " + ecoKey.getId());
        else
            logger.info("Setting balance for player " + otherPlayer.uuid + " for currency " + ecoKey.getId());

        EcoValue ecoValue = otherPlayer.setCurrencyValue(ecoKey, num);
        logger.info("  New balance: " + ecoValue.getBalanceString(3));

        return SINGLE_SUCCESS;
    }

}
