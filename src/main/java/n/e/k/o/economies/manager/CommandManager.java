package n.e.k.o.economies.manager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.commands.BalanceCommand;
import n.e.k.o.economies.commands.BalanceTopCommand;
import n.e.k.o.economies.commands.EcoCommand;
import n.e.k.o.economies.commands.HelpCommand;
import n.e.k.o.economies.commands.admin.*;
import n.e.k.o.economies.storage.IStorage;
import n.e.k.o.economies.utils.CommandHelper;
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
    private final IStorage storage;
    private final CommandHelper commandHelper;
    private final Config config;
    private final Logger logger;

    public CommandManager(NekoEconomies nekoEconomies, UserManager userManager, EconomiesManager economiesManager, IStorage storage, CommandHelper commandHelper, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.userManager = userManager;
        this.economiesManager = economiesManager;
        this.storage = storage;
        this.commandHelper = commandHelper;
        this.config = config;
        this.logger = logger;
    }

//    public void init(CommandDispatcher<CommandSource> dispatcher) {
//        var eco = new EcoCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger);
//        var cmd = Commands.literal("eco").executes(eco);
//        PermissionAPI.registerNode("minecraft.command.eco", DefaultPermissionLevel.ALL, "Pixelmon Economies /eco");
//        dispatcher.register(cmd);
//        {
//            var ecoSet = new EcoSetCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger);
//            var cmdSet = cmd.then(Commands.literal("set").executes(ecoSet));
//            dispatcher.register(cmdSet);
//            {
//                var ecoSetNum = new EcoSetCommand.SetNum(commandHelper);
//                var cmdSetNum = cmdSet.then(Commands.argument("num", StringArgumentType.word()).executes(ecoSetNum));
//                dispatcher.register(cmdSetNum);
//
//                var ecoSetPlayerNum = new EcoSetCommand.SetPlayerNum(commandHelper);
//                var cmdSetPlayerNum = cmdSetNum.then(Commands.argument("player", StringArgumentType.word()).executes(ecoSetPlayerNum));
//                dispatcher.register(cmdSetPlayerNum);
//            }
//            {
//                var ecoSetNumCurrency = new EcoSetCommand.SetNumCurrency(commandHelper);
//                var cmdSetNumCurrency = cmdSet.then(Commands.argument("num", StringArgumentType.word()).executes(ecoSetNumCurrency));
//                dispatcher.register(cmdSetNumCurrency);
//
//                var ecoSetPlayerNumCurrency = new EcoSetCommand.SetPlayerNumCurrency(commandHelper);
//                var cmdSetPlayerNumCurrency = cmdSetNumCurrency.then(Commands.argument("num", StringArgumentType.word()).executes(ecoSetPlayerNumCurrency));
//                dispatcher.register(cmdSetPlayerNumCurrency);
//            }
//        }
//    }

    public void init(CommandDispatcher<CommandSource> dispatcher) {
        List<LiteralArgumentBuilder<CommandSource>> ecos = new ArrayList<>();

        /* Eco command */ {
            // /eco
            var eco = new EcoCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger); {
                var commandStrings = new ArrayList<>(config.settings.commands.eco.aliases);
                commandStrings.add(0, config.settings.commands.eco.command);
                for (var strCommand : commandStrings) {
                    var cmd = Commands.literal(strCommand)
                            .executes(eco)
                            .requires(commandHelper::canExecuteCommand);
                    dispatcher.register(cmd);
                    PermissionAPI.registerNode("minecraft.command." + strCommand, DefaultPermissionLevel.ALL, "Pixelmon Economies /" + strCommand);
                    ecos.add(cmd);
                }
            }

            // /eco set [player] <num> [currency]
            var set = new EcoSetCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger); {
                var commandStrings = new ArrayList<>(config.settings.commands.eco.set.aliases);
                commandStrings.add(0, config.settings.commands.eco.set.command);
                for (var strCommand : commandStrings) {
                    var cmd = Commands.literal(strCommand)
                            .requires(commandHelper::canExecuteCommand)
                            .then(Commands.argument("player", StringArgumentType.word())
                                    .suggests(commandHelper::suggestOnlinePlayers)
                                    .requires(commandHelper::canExecuteCommand)
                                    .then(Commands.argument("num", LongArgumentType.longArg())
                                            .requires(commandHelper::canExecuteCommand)
                                            .then(Commands.argument("currency", StringArgumentType.word())
                                                    .suggests(commandHelper::suggestCurrencies)
                                                    .requires(commandHelper::canExecuteCommand)
                                                    .executes(set)
                                            )
                                            .executes(set)
                                    )
                            )
                            .then(Commands.argument("num", LongArgumentType.longArg())
                                    .executes(set)
                                    .requires(commandHelper::canExecuteCommand)
                                    .then(Commands.argument("currency", StringArgumentType.word())
                                            .executes(set)
                                            .suggests(commandHelper::suggestCurrencies)
                                            .requires(commandHelper::canExecuteCommand)
                                    )
                            );
                    for (LiteralArgumentBuilder<CommandSource> literalArgumentBuilder : ecos)
                        dispatcher.register(literalArgumentBuilder.then(cmd));
                }
            }

            // /eco add [player] <num> [currency]
            var add = new EcoAddCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger); {
                var commandStrings = new ArrayList<>(config.settings.commands.eco.add.aliases);
                commandStrings.add(0, config.settings.commands.eco.add.command);
                for (var strCommand : commandStrings) {
                    var cmd = Commands.literal(strCommand)
                            .requires(commandHelper::canExecuteCommand)
                            .then(Commands.argument("player", StringArgumentType.word())
                                    .suggests(commandHelper::suggestOnlinePlayers)
                                    .requires(commandHelper::canExecuteCommand)
                                    .then(Commands.argument("num", LongArgumentType.longArg())
                                            .executes(add)
                                            .requires(commandHelper::canExecuteCommand)
                                            .then(Commands.argument("currency", StringArgumentType.word())
                                                    .executes(add)
                                                    .suggests(commandHelper::suggestCurrencies)
                                                    .requires(commandHelper::canExecuteCommand)
                                            )
                                    )
                            ).then(Commands.argument("num", LongArgumentType.longArg())
                                    .executes(add)
                                    .requires(commandHelper::canExecuteCommand)
                                    .then(Commands.argument("currency", StringArgumentType.word())
                                            .executes(add)
                                            .suggests(commandHelper::suggestCurrencies)
                                            .requires(commandHelper::canExecuteCommand)
                                    )
                            );
                    for (LiteralArgumentBuilder<CommandSource> literalArgumentBuilder : ecos)
                        dispatcher.register(literalArgumentBuilder.then(cmd));
                }
            }

            // /eco subtract [player] <num> [currency]
            var subtract = new EcoSubtractCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger); {
                var commandStrings = new ArrayList<>(config.settings.commands.eco.subtract.aliases);
                commandStrings.add(0, config.settings.commands.eco.subtract.command);
                for (var strCommand : commandStrings) {
                    var cmd = Commands.literal(strCommand)
                            .requires(commandHelper::canExecuteCommand)
                            .then(Commands.argument("player", StringArgumentType.word())
                                    .suggests(commandHelper::suggestOnlinePlayers)
                                    .requires(commandHelper::canExecuteCommand)
                                    .then(Commands.argument("num", LongArgumentType.longArg())
                                            .executes(subtract)
                                            .requires(commandHelper::canExecuteCommand)
                                            .then(Commands.argument("currency", StringArgumentType.word())
                                                    .executes(subtract)
                                                    .suggests(commandHelper::suggestCurrencies)
                                                    .requires(commandHelper::canExecuteCommand)
                                            )
                                    )
                            ).then(Commands.argument("num", LongArgumentType.longArg())
                                    .executes(subtract)
                                    .requires(commandHelper::canExecuteCommand)
                                    .then(Commands.argument("currency", StringArgumentType.word())
                                            .executes(subtract)
                                            .suggests(commandHelper::suggestCurrencies)
                                            .requires(commandHelper::canExecuteCommand)
                                    )
                            );
                    for (LiteralArgumentBuilder<CommandSource> literalArgumentBuilder : ecos)
                        dispatcher.register(literalArgumentBuilder.then(cmd));
                }
            }

            // /eco clear <player> [currency]
            var clear = new EcoClearCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger); {
                var commandStrings = new ArrayList<>(config.settings.commands.eco.clear.aliases);
                commandStrings.add(0, config.settings.commands.eco.clear.command);
                for (var strCommand : commandStrings) {
                    var cmd = Commands.literal(strCommand)
                            .then(Commands.argument("player", StringArgumentType.word())
                                    .executes(clear)
                                    .suggests(commandHelper::suggestOnlinePlayers)
                                    .requires(commandHelper::canExecuteCommand)
                                    .then(Commands.argument("currency", StringArgumentType.word())
                                            .executes(clear)
                                            .suggests(commandHelper::suggestCurrencies)
                                            .requires(commandHelper::canExecuteCommand)
                                    )
                            );
                    for (LiteralArgumentBuilder<CommandSource> literalArgumentBuilder : ecos)
                        dispatcher.register(literalArgumentBuilder.then(cmd));
                }
            }

            // /eco help
            var help = new HelpCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger); {
                var commandStrings = new ArrayList<>(config.settings.commands.eco.help.aliases);
                commandStrings.add(0, config.settings.commands.eco.help.command);
                for (var strCommand : commandStrings) {
                    var cmd = Commands.literal(strCommand)
                            .executes(help)
                            .requires(commandHelper::canExecuteCommand);
                    for (LiteralArgumentBuilder<CommandSource> literalArgumentBuilder : ecos)
                        dispatcher.register(literalArgumentBuilder.then(cmd));
                }
            }

            // /eco reload
            var reload = new ReloadCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger); {
                var commandStrings = new ArrayList<>(config.settings.commands.eco.reload.aliases);
                commandStrings.add(0, config.settings.commands.eco.reload.command);
                for (var strCommand : commandStrings) {
                    var cmd = Commands.literal(strCommand)
                            .executes(reload)
                            .requires(commandHelper::canExecuteCommand);
                    for (LiteralArgumentBuilder<CommandSource> literalArgumentBuilder : ecos)
                        dispatcher.register(literalArgumentBuilder.then(cmd));
                }
            }
        }

        /* Balance command */ {
            // /bal [player]
            var bal = new BalanceCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger); {
                var commandStrings = new ArrayList<>(config.settings.commands.balance.aliases);
                commandStrings.add(0, config.settings.commands.balance.command);
                for (var strCommand : commandStrings) {
                    var cmd = Commands.literal(strCommand)
                            .executes(bal)
                            .requires(commandHelper::canExecuteCommand)
                            .then(Commands.argument("player", StringArgumentType.word())
                                    .executes(bal)
                                    .suggests(commandHelper::suggestOnlinePlayers)
                                    .requires(commandHelper::canExecuteCommand)
                            );
                    dispatcher.register(cmd);
                    PermissionAPI.registerNode("minecraft.command." + strCommand, DefaultPermissionLevel.ALL, "Pixelmon Economies /" + strCommand);
                }
            }
        }

        /* BalanceTop command */ {
            // /baltop
            var baltop = new BalanceTopCommand(nekoEconomies, userManager, economiesManager, storage, commandHelper, config, logger); {
                var commandStrings = new ArrayList<>(config.settings.commands.baltop.aliases);
                commandStrings.add(0, config.settings.commands.baltop.command);
                for (var strCommand : commandStrings) {
                    var cmd = Commands.literal(strCommand)
                            .executes(baltop)
                            .requires(commandHelper::canExecuteCommand);
                    dispatcher.register(cmd);
                    PermissionAPI.registerNode("minecraft.command." + strCommand, DefaultPermissionLevel.ALL, "Pixelmon Economies /" + strCommand);
                }
            }
        }
    }

}
