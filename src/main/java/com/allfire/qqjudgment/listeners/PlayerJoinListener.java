package com.allfire.qqjudgment.listeners;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.managers.JudgmentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final QQJudgment plugin;
    private final JudgmentManager judgmentManager;
    private boolean debug;
    
    public PlayerJoinListener(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
        this.debug = plugin.getConfig().getBoolean("debug", false);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Проверяем, включен ли боссбар в конфиге
        if (!plugin.getConfig().getBoolean("bossbar.enabled", true)) {
            if (debug) {
                plugin.getLogger().info("[PlayerJoin] Боссбар выключен в конфиге, пропускаем");
            }
            return;
        }
        
        // Проверяем, активна ли судная ночь
        if (!judgmentManager.isJudgmentActive()) {
            if (debug) {
                plugin.getLogger().info("[PlayerJoin] Судная ночь не активна, пропускаем");
            }
            return;
        }
        
        Player player = event.getPlayer();
        
        // Показываем прогресс боссбар для игрока
        plugin.getBossBarManager().showProgressBossBar(player);
        
        int remainingSeconds = judgmentManager.getRemainingSeconds();
        int totalSeconds = judgmentManager.getTotalSeconds();
        
        if (totalSeconds > 0) {
            float progress = (float) remainingSeconds / totalSeconds;
            plugin.getBossBarManager().updateProgressForPlayer(player, progress);
        }
        
        String formattedTime = plugin.getStatsManager().formatTime(remainingSeconds);
        plugin.getBossBarManager().updateTitleForPlayer(player, formattedTime);
        
        if (debug) {
            plugin.getLogger().info("[PlayerJoin] Показан боссбар для " + player.getName() + 
                " (осталось " + remainingSeconds + " сек)");
        }
    }
}
