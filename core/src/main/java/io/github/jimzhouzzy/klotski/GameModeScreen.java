package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class GameModeScreen implements Screen {

    private final Klotski klotski;
    private final Stage stage;
    private final Skin skin;

    public GameModeScreen(final Klotski klotski) {
        this.klotski = klotski;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load the skin for UI components
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));

        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title label
        Label titleLabel = new Label("Choose Game Mode", skin);
        titleLabel.setFontScale(2);
        table.add(titleLabel).padBottom(50).row();

        // Add "Free Game" button
        TextButton freeGameButton = new TextButton("Free-Style", skin);
        freeGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.gameScreen.setGameMode(false); // Set to Free Game mode
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
            }
        });
        table.add(freeGameButton).width(300).height(50).padBottom(20).row();

        // Add "3min-Attack" button
        TextButton attackModeButton = new TextButton("3min-Attack", skin);
        attackModeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.gameScreen.setGameMode(true); // Set to 3min-Attack mode
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
            }
        });
        table.add(attackModeButton).width(300).height(50).padBottom(20).row();

        // Add "Level 1" button
        TextButton level1Button = new TextButton("Level 1", skin);
        level1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
                klotski.gameScreen.setGameMode(false); // Set to Free Game mode
                klotski.gameScreen.randomShuffle(10101L); // Shuffle with seed for Level 1
            }
        });
        table.add(level1Button).width(300).height(50).padBottom(20).row();

        // Add "Level 2" button
        TextButton level2Button = new TextButton("Level 2", skin);
        level2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.setScreen(klotski.gameScreen); // Navigate to the game screen
                klotski.gameScreen.setGameMode(false); // Set to Free Game mode
                klotski.gameScreen.randomShuffle(10102L); // Shuffle with seed for Level 2
            }
        });
        table.add(level2Button).width(300).height(50).padBottom(20).row();

        // Add "Back" button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.setScreen(klotski.mainScreen); // Navigate back to the main screen
            }
        });
        table.add(backButton).width(300).height(50);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(new Color(0.68f, 0.85f, 0.9f, 1));
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
    public void pause() {}

    @Override
    public void resume() {}
}