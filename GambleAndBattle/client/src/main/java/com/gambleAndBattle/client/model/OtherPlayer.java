package com.gambleAndBattle.client.model;

import com.badlogic.gdx.math.MathUtils;
import com.gambleAndBattle.common.network.Packets;

public class OtherPlayer {

    private final int id;
    private final String username;

    // Position réelle (affichée)
    private float x;
    private float y;

    // Position cible (reçue du serveur)
    private float targetX;
    private float targetY;

    private Packets.MoveMode currentMode = Packets.MoveMode.WALK;

    public OtherPlayer(int id, String username, float x, float y) {
        this.id = id;
        this.username = username;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
    }

    /**
     * Reçoit une mise à jour de position du serveur.
     */
    public void updateTargetPosition(float newX, float newY, Packets.MoveMode mode) {
        this.targetX = newX;
        this.targetY = newY;
        this.currentMode = mode;
    }

    /**
     * Interpole progressivement la position du joueur vers sa position cible.
     */
    public void update(float delta) {
        // Vitesse d'interpolation (plus la valeur est haute, plus c'est réactif)
        float lerpFactor = 15f * delta;
        this.x = MathUtils.lerp(this.x, this.targetX, lerpFactor);
        this.y = MathUtils.lerp(this.y, this.targetY, lerpFactor);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public float getX() { return x; }
    public float getY() { return y; }
    public Packets.MoveMode getCurrentMode() { return currentMode; }
}