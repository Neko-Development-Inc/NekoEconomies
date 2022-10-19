package n.e.k.o.economies.manager;

import n.e.k.o.economies.eco.EcoKey;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.utils.Config;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EconomiesManager {

    private final NekoEconomies nekoEconomies;
    private final Config config;
    private final Logger logger;

    private final Map<String, EcoKey> cachedCurrencies = new HashMap<>();
    private EcoKey defaultCurrency = null;

    public EconomiesManager(NekoEconomies nekoEconomies, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.config = config;
        this.logger = logger;
    }

    public boolean init() {
        try {
            for (var currency : config.settings.currencies) {
                var key = new EcoKey(currency.id, currency.displayName, currency.symbol, currency.defaultValue);
                if (currency.isDefaultCurrency)
                    defaultCurrency = key;
                add(key);
                logger.info("Adding EcoKey. key = '" + key.getId() + "', displayName = '" +
                        key.getDisplayName() + "', symbol = '" +
                        key.getSymbol() + "', defaultValue = '" + key.getDefaultValue() + "'." +
                        (currency.isDefaultCurrency ? " Default currency." : ""));
            }
            return true;
        } catch (Throwable t) {
            logger.error(t.getMessage());
            logger.error(t);
            t.printStackTrace();
        }
        return false;
    }

    public void add(EcoKey currency) {
        cachedCurrencies.put(currency.getId(), currency);
    }

    public List<EcoKey> getAllCurrencies() {
        return new ArrayList<>(cachedCurrencies.values());
    }

    public EcoKey getEcoKey(String id) {
        return cachedCurrencies.get(id);
    }

    /**
     * Probably `PokêDollars` if pixelmon
     */
    public EcoKey getDefaultCurrency() {
        return defaultCurrency;
    }

}
