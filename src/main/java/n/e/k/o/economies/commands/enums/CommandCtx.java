package n.e.k.o.economies.commands.enums;

public enum CommandCtx {

    ANY,
    PLAYER,
    CONSOLE;

    public boolean is(CommandCtx other) {
        return this == other || ANY == other;
    }

}
