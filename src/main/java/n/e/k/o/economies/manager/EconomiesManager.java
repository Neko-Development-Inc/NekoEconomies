package n.e.k.o.economies.manager;

import n.e.k.o.economies.api.EcoKey;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.utils.Config;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

public class EconomiesManager {

    private final NekoEconomies nekoEconomies;
    private final Config config;
    private final Logger logger;

    private final Map<String, EcoKey> cachedCurrencies = new HashMap<>();
    private EcoKey defaultCurrency = null;

    private final Class<?> pixelmon_bankClass;
    private final Method pixelmon_hasImplementation;
    private final Method pixelmon_getBankAccount;
    private final Class<?> pixelmon_bankAccount;
    private final Method pixelmon_getBalance;
    private final Method pixelmon_setBalance;
    private final Method pixelmon_hasBalance;
    private final Method pixelmon_updatePlayer;
    private final boolean hasPixelmonCurrency;

    public EconomiesManager(NekoEconomies nekoEconomies, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.config = config;
        this.logger = logger;
        /* Set up pixelmon currency (if it exists) */
        var info = setupPixelmonCurrency();
        this.pixelmon_bankClass = (Class<?>) info[0];
        this.pixelmon_bankAccount = (Class<?>) info[1];
        this.pixelmon_hasImplementation = (Method) info[2];
        this.pixelmon_getBankAccount = (Method) info[3];
        this.pixelmon_getBalance = (Method) info[4];
        this.pixelmon_setBalance = (Method) info[5];
        this.pixelmon_hasBalance = (Method) info[6];
        this.pixelmon_updatePlayer = (Method) info[7];
        this.hasPixelmonCurrency = (boolean) info[8];
    }

    private Object[] setupPixelmonCurrency() {
        var info = new Object[9];
        try {
            info[0] = Class.forName("com.pixelmonmod.pixelmon.api.economy.BankAccountProxy");
//            System.out.println("pixelmon_bankClass: " + info[0]);
            info[1] = Class.forName("com.pixelmonmod.pixelmon.api.economy.BankAccount");
//            System.out.println("pixelmon_bankAccount: " + info[1]);
            try {
                info[2] = ((Class<?>)info[0]).getMethod("hasImplementation");
//                System.out.println("pixelmon_hasImplementation 1: " + info[2]);
            } catch (Throwable ignored) {
                info[2] = ((Class<?>)info[0]).getDeclaredMethod("hasImplementation");
//                System.out.println("pixelmon_hasImplementation 2: " + info[2]);
            }
            try {
                info[3] = ((Class<?>)info[0]).getMethod("getBankAccount", UUID.class);
//                System.out.println("pixelmon_getBankAccount 1: " + info[3]);
            } catch (Throwable ignored) {
                info[3] = ((Class<?>)info[0]).getDeclaredMethod("getBankAccount", UUID.class);
//                System.out.println("pixelmon_getBankAccount 2: " + info[3]);
            }
            try {
                info[4] = ((Class<?>)info[1]).getMethod("getBalance");
//                System.out.println("pixelmon_getBalance 1: " + info[4]);
            } catch (Throwable ignored) {
                info[4] = ((Class<?>)info[1]).getDeclaredMethod("getBalance");
//                System.out.println("pixelmon_getBalance 2: " + info[4]);
            }
            try {
                info[5] = ((Class<?>)info[1]).getMethod("setBalance", BigDecimal.class);
//                System.out.println("pixelmon_setBalance 1: " + info[5]);
            } catch (Throwable ignored) {
                info[5] = ((Class<?>)info[1]).getDeclaredMethod("setBalance", BigDecimal.class);
//                System.out.println("pixelmon_setBalance 2: " + info[5]);
            }
            try {
                info[6] = ((Class<?>)info[1]).getMethod("hasBalance", BigDecimal.class);
//                System.out.println("pixelmon_hasBalance 1: " + info[6]);
            } catch (Throwable ignored) {
                info[6] = ((Class<?>)info[1]).getDeclaredMethod("hasBalance", BigDecimal.class);
//                System.out.println("pixelmon_hasBalance 2: " + info[6]);
            }
            try {
                info[7] = ((Class<?>)info[1]).getMethod("updatePlayer");
//                System.out.println("pixelmon_updatePlayer 1: " + info[7]);
            } catch (Throwable ignored) {
                info[7] = ((Class<?>)info[1]).getDeclaredMethod("updatePlayer");
//                System.out.println("pixelmon_updatePlayer 2: " + info[7]);
            }
            info[8] = true;
        } catch (Throwable ignored) {
            ignored.printStackTrace();
            info[8] = false;
        }
        return info;
    }

    public boolean init() {
        try {
            for (var currency : config.settings.currencies) {
                if (currency.overridePixelmonCurrency) {
                    if (hasPixelmonCurrency) {
                        logger.info("Enabled pixelmon currency overriding, linked to the NekoEconomy currency '" + currency.id + "'.");
                    } else {
                        currency.overridePixelmonCurrency = false;
                        logger.warn("OverridePixelmonCurrency was enabled for '" + currency.id + "', but Pixelmon itself wasn't found. Ignoring override.");
                    }
                }
                var key = new EcoKey(currency.id, currency.displayName, currency.symbol, currency.defaultValue, currency.overridePixelmonCurrency);
                if (currency.isDefaultCurrency)
                    defaultCurrency = key;
                add(key);
                logger.info("Adding EcoKey. key = '" + key.getId() + "', displayName = '" +
                        key.getDisplayName() + "', symbol = '" +
                        key.getSymbol() + "', defaultValue = '" + key.getDefaultValue() + "'." +
                        (currency.isDefaultCurrency ? " Default currency." : "") +
                        (currency.overridePixelmonCurrency ? " Overrides pixelmon currency." : ""));
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

    public EcoKey getDefaultCurrency() {
        return defaultCurrency;
    }

    public boolean pixelmonHasImplementation() {
        if (!hasPixelmonCurrency)
            return false;
        try {
            return (boolean) pixelmon_hasImplementation.invoke(null);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        return false;
    }

    public Optional<Object> getPixelmonBankAccount(UUID uuid) {
        if (!hasPixelmonCurrency)
            return Optional.empty();
        try {
            Optional<Object> optional = (Optional<Object>) pixelmon_getBankAccount.invoke(null, uuid);
            if (optional.isPresent()) {
                var obj = optional.get();
//                System.out.println("getPixelmonBankAccount [" + uuid + "]: " + obj);
                return Optional.of(obj);
            }
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        return Optional.empty();
    }

    public BigDecimal getPixelmonCurrency(UUID uuid) {
        var optional = getPixelmonBankAccount(uuid);
        if (optional.isEmpty())
            return null;
        try {
            return (BigDecimal) pixelmon_getBalance.invoke(optional.get());
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    public void setPixelmonCurrency(UUID uuid, BigDecimal amount) {
        var optional = getPixelmonBankAccount(uuid);
        if (optional.isEmpty())
            return;
        try {
            var account = optional.get();
            pixelmon_setBalance.invoke(account, amount);
            pixelmon_updatePlayer.invoke(account);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
    }

    public boolean hasPixelmonCurrency(UUID uuid, BigDecimal amount) {
        var optional = getPixelmonBankAccount(uuid);
        if (optional.isEmpty())
            return false;
        try {
            return (boolean) pixelmon_hasBalance.invoke(optional.get(), amount);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        return false;
    }

}
