package n.e.k.o.economies.eco;

import n.e.k.o.economies.manager.EconomiesManager;
import n.e.k.o.economies.utils.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.math.BigDecimal;
import java.math.BigInteger;
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

    public EcoValue getCurrency(EcoKey currency) {
        return balances.get(currency);
    }

    public EcoValue addCurrencyValue(EcoKey currency, int num) {
        return addCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue addCurrencyValue(EcoKey currency, BigDecimal num) {
        var ecoValue = balances.get(currency);
        ecoValue.add(num);
        return ecoValue;
    }

    public EcoValue subtractCurrencyValue(EcoKey currency, int num) {
        return subtractCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue subtractCurrencyValue(EcoKey currency, BigDecimal num) {
        var ecoValue = balances.get(currency);
        ecoValue.subtract(num);
        return ecoValue;
    }

    public EcoValue setCurrencyValue(EcoKey currency, int num) {
        return setCurrencyValue(currency, new BigDecimal(num));
    }
    public EcoValue setCurrencyValue(EcoKey currency, BigDecimal num) {
        var ecoValue = balances.get(currency);
        ecoValue.set(num);
        return ecoValue;
    }

    public EcoValue clearCurrencyValue(EcoKey currency) {
        var ecoValue = balances.get(currency);
        ecoValue.clear();
        return ecoValue;
    }

    /**
     * Same as `isGreaterThan(num)`
     */
    public boolean hasEnough(EcoKey currency, int num) {
        var ecoValue = balances.get(currency);
        return ecoValue.isGreaterThan(num);
    }

    public boolean moveBalance(EcoKey currency, int num, EcoValue toOther) {
        var ecoValue = balances.get(currency);
        return ecoValue.moveBalance(num, toOther);
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

}
