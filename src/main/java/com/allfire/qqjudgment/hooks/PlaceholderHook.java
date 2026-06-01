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
        String defaultFallback = plugin.getConfig().getString("fallback-message", "Судная ночь не началась");
        
        // Разбираем параметр: основная_часть и fallback (всё что после первого _)
        String mainParam = params;
        String fallbackText = null;
        
        int underscoreIndex = params.indexOf('_');
        if (underscoreIndex > 0) {
            String possibleMain = params.substring(0, underscoreIndex);
            if (isValidParam(possibleMain)) {
                mainParam = possibleMain;
                fallbackText = params.substring(underscoreIndex + 1);
                if (fallbackText.isEmpty()) {
                    fallbackText = "";
                }
            }
        }
        
        String finalFallback = fallbackText != null ? fallbackText : defaultFallback;
        
        // ========== ОСНОВНЫЕ ЗАПОЛНИТЕЛИ ==========
        
        if (mainParam.equalsIgnoreCase("end")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return finalFallback;
        }
        
        if (mainParam.equalsIgnoreCase("time_end")) {
            if (judgmentManager.isJudgmentActive()) {
                return judgmentManager.getTimeRemainingFormatted();
            }
            return finalFallback;
        }
        
        if (mainParam.equalsIgnoreCase("seconds_end")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return finalFallback;
        }
        
        // ========== КОМПОНЕНТЫ ВРЕМЕНИ ==========
        
        if (mainParam.equalsIgnoreCase("hours")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toHours());
            }
            return finalFallback;
        }
        
        if (mainParam.equalsIgnoreCase("hours_padded")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toHours());
            }
            return finalFallback;
        }
        
        if (mainParam.equalsIgnoreCase("minutes")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toMinutesPart());
            }
            return finalFallback;
        }
        
        if (mainParam.equalsIgnoreCase("minutes_padded")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toMinutesPart());
            }
            return finalFallback;
        }
        
        if (mainParam.equalsIgnoreCase("seconds")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toSecondsPart());
            }
            return finalFallback;
        }
        
        if (mainParam.equalsIgnoreCase("seconds_padded")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toSecondsPart());
            }
            return finalFallback;
        }
        
        if (mainParam.equalsIgnoreCase("total_minutes")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toMinutes());
            }
            return finalFallback;
        }
        
        if (mainParam.equalsIgnoreCase("total_seconds")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return finalFallback;
        }
        
        // ========== СТАТУС ==========
        
        if (mainParam.equalsIgnoreCase("is_active")) {
            if (judgmentManager.isJudgmentActive()) {
                return "true";
            }
            return finalFallback;
        }
        
        if (mainParam.equalsIgnoreCase("active_text")) {
            if (judgmentManager.isJudgmentActive()) {
                return "§aАктивна";
            }
            return "§cНе активна";
        }
        
        // ========== ПРОГРЕСС ==========
        
        if (mainParam.equalsIgnoreCase("progress")) {
            if (!judgmentManager.isJudgmentActive()) {
                return finalFallback;
            }
            int totalSeconds = 3600;
            int remaining = judgmentManager.getRemainingSeconds();
            int percent = (int) ((double) (totalSeconds - remaining) / totalSeconds * 100);
            percent = Math.min(100, Math.max(0, percent));
            return String.valueOf(percent);
        }
        
        // ========== ТОП ИГРОКОВ ==========
        
        if (mainParam.toLowerCase().startsWith("top_")) {
            String[] parts = mainParam.split("_");
            if (parts.length >= 2) {
                try {
                    int topNumber = Integer.parseInt(parts[1]);
                    
                    boolean wantName = mainParam.toLowerCase().contains("name");
                    boolean wantScore = mainParam.toLowerCase().contains("score");
                    
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
            return finalFallback;
        }
        
        if (player == null) return "";
        
        // ========== СТАТИСТИКА ИГРОКА ==========
        
        if (mainParam.equalsIgnoreCase("deaths")) {
            int deaths = statsManager.getPlayerDeaths(player.getUniqueId());
            if (deaths == 0 && fallbackText != null) {
                return finalFallback;
            }
            return String.valueOf(deaths);
        }
        
        if (mainParam.equalsIgnoreCase("kills_players")) {
            int kills = statsManager.getPlayerPlayerKills(player.getUniqueId());
            if (kills == 0 && fallbackText != null) {
                return finalFallback;
            }
            return String.valueOf(kills);
        }
        
        if (mainParam.equalsIgnoreCase("kills_mobs")) {
            int kills = statsManager.getPlayerMobKills(player.getUniqueId());
            if (kills == 0 && fallbackText != null) {
                return finalFallback;
            }
            return String.valueOf(kills);
        }
        
        if (mainParam.equalsIgnoreCase("total_kills")) {
            int total = statsManager.getPlayerPlayerKills(player.getUniqueId()) + 
                       statsManager.getPlayerMobKills(player.getUniqueId());
            if (total == 0 && fallbackText != null) {
                return finalFallback;
            }
            return String.valueOf(total);
        }
        
        return null;
    }
    
    private boolean isValidParam(String param) {
        return List.of(
            "end", "time_end", "seconds_end",
            "hours", "hours_padded",
            "minutes", "minutes_padded", 
            "seconds", "seconds_padded",
            "total_minutes", "total_seconds",
            "is_active", "active_text",
            "progress",
            "deaths", "kills_players", "kills_mobs", "total_kills"
        ).contains(param.toLowerCase()) || param.toLowerCase().startsWith("top_");
    }
    
    @Override
    public @Nullable List<String> getPlaceholders() {
        List<String> placeholders = new ArrayList<>();
        
        // ========== ОСНОВНЫЕ ==========
        placeholders.add("end");
        placeholders.add("end_Не активно");
        placeholders.add("end_");
        placeholders.add("time_end");
        placeholders.add("time_end_Не активно");
        placeholders.add("time_end_");
        placeholders.add("seconds_end");
        placeholders.add("seconds_end_0");
        placeholders.add("seconds_end_");
        
        // ========== КОМПОНЕНТЫ ВРЕМЕНИ ==========
        placeholders.add("hours");
        placeholders.add("hours_0");
        placeholders.add("hours_");
        placeholders.add("hours_padded");
        placeholders.add("hours_padded_00");
        placeholders.add("hours_padded_");
        
        placeholders.add("minutes");
        placeholders.add("minutes_0");
        placeholders.add("minutes_");
        placeholders.add("minutes_padded");
        placeholders.add("minutes_padded_00");
        placeholders.add("minutes_padded_");
        
        placeholders.add("seconds");
        placeholders.add("seconds_0");
        placeholders.add("seconds_");
        placeholders.add("seconds_padded");
        placeholders.add("seconds_padded_00");
        placeholders.add("seconds_padded_");
        
        placeholders.add("total_minutes");
        placeholders.add("total_minutes_0");
        placeholders.add("total_minutes_");
        placeholders.add("total_seconds");
        placeholders.add("total_seconds_0");
        placeholders.add("total_seconds_");
        
        // ========== СТАТУС ==========
        placeholders.add("is_active");
        placeholders.add("is_active_Не активна");
        placeholders.add("is_active_");
        placeholders.add("active_text");
        
        // ========== ПРОГРЕСС ==========
        placeholders.add("progress");
        placeholders.add("progress_0%");
        placeholders.add("progress_");
        
        // ========== ТОП ИГРОКОВ (1-10) ==========
        for (int i = 1; i <= 10; i++) {
            placeholders.add("top_" + i);
            placeholders.add("top_" + i + "_Нет игроков");
            placeholders.add("top_" + i + "_");
            placeholders.add("top_" + i + "_name");
            placeholders.add("top_" + i + "_name_Нет имени");
            placeholders.add("top_" + i + "_name_");
            placeholders.add("top_" + i + "_score");
            placeholders.add("top_" + i + "_score_0");
            placeholders.add("top_" + i + "_score_");
        }
        
        // ========== СТАТИСТИКА ==========
        placeholders.add("deaths");
        placeholders.add("deaths_0");
        placeholders.add("deaths_Нет смертей");
        placeholders.add("deaths_");
        placeholders.add("kills_players");
        placeholders.add("kills_players_0");
        placeholders.add("kills_players_Нет убийств");
        placeholders.add("kills_players_");
        placeholders.add("kills_mobs");
        placeholders.add("kills_mobs_0");
        placeholders.add("kills_mobs_Никого");
        placeholders.add("kills_mobs_");
        placeholders.add("total_kills");
        placeholders.add("total_kills_0");
        placeholders.add("total_kills_---");
        placeholders.add("total_kills_");
        
        return placeholders;
    }
}
