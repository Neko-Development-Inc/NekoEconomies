package n.e.k.o.economies.manager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.commands.EcoCommand;
import n.e.k.o.economies.utils.Config;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final NekoEconomies nekoEconomies;
    private final UserManager userManager;
    private final EconomiesManager economiesManager;
    private final Config config;
    private final Logger logger;

    public CommandManager(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.userManager = userManager;
        this.economiesManager = economiesManager;
        this.config = config;
        this.logger = logger;
    }

    public void init(CommandDispatcher<CommandSource> dispatcher) {
        var ecoCommand = new EcoCommand(nekoEconomies, userManager, economiesManager, config, logger);
        var commandStrings = new ArrayList<>(config.settings.commands.root.aliases);
        commandStrings.add(0, config.settings.commands.root.command);
        for (var strCommand : commandStrings) {
            var cmd = Commands.literal(strCommand)
                    .executes(ecoCommand)
                    .requires(nekoEconomies::canExecuteCommand);
            ecoCommand.register(cmd);
            dispatcher.register(cmd);
            PermissionAPI.registerNode("minecraft.command." + strCommand, DefaultPermissionLevel.ALL, "Pixelmon Economies");
        }
    }

}
