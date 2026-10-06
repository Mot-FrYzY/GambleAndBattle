package com.gambleAndBattle.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gambleAndBattle.common.model.CharacterStats;
import com.gambleAndBattle.common.network.Packets;
import com.gambleAndBattle.client.input.PlayerController;
import com.gambleAndBattle.client.model.OtherPlayer;
import com.gambleAndBattle.client.network.NetworkManager;

public class GambleAndBattleClient extends ApplicationAdapter {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private NetworkManager networkManager;
    private PlayerController controller;
    private CharacterStats playerStats;

    // Textures temporaires (placeholders en mémoire)
    private Texture playerTexture;      // Joueur local (Doré)
    private Texture otherPlayerTexture; // Joueurs distants (Corail/Rouge)

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        // 1. Initialisation des données du joueur local
        playerStats = new CharacterStats(100, 10, 10, 50, 100);
        controller = new PlayerController();

        // 2. Initialisation du réseau et connexion au serveur
        networkManager = new NetworkManager();
        networkManager.connect("127.0.0.1", 54555, 54777);

        // 3. Création des textures temporaires sans fichiers externes
        playerTexture = createPlaceholderTexture(32, 32, Color.GOLD);
        otherPlayerTexture = createPlaceholderTexture(32, 32, Color.CORAL);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // --- 1. LOGIQUE & DÉPLACEMENTS ---

        // Joueur local : mise à jour et envoi UDP
        controller.update(delta, playerStats, (x, y, vx, vy, mode) -> {
            networkManager.sendUDP(new Packets.PlayerMove(0, x, y, vx, vy, mode));
        });

        // Joueurs distants : interpolation des positions reçues
        for (OtherPlayer other : networkManager.getOtherPlayers().values()) {
            other.update(delta);
        }

        // --- 2. RENDU GRAPHIQUE DU MONDE ---

        // Effacement de l'écran (fond bleu sombre)
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Rendu des joueurs distants et de leurs pseudos
        for (OtherPlayer other : networkManager.getOtherPlayers().values()) {
            batch.draw(otherPlayerTexture, other.getX(), other.getY());
            font.setColor(Color.WHITE);
            font.draw(batch, other.getUsername(), other.getX() - 5, other.getY() + 45);
        }

        // Rendu du joueur local
        batch.draw(playerTexture, controller.getX(), controller.getY());
        font.setColor(Color.GOLD);
        font.draw(batch, "Moi", controller.getX() + 4, controller.getY() + 45);

        batch.end();

        // --- 3. RENDU DU HUD (UI) ---
        renderHUD();
    }

    /**
     * Dessine l'interface utilisateur (PV, Mana, Endurance, état réseau).
     */
    private void renderHUD() {
        float x = 20f;
        float startY = Gdx.graphics.getHeight() - 30f;
        float barWidth = 200f;
        float barHeight = 16f;
        float spacing = 24f;

        // Activation de la transparence pour les formes
        Gdx.gl.glEnable(GL20.GL_BLEND);

        // Remplissage des jauges
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Barre PV (Rouge)
        drawBar(x, startY, barWidth, barHeight,
                (float) playerStats.getCurrentHp() / playerStats.getMaxHp(),
                Color.FIREBRICK, Color.RED);

        // Barre Mana (Bleu)
        drawBar(x, startY - spacing, barWidth, barHeight,
                (float) playerStats.getCurrentMana() / playerStats.getMaxMana(),
                Color.NAVY, Color.ROYAL);

        // Barre Endurance (Vert)
        drawBar(x, startY - (spacing * 2), barWidth, barHeight,
                (float) playerStats.getCurrentStamina() / playerStats.getMaxStamina(),
                Color.FOREST, Color.LIME);

        shapeRenderer.end();

        // Contours des jauges
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x, startY, barWidth, barHeight);
        shapeRenderer.rect(x, startY - spacing, barWidth, barHeight);
        shapeRenderer.rect(x, startY - (spacing * 2), barWidth, barHeight);
        shapeRenderer.end();

        // Textes et valeurs chiffrées sur le HUD
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "PV: " + playerStats.getCurrentHp() + " / " + playerStats.getMaxHp(), x + 10, startY + 13);
        font.draw(batch, "Mana: " + playerStats.getCurrentMana() + " / " + playerStats.getMaxMana(), x + 10, startY - spacing + 13);
        font.draw(batch, "Endurance: " + playerStats.getCurrentStamina() + " / " + playerStats.getMaxStamina(), x + 10, startY - (spacing * 2) + 13);

        // Indicateur d'état du réseau (haut à droite)
        String status = networkManager.isConnected() ? "Connecté (ID: " + networkManager.getLocalPlayerId() + ")" : "Déconnecté";
        font.setColor(networkManager.isConnected() ? Color.GREEN : Color.RED);
        font.draw(batch, "Réseau: " + status, Gdx.graphics.getWidth() - 220, Gdx.graphics.getHeight() - 15);

        batch.end();
    }

    private void drawBar(float x, float y, float width, float height, float ratio, Color bgColor, Color fillColor) {
        shapeRenderer.setColor(bgColor);
        shapeRenderer.rect(x, y, width, height);

        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, width * Math.max(0f, Math.min(1f, ratio)), height);
    }

    private Texture createPlaceholderTexture(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (otherPlayerTexture != null) otherPlayerTexture.dispose();
        if (networkManager != null) networkManager.stop();
    }
}