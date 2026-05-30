package com.allfire.qqjudgment;

import com.allfire.qqjudgment.commands.JudgmentCommand;
import com.allfire.qqjudgment.hooks.PlaceholderHook;
import com.allfire.qqjudgment.hooks.WorldGuardHook;
import com.allfire.qqjudgment.managers.*;
import com.allfire.qqjudgment.listeners.*;
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
    private MobSpawnListener mobSpawnListener;
    private boolean debug;
    
    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);
        
        saveDefaultConfig();
        
        // Загружаем debug режим из конфига
        debug = getConfig().getBoolean("debug", false);
        
        if (debug) {
            getLogger().info("§e[QQJudgment] Debug режим ВКЛЮЧЕН!");
        }
        
        worldGuardHook = new WorldGuardHook();
        if (!worldGuardHook.setup()) {
            getLogger().log(Level.SEVERE, "WorldGuard не найден! Плагин отключается.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        if (debug) {
            getLogger().info("[QQJudgment] WorldGuard найден и подключен");
        }
        
        messageManager = new MessageManager(this);
        statsManager = new StatsManager(this);
        judgmentManager = new JudgmentManager(this);
        bossBarManager = new BossBarManager(this);
        
        if (debug) {
            getLogger().info("[QQJudgment] Все менеджеры инициализированы");
        }
        
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
            new PVPListener(this);
            if (debug) getLogger().info("[QQJudgment] PVPListener зарегистрирован");
            
            new PlayerActionListener(this);
            if (debug) getLogger().info("[QQJudgment] PlayerActionListener зарегистрирован");
            
            // Всегда создаем слушатель, он сам проверит enabled в своем коде
            mobSpawnListener = new MobSpawnListener(this);
            if (debug) {
                getLogger().info("[QQJudgment] MobSpawnListener зарегистрирован (enabled: " + 
                    getConfig().getBoolean("mob-spawning.enabled", false) + ")");
            }
            
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Не удалось зарегистрировать слушателей", e);
        }
    }
    
    @Override
    public void onDisable() {
        if (debug) {
            getLogger().info("[QQJudgment] Выключение плагина...");
        }
        
        if (judgmentManager != null && judgmentManager.isJudgmentActive()) {
            judgmentManager.stopJudgment(false);
        }
        if (placeholderHook != null) {
            placeholderHook.unregister();
        }
        if (mobSpawnListener != null) {
            mobSpawnListener.stop();
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
    
    public boolean isDebug() {
        return debug;
    }
}
