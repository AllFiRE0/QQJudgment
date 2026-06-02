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
    
    // Список всех основных параметров (без fallback)
    private final String[] MAIN_PARAMS = {
        "end", "time_end", "seconds_end",
        "hours", "hours_padded", "minutes", "minutes_padded", "seconds", "seconds_padded",
        "total_minutes", "total_seconds",
        "is_active", "active_text", "progress",
        "deaths", "kills_players", "kills_mobs", "total_kills"
    };
    
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
    
    /**
     * Разбирает параметр на основную часть и fallback
     * @param params входной параметр (например "kills_mobs_Никого")
     * @return массив [основная_часть, fallback, hasFallback]
     */
    private ParseResult parseParams(String params) {
        // Сначала проверяем top_ параметры (у них своя структура)
        if (params.startsWith("top_")) {
            String[] parts = params.split("_");
            if (parts.length >= 2) {
                // top_1, top_1_name, top_1_score - без fallback
                if (parts.length == 2) {
                    return new ParseResult(params, "", false);
                }
                // top_1_name, top_1_score - без fallback
                if (parts.length == 3 && (parts[2].equals("name") || parts[2].equals("score"))) {
                    return new ParseResult(params, "", false);
                }
                // top_1_Текст - с fallback
                if (parts.length >= 3) {
                    String base = parts[0] + "_" + parts[1];
                    String fallback = params.substring(base.length() + 1);
                    return new ParseResult(base, fallback, true);
                }
            }
            return new ParseResult(params, "", false);
        }
        
        // Проверяем основные параметры
        for (String main : MAIN_PARAMS) {
            if (params.equals(main)) {
                // Точное совпадение - без fallback
                return new ParseResult(main, "", false);
            }
            if (params.startsWith(main + "_")) {
                // Есть fallback после _
                String fallback = params.substring(main.length() + 1);
                return new ParseResult(main, fallback, true);
            }
        }
        
        // Неизвестный параметр
        return new ParseResult(params, "", false);
    }
    
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params == null || params.isEmpty()) return "";
        
        ParseResult parsed = parseParams(params);
        String baseParam = parsed.baseParam;
        String fallback = parsed.fallback;
        boolean hasFallback = parsed.hasFallback;
        
        // ========== ОСНОВНЫЕ ==========
        if (baseParam.equalsIgnoreCase("end")) {
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return hasFallback ? fallback : "";
        }
        
        if (baseParam.equalsIgnoreCase("time_end")) {
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
            // ЕСЛИ НЕТ ИГРОКА В ТОПЕ - ВОЗВРАЩАЕМ FALLBACK (ЕСЛИ ОН БЫЛ УКАЗАН)
            if (hasFallback) {
                return fallback;
            }
            return "";
        }
        
        if (player == null) return "";
        
        // ========== СТАТИСТИКА ИГРОКА ==========
        if (baseParam.equalsIgnoreCase("deaths")) {
            int deaths = statsManager.getPlayerDeaths(player.getUniqueId());
            if (deaths > 0) {
                return String.valueOf(deaths);
            }
            return hasFallback ? fallback : "0";
        }
        
        if (baseParam.equalsIgnoreCase("kills_players")) {
            int kills = statsManager.getPlayerPlayerKills(player.getUniqueId());
            if (kills > 0) {
                return String.valueOf(kills);
            }
            return hasFallback ? fallback : "0";
        }
        
        if (baseParam.equalsIgnoreCase("kills_mobs")) {
            int kills = statsManager.getPlayerMobKills(player.getUniqueId());
            if (kills > 0) {
                return String.valueOf(kills);
            }
            return hasFallback ? fallback : "0";
        }
        
        if (baseParam.equalsIgnoreCase("total_kills")) {
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
        
        // Основные заполнители
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
        
        // Компоненты времени
        placeholders.add("%qqjudgment_hours%");
        placeholders.add("%qqjudgment_hours_padded%");
        placeholders.add("%qqjudgment_minutes%");
        placeholders.add("%qqjudgment_minutes_padded%");
        placeholders.add("%qqjudgment_seconds%");
        placeholders.add("%qqjudgment_seconds_padded%");
        placeholders.add("%qqjudgment_total_minutes%");
        placeholders.add("%qqjudgment_total_seconds%");
        
        // Топ игроков
        for (int i = 1; i <= 10; i++) {
            placeholders.add("%qqjudgment_top_" + i + "%");
            placeholders.add("%qqjudgment_top_" + i + "_name%");
            placeholders.add("%qqjudgment_top_" + i + "_score%");
        }
        
        return placeholders;
    }
    
    // Вспомогательный класс для результата парсинга
    private static class ParseResult {
        String baseParam;
        String fallback;
        boolean hasFallback;
        
        ParseResult(String baseParam, String fallback, boolean hasFallback) {
            this.baseParam = baseParam;
            this.fallback = fallback;
            this.hasFallback = hasFallback;
        }
    }
}
