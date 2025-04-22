package io.github.jimzhouzzy.klotski;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
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
    private Map<String, Color> colorCache; // Cache for storing colors

    public MainScreen(final Klotski klotski) {
        this.klotski = klotski;
        create();
    }

    public void create() {
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Initialize ShapeRenderer
        shapeRenderer = new ShapeRenderer();
        random = new Random();
        colorCache = new HashMap<>();
        
        // Predefined list of colors
        Color[] colorList = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK,
            Color.GRAY
        };

        for (int i = -100; i <= 100; i++) {
            for (int j = -100; j <= 100; j++) {
                String key = i + "," + j;
                if (!colorCache.containsKey(key)) {
                    Color chosenColor = colorList[random.nextInt(colorList.length)];
                    colorCache.put(key, chosenColor);
                }
            }
        }

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

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float baseTileSize = 50; // Base tile size
    
        float focalLength = 300; // Focal length for perspective projection
        float centerX = screenWidth / 2f;
        float centerY = screenHeight / 3f; // Center Y position for perspective projection

        for (float y = -offsetY - 10 * baseTileSize; y < screenHeight + 10 * baseTileSize; y += baseTileSize) {
            for (float x = -offsetX - 10 * baseTileSize; x <= screenWidth + 10 * baseTileSize; x += baseTileSize) {
                if (x < 0 - 10 * baseTileSize|| x > screenWidth + 10 * baseTileSize || y < 0 - 1 * baseTileSize || y > screenHeight + 5 * baseTileSize) {
                    continue; // Skip tiles outside the screen
                }

                // Calculate the projected positions for the four corners of the tile
                Vector2 tl = projectPerspective(x, y, focalLength, centerX, centerY);
                Vector2 tr = projectPerspective(x + baseTileSize, y, focalLength, centerX, centerY);
                Vector2 bl = projectPerspective(x, y + baseTileSize, focalLength, centerX, centerY);
                Vector2 br = projectPerspective(x + baseTileSize, y + baseTileSize, focalLength, centerX, centerY);
        
                // Draw the tile using the projected positions
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.line(tl.x, tl.y, tr.x, tr.y);
                shapeRenderer.line(tr.x, tr.y, br.x, br.y);
                shapeRenderer.line(br.x, br.y, bl.x, bl.y);
                shapeRenderer.line(bl.x, bl.y, tl.x, tl.y);
                shapeRenderer.end();
                
                // Generate a unique key for the current tile
                String key = (int) ((int)Math.floor(x / baseTileSize) + (int) Math.floor(offsetX / baseTileSize)) + "," + (int) ((int) Math.floor(y / baseTileSize) + (int) Math.floor(offsetY / baseTileSize));

                // Get or generate a random color for the tile
                Color tileColor = colorCache.computeIfAbsent(key, k -> new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1f));
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(tileColor);
                shapeRenderer.triangle(tl.x, tl.y, tr.x, tr.y, br.x, br.y);
                shapeRenderer.triangle(br.x, br.y, bl.x, bl.y, tl.x, tl.y);
                shapeRenderer.end();
            }
        }

        // Update the greeting label with the logged-in user's name
        String username = klotski.getLoggedInUser();
        String greetingText = username != null ? "Welcome, " + username + "!" : "Welcome, Guest!";
        greetingLabel.setText(greetingText); // Update the greeting label

        // Render the stage
        stage.act(delta);
        stage.draw();
    }

    // Projection 'Matrix'
    public Vector2 projectPerspective(float x, float y, float focal, float cx, float cy) {
        float scale = focal / (focal + y);
        float screenX = cx + (x - cx) * scale;
        float screenY = cy + (y - cy) * scale;
        return new Vector2(screenX, screenY);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        dispose();
        create();
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
