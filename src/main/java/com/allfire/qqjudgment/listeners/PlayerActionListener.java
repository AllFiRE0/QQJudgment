package com.allfire.qqjudgment.listeners;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.managers.JudgmentManager;
import com.allfire.qqjudgment.managers.StatsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

import java.util.List;
import java.util.UUID;

public class PlayerActionListener implements Listener {
    
    private final QQJudgment plugin;
    private final JudgmentManager judgmentManager;
    private final StatsManager statsManager;
    
    public PlayerActionListener(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
        this.statsManager = plugin.getStatsManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!judgmentManager.isJudgmentActive()) return;
        
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer != null && !killer.equals(victim)) {
            statsManager.recordPlayerKill(killer, victim);
        }
        
        statsManager.recordPlayerDeath(victim.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!judgmentManager.isJudgmentActive()) return;
        
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            statsManager.recordMobKill(killer, event.getEntityType());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!judgmentManager.isJudgmentActive()) return;
        
        if (event.getPlayer().hasPermission("qqjudgment.bypass.commands")) return;
        
        List<String> blockedCommands = plugin.getConfig().getStringList("blocked-commands");
        String command = event.getMessage().toLowerCase().split(" ")[0].replace("/", "");
        
        if (blockedCommands.contains(command)) {
            event.setCancelled(true);
            plugin.getMessageManager().sendMessage(event.getPlayer(), "command-blocked", true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!judgmentManager.isJudgmentActive()) return;
        
        boolean sleepRestricted = plugin.getConfig().getBoolean("restrictions.sleep", false);
        
        if (sleepRestricted && !event.getPlayer().hasPermission("qqjudgment.bypass.sleep")) {
            event.setCancelled(true);
            plugin.getMessageManager().sendMessage(event.getPlayer(), "cant-sleep", true);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Очистка данных при выходе (опционально)
    }
}
