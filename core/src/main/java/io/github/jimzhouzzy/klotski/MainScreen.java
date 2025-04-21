package io.github.jimzhouzzy.klotski;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    private ShapeRenderer shapeRenderer;
    private float offsetX; // Offset for translation animation
    private float offsetY; // Offset for vertical translation animation
    private Random random; 

    public MainScreen(final Klotski klotski) {
        this.klotski = klotski;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Initialize ShapeRenderer
        shapeRenderer = new ShapeRenderer();
        random = new Random();

        // Load the skin for UI components
        skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));

        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title label
        Label.LabelStyle titleStyle = skin.get("title", Label.LabelStyle.class);
        Label titleLabel = new Label("Klotski Game", titleStyle);
        titleLabel.setFontScale(1.5f); // Make the title larger
        titleLabel.setColor(Color.WHITE); // Set the title color to white
        table.add(titleLabel).padBottom(50).row();

        // Add a greeting label
        Label.LabelStyle narrationStyle = skin.get("narration", Label.LabelStyle.class);
        String username = klotski.getLoggedInUser();
        String greetingText = username != null ? "Welcome, " + username + "!" : "Welcome, Guest!";
        this.greetingLabel = new Label(greetingText, narrationStyle);
        greetingLabel.setFontScale(2.0f);
        greetingLabel.setColor(Color.WHITE); // Set the title color to white
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
        // Clear the screen and set the background to blue
        ScreenUtils.clear(Color.BLUE);

        // Update offsets for diagonal translation animation (45-degree movement)
        offsetX += delta * 50; // Move 50 pixels per second horizontally
        offsetY += delta * 50; // Move 50 pixels per second vertically

        // Loop offsets to avoid infinite growth
        if (offsetX > 100) {
            offsetX -= 100;
        }
        if (offsetY > 100) {
            offsetY -= 100;
        }

        // Draw tiled background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float baseTileSize = 50; // Base tile size
    
        // Shear factors
        float shx = 0.1f; // Shear factor in the x direction
        float shy = 0.1f; // Shear factor in the y direction

        // Iterate through the grid and draw rectangles with perspective effect
        for (float y = -offsetY - 10 * baseTileSize; y < screenHeight + 10 * baseTileSize; y += baseTileSize) {
            for (float x = -offsetX - 10 * baseTileSize; x <= screenWidth + 10 * baseTileSize; x += baseTileSize) {
                // Calculate the four corners of the rectangle with shear transformation
                float topLeftX = x + shx * y;
                float topLeftY = y + shy * x;
                topLeftY = screenHeight * (float) Math.pow(topLeftY / screenHeight, 0.5f);

                float topRightX = (x + baseTileSize) + shx * y;
                float topRightY = y + shy * (x + baseTileSize);
                topRightY = screenHeight * (float) Math.pow(topRightY / screenHeight, 0.5f);

                float bottomLeftX = x + shx * (y + baseTileSize);
                float bottomLeftY = (y + baseTileSize) + shy * x;
                bottomLeftY = screenHeight * (float) Math.pow(bottomLeftY / screenHeight, 0.5f);

                float bottomRightX = (x + baseTileSize) + shx * (y + baseTileSize);
                float bottomRightY = (y + baseTileSize) + shy * (x + baseTileSize);
                bottomRightY = screenHeight * (float) Math.pow(bottomRightY / screenHeight, 0.5f);

                // Draw the rectangle by connecting the four corners
                shapeRenderer.line(topLeftX, topLeftY, topRightX, topRightY); // Top edge
                shapeRenderer.line(topRightX, topRightY, bottomRightX, bottomRightY); // Right edge
                shapeRenderer.line(bottomRightX, bottomRightY, bottomLeftX, bottomLeftY); // Bottom edge
                shapeRenderer.line(bottomLeftX, bottomLeftY, topLeftX, topLeftY); // Left edge
            }
        }

        shapeRenderer.end();

        // Update the greeting label with the logged-in user's name
        String username = klotski.getLoggedInUser();
        String greetingText = username != null ? "Welcome, " + username + "!" : "Welcome, Guest!";
        greetingLabel.setText(greetingText); // Update the greeting label

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
        shapeRenderer.dispose(); // Dispose ShapeRenderer resources
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
