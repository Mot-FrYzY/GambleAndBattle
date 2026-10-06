package com.gambleAndBattle.server;

import com.gambleAndBattle.server.network.GameServer;

public class ServerMain {

    public static void main(String[] args) {
        System.out.println("=== Démarrage du Serveur GambleAndBattle (Headless) ===");

        GameServer gameServer = new GameServer();
        try {
            gameServer.start(54555, 54777);

            // Boucle principale headless adaptée aux processeurs basse consommation (Debian Celeron)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[SERVEUR] Arrêt en cours...");
                gameServer.stop();
            }));

            // Main-loop à 30 ticks par seconde pour minimiser l'usage CPU sur Celeron
            long lastTime = System.currentTimeMillis();
            while (true) {
                long now = System.currentTimeMillis();
                long elapsed = now - lastTime;

                if (elapsed < 33) { // ~30 FPS/Ticks
                    Thread.sleep(33 - elapsed);
                }
                lastTime = System.currentTimeMillis();
            }

        } catch (Exception e) {
            System.err.println("[SERVEUR] Erreur fatale : " + e.getMessage());
            e.printStackTrace();
        }
    }
}