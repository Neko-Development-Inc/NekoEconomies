package n.e.k.o.economies.manager;

import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.eco.EcoUser;
import n.e.k.o.economies.storage.IStorage;
import n.e.k.o.economies.utils.Config;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class UserManager {

    private final NekoEconomies nekoEconomies;
    private final EconomiesManager economiesManager;
    private IStorage storage;
    private final Config config;
    private final Logger logger;

    private static final Map<String, EcoUser> cachedUsers = new HashMap<>();

    public UserManager(NekoEconomies nekoEconomies, EconomiesManager economiesManager, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.economiesManager = economiesManager;
        this.config = config;
        this.logger = logger;
    }

    public void setStorage(IStorage storage) {
        this.storage = storage;
    }

    public EcoUser getUser(String uuid) {
        return cachedUsers.get(uuid);
    }

    public EcoUser getUser(UUID uuid) {
        var ecoUser = cachedUsers.get(uuid.toString());
        if (ecoUser != null)
            return ecoUser;
        ecoUser = new EcoUser(uuid, economiesManager, config);
        add(ecoUser);
        storage.save(ecoUser);
        return ecoUser;
    }

    public void add(EcoUser user) {
        checkPlayer(user);
        cachedUsers.put(user.getUuid().toString(), user);
    }

    private void checkPlayer(EcoUser user) {
        final var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;
        var player = server.getPlayerList().getPlayerByUUID(user.getUuid());
        if (player != null)
            user.updatePlayer(player);
    }

    public List<EcoUser> getAllUsers() {
        return new ArrayList<>(cachedUsers.values());
    }

    public void setAllUnsaved() {
        cachedUsers.values().forEach(EcoUser::setUnsaved);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        handle((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        handle((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        var player = (ServerPlayerEntity) event.getPlayer();
        var uuid = player.getUniqueID().toString();
        EcoUser user;
        if (cachedUsers.containsKey(uuid)) {
            user = cachedUsers.get(uuid);
            user.updatePlayer(player);
        }
        else {
            user = new EcoUser(player, economiesManager, config);
            user.setUnsaved();
            cachedUsers.put(uuid, user);
        }
        storage.save(user);
    }

    private void handle(ServerPlayerEntity player) {
        var uuid = player.getUniqueID().toString();
        if (cachedUsers.containsKey(uuid))
            cachedUsers.get(uuid).updatePlayer(player);
        else {
            var user = new EcoUser(player, economiesManager, config);
            user.setUnsaved();
            storage.save(user);
            cachedUsers.put(uuid, user);
        }
    }

    /**
     * Only used by MySqlStorage
     */
    public void create(Map<String, Object> map, Config config, EconomiesManager economiesManager) {
        var uuid = (String) map.get("uuid");
        if (cachedUsers.containsKey(uuid)) {
            checkPlayer(cachedUsers.get(uuid));
            return;
        }
        var user = new EcoUser(UUID.fromString(uuid), economiesManager, config);
        user.balances.putAll(NekoEconomies.mapStringToCurrencies(user, (String)map.get("balances"), economiesManager));
        add(user);
    }

}
