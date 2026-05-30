package com.allfire.qqjudgment.managers;

import com.allfire.qqjudgment.QQJudgment;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {
    
    private final QQJudgment plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Pattern actionbarPattern = Pattern.compile("^actionbar!?(?::(\\d+))?\\s+(.*)$", Pattern.DOTALL);
    private final Pattern titlePattern = Pattern.compile("^title!?(?::(\\d+):(\\d+):(\\d+))?\\s+(.*)$", Pattern.DOTALL);
    
    public MessageManager(QQJudgment plugin) {
        this.plugin = plugin;
    }
    
    public void sendMessage(CommandSender sender, String messageKey, boolean silent) {
        if (silent) return;
        sendMessage(sender, messageKey, null);
    }
    
    public void sendMessage(CommandSender sender, String messageKey, Map<String, String> placeholders) {
        String rawMessage = plugin.getConfig().getString("messages." + messageKey);
        if (rawMessage == null) return;
        
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        if (!prefix.isEmpty() && !rawMessage.startsWith("actionbar!") && !rawMessage.startsWith("title!")) {
            rawMessage = prefix + rawMessage;
        }
        
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                rawMessage = rawMessage.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        
        sendFormattedMessage(sender, rawMessage);
    }
    
    public void broadcastMessage(String messageKey, Map<String, String> placeholders) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMessage(player, messageKey, placeholders);
        }
    }
    
    private void sendFormattedMessage(CommandSender sender, String message) {
        // Обработка PlaceholderAPI для игроков
        String parsedMessage = message;
        if (sender instanceof Player player && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            parsedMessage = PlaceholderAPI.setPlaceholders(player, message);
        }
        
        // Actionbar
        Matcher actionbarMatcher = actionbarPattern.matcher(parsedMessage);
        if (actionbarMatcher.matches()) {
            String durationStr = actionbarMatcher.group(1);
            String content = actionbarMatcher.group(2);
            Component component = parseMessage(content);
            
            if (durationStr != null) {
                int duration = Integer.parseInt(durationStr);
                sendActionBarWithDuration(sender, component, duration);
            } else {
                sendActionBar(sender, component);
            }
            return;
        }
        
        // Title
        Matcher titleMatcher = titlePattern.matcher(parsedMessage);
        if (titleMatcher.matches()) {
            String fadeIn = titleMatcher.group(1);
            String stay = titleMatcher.group(2);
            String fadeOut = titleMatcher.group(3);
            String content = titleMatcher.group(4);
            Component component = parseMessage(content);
            
            if (fadeIn != null && stay != null && fadeOut != null) {
                sendTitle(sender, component, 
                        Title.Times.times(
                                Duration.ofMillis(Integer.parseInt(fadeIn) * 50L),
                                Duration.ofMillis(Integer.parseInt(stay) * 50L),
                                Duration.ofMillis(Integer.parseInt(fadeOut) * 50L)
                        ));
            } else {
                sendTitle(sender, component, Title.DEFAULT_TIMES);
            }
            return;
        }
        
        // Обычное сообщение
        sender.sendMessage(parseMessage(parsedMessage));
    }
    
    public Component parseMessage(String message) {
        // Конвертация & цветов в MiniMessage
        String converted = convertLegacyToMiniMessage(message);
        // Конвертация CMI градиентов
        converted = convertCMIGradients(converted);
        
        try {
            return miniMessage.deserialize(converted);
        } catch (Exception e) {
            return Component.text(message);
        }
    }
    
    private String convertLegacyToMiniMessage(String message) {
        // &x&f&f&f&f&f&f -> <#ffffff>
        Pattern hexPattern = Pattern.compile("&x(&[0-9a-fA-F]){6}");
        Matcher hexMatcher = hexPattern.matcher(message);
        while (hexMatcher.find()) {
            String hexCode = hexMatcher.group();
            String color = hexCode.replace("&x", "").replace("&", "");
            message = message.replace(hexCode, "<#" + color + ">");
        }
        
        // &#FFFFFF -> <#FFFFFF>
        message = message.replaceAll("&#([0-9a-fA-F]{6})", "<#$1>");
        
        // &f, &a, &c etc -> стандартные MiniMessage
        message = message.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");
        
        return message;
    }
    
    private String convertCMIGradients(String message) {
        Pattern gradientPattern = Pattern.compile("\\{([#>]?[0-9a-fA-F]{6}[<>]?)\\}(.*?)\\{([#>]?[0-9a-fA-F]{6}[<>]?)\\}");
        Matcher matcher = gradientPattern.matcher(message);
        
        while (matcher.find()) {
            String startColor = matcher.group(1).replace("{", "").replace("}", "");
            String text = matcher.group(2);
            String endColor = matcher.group(3).replace("{", "").replace("}", "");
            
            startColor = startColor.replace("#", "");
            endColor = endColor.replace("#", "");
            
            String gradientTag = "<gradient:" + startColor + ":" + endColor + ">" + text + "</gradient>";
            message = message.replace(matcher.group(0), gradientTag);
        }
        
        return message;
    }
    
    private void sendActionBar(CommandSender sender, Component component) {
        if (sender instanceof Player player) {
            plugin.getAdventure().player(player).sendActionBar(component);
        }
    }
    
    private void sendActionBarWithDuration(CommandSender sender, Component component, int durationTicks) {
        if (sender instanceof Player player) {
            plugin.getAdventure().player(player).sendActionBar(component);
            // Для длительности потребуется повторная отправка, но это базовая реализация
        }
    }
    
    private void sendTitle(CommandSender sender, Component component, Title.Times times) {
        if (sender instanceof Player player) {
            plugin.getAdventure().player(player).showTitle(Title.title(component, Component.empty(), times));
        }
    }
}
