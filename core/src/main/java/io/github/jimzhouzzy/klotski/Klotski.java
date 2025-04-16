package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.jimzhouzzy.klotski.GameWebSocketServer;

import java.io.*;

public class Klotski extends Game {
	private GameWebSocketServer webSocketServer;

    private static final String LOGIN_STATUS_FILE = "login_status.dat"; // File to store login status
    public SpriteBatch batch;
    public BitmapFont font;
    public FitViewport viewport;
    public GameScreen gameScreen;
    public MainScreen mainScreen;

    private String loggedInUser; // Field to store the logged-in user's name

    public void create() {
        batch = new SpriteBatch();
        // use libGDX's default font
        font = new BitmapFont();
        viewport = new FitViewport(8, 5);
        
        // font has 15pt, but we need to scale it to our viewport by ratio of viewport height to screen height 
        font.setUseIntegerPositions(false);
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());

        // Load the last logged-in user
        loadLoginStatus();

        this.mainScreen = new MainScreen(this);
        this.gameScreen = new GameScreen(this);
        
        this.setScreen(mainScreen);

		webSocketServer = new GameWebSocketServer(8014);
        webSocketServer.start();
    }

    public String getLoggedInUser() {
        return loggedInUser; // Return the logged-in user's name
    }

    public void setLoggedInUser(String username) {
        this.loggedInUser = username; // Set the logged-in user's name
        saveLoginStatus(); // Save the login status whenever it changes
    }

    private void loadLoginStatus() {
        File file = new File(Gdx.files.getLocalStoragePath(), LOGIN_STATUS_FILE);
        if (!file.exists()) {
            return; // No login status file exists yet
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            loggedInUser = reader.readLine(); // Read the logged-in username
        } catch (IOException e) {
            System.err.println("Failed to load login status: " + e.getMessage());
        }
    }

    private void saveLoginStatus() {
        File file = new File(Gdx.files.getLocalStoragePath(), LOGIN_STATUS_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(loggedInUser != null ? loggedInUser : ""); // Save the username or an empty string
        } catch (IOException e) {
            System.err.println("Failed to save login status: " + e.getMessage());
        }
    }

    public void render() {
        super.render();
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
		try {
            webSocketServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public GameWebSocketServer getWebSocketServer() {
        return webSocketServer;
    }
}