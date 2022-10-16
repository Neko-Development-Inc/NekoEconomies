package n.e.k.o.economies.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.commands.enums.CommandCtx;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.utils.Config;
import n.e.k.o.economies.utils.StringColorUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class EcoCommand implements Command<CommandSource> {

    private final NekoEconomies nekoEconomies;
    private final UserManager userManager;
    private final EconomiesManager economiesManager;
    private final Config config;
    private final Logger logger;

    public EcoCommand(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, Config config, Logger logger) {
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

        source.sendFeedback(StringColorUtils.getColoredString("BalanceCommand command"), true);
        var player = source.asPlayer();



        return SINGLE_SUCCESS;
    }

    public void register(LiteralArgumentBuilder<CommandSource> builder) {
        var balanceCommand = new BalanceCommand(nekoEconomies, userManager, economiesManager, config, logger);
        var commandStrings = new ArrayList<>(config.settings.commands.balance.aliases);
        commandStrings.add(0, config.settings.commands.balance.command);
        for (var strCommand : commandStrings) {
            var cmd = Commands.literal(strCommand)
                    .executes(balanceCommand)
                    .requires(nekoEconomies::canExecuteCommand)
                    .then(Commands.argument("player", StringArgumentType.word())
                            .executes(balanceCommand)
                            .requires(nekoEconomies::canExecuteCommand)
                    );
            balanceCommand.register(cmd);
            builder.then(cmd);
        }
    }

}
