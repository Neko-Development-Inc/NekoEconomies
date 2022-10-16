package n.e.k.o.economies;

import n.e.k.o.economies.utils.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public class EcoUser {

    private transient final Config config;
    public UUID uuid;
    public transient ServerPlayerEntity player;
    private transient boolean hasUpdate;
    public Map<String, String> currencies;

    public EcoUser(UUID uuid, Config config) {
        this.uuid = uuid;
        this.config = config;
    }

    public EcoUser(PlayerEntity player, Config config) {
        this.uuid = player.getUniqueID();
        this.config = config;
    }

    public EcoUser(ServerPlayerEntity player, Config config) {
        this.uuid = player.getUniqueID();
        this.config = config;
    }

    public UUID getUuid() {
        return uuid;
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
