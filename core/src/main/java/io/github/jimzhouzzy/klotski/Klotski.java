package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.audio.Music;

import io.github.jimzhouzzy.klotski.GameWebSocketServer;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;

import java.io.*;
import java.lang.reflect.Field;

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
    public DynamicBoard dynamicBoard;
    public WebServer webServer;
    public SettingsScreen settingsScreen;
    private Music backgroundMusic;

    public KlotskiTheme klotskiTheme;
    private String loggedInUser; // Field to store the logged-in user's name
    private boolean antialiasingEnabled = true; // Default to enabled
    private boolean vsyncEnabled = true; // Default to enabled
    private Lwjgl3ApplicationConfiguration lwjgl3Config;
    private Skin skin;
    private boolean musicEnabled;
    private boolean isOfflineMode;

    public void create() {
        // Load the music file
        // MUST before load configurations
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sound_fx/soundtrack.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(1f);
        backgroundMusic.play();

        // Load the skin for UI components
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));
        
        batch = new SpriteBatch();
        // use libGDX's default font
        font = new BitmapFont();
        viewport = new FitViewport(8, 5);

        // font has 15pt, but we need to scale it to our viewport by ratio of viewport
        // height to screen height
        font.setUseIntegerPositions(false);
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());

        // Load the last logged-in user
        loadLoginStatus();

        // Cresate dynamic board before screens
        this.dynamicBoard = new DynamicBoard(this, null);

        // After the user loading, settings screen must come first to load settings
        this.settingsScreen = new SettingsScreen(this);
        this.mainScreen = new MainScreen(this);
        this.gameScreen = new GameScreen(this);
        this.setScreen(mainScreen);

        // Start web socket server
        webSocketServer = new GameWebSocketServer(8014);
        webSocketServer.start();

        // Start html web server
        try {
            webServer = new WebServer(8013);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set custom cursor
        Pixmap originalPixmap = new Pixmap(Gdx.files.internal("assets/image/cursor.png"));

        Pixmap resizedPixmap = new Pixmap(32, 32, originalPixmap.getFormat());
        resizedPixmap.drawPixmap(originalPixmap,
                0, 0, originalPixmap.getWidth(), originalPixmap.getHeight(),
                0, 0, resizedPixmap.getWidth(), resizedPixmap.getHeight());

        int xHotspot = 7, yHotspot = 1;
        Cursor cursor = Gdx.graphics.newCursor(resizedPixmap, xHotspot, yHotspot);
        resizedPixmap.dispose();
        originalPixmap.dispose();
        Gdx.graphics.setCursor(cursor);
    }

    public String getLoggedInUser() {
        if (loggedInUser == null) {
            return "Guest"; // If no user is logged in, return "Guest"
        }
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

    public void saveLoginStatus() {
        File file = new File(Gdx.files.getLocalStoragePath(), LOGIN_STATUS_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(loggedInUser != null ? loggedInUser : ""); // Save the username or an empty string
        } catch (IOException e) {
            System.err.println("Failed to save login status: " + e.getMessage());
        }
    }

    public void clearLoginStatus() {
        loggedInUser = null;
        File file = new File(Gdx.files.getLocalStoragePath(), LOGIN_STATUS_FILE);
        if (file.exists()) {
            file.delete(); // Delete the login status file
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

    public boolean isAntialiasingEnabled() {
        return antialiasingEnabled;
    }

    public void setAntialiasingEnabled(boolean enabled, Stage stage) {
        // NO EFFECT NOW!
        this.antialiasingEnabled = enabled;
        updateLwjgl3Config(stage);
    }

    public boolean isVsyncEnabled() {
        return vsyncEnabled;
    }

    public void setVsyncEnabled(boolean enabled, Stage stage) {
        // NO EFFECT NOW!
        this.vsyncEnabled = enabled;
        updateLwjgl3Config(stage);
    }

    public void setLwjgl3Config(Lwjgl3ApplicationConfiguration config) {
        this.lwjgl3Config = config;
    }

    public void updateLwjgl3Config(Stage stage) {
        //// DO NOT USE NOW!
        if (lwjgl3Config != null) {
            boolean needRestart = false;
            // Update VSync
            boolean newVsync = isVsyncEnabled();
            if (newVsync != getVsyncEnabled(lwjgl3Config)){
                System.out.println("VSync enabled: " + newVsync);
                lwjgl3Config.useVsync(newVsync);
                needRestart = true;
            }

            // Update Antialiasing
            int newSamples = isAntialiasingEnabled() ? 4 : 0; // 4x MSAA if enabled, 0 otherwise
            if (newSamples != getBackBufferConfig(lwjgl3Config)[6]) {
                System.out.println("Antialiasing samples: " + newSamples);
                lwjgl3Config.setBackBufferConfig(8, 8, 8, 8, 16, 8, newSamples);
                needRestart = true;
            }
            
            if (needRestart) {
                // restartApplication();
                showErrorDialog("Some changes needs restarting the game to take effect", stage);
            }
        }
    }

    public boolean getVsyncEnabled(Lwjgl3ApplicationConfiguration config) {
        try {
            Field vSyncField = Lwjgl3WindowConfiguration.class.getDeclaredField("vSyncEnabled");
            vSyncField.setAccessible(true); // Make the field accessible
            return vSyncField.getBoolean(config);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false; // Default to false if an error occurs
        }
    }

    public int[] getBackBufferConfig(Lwjgl3ApplicationConfiguration config) {
        try {
            // Access the private fields using reflection
            Field rField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("r");
            Field gField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("g");
            Field bField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("b");
            Field aField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("a");
            Field depthField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("depth");
            Field stencilField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("stencil");
            Field samplesField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("samples");
    
            // Make the fields accessible
            rField.setAccessible(true);
            gField.setAccessible(true);
            bField.setAccessible(true);
            aField.setAccessible(true);
            depthField.setAccessible(true);
            stencilField.setAccessible(true);
            samplesField.setAccessible(true);
    
            // Retrieve the values of the fields
            int r = rField.getInt(config);
            int g = gField.getInt(config);
            int b = bField.getInt(config);
            int a = aField.getInt(config);
            int depth = depthField.getInt(config);
            int stencil = stencilField.getInt(config);
            int samples = samplesField.getInt(config);
    
            // Return the values as an array
            return new int[]{r, g, b, a, depth, stencil, samples};
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null; // Return null if an error occurs
        }
    }

    public void restartApplication() {
        //// DO NOT USE NOW!

        // Save the current configuration
        Lwjgl3ApplicationConfiguration config = lwjgl3Config;

        // Dispose of the current application
        Gdx.app.exit();

        // Restart the application with the updated configuration
        Klotski newKlotski = new Klotski();
        newKlotski.setLwjgl3Config(config);
        new Lwjgl3Application(newKlotski, config);
    }

    private void showErrorDialog(String message, Stage stage) {
        // Create a group to act as the dialog container
        Group dialogGroup = new Group();

        // Create a background for the dialog
        Image background = new Image(skin.newDrawable("white", new Color(1.0f, 1.0f, 1.0f, 0.9f)));
        background.setSize(400, 250);
        background.setPosition((stage.getWidth() - background.getWidth()) / 2, (stage.getHeight() - background.getHeight()) / 2);
        dialogGroup.addActor(background);

        // Create a title label for the dialog
        Label titleLabel = new Label("Error", skin);
        titleLabel.setColor(Color.RED);
        titleLabel.setFontScale(2.0f);
        titleLabel.setPosition(background.getX() + (background.getWidth() - titleLabel.getWidth()) / 2, background.getY() + 180);
        dialogGroup.addActor(titleLabel);

        // Create a label for the error message
        Label messageLabel = new Label(message, skin);
        messageLabel.setColor(Color.BLACK);
        messageLabel.setFontScale(1.5f);
        messageLabel.setWrap(true);
        messageLabel.setWidth(360);
        messageLabel.setPosition(background.getX() + 20, background.getY() + 100);
        dialogGroup.addActor(messageLabel);

        // Create an OK button
        TextButton okButton = new TextButton("OK", skin);
        okButton.setSize(100, 40);
        okButton.setPosition(background.getX() + (background.getWidth() - okButton.getWidth()) / 2, background.getY() + 20);
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialogGroup.remove(); // Remove the dialog when OK is clicked
            }
        });
        dialogGroup.addActor(okButton);

        // Add the dialog group to the stage
        stage.addActor(dialogGroup);
    }

    public void setMusicEnabled(boolean enabled) {
        if (backgroundMusic != null) {
            if (enabled) {
                backgroundMusic.play();
            } else {
                backgroundMusic.stop();
            }
            this.musicEnabled = enabled;
        }
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.isOfflineMode = offlineMode;
    }

    public boolean isOfflineMode() {
        return isOfflineMode;
    }
}