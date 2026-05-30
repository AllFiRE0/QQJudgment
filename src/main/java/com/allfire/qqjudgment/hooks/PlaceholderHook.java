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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderHook extends PlaceholderExpansion {
    
    private final QQJudgment plugin;
    private final JudgmentManager judgmentManager;
    private final StatsManager statsManager;
    private final Pattern fallbackPattern = Pattern.compile("^(.*?)_(.*)$");
    
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
        return judgmentManager.getRemainingSeconds();
    }
    
    private Duration getDuration() {
        return Duration.ofSeconds(getRemainingSeconds());
    }
    
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        String defaultFallback = plugin.getConfig().getString("fallback-message", "Судная ночь не началась");
        
        String mainParam = params;
        String customFallback = null;
        
        Matcher matcher = fallbackPattern.matcher(params);
        if (matcher.matches()) {
            String possibleMain = matcher.group(1);
            String possibleFallback = matcher.group(2);
            
            if (isValidParam(possibleMain)) {
                mainParam = possibleMain;
                customFallback = possibleFallback;
            }
        }
        
        String fallbackToUse = customFallback != null ? customFallback : defaultFallback;
        
        // ========== ОСНОВНЫЕ ЗАПОЛНИТЕЛИ ==========
        
        if (mainParam.equalsIgnoreCase("end") || mainParam.equalsIgnoreCase("end_fallbackMsg")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return fallbackToUse;
        }
        
        if (mainParam.equalsIgnoreCase("time_end") || mainParam.equalsIgnoreCase("time_end_fallbackMsg")) {
            if (judgmentManager.isJudgmentActive()) {
                return judgmentManager.getTimeRemainingFormatted();
            }
            return fallbackToUse;
        }
        
        if (mainParam.equalsIgnoreCase("seconds_end")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return "0";
        }
        
        // ========== КОМПОНЕНТЫ ВРЕМЕНИ ==========
        
        if (mainParam.equalsIgnoreCase("hours")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toHours());
            }
            return customFallback != null ? customFallback : "0";
        }
        
        if (mainParam.equalsIgnoreCase("hours_padded")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toHours());
            }
            return customFallback != null ? customFallback : "00";
        }
        
        if (mainParam.equalsIgnoreCase("minutes")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toMinutesPart());
            }
            return customFallback != null ? customFallback : "0";
        }
        
        if (mainParam.equalsIgnoreCase("minutes_padded")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toMinutesPart());
            }
            return customFallback != null ? customFallback : "00";
        }
        
        if (mainParam.equalsIgnoreCase("seconds")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toSecondsPart());
            }
            return customFallback != null ? customFallback : "0";
        }
        
        if (mainParam.equalsIgnoreCase("seconds_padded")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toSecondsPart());
            }
            return customFallback != null ? customFallback : "00";
        }
        
        if (mainParam.equalsIgnoreCase("total_minutes")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toMinutes());
            }
            return customFallback != null ? customFallback : "0";
        }
        
        if (mainParam.equalsIgnoreCase("total_seconds")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return customFallback != null ? customFallback : "0";
        }
        
        // ========== СТАТУС ==========
        
        if (mainParam.equalsIgnoreCase("is_active")) {
            return String.valueOf(judgmentManager.isJudgmentActive());
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
                return customFallback != null ? customFallback : "0";
            }
            int totalSeconds = 3600;
            int remaining = judgmentManager.getRemainingSeconds();
            int percent = (int) ((double) (totalSeconds - remaining) / totalSeconds * 100);
            return String.valueOf(Math.min(100, Math.max(0, percent)));
        }
        
        if (mainParam.equalsIgnoreCase("progress_bar")) {
            if (!judgmentManager.isJudgmentActive()) {
                return customFallback != null ? customFallback : "■■■■■■■■■■";
            }
            int totalSeconds = 3600;
            int remaining = judgmentManager.getRemainingSeconds();
            int percent = (int) ((double) (totalSeconds - remaining) / totalSeconds * 10);
            percent = Math.min(10, Math.max(0, percent));
            return "█".repeat(percent) + "░".repeat(10 - percent);
        }
        
        // ========== ТОП ИГРОКОВ ==========
        
        if (mainParam.toLowerCase().startsWith("top_")) {
            String[] parts = mainParam.split("_");
            if (parts.length >= 2) {
                try {
                    String numPart = parts[1].replaceAll("[^0-9]", "");
                    int topNumber = Integer.parseInt(numPart);
                    
                    List<TopEntry> topPlayers = statsManager.getTopPlayers(topNumber);
                    
                    if (topPlayers.size() >= topNumber) {
                        TopEntry entry = topPlayers.get(topNumber - 1);
                        if (mainParam.toLowerCase().contains("name")) {
                            return entry.getName();
                        }
                        if (mainParam.toLowerCase().contains("score")) {
                            return String.valueOf(entry.getScore());
                        }
                        return entry.getName() + ": " + entry.getScore();
                    }
                } catch (NumberFormatException ignored) {}
            }
            return fallbackToUse;
        }
        
        if (player == null) return "";
        
        // ========== СТАТИСТИКА ИГРОКА ==========
        
        if (mainParam.equalsIgnoreCase("deaths") || mainParam.equalsIgnoreCase("death_fallbackMsg")) {
            int deaths = statsManager.getPlayerDeaths(player.getUniqueId());
            if (deaths > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(deaths);
            }
            return fallbackToUse;
        }
        
        if (mainParam.equalsIgnoreCase("kills_players") || mainParam.equalsIgnoreCase("kills_players_fallbackMsg")) {
            int kills = statsManager.getPlayerPlayerKills(player.getUniqueId());
            if (kills > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(kills);
            }
            return fallbackToUse;
        }
        
        if (mainParam.equalsIgnoreCase("kills_mobs") || mainParam.equalsIgnoreCase("kills_mobs_fallbackMsg")) {
            int kills = statsManager.getPlayerMobKills(player.getUniqueId());
            if (kills > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(kills);
            }
            return fallbackToUse;
        }
        
        if (mainParam.equalsIgnoreCase("total_kills") || mainParam.equalsIgnoreCase("total_kills_fallbackMsg")) {
            int total = statsManager.getPlayerPlayerKills(player.getUniqueId()) + 
                       statsManager.getPlayerMobKills(player.getUniqueId());
            if (total > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(total);
            }
            return fallbackToUse;
        }
        
        return null;
    }
    
    private boolean isValidParam(String param) {
        return List.of(
            "end", "end_fallbackMsg",
            "time_end", "time_end_fallbackMsg", 
            "seconds_end",
            "hours", "hours_padded",
            "minutes", "minutes_padded", 
            "seconds", "seconds_padded",
            "total_minutes", "total_seconds",
            "is_active", "active_text",
            "progress", "progress_bar",
            "deaths", "death_fallbackMsg",
            "kills_players", "kills_players_fallbackMsg",
            "kills_mobs", "kills_mobs_fallbackMsg",
            "total_kills", "total_kills_fallbackMsg"
        ).contains(param.toLowerCase()) || param.toLowerCase().startsWith("top_");
    }
    
    @Override
    public @Nullable List<String> getPlaceholders() {
        List<String> placeholders = new ArrayList<>();
        
        // Основные
        placeholders.add("end");
        placeholders.add("end_Мой текст");
        placeholders.add("time_end");
        placeholders.add("time_end_Не активно");
        placeholders.add("seconds_end");
        
        // Компоненты времени
        placeholders.add("hours");
        placeholders.add("hours_0");
        placeholders.add("hours_padded");
        placeholders.add("hours_padded_00");
        placeholders.add("minutes");
        placeholders.add("minutes_0");
        placeholders.add("minutes_padded");
        placeholders.add("minutes_padded_00");
        placeholders.add("seconds");
        placeholders.add("seconds_0");
        placeholders.add("seconds_padded");
        placeholders.add("seconds_padded_00");
        placeholders.add("total_minutes");
        placeholders.add("total_seconds");
        
        // Статус
        placeholders.add("is_active");
        placeholders.add("active_text");
        
        // Прогресс
        placeholders.add("progress");
        placeholders.add("progress_0%");
        placeholders.add("progress_bar");
        placeholders.add("progress_bar_░░░░░░░░░░");
        
        // Топ игроков (1-10)
        for (int i = 1; i <= 10; i++) {
            placeholders.add("top_" + i);
            placeholders.add("top_" + i + "_name");
            placeholders.add("top_" + i + "_score");
            placeholders.add("top_" + i + "_Нет игроков");
        }
        
        // Статистика
        placeholders.add("deaths");
        placeholders.add("deaths_Нет смертей");
        placeholders.add("kills_players");
        placeholders.add("kills_players_0");
        placeholders.add("kills_mobs");
        placeholders.add("kills_mobs_Никого");
        placeholders.add("total_kills");
        placeholders.add("total_kills_---");
        
        return placeholders;
    }
}
