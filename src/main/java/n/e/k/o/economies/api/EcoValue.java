package n.e.k.o.economies.api;

import n.e.k.o.economies.manager.EconomiesManager;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class EcoValue {

    private final EcoUser user;
    private final EcoKey currency;
    private BigDecimal balance;

    private final EconomiesManager economiesManager;
    private final boolean overridePixelmonCurrency;

    private transient final Object _lock = new Object();

    public EcoValue(EcoUser user, EcoKey currency) {
        this(user, currency, BigDecimal.ZERO);
    }

    public EcoValue(EcoUser user, EcoKey currency, int num) {
        this(user, currency, new BigDecimal(num));
    }

    public EcoValue(EcoUser user, EcoKey currency, String num) {
        this(user, currency, new BigDecimal(num));
    }

    public EcoValue(EcoUser user, EcoKey currency, BigDecimal num) {
        this.user = user;
        this.currency = currency;
        this.balance = num;
        this.economiesManager = user.getEconomiesManager();
        this.overridePixelmonCurrency = currency.isOverridePixelmonCurrency();
        this.setDefaultPixelmonCurrencyBalance();
    }

    private void setDefaultPixelmonCurrencyBalance() {
//        System.out.println("new EcoValue(" + getId() + "): " + overridePixelmonCurrency);
        if (overridePixelmonCurrency) {
            var current = economiesManager.getPixelmonCurrency(user.uuid);
//            System.out.println("!!! Current pixelmon balance: " + current);
            if (BigDecimal.ZERO.equals(current)) { // TODO: Check if this is exploitable by just having 0 cash lmao
                economiesManager.setPixelmonCurrency(user.uuid, balance);
//                var newBalance = economiesManager.getPixelmonCurrency(user.uuid);
//                System.out.println("!!! New pixelmon balance: " + newBalance);
            }
        }
    }

    public EcoKey getCurrency() {
        return currency;
    }

    public String getId() {
        return currency.getId();
    }

    public String getDisplayName() {
        return currency.getDisplayName();
    }

    public EcoUser getUser() {
        return user;
    }

    public BigDecimal getBalance() {
        synchronized (_lock) {
            if (overridePixelmonCurrency) {
                return economiesManager.getPixelmonCurrency(user.uuid);
            }
            return balance;
        }
    }

    public String getBalanceString() {
        synchronized (_lock) {
            BigDecimal tmp;
            if (overridePixelmonCurrency) {
                tmp = economiesManager.getPixelmonCurrency(user.uuid);
            } else {
                tmp = balance;
            }
            return tmp.stripTrailingZeros().toPlainString();
        }
    }

    public String getBalanceString(int decimals) {
        return getBalanceString(decimals, false);
    }

    public String getBalanceString(int decimals, boolean useGrouping) {
        var nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(decimals);
        nf.setMinimumIntegerDigits(3);
        nf.setGroupingUsed(useGrouping);
        BigDecimal tmp;
        if (overridePixelmonCurrency) {
            tmp = economiesManager.getPixelmonCurrency(user.uuid);
        } else {
            tmp = balance;
        }
        return nf.format(tmp).replaceFirst("^0+(?!$)", "");
    }

    public BigDecimal add(int num) {
        return add(BigDecimal.valueOf(num));
    }
    public BigDecimal add(long num) {
        return add(BigDecimal.valueOf(num));
    }
    public BigDecimal add(float num) {
        return add(BigDecimal.valueOf(num));
    }
    public BigDecimal add(double num) {
        return add(BigDecimal.valueOf(num));
    }
    public BigDecimal add(BigDecimal num) {
        synchronized (_lock) {
            BigDecimal tmp;
            if (overridePixelmonCurrency) {
                tmp = economiesManager.getPixelmonCurrency(user.uuid);
            } else {
                tmp = balance;
            }
            tmp = tmp.add(num);
            if (overridePixelmonCurrency) {
                economiesManager.setPixelmonCurrency(user.uuid, tmp);
            } else {
                balance = tmp;
                user.setUnsaved();
            }
            return tmp;
        }
    }

    public BigDecimal subtract(int num) {
        return subtract(BigDecimal.valueOf(num));
    }
    public BigDecimal subtract(long num) {
        return subtract(BigDecimal.valueOf(num));
    }
    public BigDecimal subtract(float num) {
        return subtract(BigDecimal.valueOf(num));
    }
    public BigDecimal subtract(double num) {
        return subtract(BigDecimal.valueOf(num));
    }
    public BigDecimal subtract(BigDecimal num) {
        synchronized (_lock) {
            BigDecimal tmp;
            if (overridePixelmonCurrency) {
                tmp = economiesManager.getPixelmonCurrency(user.uuid);
            } else {
                tmp = balance;
            }
            tmp = tmp.subtract(num);
            if (overridePixelmonCurrency) {
                economiesManager.setPixelmonCurrency(user.uuid, tmp);
            } else {
                balance = tmp;
                user.setUnsaved();
            }
            return tmp;
        }
    }

    public BigDecimal set(int num) {
        return set(BigDecimal.valueOf(num));
    }
    public BigDecimal set(long num) {
        return set(BigDecimal.valueOf(num));
    }
    public BigDecimal set(float num) {
        return set(BigDecimal.valueOf(num));
    }
    public BigDecimal set(double num) {
        return set(BigDecimal.valueOf(num));
    }
    public BigDecimal set(BigDecimal num) {
        synchronized (_lock) {
            if (overridePixelmonCurrency) {
                economiesManager.setPixelmonCurrency(user.uuid, num);
            } else {
                balance = num;
                user.setUnsaved();
            }
            return num;
        }
    }

    public BigDecimal clear() {
        synchronized (_lock) {
            BigDecimal tmp = new BigDecimal(0);
            if (overridePixelmonCurrency) {
                economiesManager.setPixelmonCurrency(user.uuid, tmp);
            } else {
                balance = tmp;
                user.setUnsaved();
            }
            return tmp;
        }
    }

    public boolean isGreaterThan(int num) {
        return isGreaterThan(BigDecimal.valueOf(num));
    }
    public boolean isGreaterThan(long num) {
        return isGreaterThan(BigDecimal.valueOf(num));
    }
    public boolean isGreaterThan(float num) {
        return isGreaterThan(BigDecimal.valueOf(num));
    }
    public boolean isGreaterThan(double num) {
        return isGreaterThan(BigDecimal.valueOf(num));
    }
    public boolean isGreaterThan(BigDecimal num) {
        synchronized (_lock) {
            BigDecimal tmp;
            if (overridePixelmonCurrency) {
                tmp = economiesManager.getPixelmonCurrency(user.uuid);
            } else {
                tmp = balance;
            }
            return tmp.compareTo(num) >= 0;
        }
    }

    // Synchronically move money from one user to another
    public boolean moveBalance(int num, EcoValue toOther) {
        return moveBalance(BigDecimal.valueOf(num), toOther);
    }
    public boolean moveBalance(long num, EcoValue toOther) {
        return moveBalance(BigDecimal.valueOf(num), toOther);
    }
    public boolean moveBalance(float num, EcoValue toOther) {
        return moveBalance(BigDecimal.valueOf(num), toOther);
    }
    public boolean moveBalance(double num, EcoValue toOther) {
        return moveBalance(BigDecimal.valueOf(num), toOther);
    }
    public boolean moveBalance(BigDecimal num, EcoValue toOther) {
        synchronized (_lock) {
            BigDecimal tmp;
            if (overridePixelmonCurrency) {
                tmp = economiesManager.getPixelmonCurrency(user.uuid);
            } else {
                tmp = balance;
            }
            if (tmp.compareTo(num) < 0) // Not enough money!
                return false;
            synchronized (toOther._lock) {
                var copy = tmp;
                tmp = tmp.subtract(num);
                if (tmp.compareTo(BigDecimal.ZERO) < 0) { // Somehow we ended up at negative balance, abort!
                    if (!overridePixelmonCurrency)
                        balance = copy;
                    return false;
                }
                if (overridePixelmonCurrency) {
                    economiesManager.setPixelmonCurrency(user.uuid, tmp);
                } else {
                    balance = tmp;
                    user.setUnsaved();
                }
                if (toOther.isOverridePixelmonCurrency()) {
                    economiesManager.setPixelmonCurrency(toOther.user.uuid, toOther.getBalance().add(num));
                } else {
                    toOther.balance = toOther.balance.add(num);
                    toOther.user.setUnsaved();
                }
                return true;
            }
        }
    }

    public int intVal() {
        BigDecimal tmp;
        if (overridePixelmonCurrency) {
            tmp = economiesManager.getPixelmonCurrency(user.uuid);
        } else {
            tmp = balance;
        }
        return tmp.intValue();
    }

    public int intValExact() {
        BigDecimal tmp;
        if (overridePixelmonCurrency) {
            tmp = economiesManager.getPixelmonCurrency(user.uuid);
        } else {
            tmp = balance;
        }
        return tmp.intValueExact();
    }

    public short shortVal() {
        BigDecimal tmp;
        if (overridePixelmonCurrency) {
            tmp = economiesManager.getPixelmonCurrency(user.uuid);
        } else {
            tmp = balance;
        }
        return tmp.shortValue();
    }

    public short shortValExact() {
        BigDecimal tmp;
        if (overridePixelmonCurrency) {
            tmp = economiesManager.getPixelmonCurrency(user.uuid);
        } else {
            tmp = balance;
        }
        return tmp.shortValueExact();
    }

    public long longVal() {
        BigDecimal tmp;
        if (overridePixelmonCurrency) {
            tmp = economiesManager.getPixelmonCurrency(user.uuid);
        } else {
            tmp = balance;
        }
        return tmp.longValue();
    }

    public long longValExact() {
        BigDecimal tmp;
        if (overridePixelmonCurrency) {
            tmp = economiesManager.getPixelmonCurrency(user.uuid);
        } else {
            tmp = balance;
        }
        return tmp.longValueExact();
    }

    public float floatVal() {
        BigDecimal tmp;
        if (overridePixelmonCurrency) {
            tmp = economiesManager.getPixelmonCurrency(user.uuid);
        } else {
            tmp = balance;
        }
        return tmp.floatValue();
    }

    public double doubleVal() {
        BigDecimal tmp;
        if (overridePixelmonCurrency) {
            tmp = economiesManager.getPixelmonCurrency(user.uuid);
        } else {
            tmp = balance;
        }
        return tmp.doubleValue();
    }

    public boolean isOverridePixelmonCurrency() {
        return overridePixelmonCurrency;
    }

}
