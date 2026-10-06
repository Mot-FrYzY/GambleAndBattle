package com.gambleAndBattle.client.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.gambleAndBattle.common.network.Packets;

import java.io.IOException;

public class NetworkManager {

    private final Client client;
    private boolean connected = false;

    public NetworkManager() {
        this.client = new Client();
        // Enregistrement des paquets définis dans le module common
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
                System.out.println("[CLIENT] Déconnecté du serveur.");
            }

            @Override
            public void received(Connection connection, Object object) {
                // Traitement des paquets reçus du serveur
                if (object instanceof Packets.LoginResponse response) {
                    System.out.println("[CLIENT] Réponse de connexion : " + response.message + " (Succès: " + response.success + ")");
                }
            }
        });
    }

    public void connect(String host, int tcpPort, int udpPort) {
        // Démarrage du thread réseau KryoNet
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
}