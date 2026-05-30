package com.allfire.qqjudgment.managers;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.models.JudgmentPlayer;
import com.allfire.qqjudgment.models.TopEntry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager {
    
    private final QQJudgment plugin;
    private final Map<UUID, JudgmentPlayer> playerStats = new ConcurrentHashMap<>();
    private boolean tracking = false;
    
    public StatsManager(QQJudgment plugin) {
        this.plugin = plugin;
    }
    
    public void startTracking() {
        tracking = true;
        playerStats.clear();
    }
    
    public void stopTracking() {
        tracking = false;
    }
    
    public void recordPlayerKill(Player killer, Player victim) {
        if (!tracking || killer == null || victim == null) return;
        
        JudgmentPlayer stats = playerStats.computeIfAbsent(killer.getUniqueId(), 
                u -> new JudgmentPlayer(killer.getName()));
        stats.addPlayerKill();
        
        JudgmentPlayer victimStats = playerStats.computeIfAbsent(victim.getUniqueId(),
                u -> new JudgmentPlayer(victim.getName()));
        victimStats.addDeath();
    }
    
    public void recordMobKill(Player killer, EntityType mobType) {
        if (!tracking || killer == null) return;
        
        if (isHostileMob(mobType)) {
            JudgmentPlayer stats = playerStats.computeIfAbsent(killer.getUniqueId(),
                    u -> new JudgmentPlayer(killer.getName()));
            stats.addMobKill();
        }
    }
    
    private boolean isHostileMob(EntityType type) {
        return switch (type) {
            case ZOMBIE, SKELETON, SPIDER, CREEPER, ENDERMAN, WITCH, 
                 HUSK, STRAY, PHANTOM, DROWNED, PILLAGER, VINDICATOR,
                 RAVAGER, EVOKER, GUARDIAN, ELDER_GUARDIAN, BLAZE,
                 WITHER_SKELETON, MAGMA_CUBE, HOGLIN, PIGLIN_BRUTE,
                 SHULKER, SILVERFISH, ENDERMITE, VEX -> true;
            default -> false;
        };
    }
    
    public List<TopEntry> getTopPlayers(int limit) {
        return playerStats.values().stream()
                .map(p -> new TopEntry(p.getName(), p.getTotalKills()))
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .toList();
    }
    
    public int getPlayerDeaths(UUID playerId) {
        JudgmentPlayer stats = playerStats.get(playerId);
        return stats != null ? stats.getDeaths() : 0;
    }
    
    public int getPlayerPlayerKills(UUID playerId) {
        JudgmentPlayer stats = playerStats.get(playerId);
        return stats != null ? stats.getPlayerKills() : 0;
    }
    
    public int getPlayerMobKills(UUID playerId) {
        JudgmentPlayer stats = playerStats.get(playerId);
        return stats != null ? stats.getMobKills() : 0;
    }
    
    public String formatTime(int seconds) {
        String format = plugin.getConfig().getString("timer-format", "HH:mm:ss");
        
        if (seconds <= 0) {
            return plugin.getConfig().getString("fallback-message", "Судная ночь не началась");
        }
        
        Duration duration = Duration.ofSeconds(seconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long secs = duration.toSecondsPart();
        
        return switch (format) {
            case "HH:mm:ss" -> String.format("%02d:%02d:%02d", hours, minutes, secs);
            case "mm:ss" -> String.format("%02d:%02d", minutes, secs);
            case "HH:mm" -> String.format("%02d:%02d", hours, minutes);
            case "ss" -> String.format("%02d", secs);
            default -> String.format("%02d:%02d:%02d", hours, minutes, secs);
        };
    }
}
