package n.e.k.o.economies.eco;

public class EcoKey {

    private final String id;
    private final String displayName;
    private final String symbol;
    private final String defaultValue;
    private final boolean overridePixelmonCurrency;

    public EcoKey(String id, String displayName, String symbol, String defaultValue, boolean overridePixelmonCurrency) {
        this.id = id;
        this.displayName = displayName;
        this.symbol = symbol;
        this.defaultValue = defaultValue;
        this.overridePixelmonCurrency = overridePixelmonCurrency;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isOverridePixelmonCurrency() {
        return overridePixelmonCurrency;
    }

}
