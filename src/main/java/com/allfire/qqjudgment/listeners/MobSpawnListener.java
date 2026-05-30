package com.allfire.qqjudgment.listeners;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.managers.JudgmentManager;
import org.bukkit.GameMode;
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
    private boolean isRunning = false;
    
    public MobSpawnListener(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
        this.debug = plugin.getConfig().getBoolean("debug", false);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        if (debug) {
            plugin.getLogger().info("[MobSpawn] Debug режим ВКЛЮЧЕН");
        }
        
        startCheckerTask();
    }
    
    private void startCheckerTask() {
        spawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!judgmentManager.isJudgmentActive()) {
                    if (isRunning && debug) {
                        plugin.getLogger().info("[MobSpawn] СН закончилась, останавливаем спавн мобов");
                        isRunning = false;
                    }
                    return;
                }
                
                if (!plugin.getConfig().getBoolean("mob-spawning.enabled", false)) {
                    if (isRunning && debug) {
                        plugin.getLogger().info("[MobSpawn] Спавн мобов выключен в конфиге");
                        isRunning = false;
                    }
                    return;
                }
                
                if (!isRunning && debug) {
                    plugin.getLogger().info("[MobSpawn] СН активна, начинаем спавн мобов");
                    isRunning = true;
                }
                
                long delayTicks = plugin.getConfig().getLong("mob-spawning.delay-ticks", 400);
                long delayMillis = delayTicks * 50L;
                
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    // ===== ВСЕ ПРОВЕРКИ ЗДЕСЬ, БЕЗ ЛОГОВ =====
                    
                    // 1. Только выживание и приключение
                    GameMode gm = player.getGameMode();
                    if (gm != GameMode.SURVIVAL && gm != GameMode.ADVENTURE) {
                        continue;
                    }
                    
                    // 2. Проверка bypass права
                    if (player.hasPermission("qqjudgment.bypass.mobspawn")) {
                        continue;
                    }
                    
                    // 3. Время суток: ночь ИЛИ под землей
                    World world = player.getWorld();
                    long time = world.getTime();
                    boolean isNight = time >= 13000 && time <= 23000;
                    boolean isUnderground = player.getLocation().getY() < 50;
                    
                    if (!isNight && !isUnderground) {
                        continue;
                    }
                    
                    // 4. Задержка между спавнами
                    long lastSpawn = lastSpawnTime.getOrDefault(player.getUniqueId(), 0L);
                    long currentTime = System.currentTimeMillis();
                    
                    if (currentTime - lastSpawn >= delayMillis) {
                        lastSpawnTime.put(player.getUniqueId(), currentTime);
                        spawnMobsAroundPlayer(player);
                    }
                }
            }
        };
        
        spawnTask.runTaskTimer(plugin, 0L, 20L);
        if (debug) {
            plugin.getLogger().info("[MobSpawn] Чекер-таск запущен");
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!judgmentManager.isJudgmentActive()) return;
        if (!plugin.getConfig().getBoolean("mob-spawning.enabled", false)) return;
        
        Player player = event.getPlayer();
        
        // Те же проверки, что и выше
        GameMode gm = player.getGameMode();
        if (gm != GameMode.SURVIVAL && gm != GameMode.ADVENTURE) return;
        if (player.hasPermission("qqjudgment.bypass.mobspawn")) return;
        
        World world = player.getWorld();
        long time = world.getTime();
        boolean isNight = time >= 13000 && time <= 23000;
        boolean isUnderground = player.getLocation().getY() < 50;
        if (!isNight && !isUnderground) return;
        
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
            if (debug) plugin.getLogger().warning("[MobSpawn] Нет мобов в конфиге!");
            return;
        }
        
        if (debug) {
            plugin.getLogger().info("[MobSpawn] Спавн мобов для " + player.getName());
        }
        
        int totalSpawned = 0;
        
        for (Map.Entry<String, Integer> entry : mobs.entrySet()) {
            String mobName = entry.getKey();
            int count = entry.getValue();
            
            for (int i = 0; i < count; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double distance = 5 + Math.random() * (radius - 5);
                double x = center.getX() + Math.cos(angle) * distance;
                double z = center.getZ() + Math.sin(angle) * distance;
                double y = center.getY();
                
                Location spawnLoc = new Location(world, x, y, z);
                
                for (int offset = -3; offset <= 3; offset++) {
                    Location checkLoc = spawnLoc.clone().add(0, offset, 0);
                    
                    if (checkLoc.getBlock().getType().isSolid() && 
                        checkLoc.clone().add(0, 1, 0).getBlock().isEmpty() &&
                        checkLoc.clone().add(0, 2, 0).getBlock().isEmpty()) {
                        
                        Location finalLoc = checkLoc.clone().add(0, 1, 0);
                        
                        int lightLevel = finalLoc.getBlock().getLightLevel();
                        EntityType type = EntityType.valueOf(mobName.toUpperCase());
                        
                        boolean canSpawn = true;
                        if ((type == EntityType.ZOMBIE || type == EntityType.SKELETON || 
                             type == EntityType.HUSK || type == EntityType.STRAY) && lightLevel > 7) {
                            canSpawn = false;
                        }
                        
                        if (canSpawn) {
                            try {
                                world.spawnEntity(finalLoc, type);
                                totalSpawned++;
                                if (debug) {
                                    plugin.getLogger().info("[MobSpawn] Заспавнен " + mobName);
                                }
                                break;
                            } catch (Exception e) {
                                if (debug) plugin.getLogger().warning("[MobSpawn] Ошибка: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
        
        if (debug && totalSpawned > 0) {
            plugin.getLogger().info("[MobSpawn] Заспавнено мобов: " + totalSpawned);
        }
    }
    
    public void stop() {
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
            if (debug) plugin.getLogger().info("[MobSpawn] Чекер-таск остановлен");
        }
    }
}
