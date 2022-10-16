package n.e.k.o.economies.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.commands.enums.CommandCtx;
import n.e.k.o.economies.eco.EcoUser;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.storage.IStorage;
import n.e.k.o.economies.utils.CommandHelper;
import n.e.k.o.economies.utils.Config;
import n.e.k.o.economies.utils.StringColorUtils;
import net.minecraft.command.CommandSource;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;

public class BalanceTopCommand implements Command<CommandSource> {

    private final NekoEconomies nekoEconomies;
    private final UserManager userManager;
    private final EconomiesManager economiesManager;
    private final IStorage storage;
    private final CommandHelper commandHelper;
    private final Config config;
    private final Logger logger;

    public BalanceTopCommand(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, IStorage storage, CommandHelper commandHelper, Config config, Logger logger) {
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
        if (!commandHelper.canExecuteCommand(source, CommandCtx.PLAYER, config.settings.permissions.baltop, true))
            return SINGLE_SUCCESS;

        source.sendFeedback(StringColorUtils.getColoredString("Top 1000 balances:"), true);



        return SINGLE_SUCCESS;
    }

}
