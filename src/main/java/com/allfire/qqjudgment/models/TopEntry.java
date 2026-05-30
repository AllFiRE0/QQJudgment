package com.allfire.qqjudgment.models;

public class TopEntry {
    private final String name;
    private final int score;
    
    public TopEntry(String name, int score) {
        this.name = name;
        this.score = score;
    }
    
    public String getName() {
        return name;
    }
    
    public int getScore() {
        return score;
    }
}
