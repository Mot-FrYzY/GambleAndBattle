package com.gambleAndBattle.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.gambleAndBattle.common.model.CharacterStats;
import com.gambleAndBattle.common.network.Packets;

public class PlayerController {

    private float x = 100f, y = 100f;

    // Vitesses de base du personnage (modifiables selon la classe/stats)
    private float baseSpeed = 120f; // Vitesse de marche (pixels/sec)
    private float runMultiplier = 1.8f;
    private float dodgeMultiplier = 4.5f;

    // Gestion de l'esquive (cooldown et durée)
    private boolean isDodging = false;
    private float dodgeTimer = 0f;
    private final float dodgeDuration = 0.25f; // L'esquive dure 0.25s
    private float dodgeDirX = 0f, dodgeDirY = 0f;

    // Coûts en Endurance
    private final float runStaminaCostPerSec = 15f;
    private final int dodgeStaminaCost = 25;

    public void update(float delta, CharacterStats stats, MoveCallback onMove) {
        if (stats.isDead()) return;

        // 1. GESTION DE L'ESQUIVE EN COURS
        if (isDodging) {
            dodgeTimer -= delta;
            float currentSpeed = baseSpeed * dodgeMultiplier;
            x += dodgeDirX * currentSpeed * delta;
            y += dodgeDirY * currentSpeed * delta;

            onMove.send(x, y, dodgeDirX * currentSpeed, dodgeDirY * currentSpeed, Packets.MoveMode.DODGE);

            if (dodgeTimer <= 0) {
                isDodging = false;
            }
            return;
        }

        // 2. LECTURE DES TOUCHES DIRECTIONNELLES
        float dirX = 0f;
        float dirY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.Z) || Gdx.input.isKeyPressed(Input.Keys.UP)) dirY += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) dirY -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.Q) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) dirX -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dirX += 1f;

        // Pas de mouvement
        if (dirX == 0f && dirY == 0f) {
            // Regeneration lente de l'endurance au repos
            stats.restoreStamina((int)(10 * delta));
            return;
        }

        // Normalisation du vecteur pour éviter d'aller plus vite en diagonale
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        dirX /= length;
        dirY /= length;

        // 3. DÉTERMINATION DU MODE (DODGE / RUN / WALK)
        Packets.MoveMode mode = Packets.MoveMode.WALK;
        float speed = baseSpeed;

        // A) DECLENCHEMENT D'UNE ESQUIVE (Touche Espace)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (stats.consumeStamina(dodgeStaminaCost)) {
                isDodging = true;
                dodgeTimer = dodgeDuration;
                dodgeDirX = dirX;
                dodgeDirY = dirY;
                return;
            }
        }

        // B) COURSE (Touche Shift Gauche)
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            int staminaToConsume = (int) (runStaminaCostPerSec * delta);
            if (stats.getCurrentStamina() >= staminaToConsume && stats.consumeStamina(staminaToConsume)) {
                mode = Packets.MoveMode.RUN;
                speed *= runMultiplier;
            }
        } else {
            // Régénération d'endurance pendant la marche
            stats.restoreStamina((int)(5 * delta));
        }

        // 4. APPLICATION DU DÉPLACEMENT
        float vx = dirX * speed;
        float vy = dirY * speed;
        x += vx * delta;
        y += vy * delta;

        // Notification / Envoi au réseau
        onMove.send(x, y, vx, vy, mode);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setBaseSpeed(float baseSpeed) { this.baseSpeed = baseSpeed; }

    @FunctionalInterface
    public interface MoveCallback {
        void send(float x, float y, float vx, float vy, Packets.MoveMode mode);
    }
}