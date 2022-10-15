package n.e.k.o.economies.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.commands.enums.CommandCtx;
import n.e.k.o.economies.utils.Config;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.Logger;

public class EcoCommand implements Command<CommandSource> {

    private final NekoEconomies nekoEconomies;
    private final Config config;
    private final Logger logger;

    public EcoCommand(NekoEconomies nekoEconomies, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public int run(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        CommandSource source = ctx.getSource();
        if (!nekoEconomies.canExecuteCommand(source, CommandCtx.PLAYER, true))
            return SINGLE_SUCCESS;

        source.sendFeedback(new StringTextComponent("BalanceCommand command"), true);
        ServerPlayerEntity player = source.asPlayer();



        return SINGLE_SUCCESS;
    }

    public void register(LiteralArgumentBuilder<CommandSource> builder) {

    }

}
