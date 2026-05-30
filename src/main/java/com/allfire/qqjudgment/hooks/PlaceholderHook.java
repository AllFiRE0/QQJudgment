package com.allfire.qqjudgment.hooks;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.managers.JudgmentManager;
import com.allfire.qqjudgment.managers.StatsManager;
import com.allfire.qqjudgment.models.TopEntry;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceholderHook extends PlaceholderExpansion {
    
    private final QQJudgment plugin;
    private final JudgmentManager judgmentManager;
    private final StatsManager statsManager;
    
    public PlaceholderHook(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
        this.statsManager = plugin.getStatsManager();
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "qqjudgment";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "AllF1RE";
    }
    
    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        String fallbackMsg = plugin.getConfig().getString("fallback-message", "Судная ночь не началась");
        
        if (params.equalsIgnoreCase("end_fallbackMsg")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return fallbackMsg;
        }
        
        if (params.equalsIgnoreCase("time_end_fallbackMsg")) {
            if (judgmentManager.isJudgmentActive()) {
                return judgmentManager.getTimeRemainingFormatted();
            }
            return fallbackMsg;
        }
        
        if (params.equalsIgnoreCase("time_end")) {
            if (judgmentManager.isJudgmentActive()) {
                return judgmentManager.getTimeRemainingFormatted();
            }
            return "00:00:00";
        }
        
        if (params.equalsIgnoreCase("seconds_end")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return "0";
        }
        
        if (params.equalsIgnoreCase("is_active")) {
            return String.valueOf(judgmentManager.isJudgmentActive());
        }
        
        if (params.toLowerCase().startsWith("top_")) {
            String[] parts = params.split("_");
            if (parts.length >= 2) {
                try {
                    String numPart = parts[1].replaceAll("[^0-9]", "");
                    int topNumber = Integer.parseInt(numPart);
                    boolean withFallback = params.toLowerCase().endsWith("_fallbackmsg");
                    
                    List<TopEntry> topPlayers = statsManager.getTopPlayers(topNumber);
                    
                    if (topPlayers.size() >= topNumber) {
                        TopEntry entry = topPlayers.get(topNumber - 1);
                        if (withFallback) {
                            return entry.getName() + ": " + entry.getScore();
                        }
                        return entry.getName();
                    } else if (withFallback && topPlayers.size() < topNumber) {
                        return fallbackMsg;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        if (player == null) return "";
        
        if (params.equalsIgnoreCase("death_fallbackMsg")) {
            int deaths = statsManager.getPlayerDeaths(player.getUniqueId());
            if (deaths > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(deaths);
            }
            return fallbackMsg;
        }
        
        if (params.equalsIgnoreCase("deaths")) {
            return String.valueOf(statsManager.getPlayerDeaths(player.getUniqueId()));
        }
        
        if (params.equalsIgnoreCase("kills_players_fallbackMsg")) {
            int kills = statsManager.getPlayerPlayerKills(player.getUniqueId());
            if (kills > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(kills);
            }
            return fallbackMsg;
        }
        
        if (params.equalsIgnoreCase("kills_players")) {
            return String.valueOf(statsManager.getPlayerPlayerKills(player.getUniqueId()));
        }
        
        if (params.equalsIgnoreCase("kills_mobs_fallbackMsg")) {
            int kills = statsManager.getPlayerMobKills(player.getUniqueId());
            if (kills > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(kills);
            }
            return fallbackMsg;
        }
        
        if (params.equalsIgnoreCase("kills_mobs")) {
            return String.valueOf(statsManager.getPlayerMobKills(player.getUniqueId()));
        }
        
        if (params.equalsIgnoreCase("total_kills_fallbackMsg")) {
            int total = statsManager.getPlayerPlayerKills(player.getUniqueId()) + 
                       statsManager.getPlayerMobKills(player.getUniqueId());
            if (total > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(total);
            }
            return fallbackMsg;
        }
        
        if (params.equalsIgnoreCase("total_kills")) {
            int total = statsManager.getPlayerPlayerKills(player.getUniqueId()) + 
                       statsManager.getPlayerMobKills(player.getUniqueId());
            return String.valueOf(total);
        }
        
        if (params.equalsIgnoreCase("remaining_percent")) {
            if (!judgmentManager.isJudgmentActive()) return "0";
            int totalSeconds = judgmentManager.getRemainingSeconds();
            return "50";
        }
        
        return null;
    }
    
    @Override
    public @Nullable List<String> getPlaceholders() {
        return List.of(
            "end_fallbackMsg",
            "time_end_fallbackMsg",
            "time_end",
            "seconds_end",
            "is_active",
            "top_1_fallbackMsg",
            "top_2_fallbackMsg",
            "top_3_fallbackMsg",
            "top_4_fallbackMsg",
            "top_5_fallbackMsg",
            "top_6_fallbackMsg",
            "top_7_fallbackMsg",
            "top_8_fallbackMsg",
            "top_9_fallbackMsg",
            "top_10_fallbackMsg",
            "death_fallbackMsg",
            "deaths",
            "kills_players_fallbackMsg",
            "kills_players",
            "kills_mobs_fallbackMsg",
            "kills_mobs",
            "total_kills_fallbackMsg",
            "total_kills",
            "remaining_percent"
        );
    }
}
