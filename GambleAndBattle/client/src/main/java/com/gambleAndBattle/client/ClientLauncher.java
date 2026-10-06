package com.gambleAndBattle.client;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class ClientLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("GambleAndBattle");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        config.setResizable(false);

        new Lwjgl3Application(new GambleAndBattleClient(), config);
    }
}