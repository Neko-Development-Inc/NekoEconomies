package n.e.k.o.economies.eco;

import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.utils.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EcoUser {

    private final EconomiesManager economiesManager;
    private transient final Config config;
    public transient UUID uuid;
    public transient ServerPlayerEntity player;
    private transient boolean hasUpdate;
    public Map<EcoKey, EcoValue> balances;

    public EcoUser(PlayerEntity player, EconomiesManager economiesManager, Config config) {
        this(player.getUniqueID(), economiesManager, config);
    }

    public EcoUser(ServerPlayerEntity player, EconomiesManager economiesManager, Config config) {
        this(player.getUniqueID(), economiesManager, config);
    }

    public EcoUser(UUID uuid, EconomiesManager economiesManager, Config config) {
        this.uuid = uuid;
        this.economiesManager = economiesManager;
        this.config = config;
        this.balances = new HashMap<>();
        this.addDefaultBalances();
    }

    private void addDefaultBalances() {
        for (EcoKey currency : economiesManager.getAllCurrencies()) {
            balances.put(currency, new EcoValue(this, currency, currency.getDefaultValue()));
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public EcoValue getDefaultCurrency() {
        return balances.get(economiesManager.getDefaultCurrency());
    }

    public EcoValue getCurrency(EcoKey currency) {
        return balances.get(currency);
    }

    public EcoValue getCurrency(String currencyId) {
        return balances.get(economiesManager.getEcoKey(currencyId));
    }

    public EcoValue addCurrencyValue(String currencyId, int num) {
        return addCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue addCurrencyValue(String currencyId, long num) {
        return addCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue addCurrencyValue(String currencyId, float num) {
        return addCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue addCurrencyValue(String currencyId, double num) {
        return addCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue addCurrencyValue(String currencyId, BigDecimal num) {
        return addCurrencyValue(economiesManager.getEcoKey(currencyId), num);
    }
    public EcoValue addCurrencyValue(EcoKey currency, int num) {
        return addCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue addCurrencyValue(EcoKey currency, long num) {
        return addCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue addCurrencyValue(EcoKey currency, float num) {
        return addCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue addCurrencyValue(EcoKey currency, double num) {
        return addCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue addCurrencyValue(EcoKey currency, BigDecimal num) {
        var ecoValue = balances.get(currency);
        ecoValue.add(num);
        return ecoValue;
    }

    public EcoValue subtractCurrencyValue(String currencyId, int num) {
        return subtractCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue subtractCurrencyValue(String currencyId, long num) {
        return subtractCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue subtractCurrencyValue(String currencyId, float num) {
        return subtractCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue subtractCurrencyValue(String currencyId, double num) {
        return subtractCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue subtractCurrencyValue(String currencyId, BigDecimal num) {
        return subtractCurrencyValue(economiesManager.getEcoKey(currencyId), num);
    }
    public EcoValue subtractCurrencyValue(EcoKey currency, int num) {
        return subtractCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue subtractCurrencyValue(EcoKey currency, long num) {
        return subtractCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue subtractCurrencyValue(EcoKey currency, float num) {
        return subtractCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue subtractCurrencyValue(EcoKey currency, double num) {
        return subtractCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue subtractCurrencyValue(EcoKey currency, BigDecimal num) {
        var ecoValue = balances.get(currency);
        ecoValue.subtract(num);
        return ecoValue;
    }

    public EcoValue setCurrencyValue(String currencyId, int num) {
        return setCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue setCurrencyValue(String currencyId, long num) {
        return setCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue setCurrencyValue(String currencyId, float num) {
        return setCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue setCurrencyValue(String currencyId, double num) {
        return setCurrencyValue(economiesManager.getEcoKey(currencyId), new BigDecimal(num));
    }
    public EcoValue setCurrencyValue(String currencyId, BigDecimal num) {
        return setCurrencyValue(economiesManager.getEcoKey(currencyId), num);
    }
    public EcoValue setCurrencyValue(EcoKey currency, int num) {
        return setCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue setCurrencyValue(EcoKey currency, long num) {
        return setCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue setCurrencyValue(EcoKey currency, float num) {
        return setCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue setCurrencyValue(EcoKey currency, double num) {
        return setCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue setCurrencyValue(EcoKey currency, BigDecimal num) {
        var ecoValue = balances.get(currency);
        ecoValue.set(num);
        return ecoValue;
    }

    public EcoValue clearCurrencyValue(String currencyId) {
        return clearCurrencyValue(economiesManager.getEcoKey(currencyId));
    }
    public EcoValue clearCurrencyValue(EcoKey currency) {
        var ecoValue = balances.get(currency);
        ecoValue.clear();
        return ecoValue;
    }

    /**
     * Same as `isGreaterThan(num)`
     */
    public boolean hasEnough(String currencyId, int num) {
        var ecoValue = balances.get(economiesManager.getEcoKey(currencyId));
        return ecoValue.isGreaterThan(num);
    }
    public boolean hasEnough(String currencyId, long num) {
        var ecoValue = balances.get(economiesManager.getEcoKey(currencyId));
        return ecoValue.isGreaterThan(num);
    }
    public boolean hasEnough(String currencyId, float num) {
        var ecoValue = balances.get(economiesManager.getEcoKey(currencyId));
        return ecoValue.isGreaterThan(num);
    }
    public boolean hasEnough(String currencyId, double num) {
        var ecoValue = balances.get(economiesManager.getEcoKey(currencyId));
        return ecoValue.isGreaterThan(num);
    }
    public boolean hasEnough(String currencyId, BigDecimal num) {
        var ecoValue = balances.get(economiesManager.getEcoKey(currencyId));
        return ecoValue.isGreaterThan(num);
    }
    public boolean hasEnough(EcoKey currency, int num) {
        var ecoValue = balances.get(currency);
        return ecoValue.isGreaterThan(num);
    }
    public boolean hasEnough(EcoKey currency, long num) {
        var ecoValue = balances.get(currency);
        return ecoValue.isGreaterThan(num);
    }
    public boolean hasEnough(EcoKey currency, float num) {
        var ecoValue = balances.get(currency);
        return ecoValue.isGreaterThan(num);
    }
    public boolean hasEnough(EcoKey currency, double num) {
        var ecoValue = balances.get(currency);
        return ecoValue.isGreaterThan(num);
    }
    public boolean hasEnough(EcoKey currency, BigDecimal num) {
        var ecoValue = balances.get(currency);
        return ecoValue.isGreaterThan(num);
    }

    public boolean moveBalance(String currencyId, int num, EcoValue toOther) {
        return moveBalance(economiesManager.getEcoKey(currencyId), num, toOther);
    }
    public boolean moveBalance(String currencyId, long num, EcoValue toOther) {
        return moveBalance(economiesManager.getEcoKey(currencyId), num, toOther);
    }
    public boolean moveBalance(String currencyId, float num, EcoValue toOther) {
        return moveBalance(economiesManager.getEcoKey(currencyId), num, toOther);
    }
    public boolean moveBalance(String currencyId, double num, EcoValue toOther) {
        return moveBalance(economiesManager.getEcoKey(currencyId), num, toOther);
    }
    public boolean moveBalance(String currencyId, BigDecimal num, EcoValue toOther) {
        return moveBalance(economiesManager.getEcoKey(currencyId), num, toOther);
    }
    public boolean moveBalance(EcoKey currency, int num, EcoValue toOther) {
        var ecoValue = balances.get(currency);
        return ecoValue.moveBalance(num, toOther);
    }
    public boolean moveBalance(EcoKey currency, long num, EcoValue toOther) {
        var ecoValue = balances.get(currency);
        return ecoValue.moveBalance(num, toOther);
    }
    public boolean moveBalance(EcoKey currency, float num, EcoValue toOther) {
        var ecoValue = balances.get(currency);
        return ecoValue.moveBalance(num, toOther);
    }
    public boolean moveBalance(EcoKey currency, double num, EcoValue toOther) {
        var ecoValue = balances.get(currency);
        return ecoValue.moveBalance(num, toOther);
    }
    public boolean moveBalance(EcoKey currency, BigDecimal num, EcoValue toOther) {
        var ecoValue = balances.get(currency);
        return ecoValue.moveBalance(num, toOther);
    }

    public boolean moveBalance(EcoKey currency, int num, EcoUser toOther) {
        var ecoValue = balances.get(currency);
        var toEcoValue = toOther.getCurrency(currency);
        return ecoValue.moveBalance(num, toEcoValue);
    }
    public boolean moveBalance(EcoKey currency, long num, EcoUser toOther) {
        var ecoValue = balances.get(currency);
        var toEcoValue = toOther.getCurrency(currency);
        return ecoValue.moveBalance(num, toEcoValue);
    }
    public boolean moveBalance(EcoKey currency, float num, EcoUser toOther) {
        var ecoValue = balances.get(currency);
        var toEcoValue = toOther.getCurrency(currency);
        return ecoValue.moveBalance(num, toEcoValue);
    }
    public boolean moveBalance(EcoKey currency, double num, EcoUser toOther) {
        var ecoValue = balances.get(currency);
        var toEcoValue = toOther.getCurrency(currency);
        return ecoValue.moveBalance(num, toEcoValue);
    }
    public boolean moveBalance(EcoKey currency, BigDecimal num, EcoUser toOther) {
        var ecoValue = balances.get(currency);
        var toEcoValue = toOther.getCurrency(currency);
        return ecoValue.moveBalance(num, toEcoValue);
    }

    public boolean hasUpdate() {
        return hasUpdate;
    }

    public void setSaved() {
        hasUpdate = false;
    }

    public void setUnsaved() {
        hasUpdate = true;
    }

    public void updatePlayer(ServerPlayerEntity player) {
        if (this.player != player) {
            this.player = player;
            this.uuid = player.getUniqueID();
            this.hasUpdate = true;
        }
    }

    public EconomiesManager getEconomiesManager() {
        return this.economiesManager;
    }

}
