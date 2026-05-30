package com.allfire.qqjudgment.listeners;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.managers.JudgmentManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MobSpawnListener implements Listener {
    
    private final QQJudgment plugin;
    private final JudgmentManager judgmentManager;
    private final Map<UUID, Long> lastSpawnTime = new HashMap<>();
    
    public MobSpawnListener(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("mob-spawning.enabled", false)) return;
        if (!judgmentManager.isJudgmentActive()) return;
        
        Player player = event.getPlayer();
        if (player.hasPermission("qqjudgment.bypass.mobspawn")) return;
        
        long delay = plugin.getConfig().getLong("mob-spawning.delay-ticks", 400);
        long lastSpawn = lastSpawnTime.getOrDefault(player.getUniqueId(), 0L);
        long currentTick = plugin.getServer().getCurrentTick();
        
        if (currentTick - lastSpawn >= delay) {
            lastSpawnTime.put(player.getUniqueId(), currentTick);
            spawnMobsAroundPlayer(player);
        }
    }
    
    private void spawnMobsAroundPlayer(Player player) {
        int radius = plugin.getConfig().getInt("mob-spawning.radius", 15);
        Location center = player.getLocation();
        World world = center.getWorld();
        
        if (world == null) return;
        
        Map<String, Integer> mobs = new HashMap<>();
        if (plugin.getConfig().contains("mob-spawning.mobs")) {
            mobs = (Map<String, Integer>) plugin.getConfig().getConfigurationSection("mob-spawning.mobs").getValues(false);
        }
        
        for (Map.Entry<String, Integer> entry : mobs.entrySet()) {
            String mobName = entry.getKey();
            int count = entry.getValue();
            
            for (int i = 0; i < count; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double distance = Math.random() * radius;
                double x = center.getX() + Math.cos(angle) * distance;
                double z = center.getZ() + Math.sin(angle) * distance;
                double y = center.getY() + (Math.random() - 0.5) * 3;
                
                Location spawnLoc = new Location(world, x, y, z);
                if (spawnLoc.getBlock().isPassable() && spawnLoc.getY() > 0) {
                    try {
                        EntityType type = EntityType.valueOf(mobName.toUpperCase());
                        world.spawnEntity(spawnLoc, type);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Неизвестный тип моба: " + mobName);
                    }
                }
            }
        }
    }
}
