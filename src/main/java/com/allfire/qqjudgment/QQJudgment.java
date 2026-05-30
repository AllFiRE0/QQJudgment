package com.allfire.qqjudgment;

import com.allfire.qqjudgment.commands.JudgmentCommand;
import com.allfire.qqjudgment.hooks.PlaceholderHook;
import com.allfire.qqjudgment.hooks.WorldGuardHook;
import com.allfire.qqjudgment.managers.*;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class QQJudgment extends JavaPlugin {
    
    private static QQJudgment instance;
    private BukkitAudiences adventure;
    private WorldGuardHook worldGuardHook;
    private PlaceholderHook placeholderHook;
    private JudgmentManager judgmentManager;
    private BossBarManager bossBarManager;
    private StatsManager statsManager;
    private MessageManager messageManager;
    
    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);
        
        saveDefaultConfig();
        
        worldGuardHook = new WorldGuardHook();
        if (!worldGuardHook.setup()) {
            getLogger().log(Level.SEVERE, "WorldGuard не найден! Плагин отключается.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        messageManager = new MessageManager(this);
        statsManager = new StatsManager(this);
        judgmentManager = new JudgmentManager(this);
        bossBarManager = new BossBarManager(this);
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderHook = new PlaceholderHook(this);
            placeholderHook.register();
            getLogger().info("PlaceholderAPI поддержка включена!");
        } else {
            getLogger().warning("PlaceholderAPI не найден! Заполнители не будут работать.");
        }
        
        getCommand("qqjudgment").setExecutor(new JudgmentCommand(this));
        registerListeners();
        
        getLogger().info("QQJudgment успешно загружен! Автор: AllF1RE");
    }
    
    private void registerListeners() {
        try {
            new com.allfire.qqjudgment.listeners.PVPListener(this);
            new com.allfire.qqjudgment.listeners.PlayerActionListener(this);
            
            if (getConfig().getBoolean("mob-spawning.enabled", false)) {
                new com.allfire.qqjudgment.listeners.MobSpawnListener(this);
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Не удалось зарегистрировать слушателей", e);
        }
    }
    
    @Override
    public void onDisable() {
        if (judgmentManager != null && judgmentManager.isJudgmentActive()) {
            judgmentManager.stopJudgment(false);
        }
        if (placeholderHook != null) {
            placeholderHook.unregister();
        }
        if (adventure != null) {
            adventure.close();
        }
        getLogger().info("QQJudgment выключен");
    }
    
    public static QQJudgment getInstance() {
        return instance;
    }
    
    public BukkitAudiences getAdventure() {
        return adventure;
    }
    
    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }
    
    public JudgmentManager getJudgmentManager() {
        return judgmentManager;
    }
    
    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }
    
    public StatsManager getStatsManager() {
        return statsManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }
}
