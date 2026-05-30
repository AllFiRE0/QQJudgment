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
            
            // Проверяем, является ли первая часть валидным параметром
            if (isValidParam(possibleMain)) {
                mainParam = possibleMain;
                customFallback = possibleFallback;
            }
        }
        
        String fallbackToUse = customFallback != null ? customFallback : defaultFallback;
        
        // %qqjudgment_end_fallbackMsg% или %qqjudgment_end_Мой текст%
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
        
        // %qqjudgment_remaining_percent%
        if (mainParam.equalsIgnoreCase("remaining_percent")) {
            if (!judgmentManager.isJudgmentActive()) return "0";
            if (judgmentManager.getRemainingSeconds() <= 0) return "0";
            int totalSeconds = plugin.getConfig().getInt("judgment.default-seconds", 3600);
            return String.valueOf((judgmentManager.getRemainingSeconds() * 100) / totalSeconds);
        }
        
        return null;
    }
    
    private boolean isValidParam(String param) {
        return List.of(
            "end", "end_fallbackMsg",
            "time_end", "time_end_fallbackMsg", 
            "seconds_end",
            "is_active", "active_text",
            "deaths", "death_fallbackMsg",
            "kills_players", "kills_players_fallbackMsg",
            "kills_mobs", "kills_mobs_fallbackMsg",
            "total_kills", "total_kills_fallbackMsg",
            "remaining_percent"
        ).contains(param.toLowerCase()) || param.toLowerCase().startsWith("top_");
    }
    
    @Override
    public @Nullable List<String> getPlaceholders() {
        return List.of(
            "end",
            "end_Мой текст",
            "time_end",
            "time_end_Не активно",
            "seconds_end",
            "is_active",
            "active_text",
            "top_1",
            "top_1_Нет игроков",
            "top_2",
            "top_3",
            "top_4",
            "top_5",
            "deaths",
            "deaths_Нет смертей",
            "kills_players",
            "kills_players_0",
            "kills_mobs",
            "kills_mobs_Никого",
            "total_kills",
            "total_kills_---",
            "remaining_percent"
        );
    }
}
