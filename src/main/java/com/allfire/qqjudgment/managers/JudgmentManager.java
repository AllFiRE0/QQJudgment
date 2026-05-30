package com.allfire.qqjudgment.managers;

import com.allfire.qqjudgment.QQJudgment;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JudgmentManager {
    
    private final QQJudgment plugin;
    private boolean judgmentActive = false;
    private int remainingSeconds = 0;
    private int totalSeconds = 0;
    private BukkitTask judgmentTask;
    private BukkitTask countdownTask;
    private boolean debug;
    
    private final Map<UUID, Boolean> previousFlyState = new HashMap<>();
    private final Map<UUID, Boolean> previousInvincibleState = new HashMap<>();
    private final Map<UUID, GameMode> previousGameMode = new HashMap<>();
    
    public JudgmentManager(QQJudgment plugin) {
        this.plugin = plugin;
        this.debug = plugin.getConfig().getBoolean("debug", false);
    }
    
    public void startJudgment(int seconds, boolean silent) {
        if (judgmentActive) return;
        
        judgmentActive = true;
        remainingSeconds = seconds;
        totalSeconds = seconds;
        
        plugin.getStatsManager().startTracking();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            previousFlyState.put(player.getUniqueId(), player.getAllowFlight());
            previousInvincibleState.put(player.getUniqueId(), player.isInvulnerable());
            previousGameMode.put(player.getUniqueId(), player.getGameMode());
            applyRestrictions(player);
        }
        
        if (debug) {
            plugin.getLogger().info("[Judgment] Судная ночь началась на " + seconds + " секунд");
        }
        
        plugin.getBossBarManager().showStartBossBar();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (judgmentActive) {
                    plugin.getBossBarManager().showProgressBossBar();
                    startCountdown();
                }
            }
        }.runTaskLater(plugin, plugin.getConfig().getInt("bossbar.start-delay", 3) * 20L);
        
        judgmentTask = new BukkitRunnable() {
            @Override
            public void run() {
                stopJudgment(false);
            }
        }.runTaskLater(plugin, seconds * 20L);
        
        if (!silent) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("seconds", String.valueOf(seconds));
            plugin.getMessageManager().broadcastMessage("judgment-started", placeholders);
        }
        
        if (plugin.getConfig().getBoolean("mob-spawning.enabled", false) && !silent) {
            plugin.getMessageManager().broadcastMessage("mob-spawn-start", null);
        }
    }
    
    public void stopJudgment(boolean silent) {
        if (!judgmentActive) return;
        
        judgmentActive = false;
        
        plugin.getStatsManager().stopTracking();
        
        if (judgmentTask != null) {
            judgmentTask.cancel();
            judgmentTask = null;
        }
        
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        
        // Показываем финальный боссбар
        plugin.getBossBarManager().showEndBossBar();
        
        // Принудительно скрываем боссбар через end-delay + 1 секунду (на всякий случай)
        int endDelay = plugin.getConfig().getInt("bossbar.end-delay", 3);
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getBossBarManager().hideBossBarFromAll();
                if (debug) {
                    plugin.getLogger().info("[Judgment] Боссбар принудительно скрыт");
                }
            }
        }.runTaskLater(plugin, (endDelay + 1) * 20L);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            
            if (previousFlyState.containsKey(uuid)) {
                player.setAllowFlight(previousFlyState.get(uuid));
                previousFlyState.remove(uuid);
            }
            if (previousInvincibleState.containsKey(uuid)) {
                player.setInvulnerable(previousInvincibleState.get(uuid));
                previousInvincibleState.remove(uuid);
            }
            if (previousGameMode.containsKey(uuid)) {
                // Восстанавливаем гейм мод, если он был изменен
                previousGameMode.remove(uuid);
            }
        }
        
        if (debug) {
            plugin.getLogger().info("[Judgment] Судная ночь закончилась");
        }
        
        if (!silent) {
            plugin.getMessageManager().broadcastMessage("judgment-ended", null);
        }
    }
    
    private void startCountdown() {
        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!judgmentActive) {
                    this.cancel();
                    return;
                }
                
                remainingSeconds--;
                
                if (remainingSeconds <= 0) {
                    plugin.getBossBarManager().updateProgress(1.0f);
                    this.cancel();
                } else {
                    plugin.getBossBarManager().updateProgress((float) remainingSeconds / totalSeconds);
                    plugin.getBossBarManager().updateTitle(plugin.getStatsManager().formatTime(remainingSeconds));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void applyRestrictions(Player player) {
        boolean flyRestricted = plugin.getConfig().getBoolean("restrictions.fly", false);
        boolean godRestricted = plugin.getConfig().getBoolean("restrictions.god", false);
        
        // Только для выживания и приключения
        GameMode gm = player.getGameMode();
        if (gm != GameMode.SURVIVAL && gm != GameMode.ADVENTURE) return;
        
        if (flyRestricted && !player.hasPermission("qqjudgment.bypass.fly")) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
        
        if (godRestricted && !player.hasPermission("qqjudgment.bypass.god")) {
            player.setInvulnerable(false);
        }
    }
    
    public boolean isJudgmentActive() {
        return judgmentActive;
    }
    
    public int getRemainingSeconds() {
        return remainingSeconds;
    }
    
    public int getTotalSeconds() {
        return totalSeconds;
    }
    
    public String getTimeRemainingFormatted() {
        return plugin.getStatsManager().formatTime(Math.max(0, remainingSeconds));
    }
    
    public String getFallbackMsg() {
        if (judgmentActive) {
            return getTimeRemainingFormatted();
        }
        return plugin.getConfig().getString("fallback-message", "Судная ночь не началась");
    }
}
