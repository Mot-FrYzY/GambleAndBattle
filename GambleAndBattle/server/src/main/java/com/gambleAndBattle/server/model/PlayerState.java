package com.gambleAndBattle.server.model;

import com.gambleAndBattle.common.model.CharacterStats;

public class PlayerState {

    private final int id;
    private final String username;
    private float x;
    private float y;
    private final CharacterStats stats;
    private long lastMoveTimestamp;

    public PlayerState(int id, String username, float x, float y) {
        this.id = id;
        this.username = username;
        this.x = x;
        this.y = y;
        // Statistiques initiales du personnage (ex: PV: 100, RM: 10, RP: 10, Mana: 50, Endurance: 100)
        this.stats = new CharacterStats(100, 10, 10, 50, 100);
        this.lastMoveTimestamp = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public CharacterStats getStats() { return stats; }
    public long getLastMoveTimestamp() { return lastMoveTimestamp; }
    public void setLastMoveTimestamp(long lastMoveTimestamp) { this.lastMoveTimestamp = lastMoveTimestamp; }
}