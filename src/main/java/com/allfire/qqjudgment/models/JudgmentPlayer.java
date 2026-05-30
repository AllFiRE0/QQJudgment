package com.allfire.qqjudgment.models;

public class JudgmentPlayer {
    private final String name;
    private int playerKills = 0;
    private int mobKills = 0;
    private int deaths = 0;
    
    public JudgmentPlayer(String name) {
        this.name = name;
    }
    
    public void addPlayerKill() {
        playerKills++;
    }
    
    public void addMobKill() {
        mobKills++;
    }
    
    public void addDeath() {
        deaths++;
    }
    
    public String getName() {
        return name;
    }
    
    public int getPlayerKills() {
        return playerKills;
    }
    
    public int getMobKills() {
        return mobKills;
    }
    
    public int getTotalKills() {
        return playerKills + mobKills;
    }
    
    public int getDeaths() {
        return deaths;
    }
}
