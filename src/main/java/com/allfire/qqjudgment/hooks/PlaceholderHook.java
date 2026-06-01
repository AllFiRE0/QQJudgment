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
        if (player == null) return "";
        
        // ТОЧНО КАК В QQCMExpansion: split("_", 4)
        String[] parts = params.split("_", 4);
        
        if (parts.length < 1) return "";
        
        String mainParam = parts[0];
        
        // ========== ОСНОВНЫЕ ==========
        if (mainParam.equalsIgnoreCase("end")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("time_end")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return judgmentManager.getTimeRemainingFormatted();
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("seconds_end")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return fallback;
        }
        
        // ========== КОМПОНЕНТЫ ВРЕМЕНИ ==========
        if (mainParam.equalsIgnoreCase("hours")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toHours());
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("hours_padded")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toHours());
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("minutes")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toMinutesPart());
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("minutes_padded")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toMinutesPart());
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("seconds")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toSecondsPart());
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("seconds_padded")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return String.format("%02d", getDuration().toSecondsPart());
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("total_minutes")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(getDuration().toMinutes());
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("total_seconds")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return String.valueOf(judgmentManager.getRemainingSeconds());
            }
            return fallback;
        }
        
        // ========== СТАТУС ==========
        if (mainParam.equalsIgnoreCase("is_active")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return "true";
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("active_text")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (judgmentManager.isJudgmentActive()) {
                return "§aАктивна";
            }
            return fallback.isEmpty() ? "§cНе активна" : fallback;
        }
        
        // ========== ПРОГРЕСС ==========
        if (mainParam.equalsIgnoreCase("progress")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (!judgmentManager.isJudgmentActive()) {
                return fallback;
            }
            int totalSeconds = 3600;
            int remaining = judgmentManager.getRemainingSeconds();
            int percent = (int) ((double) (totalSeconds - remaining) / totalSeconds * 100);
            percent = Math.min(100, Math.max(0, percent));
            return String.valueOf(percent);
        }
        
        // ========== ТОП ИГРОКОВ ==========
        if (mainParam.toLowerCase().startsWith("top_")) {
            String[] topParts = mainParam.split("_");
            String fallback = parts.length >= 2 ? parts[1] : "";
            if (topParts.length >= 2) {
                try {
                    int topNumber = Integer.parseInt(topParts[1]);
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
            return fallback;
        }
        
        // ========== СТАТИСТИКА ИГРОКА ==========
        if (mainParam.equalsIgnoreCase("deaths")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            int deaths = statsManager.getPlayerDeaths(player.getUniqueId());
            if (deaths > 0) {
                return String.valueOf(deaths);
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("kills_players")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            int kills = statsManager.getPlayerPlayerKills(player.getUniqueId());
            if (kills > 0) {
                return String.valueOf(kills);
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("kills_mobs")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            int kills = statsManager.getPlayerMobKills(player.getUniqueId());
            if (kills > 0) {
                return String.valueOf(kills);
            }
            return fallback;
        }
        
        if (mainParam.equalsIgnoreCase("total_kills")) {
            String fallback = parts.length >= 2 ? parts[1] : "";
            int total = statsManager.getPlayerPlayerKills(player.getUniqueId()) + 
                       statsManager.getPlayerMobKills(player.getUniqueId());
            if (total > 0) {
                return String.valueOf(total);
            }
            return fallback;
        }
        
        return "";
    }
    
    @Override
    public @Nullable List<String> getPlaceholders() {
        List<String> placeholders = new ArrayList<>();
        
        // Основные
        placeholders.add("%qqjudgment_end%");
        placeholders.add("%qqjudgment_end_Не активно%");
        placeholders.add("%qqjudgment_end_%");
        placeholders.add("%qqjudgment_time_end%");
        placeholders.add("%qqjudgment_time_end_Не активно%");
        placeholders.add("%qqjudgment_time_end_%");
        placeholders.add("%qqjudgment_seconds_end%");
        placeholders.add("%qqjudgment_seconds_end_0%");
        placeholders.add("%qqjudgment_seconds_end_%");
        
        // Компоненты времени
        placeholders.add("%qqjudgment_hours%");
        placeholders.add("%qqjudgment_hours_0%");
        placeholders.add("%qqjudgment_hours_%");
        placeholders.add("%qqjudgment_hours_padded%");
        placeholders.add("%qqjudgment_hours_padded_00%");
        placeholders.add("%qqjudgment_hours_padded_%");
        
        placeholders.add("%qqjudgment_minutes%");
        placeholders.add("%qqjudgment_minutes_0%");
        placeholders.add("%qqjudgment_minutes_%");
        placeholders.add("%qqjudgment_minutes_padded%");
        placeholders.add("%qqjudgment_minutes_padded_00%");
        placeholders.add("%qqjudgment_minutes_padded_%");
        
        placeholders.add("%qqjudgment_seconds%");
        placeholders.add("%qqjudgment_seconds_0%");
        placeholders.add("%qqjudgment_seconds_%");
        placeholders.add("%qqjudgment_seconds_padded%");
        placeholders.add("%qqjudgment_seconds_padded_00%");
        placeholders.add("%qqjudgment_seconds_padded_%");
        
        placeholders.add("%qqjudgment_total_minutes%");
        placeholders.add("%qqjudgment_total_minutes_0%");
        placeholders.add("%qqjudgment_total_minutes_%");
        placeholders.add("%qqjudgment_total_seconds%");
        placeholders.add("%qqjudgment_total_seconds_0%");
        placeholders.add("%qqjudgment_total_seconds_%");
        
        // Статус
        placeholders.add("%qqjudgment_is_active%");
        placeholders.add("%qqjudgment_is_active_Не активна%");
        placeholders.add("%qqjudgment_is_active_%");
        placeholders.add("%qqjudgment_active_text%");
        placeholders.add("%qqjudgment_active_text_Не активно%");
        
        // Прогресс
        placeholders.add("%qqjudgment_progress%");
        placeholders.add("%qqjudgment_progress_0%%");
        placeholders.add("%qqjudgment_progress_%");
        
        // Топ игроков 1-10
        for (int i = 1; i <= 10; i++) {
            placeholders.add("%qqjudgment_top_" + i + "%");
            placeholders.add("%qqjudgment_top_" + i + "_Нет игроков%");
            placeholders.add("%qqjudgment_top_" + i + "_%");
            placeholders.add("%qqjudgment_top_" + i + "_name%");
            placeholders.add("%qqjudgment_top_" + i + "_name_Нет имени%");
            placeholders.add("%qqjudgment_top_" + i + "_name_%");
            placeholders.add("%qqjudgment_top_" + i + "_score%");
            placeholders.add("%qqjudgment_top_" + i + "_score_0%");
            placeholders.add("%qqjudgment_top_" + i + "_score_%");
        }
        
        // Статистика
        placeholders.add("%qqjudgment_deaths%");
        placeholders.add("%qqjudgment_deaths_0%");
        placeholders.add("%qqjudgment_deaths_Нет смертей%");
        placeholders.add("%qqjudgment_deaths_%");
        
        placeholders.add("%qqjudgment_kills_players%");
        placeholders.add("%qqjudgment_kills_players_0%");
        placeholders.add("%qqjudgment_kills_players_Нет убийств%");
        placeholders.add("%qqjudgment_kills_players_%");
        
        placeholders.add("%qqjudgment_kills_mobs%");
        placeholders.add("%qqjudgment_kills_mobs_0%");
        placeholders.add("%qqjudgment_kills_mobs_Никого%");
        placeholders.add("%qqjudgment_kills_mobs_%");
        
        placeholders.add("%qqjudgment_total_kills%");
        placeholders.add("%qqjudgment_total_kills_0%");
        placeholders.add("%qqjudgment_total_kills_---%");
        placeholders.add("%qqjudgment_total_kills_%");
        
        return placeholders;
    }
}
