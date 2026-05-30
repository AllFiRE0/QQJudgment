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
        Player player = event.getPlayer();
        
        // Если судная ночь активна
        if (judgmentManager.isJudgmentActive()) {
            // Показываем прогресс боссбар
            plugin.getBossBarManager().showProgressBossBar(player);
            
            // Устанавливаем правильный прогресс и время
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
}
