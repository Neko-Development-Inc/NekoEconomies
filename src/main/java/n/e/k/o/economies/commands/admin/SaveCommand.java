package n.e.k.o.economies.commands.admin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.manager.UserManager;
import n.e.k.o.economies.storage.IStorage;
import n.e.k.o.economies.utils.CommandHelper;
import n.e.k.o.economies.utils.Config;
import n.e.k.o.economies.utils.StringColorUtils;
import net.minecraft.command.CommandSource;
import org.apache.logging.log4j.Logger;

public class SaveCommand implements Command<CommandSource> {

    private final NekoEconomies nekoEconomies;
    private final UserManager userManager;
    private final EconomiesManager economiesManager;
    private final IStorage storage;
    private final CommandHelper commandHelper;
    private final Config config;
    private final Logger logger;

    public SaveCommand(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, IStorage storage, CommandHelper commandHelper, Config config, Logger logger) {
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

        source.sendFeedback(StringColorUtils.getColoredString("SaveCommand command"), true);

        storage.saveAsync().thenAccept(v -> source.sendFeedback(StringColorUtils.getColoredString("Saved."), true));

        return SINGLE_SUCCESS;
    }

}
