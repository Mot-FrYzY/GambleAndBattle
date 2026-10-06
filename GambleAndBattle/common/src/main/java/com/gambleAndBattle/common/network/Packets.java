package com.gambleAndBattle.common.network;

import com.esotericsoftware.kryo.Kryo;

/**
 * Définition et enregistrement de tous les paquets réseau échangés
 * entre le client libGDX et le serveur.
 */
public class Packets {

    /**
     * Enregistre toutes les classes de paquets auprès de l'instance Kryo.
     * Doit être appelée à la fois sur le serveur et le client avant toute connexion.
     */
    public static void register(Kryo kryo) {
        // Paquets de connexion / d'authentification
        kryo.register(LoginRequest.class);
        kryo.register(LoginResponse.class);

        // Paquets d'état du joueur et du monde
        kryo.register(PlayerConnect.class);
        kryo.register(PlayerDisconnect.class);
        kryo.register(PlayerMove.class);
        kryo.register(PlayerStatsUpdate.class);

        // Actions de combat / jeu
        kryo.register(PlayerActionRequest.class);
        kryo.register(PlayerActionResponse.class);

        // Dans Packets.register(Kryo kryo)
        kryo.register(com.gambleAndBattle.common.model.CharacterStats.class);
    }

    // ==========================================
    // 1. CONNEXION & AUTHENTIFICATION
    // ==========================================

    public static class LoginRequest {
        public String username;
        public String passwordHash;

        public LoginRequest() {} // Constructeur vide requis par Kryo

        public LoginRequest(String username, String passwordHash) {
            this.username = username;
            this.passwordHash = passwordHash;
        }
    }

    public static class LoginResponse {
        public boolean success;
        public String message;
        public int playerId;

        public LoginResponse() {}

        public LoginResponse(boolean success, String message, int playerId) {
            this.success = success;
            this.message = message;
            this.playerId = playerId;
        }
    }

    // ==========================================
    // 2. GESTION DE LA PRÉSENCE DES JOUEURS
    // ==========================================

    public static class PlayerConnect {
        public int playerId;
        public String username;
        public float x;
        public float y;

        public PlayerConnect() {}

        public PlayerConnect(int playerId, String username, float x, float y) {
            this.playerId = playerId;
            this.username = username;
            this.x = x;
            this.y = y;
        }
    }

    public static class PlayerDisconnect {
        public int playerId;

        public PlayerDisconnect() {}

        public PlayerDisconnect(int playerId) {
            this.playerId = playerId;
        }
    }

    // ==========================================
    // 3. DÉPLACEMENT ET POSITION
    // ==========================================

    public static class PlayerMove {
        public int playerId;
        public float x;
        public float y;
        public float velocityX;
        public float velocityY;

        public PlayerMove() {}

        public PlayerMove(int playerId, float x, float y, float velocityX, float velocityY) {
            this.playerId = playerId;
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }
    }

    // ==========================================
    // 4. STATISTIQUES ET ÉTAT DU PERSONNAGE
    // ==========================================

    public static class PlayerStatsUpdate {
        public int playerId;
        public int health;
        public int maxHealth;
        public int mana;
        public int maxMana;
        public int stamina;
        public int maxStamina;

        public PlayerStatsUpdate() {}

        public PlayerStatsUpdate(int playerId, int health, int maxHealth, int mana, int maxMana, int stamina, int maxStamina) {
            this.playerId = playerId;
            this.health = health;
            this.maxHealth = maxHealth;
            this.mana = mana;
            this.maxMana = maxMana;
            this.stamina = stamina;
            this.maxStamina = maxStamina;
        }
    }

    // ==========================================
    // 5. ACTIONS & COMBAT (Gamble / Battle)
    // ==========================================

    public static class PlayerActionRequest {
        public int actionId; // Identifiant du sort ou de l'action de pari/combat
        public float targetX;
        public float targetY;

        public PlayerActionRequest() {}

        public PlayerActionRequest(int actionId, float targetX, float targetY) {
            this.actionId = actionId;
            this.targetX = targetX;
            this.targetY = targetY;
        }
    }

    public static class PlayerActionResponse {
        public int playerId;
        public int actionId;
        public boolean success;

        public PlayerActionResponse() {}

        public PlayerActionResponse(int playerId, int actionId, boolean success) {
            this.playerId = playerId;
            this.actionId = actionId;
            this.success = success;
        }
    }
}