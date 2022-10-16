package n.e.k.o.economies.eco;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class EcoValue {

    private final EcoUser user;
    private final EcoKey currency;
    private BigDecimal balance;

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
            return balance;
        }
    }

    public String getBalanceString() {
        synchronized (_lock) {
            return balance.stripTrailingZeros().toPlainString();
        }
    }

    private static final Map<Integer, DecimalFormat> formatters = new HashMap<>(){{
        for (int i = 0; i <= 4; i++) {
            var df = new DecimalFormat();
            df.setMaximumFractionDigits(i);
            df.setMinimumFractionDigits(0);
            df.setGroupingUsed(false);
            put(i, df);
        }
    }};

    public String getBalanceString(int decimals) {
        var df = formatters.get(decimals);
        if (df == null) {
            df = new DecimalFormat();
            df.setRoundingMode(RoundingMode.DOWN);
            df.setMaximumFractionDigits(decimals);
            df.setMinimumFractionDigits(0);
            df.setGroupingUsed(false);
            formatters.put(decimals, df);
        }
        synchronized (_lock) {
            return df.format(balance.stripTrailingZeros());
        }
    }

    public BigDecimal add(int num) {
        return add(BigDecimal.valueOf(num));
    }
    public BigDecimal add(BigDecimal num) {
        synchronized (_lock) {
            balance = balance.add(num);
            user.setUnsaved();
            return balance;
        }
    }

    public BigDecimal subtract(int num) {
        return subtract(BigDecimal.valueOf(num));
    }
    public BigDecimal subtract(BigDecimal num) {
        synchronized (_lock) {
            balance = balance.subtract(num);
            user.setUnsaved();
            return balance;
        }
    }

    public BigDecimal set(int num) {
        return set(BigDecimal.valueOf(num));
    }
    public BigDecimal set(BigDecimal num) {
        synchronized (_lock) {
            balance = num;
            user.setUnsaved();
            return balance;
        }
    }

    public BigDecimal clear() {
        synchronized (_lock) {
            balance = new BigDecimal(0);
            user.setUnsaved();
            return balance;
        }
    }

    public boolean isGreaterThan(int num) {
        return isGreaterThan(BigDecimal.valueOf(num));
    }
    public boolean isGreaterThan(BigDecimal num) {
        synchronized (_lock) {
            return balance.compareTo(num) >= 0;
        }
    }

    // Synchronically move money from one user to another
    public boolean moveBalance(int num, EcoValue toOther) {
        return moveBalance(BigDecimal.valueOf(num), toOther);
    }
    public boolean moveBalance(BigDecimal num, EcoValue toOther) {
        synchronized (_lock) {
            if (balance.compareTo(num) < 0) // Not enough money!
                return false;
            synchronized (toOther._lock) {
                var tmp = balance;
                balance = balance.subtract(num);
                if (balance.compareTo(BigDecimal.ZERO) < 0) { // Somehow we ended up at negative balance, abort!
                    balance = tmp;
                    return false;
                }
                toOther.balance = toOther.balance.add(num);
                return true;
            }
        }
    }

}
