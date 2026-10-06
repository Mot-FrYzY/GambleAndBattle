package com.gambleAndBattle.client.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.gambleAndBattle.common.network.Packets;
import com.gambleAndBattle.client.model.OtherPlayer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {

    private final Client client;
    private boolean connected = false;

    // Map thread-safe pour stocker les joueurs distants
    private final Map<Integer, OtherPlayer> otherPlayers = new ConcurrentHashMap<>();

    public NetworkManager() {
        this.client = new Client();
        // Enregistrement des classes Kryo depuis le module common
        Packets.register(client.getKryo());
        setupListeners();
    }

    private void setupListeners() {
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                connected = true;
                System.out.println("[CLIENT] Connecté au serveur (ID: " + connection.getID() + ")");
            }

            @Override
            public void disconnected(Connection connection) {
                connected = false;
                otherPlayers.clear();
                System.out.println("[CLIENT] Déconnecté du serveur.");
            }

            @Override
            public void received(Connection connection, Object object) {
                // 1. Connexion d'un autre joueur
                if (object instanceof Packets.PlayerConnect connect) {
                    if (connect.playerId != client.getID()) {
                        otherPlayers.put(connect.playerId, new OtherPlayer(connect.playerId, connect.username, connect.x, connect.y));
                        System.out.println("[CLIENT] Nouveau joueur connecté : " + connect.username + " (ID: " + connect.playerId + ")");
                    }
                }
                // 2. Déconnexion d'un autre joueur
                else if (object instanceof Packets.PlayerDisconnect disconnect) {
                    otherPlayers.remove(disconnect.playerId);
                    System.out.println("[CLIENT] Joueur déconnecté (ID: " + disconnect.playerId + ")");
                }
                // 3. Déplacement d'un autre joueur
                else if (object instanceof Packets.PlayerMove move) {
                    if (move.playerId != client.getID()) {
                        OtherPlayer other = otherPlayers.get(move.playerId);
                        if (other != null) {
                            other.updateTargetPosition(move.x, move.y, move.mode);
                        } else {
                            // Enregistrement de secours si le paquet PlayerConnect a été manqué
                            otherPlayers.put(move.playerId, new OtherPlayer(move.playerId, "Joueur_" + move.playerId, move.x, move.y));
                        }
                    }
                }
                // 4. Réponse d'authentification / Login
                else if (object instanceof Packets.LoginResponse response) {
                    System.out.println("[CLIENT] Réponse de connexion : " + response.message + " (Succès: " + response.success + ")");
                }
            }
        });
    }

    public void connect(String host, int tcpPort, int udpPort) {
        client.start();
        new Thread(() -> {
            try {
                client.connect(5000, host, tcpPort, udpPort);
            } catch (IOException e) {
                System.err.println("[CLIENT] Impossible de se connecter au serveur : " + e.getMessage());
            }
        }).start();
    }

    public void sendTCP(Object object) {
        if (connected) {
            client.sendTCP(object);
        }
    }

    public void sendUDP(Object object) {
        if (connected) {
            client.sendUDP(object);
        }
    }

    public void stop() {
        client.stop();
    }

    public boolean isConnected() {
        return connected;
    }

    public Map<Integer, OtherPlayer> getOtherPlayers() {
        return otherPlayers;
    }

    public int getLocalPlayerId() {
        return client.getID();
    }
}