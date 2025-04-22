package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.Color;

import io.github.jimzhouzzy.klotski.GameWebSocketServer;

import java.io.*;

enum KlotskiTheme {
    DARK,
    LIGHT
}

public class Klotski extends Game {
	private GameWebSocketServer webSocketServer;

    private static final String LOGIN_STATUS_FILE = "login_status.dat"; // File to store login status
    public SpriteBatch batch;
    public BitmapFont font;
    public FitViewport viewport;
    public GameScreen gameScreen;
    public MainScreen mainScreen;
    public WebServer webServer;
    public SettingsScreen settingsScreen;

    public KlotskiTheme klotskiTheme;
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

        // After the user loading, settings screen must come first to load settings
        this.settingsScreen = new SettingsScreen(this);
        this.mainScreen = new MainScreen(this);
        this.gameScreen = new GameScreen(this);
        
        this.setScreen(mainScreen);

		webSocketServer = new GameWebSocketServer(8014);
        webSocketServer.start();

        try {
            webServer = new WebServer(8013);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        webSocketServer.close();
        webServer.close();
    }

	public GameWebSocketServer getWebSocketServer() {
        return webSocketServer;
    }

    public void setGlClearColor() {
        if (klotskiTheme == KlotskiTheme.LIGHT)
            Gdx.gl.glClearColor(0.68f, 0.85f, 0.9f, 1);
        else
            Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1);
    }

    public Color getBackgroundColor() {
        if (klotskiTheme == KlotskiTheme.LIGHT)
            return new Color(0.68f, 0.85f, 0.9f, 1);
        else
            return new Color(0.25f, 0.25f, 0.25f, 1);
    }

    public Color[] getMainScreenColorList() {
        if (this.klotskiTheme == KlotskiTheme.LIGHT) {
            Color[] colorList = {
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK,
                Color.GRAY
            };
            return colorList;
        } else {
            Color[] colorList = {
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK,
                Color.GRAY
            };
            for (int i = 0; i < colorList.length; i++) {
                colorList[i].r = 0.5f * colorList[i].r;
                colorList[i].g = 0.5f * colorList[i].g;
                colorList[i].b = 0.5f * colorList[i].b;
            }
            return colorList;
        }
    }

    public Color[] getMainScreenLightColorList() {
        Color[] colorList = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK,
            Color.GRAY
        };
        return colorList;
    }

    public void updateMainScreenColors() {
        mainScreen.loadColors();
    }
}