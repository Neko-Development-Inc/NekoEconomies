package n.e.k.o.economies;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;

public class EcoUser {

    public final UUID uuid;

    public EcoUser(UUID uuid) {
        this.uuid = uuid;
    }

    public EcoUser(PlayerEntity player) {
        this.uuid = player.getUniqueID();
    }

    public EcoUser(ServerPlayerEntity player) {
        this.uuid = player.getUniqueID();
    }

}
