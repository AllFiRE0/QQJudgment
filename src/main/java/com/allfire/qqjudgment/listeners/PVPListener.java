package com.allfire.qqjudgment.listeners;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.managers.JudgmentManager;
import com.allfire.qqjudgment.hooks.WorldGuardHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

public class PVPListener implements Listener {
    
    private final QQJudgment plugin;
    private final JudgmentManager judgmentManager;
    private final WorldGuardHook worldGuard;
    private boolean debug;
    
    public PVPListener(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
        this.worldGuard = plugin.getWorldGuardHook();
        this.debug = plugin.getConfig().getBoolean("debug", false);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        
        // Если судная ночь НЕ активна - пропускаем, пусть WorldGuard сам решает
        if (!judgmentManager.isJudgmentActive()) return;
        
        // Проверка на bypass - если есть право, тоже пропускаем (пусть WG решает)
        if (attacker.hasPermission("qqjudgment.bypass.pvp")) return;
        
        // Проверка черного списка регионов (здесь PVP запрещен даже во время СН)
        List<String> blacklisted = plugin.getConfig().getStringList("blacklisted-regions");
        
        if (worldGuard.isInBlacklistedRegion(victim.getLocation(), blacklisted) ||
            worldGuard.isInBlacklistedRegion(attacker.getLocation(), blacklisted)) {
            if (debug) {
                plugin.getLogger().info("[PVP] PVP запрещен в черном списке регионов");
            }
            event.setCancelled(true);
            return;
        }
        
        // ========== ГЛАВНОЕ: РАЗРЕШАЕМ PVP ВО ВРЕМЯ СУДНОЙ НОЧИ ==========
        // Отменяем запрет WorldGuard, принудительно разрешаем PVP
        event.setCancelled(false);
        
        if (debug) {
            plugin.getLogger().info("[PVP] PVP разрешен во время Судной ночи: " + 
                attacker.getName() + " -> " + victim.getName());
        }
    }
}
