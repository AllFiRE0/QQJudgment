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
        
        // Сохранение конфига
        saveDefaultConfig();
        
        // Инициализация хуков
        worldGuardHook = new WorldGuardHook();
        if (!worldGuardHook.setup()) {
            getLogger().log(Level.SEVERE, "WorldGuard не найден! Плагин отключается.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Инициализация менеджеров
        messageManager = new MessageManager(this);
        statsManager = new StatsManager(this);
        judgmentManager = new JudgmentManager(this);
        bossBarManager = new BossBarManager(this);
        
        // Регистрация PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderHook = new PlaceholderHook(this);
            placeholderHook.register();
            getLogger().info("PlaceholderAPI поддержка включена!");
        } else {
            getLogger().warning("PlaceholderAPI не найден! Заполнители не будут работать.");
        }
        
        // Регистрация команд
        getCommand("qqjudgment").setExecutor(new JudgmentCommand(this));
        
        // Регистрация слушателей
        registerListeners();
        
        getLogger().info("QQJudgment успешно загружен! Автор: AllF1RE");
    }
    
    private void registerListeners() {
        try {
            Class.forName("com.allfire.qqjudgment.listeners.PVPListener")
                    .getConstructor(QQJudgment.class)
                    .newInstance(this);
            Class.forName("com.allfire.qqjudgment.listeners.PlayerActionListener")
                    .getConstructor(QQJudgment.class)
                    .newInstance(this);
            
            if (getConfig().getBoolean("mob-spawning.enabled", false)) {
                Class.forName("com.allfire.qqjudgment.listeners.MobSpawnListener")
                        .getConstructor(QQJudgment.class)
                        .newInstance(this);
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
