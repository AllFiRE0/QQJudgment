package com.allfire.qqjudgment.managers;

import com.allfire.qqjudgment.QQJudgment;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager {
    
    private final QQJudgment plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, BossBar> playerBossBars = new HashMap<>();
    private BukkitTask hideTask;
    private BukkitTask showTask;
    private boolean debug;
    
    public BossBarManager(QQJudgment plugin) {
        this.plugin = plugin;
        this.debug = plugin.getConfig().getBoolean("debug", false);
    }
    
    public void showStartBossBar() {
        if (!plugin.getConfig().getBoolean("bossbar.enabled", true)) return;
        
        hideBossBarFromAll();
        
        String colorStr = plugin.getConfig().getString("bossbar.color", "RED");
        BossBar.Color color = getColor(colorStr);
        int segments = plugin.getConfig().getInt("bossbar.segments", 12);
        int delaySeconds = plugin.getConfig().getInt("bossbar.start-delay", 3);
        
        String textTemplate = plugin.getConfig().getString("bossbar.start-text", "<gradient:#00FF00:#55FF55>Судная ночь началась!</gradient>");
        Component component = plugin.getMessageManager().parseMessage(textTemplate);
        
        BossBar.Overlay overlay = getOverlay(segments);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar playerBar = BossBar.bossBar(component, 1.0f, color, overlay);
            playerBossBars.put(player.getUniqueId(), playerBar);
            plugin.getAdventure().player(player).showBossBar(playerBar);
        }
        
        if (debug) {
            plugin.getLogger().info("[BossBar] Показан стартовый боссбар для " + playerBossBars.size() + " игроков");
        }
        
        if (hideTask != null) hideTask.cancel();
        hideTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (debug) plugin.getLogger().info("[BossBar] Скрываем стартовый боссбар");
                hideBossBarFromAll();
                hideTask = null;
            }
        }.runTaskLater(plugin, delaySeconds * 20L);
    }
    
    public void showProgressBossBar() {
        showProgressBossBar(null);
    }
    
    public void showProgressBossBar(Player targetPlayer) {
        if (!plugin.getConfig().getBoolean("bossbar.enabled", true)) return;
        
        String colorStr = plugin.getConfig().getString("bossbar.color", "RED");
        BossBar.Color color = getColor(colorStr);
        int segments = plugin.getConfig().getInt("bossbar.segments", 12);
        
        BossBar.Overlay overlay = getOverlay(segments);
        
        if (targetPlayer == null) {
            // Для всех игроков
            for (Player player : Bukkit.getOnlinePlayers()) {
                BossBar playerBar = BossBar.bossBar(Component.text(""), 0f, color, overlay);
                playerBossBars.put(player.getUniqueId(), playerBar);
                plugin.getAdventure().player(player).showBossBar(playerBar);
            }
            if (debug) {
                plugin.getLogger().info("[BossBar] Показан прогресс-боссбар для " + playerBossBars.size() + " игроков");
            }
        } else {
            // Только для одного игрока
            BossBar playerBar = BossBar.bossBar(Component.text(""), 0f, color, overlay);
            playerBossBars.put(targetPlayer.getUniqueId(), playerBar);
            plugin.getAdventure().player(targetPlayer).showBossBar(playerBar);
            if (debug) {
                plugin.getLogger().info("[BossBar] Показан прогресс-боссбар для " + targetPlayer.getName());
            }
        }
    }
    
    public void showEndBossBar() {
        if (!plugin.getConfig().getBoolean("bossbar.enabled", true)) return;
        
        String colorStr = plugin.getConfig().getString("bossbar.color", "RED");
        BossBar.Color color = getColor(colorStr);
        int segments = plugin.getConfig().getInt("bossbar.segments", 12);
        int delaySeconds = plugin.getConfig().getInt("bossbar.end-delay", 3);
        
        String textTemplate = plugin.getConfig().getString("bossbar.end-text", "<gradient:#FF5555:#FF0000>Судная ночь закончилась!</gradient>");
        Component component = plugin.getMessageManager().parseMessage(textTemplate);
        
        BossBar.Overlay overlay = getOverlay(segments);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar bar = playerBossBars.get(player.getUniqueId());
            if (bar != null) {
                bar.name(component);
                bar.progress(1.0f);
            } else {
                BossBar newBar = BossBar.bossBar(component, 1.0f, color, overlay);
                playerBossBars.put(player.getUniqueId(), newBar);
                plugin.getAdventure().player(player).showBossBar(newBar);
            }
        }
        
        if (debug) {
            plugin.getLogger().info("[BossBar] Показан финальный боссбар для " + playerBossBars.size() + " игроков");
        }
        
        if (hideTask != null) hideTask.cancel();
        hideTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (debug) plugin.getLogger().info("[BossBar] Скрываем финальный боссбар");
                hideBossBarFromAll();
                hideTask = null;
            }
        }.runTaskLater(plugin, delaySeconds * 20L);
    }
    
    public void hideBossBarFromAll() {
        if (hideTask != null) {
            hideTask.cancel();
            hideTask = null;
        }
        if (showTask != null) {
            showTask.cancel();
            showTask = null;
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar bar = playerBossBars.remove(player.getUniqueId());
            if (bar != null) {
                plugin.getAdventure().player(player).hideBossBar(bar);
            }
        }
        
        if (debug) {
            plugin.getLogger().info("[BossBar] Скрыт боссбар у всех игроков");
        }
    }
    
    public void hideBossBarForPlayer(Player player) {
        BossBar bar = playerBossBars.remove(player.getUniqueId());
        if (bar != null) {
            plugin.getAdventure().player(player).hideBossBar(bar);
            if (debug) {
                plugin.getLogger().info("[BossBar] Скрыт боссбар для " + player.getName());
            }
        }
    }
    
    public void updateProgress(float progress) {
        float clampedProgress = Math.min(1f, Math.max(0f, progress));
        for (BossBar bar : playerBossBars.values()) {
            bar.progress(clampedProgress);
        }
    }
    
    public void updateProgressForPlayer(Player player, float progress) {
        float clampedProgress = Math.min(1f, Math.max(0f, progress));
        BossBar bar = playerBossBars.get(player.getUniqueId());
        if (bar != null) {
            bar.progress(clampedProgress);
        }
    }
    
    public void updateTitle(String timeFormatted) {
        String textTemplate = plugin.getConfig().getString("bossbar.progress-text", "<gradient:#FF0000:#FFAA00>Судная ночь закончится через %time%</gradient>");
        String finalText = textTemplate.replace("%time%", timeFormatted);
        Component component = plugin.getMessageManager().parseMessage(finalText);
        
        for (BossBar bar : playerBossBars.values()) {
            bar.name(component);
        }
    }
    
    public void updateTitleForPlayer(Player player, String timeFormatted) {
        String textTemplate = plugin.getConfig().getString("bossbar.progress-text", "<gradient:#FF0000:#FFAA00>Судная ночь закончится через %time%</gradient>");
        String finalText = textTemplate.replace("%time%", timeFormatted);
        Component component = plugin.getMessageManager().parseMessage(finalText);
        
        BossBar bar = playerBossBars.get(player.getUniqueId());
        if (bar != null) {
            bar.name(component);
        }
    }
    
    private BossBar.Overlay getOverlay(int segments) {
        return switch (segments) {
            case 6 -> BossBar.Overlay.NOTCHED_6;
            case 10 -> BossBar.Overlay.NOTCHED_10;
            case 12 -> BossBar.Overlay.NOTCHED_12;
            case 20 -> BossBar.Overlay.NOTCHED_20;
            default -> BossBar.Overlay.PROGRESS;
        };
    }
    
    private BossBar.Color getColor(String colorStr) {
        return switch (colorStr.toUpperCase()) {
            case "BLUE" -> BossBar.Color.BLUE;
            case "GREEN" -> BossBar.Color.GREEN;
            case "PINK" -> BossBar.Color.PINK;
            case "PURPLE" -> BossBar.Color.PURPLE;
            case "RED" -> BossBar.Color.RED;
            case "WHITE" -> BossBar.Color.WHITE;
            case "YELLOW" -> BossBar.Color.YELLOW;
            default -> BossBar.Color.RED;
        };
    }
}
