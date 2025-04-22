package io.github.jimzhouzzy.klotski;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class MainScreen implements Screen {

    final Klotski klotski;
    private int frameCount;
    private int frameCountOffset;
    private Stage stage;
    private Skin skin;
    private Label greetingLabel; // Label to display the greeting message
    private ShapeRenderer shapeRenderer;
    private float offsetX; // Offset for translation animation
    private float offsetY;
    private float offsetZ;
    private Random random; 
    public Map<String, Color> colorCache; // Cache for storing colors
    private boolean moveForward;
    private boolean moveBackward;
    private boolean moveLeft;
    private boolean moveRight;
    private boolean moveShifted;
    private boolean moveUpward;
    private boolean moveDownward;
    public Color[] colorList; // Predefined list of colors

    public MainScreen(final Klotski klotski) {
        this.klotski = klotski;
        random = new Random();

        moveForward = false;
        moveBackward = false;
        moveRight = false;
        moveLeft = false;
        moveShifted = false;
        moveUpward = false;
        moveDownward = false;
        
        offsetX = 0f;
        offsetY = 0f;
        offsetZ = 0f;

        frameCount = 0;
        frameCountOffset = random.nextInt(10000); // Random offset for the frame count

        create();
    }

    public void create() {
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.W: // Move forward
                    case Input.Keys.UP:
                        moveForward = true;
                        break;
                    case Input.Keys.S: // Move backward
                    case Input.Keys.DOWN:
                        moveBackward = true;
                        break;
                    case Input.Keys.A: // Move left
                    case Input.Keys.LEFT:
                        moveLeft = true;
                        break;
                    case Input.Keys.D: // Move right
                    case Input.Keys.RIGHT:
                        moveRight = true;
                        break;
                    case Input.Keys.SHIFT_LEFT:
                        moveShifted = true;
                        break;
                    case Input.Keys.CONTROL_LEFT:
                        moveDownward = true;
                        break;
                    case Input.Keys.SPACE:
                        moveUpward = true;
                        break;
                }
                return true; // Indicate the event was handled
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.W:
                    case Input.Keys.UP:
                        moveForward = false;
                        break;
                    case Input.Keys.S:
                    case Input.Keys.DOWN:
                        moveBackward = false;
                        break;
                    case Input.Keys.A:
                    case Input.Keys.LEFT:
                        moveLeft = false;
                        break;
                    case Input.Keys.D:
                    case Input.Keys.RIGHT:
                        moveRight = false;
                        break;
                    case Input.Keys.SHIFT_LEFT:
                        moveShifted = false;
                        break;
                    case Input.Keys.CONTROL_LEFT:
                        moveDownward = false;
                        break;
                    case Input.Keys.SPACE:
                        moveUpward = false;
                        break;
                }
                return true;
            }
        });

        // Initialize ShapeRenderer
        shapeRenderer = new ShapeRenderer();
        colorCache = new HashMap<>();
        
        // Load colors
        loadColors();

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

        // Add a "Settings" button
        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.setScreen(klotski.settingsScreen); // Navigate to the SettingsScreen
            }
        });
        table.add(settingsButton).width(200).height(50).padBottom(20).row();

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
        frameCount ++;
        frameCount %= Integer.MAX_VALUE; // Avoid overflow

        // Clear the screen and set the background to light blue
        ScreenUtils.clear(klotski.getBackgroundColor());

        float moveSpeed = 200.0f;
        if (moveShifted) {
            moveSpeed = 3 * moveSpeed;
        }
        if (moveForward) {
            offsetY += delta * moveSpeed;
        }
        if (moveBackward) {
            offsetY -= delta * moveSpeed;
        }
        if (moveRight) {
            offsetX += delta * moveSpeed;
        }
        if (moveLeft) {
            offsetX -= delta * moveSpeed;
        }
        if (moveUpward){
            offsetZ += 3 * delta * moveSpeed;
        }
        if (moveDownward) {
            offsetZ -= 3 * delta * moveSpeed;
        }

        // Update offsets for diagonal translation animation (45-degree movement)
        // offsetX += delta * 20; // Move 20 pixels per second horizontally
        offsetY += delta * 50; // Move 50 pixels per second vertically

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float baseTileSize = 50; // Base tile size
    
        float focalLength = 300.0f * (1 - 0.2f * (float) veryComplexFunction((frameCount + frameCountOffset) / 5000f)); // Focal length for perspective projection
        float centerX = screenWidth / 2f * (1 - 0.2f * (float) veryComplexFunction((frameCount + frameCountOffset) / 5000f));
        float centerZ = (screenHeight + offsetZ) / 3.75f * (1 - 0.5f * (float) veryComplexFunction((frameCount + frameCountOffset) / 5000f)); // Center Y position for perspective projection

        for (float y = -offsetY - 2 * baseTileSize; y < screenHeight + 15 * baseTileSize; y += baseTileSize) {
            for (float x = -offsetX - 9 * baseTileSize; x <= screenWidth + 10 * baseTileSize; x += baseTileSize) {
                if (x < 0 - 10 * baseTileSize|| x > screenWidth + 10 * baseTileSize || y < 0 - 1 * baseTileSize || y > screenHeight + 15 * baseTileSize) {
                    continue; // Skip tiles outside the screen
                }

                // Calculate the projected positions for the four corners of the tile
                Vector2 tl = projectPerspective(x, y, focalLength, centerX, centerZ);
                Vector2 tr = projectPerspective(x + baseTileSize, y, focalLength, centerX, centerZ);
                Vector2 bl = projectPerspective(x, y + baseTileSize, focalLength, centerX, centerZ);
                Vector2 br = projectPerspective(x + baseTileSize, y + baseTileSize, focalLength, centerX, centerZ);
        
                // Generate a unique key for the current tile
                String key = (int) ((int)Math.floor(x / baseTileSize) + (int) Math.floor(offsetX / baseTileSize)) + "," + (int) ((int) Math.floor(y / baseTileSize) + (int) Math.floor(offsetY / baseTileSize));

                // Get or generate a random color for the tile
                Color tileColor = colorCache.computeIfAbsent(key, k -> new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1f));
                if (klotski.klotskiTheme == klotski.klotskiTheme.DARK) {
                    tileColor = new Color(tileColor.r * 0.65f, tileColor.g * 0.65f, tileColor.b * 0.65f, 1f);
                }
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
    private Vector2 projectPerspective(float x, float y, float focal, float cx, float cy) {
        float scale = focal / (focal + y);
        float screenX = cx + (x - cx) * scale;
        float screenY = cy + (y - cy) * scale;
        return new Vector2(screenX, screenY);
    }

    // A very complex function in [0, 1] to shake the camera
    private static double veryComplexFunction(double x) {
        double term1 = 0.5 * (Math.sin(5 * Math.PI * x) * Math.cos(3 * Math.PI * x * x) + 1);
        double term2 = 0.1 * Math.sin(20 * Math.PI * x);
        return term1 * Math.exp(-x) + term2;
    }

    private static double veryComplexFunction(float x) {
        double temp = (double) x;
        return veryComplexFunction(temp);
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
        shapeRenderer.dispose();
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
    
    public void loadColors() {
        // Predefined list of colors
        colorCache.clear();
        
        // Curently just get Light color, and dark is adopted when rendering
        colorList = klotski.getMainScreenLightColorList();

        for (int i = -100; i <= 100; i++) {
            for (int j = -100; j <= 100; j++) {
                String key = i + "," + j;
                if (!colorCache.containsKey(key)) {
                    Color chosenColor = colorList[random.nextInt(colorList.length)];
                    colorCache.put(key, chosenColor);
                }
            }
        }
    }
}
