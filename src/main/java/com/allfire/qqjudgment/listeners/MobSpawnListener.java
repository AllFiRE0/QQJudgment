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
    private BukkitRunnable spawnTask;
    
    public MobSpawnListener(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Запускаем периодическую проверку для всех игроков
        startGlobalSpawnTask();
    }
    
    private void startGlobalSpawnTask() {
        long delayTicks = plugin.getConfig().getLong("mob-spawning.delay-ticks", 400);
        
        spawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!judgmentManager.isJudgmentActive()) return;
                if (!plugin.getConfig().getBoolean("mob-spawning.enabled", false)) return;
                
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.hasPermission("qqjudgment.bypass.mobspawn")) continue;
                    
                    long lastSpawn = lastSpawnTime.getOrDefault(player.getUniqueId(), 0L);
                    long currentTime = System.currentTimeMillis();
                    long delayMillis = delayTicks * 50L;
                    
                    if (currentTime - lastSpawn >= delayMillis) {
                        lastSpawnTime.put(player.getUniqueId(), currentTime);
                        spawnMobsAroundPlayer(player);
                    }
                }
            }
        };
        
        // Запускаем каждые 20 тиков (1 секунда)
        spawnTask.runTaskTimer(plugin, 0L, 20L);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("mob-spawning.enabled", false)) return;
        if (!judgmentManager.isJudgmentActive()) return;
        
        Player player = event.getPlayer();
        if (player.hasPermission("qqjudgment.bypass.mobspawn")) return;
        
        long delayTicks = plugin.getConfig().getLong("mob-spawning.delay-ticks", 400);
        long delayMillis = delayTicks * 50L;
        long lastSpawn = lastSpawnTime.getOrDefault(player.getUniqueId(), 0L);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastSpawn >= delayMillis) {
            lastSpawnTime.put(player.getUniqueId(), currentTime);
            spawnMobsAroundPlayer(player);
        }
    }
    
    private void spawnMobsAroundPlayer(Player player) {
        int radius = plugin.getConfig().getInt("mob-spawning.radius", 15);
        Location center = player.getLocation();
        World world = center.getWorld();
        
        if (world == null) return;
        
        // Получаем мобов из конфига
        Map<String, Integer> mobs = new HashMap<>();
        if (plugin.getConfig().contains("mob-spawning.mobs")) {
            Map<String, Object> mobsRaw = plugin.getConfig().getConfigurationSection("mob-spawning.mobs").getValues(false);
            for (Map.Entry<String, Object> entry : mobsRaw.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    mobs.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                }
            }
        }
        
        if (mobs.isEmpty()) {
            plugin.getLogger().warning("[MobSpawn] Нет мобов в конфиге! Добавьте мобов в секцию mob-spawning.mobs");
            return;
        }
        
        plugin.getLogger().info("[MobSpawn] Спавним мобов для игрока " + player.getName());
        
        for (Map.Entry<String, Integer> entry : mobs.entrySet()) {
            String mobName = entry.getKey();
            int count = entry.getValue();
            
            for (int i = 0; i < count; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double distance = 2 + Math.random() * (radius - 2); // Не спавним слишком близко
                double x = center.getX() + Math.cos(angle) * distance;
                double z = center.getZ() + Math.sin(angle) * distance;
                double y = center.getY(); // На том же уровне, что и игрок
                
                Location spawnLoc = new Location(world, x, y, z);
                
                // Ищем подходящий блок для спавна
                for (int offset = -2; offset <= 2; offset++) {
                    Location checkLoc = spawnLoc.clone().add(0, offset, 0);
                    if (checkLoc.getBlock().getType().isSolid() && 
                        checkLoc.clone().add(0, 1, 0).getBlock().isEmpty() &&
                        checkLoc.clone().add(0, 2, 0).getBlock().isEmpty()) {
                        spawnLoc = checkLoc.clone().add(0, 1, 0);
                        break;
                    }
                }
                
                try {
                    EntityType type = EntityType.valueOf(mobName.toUpperCase());
                    world.spawnEntity(spawnLoc, type);
                    plugin.getLogger().info("[MobSpawn] Заспавнен " + mobName + " в " + spawnLoc.getBlockX() + "," + spawnLoc.getBlockY() + "," + spawnLoc.getBlockZ());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[MobSpawn] Неизвестный тип моба: " + mobName + ". Доступные типы: ZOMBIE, SKELETON, SPIDER, CREEPER и т.д.");
                }
            }
        }
    }
    
    public void stop() {
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
        }
    }
}
