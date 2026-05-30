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
        
        bossBar = BossBar.bossBar(
                Component.text(""),
                progress,
                color,
                BossBar.Overlay.NOTCHED_20
        );
        
        switch (segments) {
            case 6 -> bossBar = bossBar.toBuilder().overlay(BossBar.Overlay.NOTCHED_6).build();
            case 10 -> bossBar = bossBar.toBuilder().overlay(BossBar.Overlay.NOTCHED_10).build();
            case 12 -> bossBar = bossBar.toBuilder().overlay(BossBar.Overlay.NOTCHED_12).build();
            case 20 -> bossBar = bossBar.toBuilder().overlay(BossBar.Overlay.NOTCHED_20).build();
            default -> bossBar = bossBar.toBuilder().overlay(BossBar.Overlay.PROGRESS).build();
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar playerBar = bossBar;
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
        
        if (bossBar != null) {
            bossBar = null;
        }
    }
    
    public void updateProgress(float progress) {
        if (bossBar == null) return;
        for (BossBar bar : playerBossBars.values()) {
            bar.progress(Math.min(1f, Math.max(0f, progress)));
        }
    }
    
    public void updateTitle(String timeFormatted) {
        if (bossBar == null) return;
        
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
