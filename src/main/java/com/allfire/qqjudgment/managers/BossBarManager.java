package com.allfire.qqjudgment.managers;

import com.allfire.qqjudgment.QQJudgment;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager {
    
    private final QQJudgment plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BossBar bossBar;
    private final Map<UUID, BossBar> playerBossBars = new HashMap<>();
    private BukkitRunnable updateTask;
    
    public BossBarManager(QQJudgment plugin) {
        this.plugin = plugin;
    }
    
    public void showBossBarToAll() {
        if (!plugin.getConfig().getBoolean("bossbar.enabled", true)) return;
        
        String colorStr = plugin.getConfig().getString("bossbar.color", "RED");
        BossBar.Color color = getColor(colorStr);
        int segments = plugin.getConfig().getInt("bossbar.segments", 12);
        float progress = 0f;
        
        BossBar.Overlay overlay;
        switch (segments) {
            case 6 -> overlay = BossBar.Overlay.NOTCHED_6;
            case 10 -> overlay = BossBar.Overlay.NOTCHED_10;
            case 12 -> overlay = BossBar.Overlay.NOTCHED_12;
            case 20 -> overlay = BossBar.Overlay.NOTCHED_20;
            default -> overlay = BossBar.Overlay.PROGRESS;
        }
        
        bossBar = BossBar.bossBar(
                Component.text(""),
                progress,
                color,
                overlay
        );
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar playerBar = BossBar.bossBar(
                    Component.text(""),
                    progress,
                    color,
                    overlay
            );
            playerBossBars.put(player.getUniqueId(), playerBar);
            plugin.getAdventure().player(player).showBossBar(playerBar);
        }
        
        startUpdater();
    }
    
    public void hideBossBarFromAll() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar bar = playerBossBars.remove(player.getUniqueId());
            if (bar != null) {
                plugin.getAdventure().player(player).hideBossBar(bar);
            }
        }
        
        bossBar = null;
    }
    
    public void updateProgress(float progress) {
        float clampedProgress = Math.min(1f, Math.max(0f, progress));
        for (BossBar bar : playerBossBars.values()) {
            bar.progress(clampedProgress);
        }
    }
    
    public void updateTitle(String timeFormatted) {
        String textTemplate = plugin.getConfig().getString("bossbar.text", "Судная ночь закончится через %time%");
        String finalText = textTemplate.replace("%time%", timeFormatted);
        
        Component component = plugin.getMessageManager().parseMessage(finalText);
        
        for (BossBar bar : playerBossBars.values()) {
            bar.name(component);
        }
    }
    
    private void startUpdater() {
        int updateTicks = plugin.getConfig().getInt("bossbar.update-ticks", 5);
        
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getJudgmentManager().isJudgmentActive()) {
                    this.cancel();
                }
            }
        };
        updateTask.runTaskTimer(plugin, 0L, updateTicks);
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
