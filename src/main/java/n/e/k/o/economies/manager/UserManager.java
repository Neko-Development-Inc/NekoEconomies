package n.e.k.o.economies.manager;

import n.e.k.o.economies.EcoUser;
import n.e.k.o.economies.NekoEconomies;
import n.e.k.o.economies.storage.IStorage;
import n.e.k.o.economies.utils.Config;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class UserManager {

    private final NekoEconomies nekoEconomies;
    private final IStorage storage;
    private final Config config;
    private final Logger logger;

    private static final Map<String, EcoUser> cachedUsers = new HashMap<>();

    public UserManager(NekoEconomies nekoEconomies, IStorage storage, Config config, Logger logger) {
        this.nekoEconomies = nekoEconomies;
        this.storage = storage;
        this.config = config;
        this.logger = logger;
    }

    public static EcoUser getUser(UUID uuid) {
        return cachedUsers.get(uuid.toString());
    }
    public static EcoUser getUser(String uuid) {
        return cachedUsers.get(uuid);
    }

    public static void add(EcoUser user) {
        checkPlayer(user);
        cachedUsers.put(user.getUuid().toString(), user);
    }

    private static void checkPlayer(EcoUser user) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;
        ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(user.getUuid());
        if (player != null)
            user.updatePlayer(player);
    }

    public static List<EcoUser> getAllUsers() {
        return new ArrayList<>(cachedUsers.values());
    }

    public static void setAllUnsaved() {
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
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        String uuid = player.getUniqueID().toString();
        EcoUser user;
        if (cachedUsers.containsKey(uuid)) {
            user = cachedUsers.get(uuid);
            user.updatePlayer(player);
        }
        else {
            user = new EcoUser(player, config);
            user.setUnsaved();
            cachedUsers.put(uuid, user);
        }
        storage.save(user);
    }

    private void handle(ServerPlayerEntity player) {
        String uuid = player.getUniqueID().toString();
        if (cachedUsers.containsKey(uuid))
            cachedUsers.get(uuid).updatePlayer(player);
        else {
            EcoUser user = new EcoUser(player, config);
            user.setUnsaved();
            storage.save(user);
            cachedUsers.put(uuid, user);
        }
    }

    public static void create(Map<String, Object> map, Config config) {
        String uuid = (String) map.get("uuid");
        if (cachedUsers.containsKey(uuid)) {
            checkPlayer(cachedUsers.get(uuid));
            return;
        }
        EcoUser user = new EcoUser(UUID.fromString(uuid), config);
        // TODO: Set user variables
        add(user);
    }

}
