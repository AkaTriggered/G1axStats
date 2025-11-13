package dev.akatriggered.manager;

import dev.akatriggered.database.DatabaseManager;
import dev.akatriggered.util.ColorUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class StatsManager {
    
    private final DatabaseManager databaseManager;

    public StatsManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void handleKill(Player killer, Player victim) {
        String killerUuid = killer.getUniqueId().toString();
        String victimUuid = victim.getUniqueId().toString();
        
        databaseManager.incrementKills(killerUuid, victimUuid);
        
        CompletableFuture.allOf(
            databaseManager.getKills(killerUuid, victimUuid),
            databaseManager.getKills(victimUuid, killerUuid)
        ).thenAccept(v -> {
            databaseManager.getKills(killerUuid, victimUuid).thenAccept(killerKills -> {
                databaseManager.getKills(victimUuid, killerUuid).thenAccept(victimKills -> {
                    String message = ColorUtil.colorize(String.format(
                        "&a%s &7%d &8- &c%s &7%d",
                        killer.getName(), killerKills,
                        victim.getName(), victimKills
                    ));
                    killer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                });
            });
        });
    }

    public CompletableFuture<Integer> getKills(Player killer, Player victim) {
        return databaseManager.getKills(killer.getUniqueId().toString(), victim.getUniqueId().toString());
    }

    public void shutdown() {
        // Any cleanup if needed
    }
}
