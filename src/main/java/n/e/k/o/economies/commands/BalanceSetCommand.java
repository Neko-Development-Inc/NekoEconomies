package n.e.k.o.economies.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.eco.EcoUser;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.utils.Config;
import n.e.k.o.economies.utils.StringColorUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class BalanceSetCommand implements Command<CommandSource> {

    private final NekoEconomies nekoEconomies;
    private final UserManager userManager;
    private final Config config;
    private final Logger logger;

    public BalanceSetCommand(NekoEconomies nekoEconomies, UserManager userManager, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.userManager = userManager;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public int run(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        if (!nekoEconomies.canExecuteCommand(source, config.settings.permissions.admin, true))
            return SINGLE_SUCCESS;

        source.sendFeedback(StringColorUtils.getColoredString("BalanceSetCommand command"), true);

        String num;
        try {
            num = ctx.getArgument("num", String.class);
        } catch (IllegalArgumentException e) {
            // Error maybe?
            return 0;
        }

        BigDecimal bigDecimal;
        try {
            bigDecimal = new BigDecimal(num);
        } catch (Throwable t) {
            // Error maybe?
            return 0;
        }

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

        System.out.println("Setting balance for player " + otherPlayer.player.getName().getString() + " to value " + bigDecimal.toPlainString());

        return SINGLE_SUCCESS;
    }

    public void register(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(Commands.argument("num", StringArgumentType.word())
                .executes(this)
                .requires(nekoEconomies::canExecuteCommand)
        ).then(Commands.argument("player", StringArgumentType.word())
                .executes(this)
                .requires(nekoEconomies::canExecuteCommand)
                .suggests(this::suggestOnlinePlayers)
                .then(Commands.argument("num", StringArgumentType.word())
                        .executes(this)
                        .requires(nekoEconomies::canExecuteCommand)
                )
        );
    }

    private CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSource> ctx, SuggestionsBuilder builder) {
        for (String playerName : ServerLifecycleHooks.getCurrentServer().getOnlinePlayerNames())
            builder.suggest(playerName);
        return builder.buildFuture();
    }

}
