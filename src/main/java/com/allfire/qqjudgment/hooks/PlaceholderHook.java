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
    
    private int getRemainingSecondsFormatted() {
        if (!judgmentManager.isJudgmentActive()) return 0;
        return judgmentManager.getRemainingSeconds();
    }
    
    private Duration getDuration() {
        return Duration.ofSeconds(getRemainingSecondsFormatted());
    }
    
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        String defaultFallback = plugin.getConfig().getString("fallback-message", "Судная ночь не началась");
        
        // Парсим параметр на [основная_часть]_[fallback_сообщение]
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
        
        // ========== ОСНОВНЫЕ ЗАПОЛНИТЕЛИ ВРЕМЕНИ ==========
        
        // %qqjudgment_end% или %qqjudgment_end_Мой текст%
        if (mainParam.equalsIgnoreCase("end") || mainParam.equalsIgnoreCase("end_fallbackMsg")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return fallbackToUse;
        }
        
        // %qqjudgment_time_end% или %qqjudgment_time_end_Мой текст%
        if (mainParam.equalsIgnoreCase("time_end") || mainParam.equalsIgnoreCase("time_end_fallbackMsg")) {
            if (judgmentManager.isJudgmentActive()) {
                return judgmentManager.getTimeRemainingFormatted();
            }
            return fallbackToUse;
        }
        
        // %qqjudgment_seconds_end%
        if (mainParam.equalsIgnoreCase("seconds_end")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return "0";
        }
        
        // ========== ОТДЕЛЬНЫЕ КОМПОНЕНТЫ ВРЕМЕНИ ==========
        
        // %qqjudgment_hours% - часы (0-23)
        if (mainParam.equalsIgnoreCase("hours") || mainParam.startsWith("hours_")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toHours());
            }
            return customFallback != null ? customFallback : "0";
        }
        
        // %qqjudgment_hours_padded% - часы с ведущим нулем (00-23)
        if (mainParam.equalsIgnoreCase("hours_padded") || mainParam.startsWith("hours_padded_")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toHours());
            }
            return customFallback != null ? customFallback : "00";
        }
        
        // %qqjudgment_minutes% - минуты (0-59)
        if (mainParam.equalsIgnoreCase("minutes") || mainParam.startsWith("minutes_")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toMinutesPart());
            }
            return customFallback != null ? customFallback : "0";
        }
        
        // %qqjudgment_minutes_padded% - минуты с ведущим нулем (00-59)
        if (mainParam.equalsIgnoreCase("minutes_padded") || mainParam.startsWith("minutes_padded_")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toMinutesPart());
            }
            return customFallback != null ? customFallback : "00";
        }
        
        // %qqjudgment_seconds% - секунды (0-59)
        if (mainParam.equalsIgnoreCase("seconds") || mainParam.startsWith("seconds_")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toSecondsPart());
            }
            return customFallback != null ? customFallback : "0";
        }
        
        // %qqjudgment_seconds_padded% - секунды с ведущим нулем (00-59)
        if (mainParam.equalsIgnoreCase("seconds_padded") || mainParam.startsWith("seconds_padded_")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toSecondsPart());
            }
            return customFallback != null ? customFallback : "00";
        }
        
        // %qqjudgment_total_minutes% - всего минут
        if (mainParam.equalsIgnoreCase("total_minutes") || mainParam.startsWith("total_minutes_")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toMinutes());
            }
            return customFallback != null ? customFallback : "0";
        }
        
        // %qqjudgment_total_seconds% - всего секунд
        if (mainParam.equalsIgnoreCase("total_seconds") || mainParam.startsWith("total_seconds_")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return customFallback != null ? customFallback : "0";
        }
        
        // ========== ДРУГИЕ ЗАПОЛНИТЕЛИ ==========
        
        // %qqjudgment_is_active%
        if (mainParam.equalsIgnoreCase("is_active")) {
            return String.valueOf(judgmentManager.isJudgmentActive());
        }
        
        // %qqjudgment_active_text% - возвращает текст в зависимости от активности
        if (mainParam.equalsIgnoreCase("active_text")) {
            if (judgmentManager.isJudgmentActive()) {
                return "§aАктивна";
            }
            return "§cНе активна";
        }
        
        // %qqjudgment_progress% - прогресс в процентах (0-100)
        if (mainParam.equalsIgnoreCase("progress") || mainParam.startsWith("progress_")) {
            if (!judgmentManager.isJudgmentActive()) {
                return customFallback != null ? customFallback : "0";
            }
            int totalSeconds = plugin.getConfig().getInt("judgment.default-seconds", 3600);
            int remaining = judgmentManager.getRemainingSeconds();
            int percent = (int) ((double) (totalSeconds - remaining) / totalSeconds * 100);
            return String.valueOf(Math.min(100, Math.max(0, percent)));
        }
        
        // %qqjudgment_progress_bar% - прогресс в виде полосы
        if (mainParam.equalsIgnoreCase("progress_bar") || mainParam.startsWith("progress_bar_")) {
            if (!judgmentManager.isJudgmentActive()) {
                return customFallback != null ? customFallback : "■■■■■■■■■■";
            }
            int totalSeconds = plugin.getConfig().getInt("judgment.default-seconds", 3600);
            int remaining = judgmentManager.getRemainingSeconds();
            int percent = (int) ((double) (totalSeconds - remaining) / totalSeconds * 10);
            percent = Math.min(10, Math.max(0, percent));
            return "█".repeat(percent) + "░".repeat(10 - percent);
        }
        
        // Топ игроков %qqjudgment_top_X% или %qqjudgment_top_X_Мой текст%
        if (mainParam.toLowerCase().startsWith("top_")) {
            String[] parts = mainParam.split("_");
            if (parts.length >= 2) {
                try {
                    String numPart = parts[1].replaceAll("[^0-9]", "");
                    int topNumber = Integer.parseInt(numPart);
                    
                    List<TopEntry> topPlayers = statsManager.getTopPlayers(topNumber);
                    
                    if (topPlayers.size() >= topNumber) {
                        TopEntry entry = topPlayers.get(topNumber - 1);
                        // Проверяем, нужно ли вернуть только имя или имя:очки
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
        
        // %qqjudgment_deaths% или %qqjudgment_deaths_Мой текст%
        if (mainParam.equalsIgnoreCase("deaths") || mainParam.equalsIgnoreCase("death_fallbackMsg")) {
            int deaths = statsManager.getPlayerDeaths(player.getUniqueId());
            if (deaths > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(deaths);
            }
            return fallbackToUse;
        }
        
        // %qqjudgment_kills_players% или %qqjudgment_kills_players_Мой текст%
        if (mainParam.equalsIgnoreCase("kills_players") || mainParam.equalsIgnoreCase("kills_players_fallbackMsg")) {
            int kills = statsManager.getPlayerPlayerKills(player.getUniqueId());
            if (kills > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(kills);
            }
            return fallbackToUse;
        }
        
        // %qqjudgment_kills_mobs% или %qqjudgment_kills_mobs_Мой текст%
        if (mainParam.equalsIgnoreCase("kills_mobs") || mainParam.equalsIgnoreCase("kills_mobs_fallbackMsg")) {
            int kills = statsManager.getPlayerMobKills(player.getUniqueId());
            if (kills > 0 || judgmentManager.isJudgmentActive()) {
                return String.valueOf(kills);
            }
            return fallbackToUse;
        }
        
        // %qqjudgment_total_kills% или %qqjudgment_total_kills_Мой текст%
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
            // Время
            "end", "end_fallbackMsg",
            "time_end", "time_end_fallbackMsg", 
            "seconds_end",
            "hours", "hours_padded",
            "minutes", "minutes_padded", 
            "seconds", "seconds_padded",
            "total_minutes", "total_seconds",
            "progress", "progress_bar",
            // Статус
            "is_active", "active_text",
            // Статистика
            "deaths", "death_fallbackMsg",
            "kills_players", "kills_players_fallbackMsg",
            "kills_mobs", "kills_mobs_fallbackMsg",
            "total_kills", "total_kills_fallbackMsg"
        ).contains(param.toLowerCase()) || param.toLowerCase().startsWith("top_");
    }
    
    @Override
    public @Nullable List<String> getPlaceholders() {
        return List.of(
            // Основные
            "end",
            "end_Мой текст",
            "time_end",
            "time_end_Не активно",
            "seconds_end",
            // Отдельные компоненты времени
            "hours",
            "hours_0",
            "hours_padded",
            "hours_padded_00",
            "minutes",
            "minutes_00",
            "minutes_padded",
            "minutes_padded_00",
            "seconds",
            "seconds_00",
            "seconds_padded",
            "seconds_padded_00",
            "total_minutes",
            "total_seconds",
            "progress",
            "progress_0%",
            "progress_bar",
            "progress_bar_░░░░░░░░░░",
            // Статус
            "is_active",
            "active_text",
            // Топ
            "top_1",
            "top_1_name",
            "top_1_score",
            "top_1_Нет игроков",
            "top_2",
            "top_3",
            "top_4",
            "top_5",
            // Статистика игрока
            "deaths",
            "deaths_Нет смертей",
            "kills_players",
            "kills_players_0",
            "kills_mobs",
            "kills_mobs_Никого",
            "total_kills",
            "total_kills_---"
        );
    }
}
