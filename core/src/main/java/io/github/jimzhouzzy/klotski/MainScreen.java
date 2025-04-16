package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.jimzhouzzy.klotski.GameModeScreen;
import io.github.jimzhouzzy.klotski.LoginScreen;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class MainScreen implements Screen {

    final Klotski klotski;
    private Stage stage;
    private Skin skin;
    private Label greetingLabel; // Label to display the greeting message

    public MainScreen(final Klotski klotski) {
        this.klotski = klotski;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load the skin for UI components
        skin = new Skin(Gdx.files.internal("skins/default/skin/uiskin.json"));

        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title label
        Label titleLabel = new Label("Klotski Game", skin);
        titleLabel.setFontScale(2); // Make the title larger
        table.add(titleLabel).padBottom(50).row();

        // Add a greeting label
        String username = klotski.getLoggedInUser();
        String greetingText = username != null ? "Welcome, " + username + "!" : "Welcome, Guest!";
        this.greetingLabel = new Label(greetingText, skin);
        greetingLabel.setFontScale(1.5f);
        table.add(greetingLabel).padBottom(30).row();

        // Add a "Play" button
        TextButton playButton = new TextButton("Play", skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.setScreen(new GameModeScreen(klotski)); // Navigate to the GameModeScreen
            }
        });
        table.add(playButton).width(200).height(50).padBottom(20).row();

        // Add a "Login" button
        TextButton loginButton = new TextButton("Login", skin);
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.setScreen(new LoginScreen(klotski)); // Navigate to the LoginScreen
            }
        });
        table.add(loginButton).width(200).height(50).padBottom(20).row();

        // Add an "Exit" button
        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit(); // Exit the application
            }
        });
        table.add(exitButton).width(200).height(50);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK); // Clear the screen with black color
        String username = klotski.getLoggedInUser();
        String greetingText = username != null ? "Welcome, " + username + "!" : "Welcome, Guest!";
        greetingLabel.setText(greetingText); // Update the greeting label
        stage.act(delta); // Update the stage
        stage.draw(); // Draw the stage
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
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage); 
    }
}
