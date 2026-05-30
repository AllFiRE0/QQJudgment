package com.allfire.qqjudgment.commands;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.managers.JudgmentManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JudgmentCommand implements CommandExecutor, TabCompleter {
    
    private final QQJudgment plugin;
    private final JudgmentManager judgmentManager;
    
    public JudgmentCommand(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
            if (!sender.hasPermission("qqjudgment.reload")) {
                plugin.getMessageManager().sendMessage(sender, "no-permission", false);
                return true;
            }
            
            plugin.reloadConfig();
            sender.sendMessage(plugin.getMessageManager().parseMessage("&a✅ Плагин QQJudgment перезагружен!"));
            sender.sendMessage(plugin.getMessageManager().parseMessage("&7Новые настройки применены."));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("hidebossbar")) {
            if (!sender.hasPermission("qqjudgment.stop")) {
                plugin.getMessageManager().sendMessage(sender, "no-permission", false);
                return true;
            }
            plugin.getBossBarManager().hideBossBarFromAll();
            sender.sendMessage("§aБоссбар скрыт!");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("papi")) {
            if (!sender.hasPermission("qqjudgment.placeholder.parse")) {
                plugin.getMessageManager().sendMessage(sender, "no-permission", false);
                return true;
            }
            
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
                sender.sendMessage("§cPlaceholderAPI не установлен!");
                return true;
            }
            
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cЭта команда только для игроков!");
                return true;
            }
            
            if (args.length < 2) {
                sender.sendMessage("§cИспользование: /qqjudgment papi <текст с заполнителями>");
                sender.sendMessage("§7Пример: /qqjudgment papi У вас %qqjudgment_kills_players% убийств");
                return true;
            }
            
            StringBuilder textBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                textBuilder.append(args[i]).append(" ");
            }
            String text = textBuilder.toString().trim();
            String parsed = PlaceholderAPI.setPlaceholders(player, text);
            
            sender.sendMessage("§7Оригинал: §f" + text);
            sender.sendMessage("§7Результат: §a" + parsed);
            return true;
        }
        
        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }
        
        boolean silent = args.length >= 3 && args[2].equalsIgnoreCase("-s");
        
        try {
            int seconds = Integer.parseInt(args[0]);
            
            if (args[1].equalsIgnoreCase("start")) {
                if (!sender.hasPermission("qqjudgment.start")) {
                    plugin.getMessageManager().sendMessage(sender, "no-permission", silent);
                    return true;
                }
                
                if (judgmentManager.isJudgmentActive()) {
                    plugin.getMessageManager().sendMessage(sender, "already-active", silent);
                    return true;
                }
                
                judgmentManager.startJudgment(seconds, silent);
                
            } else if (args[1].equalsIgnoreCase("stop")) {
                if (!sender.hasPermission("qqjudgment.stop")) {
                    plugin.getMessageManager().sendMessage(sender, "no-permission", silent);
                    return true;
                }
                
                if (!judgmentManager.isJudgmentActive()) {
                    plugin.getMessageManager().sendMessage(sender, "not-active", silent);
                    return true;
                }
                
                judgmentManager.stopJudgment(silent);
                
            } else {
                sendHelp(sender);
            }
            
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(sender, "invalid-seconds", silent);
        }
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("60");
            completions.add("300");
            completions.add("600");
            completions.add("1800");
            completions.add("3600");
            completions.add("7200");
            completions.add("reload");
            completions.add("rl");
            completions.add("papi");
            completions.add("hidebossbar");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("papi")) {
                completions.add("<текст с %заполнителями%>");
                completions.addAll(getPlaceholderExamples());
            } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                // Нет подсказок для reload
            } else if (args[0].equalsIgnoreCase("hidebossbar")) {
                // Нет подсказок для hidebossbar
            } else {
                if (sender.hasPermission("qqjudgment.start")) completions.add("start");
                if (sender.hasPermission("qqjudgment.stop")) completions.add("stop");
            }
        } else if (args.length == 3 && !args[0].equalsIgnoreCase("papi") && !args[0].equalsIgnoreCase("reload") && !args[0].equalsIgnoreCase("hidebossbar")) {
            completions.add("-s");
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("papi")) {
            completions.addAll(getPlaceholderCompletions(args[args.length - 1]));
        }
        
        return completions;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== QQJudgment Help ===");
        sender.sendMessage("§e/qqjudgment <секунд> start [-s] §7- Запустить судную ночь");
        sender.sendMessage("§e/qqjudgment <секунд> stop [-s] §7- Остановить судную ночь");
        sender.sendMessage("§e/qqjudgment reload §7- Перезагрузить конфиг");
        sender.sendMessage("§e/qqjudgment hidebossbar §7- Скрыть боссбар");
        sender.sendMessage("§e/qqjudgment papi <текст> §7- Проверить заполнители");
        sender.sendMessage("");
        sender.sendMessage("§7Примеры заполнителей:");
        sender.sendMessage("§7%qqjudgment_time_end% §8- Время до конца (05:30:45)");
        sender.sendMessage("§7%qqjudgment_hours_padded% §8- Часы с нулем (05)");
        sender.sendMessage("§7%qqjudgment_minutes_padded% §8- Минуты с нулем (30)");
        sender.sendMessage("§7%qqjudgment_seconds_padded% §8- Секунды с нулем (45)");
        sender.sendMessage("§7%qqjudgment_kills_players% §8- Ваши убийства игроков");
        sender.sendMessage("§7%qqjudgment_top_1_name% §8- Имя топ игрока");
    }
    
    private List<String> getPlaceholderExamples() {
        return List.of(
            "%qqjudgment_end%",
            "%qqjudgment_time_end%",
            "%qqjudgment_is_active%",
            "%qqjudgment_active_text%",
            "%qqjudgment_hours%",
            "%qqjudgment_hours_padded%",
            "%qqjudgment_minutes%",
            "%qqjudgment_minutes_padded%",
            "%qqjudgment_seconds%",
            "%qqjudgment_seconds_padded%",
            "%qqjudgment_total_minutes%",
            "%qqjudgment_total_seconds%",
            "%qqjudgment_progress%",
            "%qqjudgment_progress_bar%",
            "%qqjudgment_kills_players%",
            "%qqjudgment_kills_mobs%",
            "%qqjudgment_deaths%",
            "%qqjudgment_total_kills%",
            "%qqjudgment_top_1%",
            "%qqjudgment_top_1_name%",
            "%qqjudgment_top_1_score%"
        );
    }
    
    private List<String> getPlaceholderCompletions(String current) {
        List<String> placeholders = new ArrayList<>();
        
        // Основные
        placeholders.add("%qqjudgment_end%");
        placeholders.add("%qqjudgment_end_Мой текст%");
        placeholders.add("%qqjudgment_time_end%");
        placeholders.add("%qqjudgment_time_end_Не активно%");
        placeholders.add("%qqjudgment_seconds_end%");
        
        // Компоненты времени
        placeholders.add("%qqjudgment_hours%");
        placeholders.add("%qqjudgment_hours_0%");
        placeholders.add("%qqjudgment_hours_padded%");
        placeholders.add("%qqjudgment_hours_padded_00%");
        placeholders.add("%qqjudgment_minutes%");
        placeholders.add("%qqjudgment_minutes_0%");
        placeholders.add("%qqjudgment_minutes_padded%");
        placeholders.add("%qqjudgment_minutes_padded_00%");
        placeholders.add("%qqjudgment_seconds%");
        placeholders.add("%qqjudgment_seconds_0%");
        placeholders.add("%qqjudgment_seconds_padded%");
        placeholders.add("%qqjudgment_seconds_padded_00%");
        placeholders.add("%qqjudgment_total_minutes%");
        placeholders.add("%qqjudgment_total_seconds%");
        
        // Статус
        placeholders.add("%qqjudgment_is_active%");
        placeholders.add("%qqjudgment_active_text%");
        
        // Прогресс
        placeholders.add("%qqjudgment_progress%");
        placeholders.add("%qqjudgment_progress_0%%");
        placeholders.add("%qqjudgment_progress_bar%");
        placeholders.add("%qqjudgment_progress_bar_░░░░░░░░░░%");
        
        // Топ игроков (1-10)
        for (int i = 1; i <= 10; i++) {
            placeholders.add("%qqjudgment_top_" + i + "%");
            placeholders.add("%qqjudgment_top_" + i + "_name%");
            placeholders.add("%qqjudgment_top_" + i + "_score%");
            placeholders.add("%qqjudgment_top_" + i + "_Нет игроков%");
        }
        
        // Статистика
        placeholders.add("%qqjudgment_deaths%");
        placeholders.add("%qqjudgment_deaths_Нет смертей%");
        placeholders.add("%qqjudgment_kills_players%");
        placeholders.add("%qqjudgment_kills_players_0%");
        placeholders.add("%qqjudgment_kills_mobs%");
        placeholders.add("%qqjudgment_kills_mobs_Никого%");
        placeholders.add("%qqjudgment_total_kills%");
        placeholders.add("%qqjudgment_total_kills_---%");
        
        if (current == null || current.isEmpty()) return placeholders;
        
        return placeholders.stream()
                .filter(p -> p.toLowerCase().contains(current.toLowerCase()))
                .collect(Collectors.toList());
    }
}
