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
    private boolean debug;
    
    public MobSpawnListener(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
        this.debug = plugin.getConfig().getBoolean("debug", false);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        if (debug) {
            plugin.getLogger().info("[MobSpawn] Debug режим ВКЛЮЧЕН");
        }
        
        // Запускаем периодическую проверку для всех игроков
        startGlobalSpawnTask();
    }
    
    private void startGlobalSpawnTask() {
        long delayTicks = plugin.getConfig().getLong("mob-spawning.delay-ticks", 400);
        
        spawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!judgmentManager.isJudgmentActive()) {
                    if (debug) plugin.getLogger().info("[MobSpawn] СН не активна, спавн пропущен");
                    return;
                }
                if (!plugin.getConfig().getBoolean("mob-spawning.enabled", false)) {
                    if (debug) plugin.getLogger().info("[MobSpawn] Спавн мобов выключен в конфиге");
                    return;
                }
                
                int spawned = 0;
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.hasPermission("qqjudgment.bypass.mobspawn")) {
                        if (debug) plugin.getLogger().info("[MobSpawn] Игрок " + player.getName() + " имеет bypass, пропускаем");
                        continue;
                    }
                    
                    long lastSpawn = lastSpawnTime.getOrDefault(player.getUniqueId(), 0L);
                    long currentTime = System.currentTimeMillis();
                    long delayMillis = delayTicks * 50L;
                    
                    if (currentTime - lastSpawn >= delayMillis) {
                        lastSpawnTime.put(player.getUniqueId(), currentTime);
                        spawnMobsAroundPlayer(player);
                        spawned++;
                    }
                }
                
                if (debug && spawned > 0) {
                    plugin.getLogger().info("[MobSpawn] Заспавнены мобы для " + spawned + " игроков");
                }
            }
        };
        
        // Запускаем каждые 20 тиков (1 секунда)
        spawnTask.runTaskTimer(plugin, 0L, 20L);
        if (debug) {
            plugin.getLogger().info("[MobSpawn] Глобальный таск запущен, интервал: " + delayTicks + " тиков");
        }
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
            if (debug) {
                plugin.getLogger().info("[MobSpawn] Спавн от движения игрока " + player.getName());
            }
        }
    }
    
    private void spawnMobsAroundPlayer(Player player) {
        int radius = plugin.getConfig().getInt("mob-spawning.radius", 15);
        Location center = player.getLocation();
        World world = center.getWorld();
        
        if (world == null) {
            if (debug) plugin.getLogger().warning("[MobSpawn] Мир не найден для игрока " + player.getName());
            return;
        }
        
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
            if (debug) plugin.getLogger().warning("[MobSpawn] Нет мобов в конфиге! Добавьте мобов в секцию mob-spawning.mobs");
            return;
        }
        
        if (debug) {
            plugin.getLogger().info("[MobSpawn] Спавним мобов для игрока " + player.getName() + " (радиус: " + radius + ")");
        }
        
        int totalSpawned = 0;
        
        for (Map.Entry<String, Integer> entry : mobs.entrySet()) {
            String mobName = entry.getKey();
            int count = entry.getValue();
            
            for (int i = 0; i < count; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double distance = 2 + Math.random() * (radius - 2);
                double x = center.getX() + Math.cos(angle) * distance;
                double z = center.getZ() + Math.sin(angle) * distance;
                double y = center.getY();
                
                Location spawnLoc = new Location(world, x, y, z);
                
                // Ищем подходящий блок для спавна
                boolean foundSpot = false;
                for (int offset = -2; offset <= 2; offset++) {
                    Location checkLoc = spawnLoc.clone().add(0, offset, 0);
                    if (checkLoc.getBlock().getType().isSolid() && 
                        checkLoc.clone().add(0, 1, 0).getBlock().isEmpty() &&
                        checkLoc.clone().add(0, 2, 0).getBlock().isEmpty()) {
                        spawnLoc = checkLoc.clone().add(0, 1, 0);
                        foundSpot = true;
                        break;
                    }
                }
                
                if (!foundSpot) {
                    if (debug) plugin.getLogger().warning("[MobSpawn] Не найдено место для спавна " + mobName);
                    continue;
                }
                
                try {
                    EntityType type = EntityType.valueOf(mobName.toUpperCase());
                    world.spawnEntity(spawnLoc, type);
                    totalSpawned++;
                    if (debug) {
                        plugin.getLogger().info("[MobSpawn] Заспавнен " + mobName + " в " + 
                            spawnLoc.getBlockX() + "," + spawnLoc.getBlockY() + "," + spawnLoc.getBlockZ());
                    }
                } catch (IllegalArgumentException e) {
                    if (debug) {
                        plugin.getLogger().warning("[MobSpawn] Неизвестный тип моба: " + mobName + 
                            ". Доступные: ZOMBIE, SKELETON, SPIDER, CREEPER, HUSK, DROWNED и т.д.");
                    }
                }
            }
        }
        
        if (debug && totalSpawned > 0) {
            plugin.getLogger().info("[MobSpawn] Всего заспавнено мобов: " + totalSpawned + " для " + player.getName());
        }
    }
    
    public void stop() {
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
            if (debug) plugin.getLogger().info("[MobSpawn] Глобальный таск остановлен");
        }
    }
}
