package dev.akatriggered;

import dev.akatriggered.database.DatabaseManager;
import dev.akatriggered.listener.PlayerListener;
import dev.akatriggered.manager.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class G1axStats extends JavaPlugin {

    private static G1axStats instance;
    private DatabaseManager databaseManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;
        
        databaseManager = new DatabaseManager(this);
        statsManager = new StatsManager(databaseManager);
        
        getServer().getPluginManager().registerEvents(new PlayerListener(statsManager), this);
    }

    @Override
    public void onDisable() {
        if (statsManager != null) {
            statsManager.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public static G1axStats getInstance() {
        return instance;
    }
}
