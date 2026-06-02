package com.allfire.qqjudgment.hooks;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.managers.JudgmentManager;
import com.allfire.qqjudgment.managers.StatsManager;
import com.allfire.qqjudgment.models.TopEntry;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
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
    
    private int getRemainingSeconds() {
        if (!judgmentManager.isJudgmentActive()) return 0;
        return Math.max(0, judgmentManager.getRemainingSeconds());
    }
    
    private Duration getDuration() {
        return Duration.ofSeconds(getRemainingSeconds());
    }
    
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params == null || params.isEmpty()) return "";
        
        // Определяем есть ли fallback (символ _ в параметре)
        boolean hasFallback = params.contains("_");
        String baseParam = params;
        String fallback = "";
        
        if (hasFallback) {
            int lastUnderscore = params.lastIndexOf('_');
            baseParam = params.substring(0, lastUnderscore);
            fallback = params.substring(lastUnderscore + 1);
        }
        
        // ========== ОСНОВНЫЕ ==========
        if (baseParam.equalsIgnoreCase("end") || baseParam.equalsIgnoreCase("end_fallbackMsg")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return hasFallback ? fallback : "";
        }
        
        if (baseParam.equalsIgnoreCase("time_end") || baseParam.equalsIgnoreCase("time_end_fallbackMsg")) {
            if (judgmentManager.isJudgmentActive()) {
                return judgmentManager.getTimeRemainingFormatted();
            }
            return hasFallback ? fallback : "";
        }
        
        if (baseParam.equalsIgnoreCase("seconds_end")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return hasFallback ? fallback : "0";
        }
        
        // ========== КОМПОНЕНТЫ ВРЕМЕНИ ==========
        if (baseParam.equalsIgnoreCase("hours")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toHours());
            }
            return hasFallback ? fallback : "0";
        }
        
        if (baseParam.equalsIgnoreCase("hours_padded")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toHours());
            }
            return hasFallback ? fallback : "00";
        }
        
        if (baseParam.equalsIgnoreCase("minutes")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toMinutesPart());
            }
            return hasFallback ? fallback : "0";
        }
        
        if (baseParam.equalsIgnoreCase("minutes_padded")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toMinutesPart());
            }
            return hasFallback ? fallback : "00";
        }
        
        if (baseParam.equalsIgnoreCase("seconds")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toSecondsPart());
            }
            return hasFallback ? fallback : "0";
        }
        
        if (baseParam.equalsIgnoreCase("seconds_padded")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toSecondsPart());
            }
            return hasFallback ? fallback : "00";
        }
        
        if (baseParam.equalsIgnoreCase("total_minutes")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toMinutes());
            }
            return hasFallback ? fallback : "0";
        }
        
        if (baseParam.equalsIgnoreCase("total_seconds")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return hasFallback ? fallback : "0";
        }
        
        // ========== СТАТУС ==========
        if (baseParam.equalsIgnoreCase("is_active")) {
            return String.valueOf(judgmentManager.isJudgmentActive());
        }
        
        if (baseParam.equalsIgnoreCase("active_text")) {
            if (judgmentManager.isJudgmentActive()) {
                return "§aАктивна";
            }
            return hasFallback ? fallback : "§cНе активна";
        }
        
        // ========== ПРОГРЕСС ==========
        if (baseParam.equalsIgnoreCase("progress")) {
            if (!judgmentManager.isJudgmentActive()) {
                return hasFallback ? fallback : "0";
            }
            int totalSeconds = 3600;
            int remaining = judgmentManager.getRemainingSeconds();
            int percent = (int) ((double) (totalSeconds - remaining) / totalSeconds * 100);
            percent = Math.min(100, Math.max(0, percent));
            return String.valueOf(percent);
        }
        
        // ========== ТОП ИГРОКОВ ==========
        if (baseParam.toLowerCase().startsWith("top_")) {
            String[] topParts = baseParam.split("_");
            if (topParts.length >= 2) {
                try {
                    int topNumber = Integer.parseInt(topParts[1]);
                    boolean wantName = baseParam.toLowerCase().contains("name");
                    boolean wantScore = baseParam.toLowerCase().contains("score");
                    
                    List<TopEntry> topPlayers = statsManager.getTopPlayers(topNumber);
                    
                    if (topPlayers.size() >= topNumber) {
                        TopEntry entry = topPlayers.get(topNumber - 1);
                        if (wantName) {
                            return entry.getName();
                        }
                        if (wantScore) {
                            return String.valueOf(entry.getScore());
                        }
                        return entry.getName() + ": " + entry.getScore();
                    }
                } catch (NumberFormatException ignored) {}
            }
            return hasFallback ? fallback : "";
        }
        
        if (player == null) return "";
        
        // ========== СТАТИСТИКА ИГРОКА ==========
        if (baseParam.equalsIgnoreCase("deaths") || baseParam.equalsIgnoreCase("death_fallbackMsg")) {
            int deaths = statsManager.getPlayerDeaths(player.getUniqueId());
            if (deaths > 0) {
                return String.valueOf(deaths);
            }
            return hasFallback ? fallback : "0";
        }
        
        if (baseParam.equalsIgnoreCase("kills_players") || baseParam.equalsIgnoreCase("kills_players_fallbackMsg")) {
            int kills = statsManager.getPlayerPlayerKills(player.getUniqueId());
            if (kills > 0) {
                return String.valueOf(kills);
            }
            return hasFallback ? fallback : "0";
        }
        
        if (baseParam.equalsIgnoreCase("kills_mobs") || baseParam.equalsIgnoreCase("kills_mobs_fallbackMsg")) {
            int kills = statsManager.getPlayerMobKills(player.getUniqueId());
            if (kills > 0) {
                return String.valueOf(kills);
            }
            return hasFallback ? fallback : "0";
        }
        
        if (baseParam.equalsIgnoreCase("total_kills") || baseParam.equalsIgnoreCase("total_kills_fallbackMsg")) {
            int total = statsManager.getPlayerPlayerKills(player.getUniqueId()) + 
                       statsManager.getPlayerMobKills(player.getUniqueId());
            if (total > 0) {
                return String.valueOf(total);
            }
            return hasFallback ? fallback : "0";
        }
        
        return "";
    }
    
    @Override
    public @Nullable List<String> getPlaceholders() {
        List<String> placeholders = new ArrayList<>();
        
        placeholders.add("%qqjudgment_is_active%");
        placeholders.add("%qqjudgment_end%");
        placeholders.add("%qqjudgment_time_end%");
        placeholders.add("%qqjudgment_seconds_end%");
        placeholders.add("%qqjudgment_kills_players%");
        placeholders.add("%qqjudgment_kills_mobs%");
        placeholders.add("%qqjudgment_deaths%");
        placeholders.add("%qqjudgment_total_kills%");
        placeholders.add("%qqjudgment_progress%");
        placeholders.add("%qqjudgment_active_text%");
        
        for (int i = 1; i <= 10; i++) {
            placeholders.add("%qqjudgment_top_" + i + "%");
            placeholders.add("%qqjudgment_top_" + i + "_name%");
            placeholders.add("%qqjudgment_top_" + i + "_score%");
        }
        
        return placeholders;
    }
}
