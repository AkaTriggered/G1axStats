package dev.akatriggered.listener;

import dev.akatriggered.manager.StatsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener {
    
    private final StatsManager statsManager;

    public PlayerListener(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer != null && !killer.equals(victim)) {
            statsManager.handleKill(killer, victim);
        }
    }
}
