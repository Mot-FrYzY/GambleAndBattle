package com.gambleAndBattle.server.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.gambleAndBattle.common.network.Packets;
import com.gambleAndBattle.server.model.PlayerState;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {

    private final Server server;
    private final Map<Integer, PlayerState> players = new ConcurrentHashMap<>();

    // Constantes de validation des vitesses max (marge de tolérance réseau incluse)
    private static final float BASE_WALK_SPEED = 120f;
    private static final float RUN_MULTIPLIER = 1.8f;
    private static final float DODGE_MULTIPLIER = 4.5f;
    private static final float TOLERANCE_FACTOR = 1.25f; // Marge pour le lag / ping

    public GameServer() {
        this.server = new Server();
        Packets.register(server.getKryo());
        setupListeners();
    }

    public void start(int tcpPort, int udpPort) throws IOException {
        server.start();
        server.bind(tcpPort, udpPort);
        System.out.println("[SERVEUR] Écoute démarrée sur TCP:" + tcpPort + " | UDP:" + udpPort);
    }

    private void setupListeners() {
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("[SERVEUR] Nouveau client connecté (ID Connection: " + connection.getID() + ")");
                // Création d'un joueur par défaut au spawn (100, 100)
                PlayerState newPlayer = new PlayerState(connection.getID(), "Joueur_" + connection.getID(), 100f, 100f);
                players.put(connection.getID(), newPlayer);

                // Notifier le nouveau joueur et les autres
                server.sendToAllExceptUDP(connection.getID(),
                        new Packets.PlayerConnect(newPlayer.getId(), newPlayer.getUsername(), newPlayer.getX(), newPlayer.getY()));
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("[SERVEUR] Client déconnecté (ID: " + connection.getID() + ")");
                players.remove(connection.getID());
                server.sendToAllUDP(new Packets.PlayerDisconnect(connection.getID()));
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Packets.PlayerMove movePacket) {
                    handlePlayerMove(connection, movePacket);
                }
            }
        });
    }

    private void handlePlayerMove(Connection connection, Packets.PlayerMove move) {
        PlayerState player = players.get(connection.getID());
        if (player == null || player.getStats().isDead()) return;

        long now = System.currentTimeMillis();
        float deltaTime = Math.max(0.001f, (now - player.getLastMoveTimestamp()) / 1000f);
        player.setLastMoveTimestamp(now);

        // 1. CALCUL DE LA VITESSE MAX AUTORISÉE SELON LE MODE
        float maxAllowedSpeed = BASE_WALK_SPEED;
        if (move.mode == Packets.MoveMode.RUN) {
            maxAllowedSpeed *= RUN_MULTIPLIER;
            // Consommation d'endurance pour la course
            int staminaCost = (int) (15f * deltaTime);
            if (!player.getStats().consumeStamina(staminaCost)) {
                // Pas assez d'endurance -> Rétrogradation forcée en Marche
                move.mode = Packets.MoveMode.WALK;
                maxAllowedSpeed = BASE_WALK_SPEED;
            }
        } else if (move.mode == Packets.MoveMode.DODGE) {
            maxAllowedSpeed *= DODGE_MULTIPLIER;
        } else {
            // Regeneration lente d'endurance à la marche
            player.getStats().restoreStamina((int) (5f * deltaTime));
        }

        // 2. VÉRIFICATION DE LA DISTANCE PARCOURUE (Anti-Speedhack)
        float dx = move.x - player.getX();
        float dy = move.y - player.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float maxDistanceAllowed = maxAllowedSpeed * deltaTime * TOLERANCE_FACTOR;

        if (distance <= maxDistanceAllowed) {
            // Mouvement Valide : Mise à jour de la position officielle
            player.setX(move.x);
            player.setY(move.y);

            // Re-diffusion de la position validée à tous les autres joueurs
            move.playerId = player.getId();
            server.sendToAllExceptUDP(connection.getID(), move);
        } else {
            // Mouvement Suspect/Invalide : On force le client à se recentrer
            System.out.println("[SERVEUR] Déplacement suspect détecté pour " + player.getUsername() + " (Distance: " + distance + " > Max: " + maxDistanceAllowed + ")");
            server.sendToUDP(connection.getID(), new Packets.PlayerMove(
                    player.getId(), player.getX(), player.getY(), 0f, 0f, Packets.MoveMode.WALK
            ));
        }
    }

    public void stop() {
        server.stop();
    }
}