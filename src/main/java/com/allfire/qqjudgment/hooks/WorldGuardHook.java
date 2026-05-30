package com.allfire.qqjudgment.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
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
        
        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(location.getWorld());
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(adaptedWorld);
        if (regionManager == null) return false;
        
        BlockVector3 pos = BukkitAdapter.asBlockVector(location);
        
        for (ProtectedRegion region : regionManager.getApplicableRegions(pos)) {
            if (blacklistedRegions.contains(region.getId())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isPVPAllowed(Location location) {
        if (!hooked) return true;
        
        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(location.getWorld());
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(adaptedWorld);
        if (regionManager == null) return true;
        
        BlockVector3 pos = BukkitAdapter.asBlockVector(location);
        
        StateFlag.State state = regionManager.getApplicableRegions(pos).queryState(null, Flags.PVP);
        return state != StateFlag.State.DENY;
    }
    
    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }
}
