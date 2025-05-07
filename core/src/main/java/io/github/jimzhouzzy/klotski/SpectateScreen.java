package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Arrays;

public class SpectateScreen implements Screen {

    private final Klotski klotski;
    private final Stage stage;
    private final Skin skin;
    private final ShapeRenderer shapeRenderer;
    private final String username;

    private final int rows = 5;
    private final int cols = 4;
    private final float cellSize;

    private String[][] boardState;

    public SpectateScreen(final Klotski klotski, String username) {
        this.klotski = klotski;
        this.username = username;

        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        this.skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));
        this.shapeRenderer = new ShapeRenderer();

        // Calculate cell size dynamically based on the screen size
        this.cellSize = Math.min(Gdx.graphics.getWidth() / (float) cols, Gdx.graphics.getHeight() / (float) rows);

        // Initialize the board state with empty cells
        this.boardState = new String[rows][cols];
        for (String[] row : boardState) {
            Arrays.fill(row, ".");
        }

        // Create a root table for layout
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // Add a title label
        Label titleLabel = new Label("Spectating: " + username, skin);
        titleLabel.setFontScale(2);
        rootTable.add(titleLabel).padBottom(20).row();

        // Connect to WebSocket to receive board updates
        connectWebSocket();
    }

    private void connectWebSocket() {
        // Connect to the WebSocket server
        GameWebSocketClient webSocketClient = klotski.getWebSocketClient();
        if (webSocketClient == null || !webSocketClient.isConnected()) {
            System.err.println("WebSocket client is not connected.");
            return;
        }

        webSocketClient.setOnMessageListener(message -> {
            if (message.startsWith("Board state updated:") && message.contains(username + ":")) {
                String state = message
                        .replace("Board state updated:", "")
                        .replace(username + ":", "")
                        .trim();

                // Parse the board state
                String[] rows = state.split("\n");
                for (int i = 0; i < this.rows; i++) {
                    String[] cells = rows[i].trim().split(" ");
                    System.arraycopy(cells, 0, boardState[i], 0, this.cols);
                }

                // Redraw the board
                Gdx.app.postRunnable(() -> stage.act(Gdx.graphics.getDeltaTime()));
            }
        });
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        // Draw the board
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String cell = boardState[row][col];
                Color color = getColorForCell(cell);
                shapeRenderer.setColor(color);

                float x = col * cellSize;
                float y = (rows - row - 1) * cellSize; // Invert y-axis
                shapeRenderer.rect(x, y, cellSize, cellSize);
            }
        }
        shapeRenderer.end();

        // Draw grid lines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        for (int row = 0; row <= rows; row++) {
            float y = row * cellSize;
            shapeRenderer.line(0, y, cols * cellSize, y);
        }
        for (int col = 0; col <= cols; col++) {
            float x = col * cellSize;
            shapeRenderer.line(x, 0, x, rows * cellSize);
        }
        shapeRenderer.end();

        // Draw the stage (for UI elements)
        stage.act(delta);
        stage.draw();
    }

    private Color getColorForCell(String cell) {
        switch (cell) {
            case "G":
                return Color.GREEN; // Green for generals
            case "C":
                return Color.BLUE; // Blue for Cao Cao
            case "Y":
                return Color.YELLOW; // Yellow for soldiers
            case "S":
                return Color.GRAY; // Gray for empty spaces
            default:
                return Color.WHITE; // Default to white
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        shapeRenderer.dispose();
    }
}