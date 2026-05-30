package com.allfire.qqjudgment.managers;

import com.allfire.qqjudgment.QQJudgment;
import org.bukkit.Bukkit;
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
    private long judgmentStartTime;
    
    private final Map<UUID, Boolean> previousFlyState = new HashMap<>();
    private final Map<UUID, Boolean> previousInvincibleState = new HashMap<>();
    
    public JudgmentManager(QQJudgment plugin) {
        this.plugin = plugin;
    }
    
    public void startJudgment(int seconds, boolean silent) {
        if (judgmentActive) return;
        
        judgmentActive = true;
        remainingSeconds = seconds;
        totalSeconds = seconds;
        judgmentStartTime = System.currentTimeMillis();
        
        plugin.getStatsManager().startTracking();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            previousFlyState.put(player.getUniqueId(), player.getAllowFlight());
            previousInvincibleState.put(player.getUniqueId(), player.isInvulnerable());
            applyRestrictions(player);
        }
        
        if (plugin.getConfig().getBoolean("bossbar.enabled", true)) {
            plugin.getBossBarManager().showBossBarToAll();
        }
        
        startCountdown();
        
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
        
        if (plugin.getConfig().getBoolean("mob-spawning.enabled", false)) {
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
        
        plugin.getBossBarManager().hideBossBarFromAll();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (previousFlyState.containsKey(player.getUniqueId())) {
                player.setAllowFlight(previousFlyState.get(player.getUniqueId()));
                previousFlyState.remove(player.getUniqueId());
            }
            if (previousInvincibleState.containsKey(player.getUniqueId())) {
                player.setInvulnerable(previousInvincibleState.get(player.getUniqueId()));
                previousInvincibleState.remove(player.getUniqueId());
            }
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
                    plugin.getBossBarManager().updateTitle(plugin.getStatsManager().formatTime(0));
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
    
    public String getTimeRemainingFormatted() {
        return plugin.getStatsManager().formatTime(remainingSeconds);
    }
    
    public String getFallbackMsg() {
        if (judgmentActive) {
            return getTimeRemainingFormatted();
        }
        return plugin.getConfig().getString("fallback-message", "Судная ночь не началась");
    }
}
