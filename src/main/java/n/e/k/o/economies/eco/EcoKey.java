package n.e.k.o.economies.eco;

public class EcoKey {

    private final String id;
    private final String displayName;
    private final String defaultValue;

    public EcoKey(String id, String displayName, String defaultValue) {
        this.id = id;
        this.displayName = displayName;
        this.defaultValue = defaultValue;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

}
