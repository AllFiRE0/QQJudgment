package com.allfire.qqjudgment.listeners;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.managers.JudgmentManager;
import com.allfire.qqjudgment.managers.StatsManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class PlayerActionListener implements Listener {
    
    private final QQJudgment plugin;
    private final JudgmentManager judgmentManager;
    private final StatsManager statsManager;
    private boolean debug;
    
    public PlayerActionListener(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
        this.statsManager = plugin.getStatsManager();
        this.debug = plugin.getConfig().getBoolean("debug", false);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!judgmentManager.isJudgmentActive()) return;
        
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer != null && !killer.equals(victim)) {
            statsManager.recordPlayerKill(killer, victim);
            if (debug) {
                plugin.getLogger().info("[Stats] " + killer.getName() + " убил " + victim.getName());
            }
        }
        
        statsManager.recordPlayerDeath(victim.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!judgmentManager.isJudgmentActive()) return;
        
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            statsManager.recordMobKill(killer, event.getEntityType());
            if (debug) {
                plugin.getLogger().info("[Stats] " + killer.getName() + " убил моба " + event.getEntityType().name());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!judgmentManager.isJudgmentActive()) return;
        
        Player player = event.getPlayer();
        
        // Bypass право
        if (player.hasPermission("qqjudgment.bypass.commands")) return;
        
        // Креатив и спектатор - не ограничиваем
        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        
        List<String> blockedCommands = plugin.getConfig().getStringList("blocked-commands");
        String command = event.getMessage().toLowerCase().split(" ")[0].replace("/", "");
        
        if (blockedCommands.contains(command)) {
            event.setCancelled(true);
            plugin.getMessageManager().sendMessage(player, "command-blocked", true);
            if (debug) {
                plugin.getLogger().info("[Restrict] " + player.getName() + " попытался использовать команду /" + command);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!judgmentManager.isJudgmentActive()) return;
        
        Player player = event.getPlayer();
        
        // Bypass право
        if (player.hasPermission("qqjudgment.bypass.sleep")) return;
        
        // Креатив и спектатор - не ограничиваем
        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        
        boolean sleepRestricted = plugin.getConfig().getBoolean("restrictions.sleep", false);
        
        if (sleepRestricted) {
            event.setCancelled(true);
            plugin.getMessageManager().sendMessage(player, "cant-sleep", true);
            if (debug) {
                plugin.getLogger().info("[Restrict] " + player.getName() + " попытался лечь спать");
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Ничего не логируем, просто заглушка
    }
}
