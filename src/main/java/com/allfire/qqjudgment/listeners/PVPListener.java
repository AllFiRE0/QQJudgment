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
    
    public PVPListener(QQJudgment plugin) {
        this.plugin = plugin;
        this.judgmentManager = plugin.getJudgmentManager();
        this.worldGuard = plugin.getWorldGuardHook();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        
        // Если судная ночь не активна - пропускаем (WorldGuard сам обработает)
        if (!judgmentManager.isJudgmentActive()) return;
        
        // Проверка на bypass
        if (attacker.hasPermission("qqjudgment.bypass.pvp")) return;
        
        // Проверка черного списка регионов
        List<String> blacklisted = plugin.getConfig().getStringList("blacklisted-regions");
        
        if (worldGuard.isInBlacklistedRegion(victim.getLocation(), blacklisted) ||
            worldGuard.isInBlacklistedRegion(attacker.getLocation(), blacklisted)) {
            event.setCancelled(true);
            return;
        }
        
        // Разрешаем PVP во время судной ночи даже если WorldGuard запрещает
        event.setCancelled(false);
        
        // Отправляем уведомление если нужно
        if (Math.random() < 0.1) { // 10% шанс, чтобы не спамить
            plugin.getMessageManager().sendMessage(attacker, "pvp-enabled-during-judgment", true);
        }
    }
}
