package com.allfire.qqjudgment.listeners;

import com.allfire.qqjudgment.QQJudgment;
import com.allfire.qqjudgment.hooks.WorldGuardHook;
import com.allfire.qqjudgment.managers.JudgmentManager;
import org.bukkit.entity.Player;
import org.bukkit.Location;
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
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        
        // Если судная ночь не активна - не вмешиваемся
        if (!judgmentManager.isJudgmentActive()) return;
        
        // Если есть право bypass - пропускаем (не мешаем)
        if (attacker.hasPermission("qqjudgment.bypass.pvp")) return;
        
        // Проверка черного списка регионов
        List<String> blacklisted = plugin.getConfig().getStringList("blacklisted-regions");
        
        // Если в черном списке - ЗАПРЕЩАЕМ
        if (worldGuard.isInBlacklistedRegion(victim.getLocation(), blacklisted) ||
            worldGuard.isInBlacklistedRegion(attacker.getLocation(), blacklisted)) {
            event.setCancelled(true);
            return;
        }
        
        // ВСЕМ ОСТАЛЬНЫМ - РАЗРЕШАЕМ (отменяем запрет WorldGuard)
        // Важно: setCancelled(false) переопределяет запрет других плагинов
        if (event.isCancelled()) {
            event.setCancelled(false);
        }
        
        if (debug) {
            plugin.getLogger().info("[PVP] РАЗРЕШЕН: " + attacker.getName() + " -> " + victim.getName());
        }
    }
}
