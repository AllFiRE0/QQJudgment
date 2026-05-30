package com.allfire.qqjudgment.hooks;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class WorldGuardHook {
    
    private WorldGuardPlugin worldGuardPlugin;
    private WorldGuard worldGuard;
    private boolean hooked = false;
    
    public boolean setup() {
        try {
            worldGuardPlugin = JavaPlugin.getPlugin(WorldGuardPlugin.class);
            worldGuard = WorldGuard.getInstance();
            hooked = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isInBlacklistedRegion(Location location, List<String> blacklistedRegions) {
        if (!hooked) return false;
        
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(location.getWorld());
        if (regionManager == null) return false;
        
        for (ProtectedRegion region : regionManager.getApplicableRegions(location)) {
            if (blacklistedRegions.contains(region.getId())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isPVPAllowed(Location location) {
        if (!hooked) return true;
        
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(location.getWorld());
        if (regionManager == null) return true;
        
        StateFlag.State state = regionManager.getApplicableRegions(location).queryState(null, Flags.PVP);
        return state != StateFlag.State.DENY;
    }
    
    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }
}
