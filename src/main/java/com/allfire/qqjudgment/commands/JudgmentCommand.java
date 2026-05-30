package com.allfire.qqjudgment.commands;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.managers.JudgmentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JudgmentCommand implements CommandExecutor, TabCompleter {
    
    private final QQJudgment plugin;
    private final JudgmentManager judgmentManager;
    
    public JudgmentCommand(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
            completions.add("3600");
        } else if (args.length == 2) {
            if (sender.hasPermission("qqjudgment.start")) completions.add("start");
            if (sender.hasPermission("qqjudgment.stop")) completions.add("stop");
        } else if (args.length == 3) {
            completions.add("-s");
        }
        
        return completions;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== QQJudgment Help ===");
        sender.sendMessage("§e/qqjudgment <секунд> start [-s] §7- Запустить судную ночь");
        sender.sendMessage("§e/qqjudgment <секунд> stop [-s] §7- Остановить судную ночь");
    }
}
