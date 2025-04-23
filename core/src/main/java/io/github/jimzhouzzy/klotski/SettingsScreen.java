package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;
import java.util.Map;

public class SettingsScreen implements Screen {

    private final Klotski klotski;
    private Stage stage;
    private Skin skin;
    private boolean isDarkMode = false; // Default to light mode
    private static final String SETTINGS_FILE = "settings.json";

    public SettingsScreen(final Klotski klotski) {
        this.klotski = klotski;
        loadSettings(); // Load settings from file
        create();
    }

    public void create() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Pixmap clickedPixmap = new Pixmap(Gdx.files.internal("assets/image/clicked.png"));

                Pixmap resizedClickedPixmap = new Pixmap(32, 32, clickedPixmap.getFormat());
                resizedClickedPixmap.drawPixmap(clickedPixmap,
                        0, 0, clickedPixmap.getWidth(), clickedPixmap.getHeight(),
                        0, 0, resizedClickedPixmap.getWidth(), resizedClickedPixmap.getHeight());

                int xHotspot = 7, yHotspot = 1;
                Cursor clickedCursor = Gdx.graphics.newCursor(resizedClickedPixmap, xHotspot, yHotspot);
                resizedClickedPixmap.dispose();
                clickedPixmap.dispose();
                Gdx.graphics.setCursor(clickedCursor);

                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Pixmap clickedPixmap = new Pixmap(Gdx.files.internal("assets/image/cursor.png"));

                Pixmap resizedClickedPixmap = new Pixmap(32, 32, clickedPixmap.getFormat());
                resizedClickedPixmap.drawPixmap(clickedPixmap,
                        0, 0, clickedPixmap.getWidth(), clickedPixmap.getHeight(),
                        0, 0, resizedClickedPixmap.getWidth(), resizedClickedPixmap.getHeight());

                int xHotspot = 7, yHotspot = 1;
                Cursor clickedCursor = Gdx.graphics.newCursor(resizedClickedPixmap, xHotspot, yHotspot);
                resizedClickedPixmap.dispose();
                clickedPixmap.dispose();
                Gdx.graphics.setCursor(clickedCursor);
            }
        });

        // Load the skin for UI components
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));

        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title label
        Label titleLabel = new Label("Settings", skin, "title");
        titleLabel.setFontScale(1.5f);
        table.add(titleLabel).padBottom(50).row();

        // Add a checkbox for light/dark mode
        CheckBox darkModeCheckBox = new CheckBox("Dark Mode", skin);
        darkModeCheckBox.setChecked(isDarkMode);
        darkModeCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (darkModeCheckBox.isChecked()) {
                    isDarkMode = true;
                    klotski.klotskiTheme = KlotskiTheme.DARK;
                    klotski.updateMainScreenColors();
                    Gdx.app.log("Settings", "Dark mode enabled");
                } else {
                    isDarkMode = false;
                    klotski.klotskiTheme = KlotskiTheme.LIGHT;
                    klotski.updateMainScreenColors();
                    Gdx.app.log("Settings", "Light mode enabled");
                }
                saveSettings();
            }

        });
        table.add(darkModeCheckBox).padBottom(20).row();

        // Add a "Back" button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.setScreen(new MainScreen(klotski)); // Navigate back to the main screen
                saveSettings();
            }
        });
        table.add(backButton).width(200).height(50);
    }

    private void saveSettings() {
        // Save settings to a JSON file
        Map<String, Object> settings = new HashMap<>();
        settings.put("isDarkMode", isDarkMode);

        String username = klotski.getLoggedInUser();
        if (username == null || username.isEmpty()) {
            username = "guest";
        }
        settings.put("username", username);

        Json json = new Json();
        FileHandle file = Gdx.files.local(SETTINGS_FILE);
        file.writeString(json.prettyPrint(settings), false);

        Gdx.app.log("Settings", "Settings saved for user: " + username);
    }

    private Map<String, Object> getDefaultSettings() {
        Map<String, Object> defaultSettings = new HashMap<>();
        defaultSettings.put("isDarkMode", false); // Default to light mode
        defaultSettings.put("username", "guest"); // Default username
        return defaultSettings;
    }

    private void loadSettings() {
        // Load settings from a JSON file
        FileHandle file = Gdx.files.local(SETTINGS_FILE);
        Map<String, Object> settings;

        if (!file.exists()) {
            Gdx.app.log("Settings", "No settings file found. Using default settings.");
            settings = getDefaultSettings();
        } else {
            Json json = new Json();
            settings = json.fromJson(HashMap.class, file.readString());
        }

        // Apply settings
        String username = klotski.getLoggedInUser();
        if (username != null && username.equals(settings.getOrDefault("username", "guest"))) {
            isDarkMode = (boolean) settings.getOrDefault("isDarkMode", getDefaultSettings().get("isDarkMode"));
            Gdx.app.log("Settings", "Settings loaded for user: " + username);
        } else {
            Gdx.app.log("Settings", "No settings found for user: " + username + ". Using default settings.");
            settings = getDefaultSettings();
            isDarkMode = (boolean) settings.get("isDarkMode");
        }

        // Set Klotski.klotskiTheme based on isDarkMode
        if (isDarkMode) {
            klotski.klotskiTheme = KlotskiTheme.DARK;
        } else {
            klotski.klotskiTheme = KlotskiTheme.LIGHT;
        }
    }

    @Override
    public void render(float delta) {
        klotski.setGlClearColor();
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        // Render the stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}