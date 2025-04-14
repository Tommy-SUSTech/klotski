package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.InputProcessor;

import java.util.List;

class RectangleBlock extends Rectangle {
    public float targetX;
    public float targetY;

    public RectangleBlock(float x, float y, float width, float height, float targetX, float targetY) {
        super(x, y, width, height);
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public int[] getGridPosition(float cellSize) {
        int row = 5 - ((int) Math.round((targetY) / (cellSize)) + Math.round(height
                / (cellSize)));
        int col = (int) Math.round(targetX / (cellSize));
        // System.out.println("Block target position: (" + targetX + ", " + targetY +
        // ")");
        // System.out.println("Block position: (" + row + ", " + col + ")");
        return new int[] { row, col };
    }
}

public class Klotski extends ApplicationAdapter implements InputProcessor {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private RectangleBlock[] blocks;
    private Color[] colors;
    private String[] labels;
    private int draggedBlock = -1;
    private Vector2 dragOffset;
    private int gridCols = 4;
    private int gridRows = 5;
    private float cellSize;
    private boolean isAnimating = false;
    private KlotskiGame game;
    private boolean isInitializing = true;
    private boolean isAutoSolving = false;
    private int autoStep = 0;
    private int[][] autoMoves;

    // Buttons
    private Rectangle restartButton;
    private Rectangle hintButton;
    private Rectangle exitButton;
    private Rectangle autoButton;

    // Hint animation variables
    private boolean isHintAnimating = false;
    private boolean showCongratulation = false;
    private int hintBlock = -1;
    private float hintTargetX;
    private float hintTargetY;

    @Override
    public void create() {
        // Set this class as the input processor
        Gdx.input.setInputProcessor(this);

        // Initialize shape renderer and batch
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
    
        // Calculate cell size based on window dimensions
        cellSize = Math.min(Gdx.graphics.getWidth() / gridCols, Gdx.graphics.getHeight() / gridRows);
    
        game = new KlotskiGame();
    
        // Create blocks with labels
        blocks = new RectangleBlock[game.pieces.length];
        colors = new Color[game.pieces.length];
        labels = new String[game.pieces.length];
    
        for (int i = 0; i < game.pieces.length; i++) {
            blocks[i] = new RectangleBlock(-1 * cellSize, -1 * cellSize, game.getPiece(i).width * cellSize,
                    game.getPiece(i).height * cellSize,
                    0.0f, 0.0f);
            labels[i] = game.getPiece(i).name;
            colors[i] = Color.WHITE;
        }
    
        updatePiecesFromGame();
    
        // Initialize buttons for the main game
        float buttonWidth = 150;
        float buttonHeight = 50;
        float buttonX = Gdx.graphics.getWidth() - buttonWidth - 20;

        restartButton = new Rectangle(buttonX, Gdx.graphics.getHeight() - buttonHeight - 20, buttonWidth, buttonHeight);
        hintButton = new Rectangle(buttonX, Gdx.graphics.getHeight() - 2 * (buttonHeight + 10) - 20, buttonWidth,
                buttonHeight);
        autoButton = new Rectangle(buttonX, Gdx.graphics.getHeight() - 3 * (buttonHeight + 10) - 20, buttonWidth, buttonHeight);
        exitButton = new Rectangle(buttonX, Gdx.graphics.getHeight() - 4 * (buttonHeight + 10) - 20, buttonWidth, buttonHeight);
    }

    public void updatePiecesFromGame() {
        for (int i = 0; i < blocks.length; i++) {
            snapBlockToGrid(i, game.getPiece(i).getPosition()[0], game.getPiece(i).getPosition()[1]);
        }
    }

    @Override
    public void render() {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw grid
        drawGrid();

        // Draw blocks
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < blocks.length; i++) {
            // Draw filled rectangle
            shapeRenderer.setColor(colors[i]);
            shapeRenderer.rect(blocks[i].x, blocks[i].y, blocks[i].width, blocks[i].height);
        }
        shapeRenderer.end();

        // Draw borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < blocks.length; i++) {
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.rect(blocks[i].x, blocks[i].y, blocks[i].width, blocks[i].height);
        }
        shapeRenderer.end();

        // Draw text
        batch.begin();
        for (int i = 0; i < blocks.length; i++) {
            Rectangle r = blocks[i];
            font.draw(batch, labels[i],
                    r.x + r.width / 4,
                    r.y + r.height / 2 + font.getCapHeight() / 2);
        }

        // Draw buttons
        font.draw(batch, "Restart", restartButton.x + 20, restartButton.y + 30);
        font.draw(batch, "Hint", hintButton.x + 40, hintButton.y + 30);
        if (isAutoSolving) {
            font.draw(batch, "Stop", autoButton.x + 40, autoButton.y + 30);
        } else {
            font.draw(batch, "Auto", autoButton.x + 40, autoButton.y + 30);
        }
        font.draw(batch, "Exit", exitButton.x + 40, exitButton.y + 30);
        batch.end();

        // Draw buttons' borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(restartButton.x, restartButton.y, restartButton.width, restartButton.height);
        shapeRenderer.rect(hintButton.x, hintButton.y, hintButton.width, hintButton.height);
        shapeRenderer.rect(autoButton.x, autoButton.y, autoButton.width, autoButton.height);
        shapeRenderer.rect(exitButton.x, exitButton.y, exitButton.width, exitButton.height);
        shapeRenderer.end();

        // Handle input
        handleInput();

        // Handle hint animation
        if (isHintAnimating) {
            animateHint();
        }

        // Handle auto-solve
        if (isAutoSolving && !isHintAnimating) {
            if (autoStep < autoMoves.length) {
                int fromRow = autoMoves[autoStep][0];
                int fromCol = autoMoves[autoStep][1];
                int toRow = autoMoves[autoStep][2];
                int toCol = autoMoves[autoStep][3];

                hintBlock = getBlockIdentityByPosition(fromRow, fromCol);
                hintTargetX = toCol * cellSize;
                hintTargetY = (gridRows - toRow) * cellSize - blocks[hintBlock].height;
                colors[hintBlock] = Color.YELLOW;
                isHintAnimating = true;

                autoStep++;
            } else {
                isAutoSolving = false;
            }
        }

        // Block animation
        if (!isInitializing) {
            deltaSnapBlocks(3.0f);
        } else if (isInitializing) {
            if (deltaSnapBlocksSeparately(5.0f)) {
                isInitializing = false;
            }
        }

        // Check for success
        if (game.isTerminal()) {
            showCongratulation = true;
        }

        // Show congratulation screen
        if (showCongratulation) {
            drawCongratulationScreen();
        }
    }

    private void updateGame() {
        for (int i = 0; i < blocks.length; i++) {
            // System.out.println("Block " + i + " original position: (" +
            // game.pieces[i].getPosition()[0] + ", "
            // + game.pieces[i].getPosition()[1] + ")");
            // System.out.println("Block " + i + " position: (" +
            // blocks[i].getGridPosition(cellSize)[0] + ", "
            // + blocks[i].getGridPosition(cellSize)[1] + ")");
            game.pieces[i].setPosition(blocks[i].getGridPosition(cellSize));
        }
    }

    private void animateHint() {
        RectangleBlock block = blocks[hintBlock];
        float deltaX = hintTargetX - block.x;
        float deltaY = hintTargetY - block.y;
        block.targetX = hintTargetX;
        block.targetY = hintTargetY;
        updateGame();

        if (Math.abs(deltaX) > 1.0f || Math.abs(deltaY) > 1.0f) {
        } else {
            block.x = hintTargetX;
            block.y = hintTargetY;
            colors[hintBlock] = Color.WHITE; // Reset color
            isHintAnimating = false; // End animation
        }
    }

    private void drawCongratulationScreen() {
        // Draw semi-transparent background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f); // Semi-transparent black background
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();

        // Draw congratulation text
        batch.begin();
        font.setColor(Color.WHITE); // Ensure text is readable
        font.draw(batch, "Congratulations!", Gdx.graphics.getWidth() / 2 - 100, Gdx.graphics.getHeight() / 2 + 50);
        batch.end();

        // Draw restart button
        float buttonWidth = 200;
        float buttonHeight = 60;
        float buttonX = Gdx.graphics.getWidth() / 2 - buttonWidth / 2;
        float buttonY = Gdx.graphics.getHeight() / 2 - 100;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY); // Button background
        shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE); // Button border
        shapeRenderer.rect(buttonX, buttonY, buttonWidth, buttonHeight);
        shapeRenderer.end();

        batch.begin();
        font.draw(batch, "Restart", buttonX + 50, buttonY + 40);
        batch.end();

        // Use a temporary button for the congratulation screen
        Rectangle tempRestartButton = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);

        // Check if the temporary restart button is clicked
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();
            if (tempRestartButton.contains(touchX, touchY)) {
                game.initialize();
                updatePiecesFromGame();
                isInitializing = true;
                showCongratulation = false;
            }
        }

        // Draw Exit button
        float exitButtonX = Gdx.graphics.getWidth() / 2 - buttonWidth / 2;
        float exitButtonY = Gdx.graphics.getHeight() / 2 - 200;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY); // Button background
        shapeRenderer.rect(exitButtonX, exitButtonY, buttonWidth, buttonHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE); // Button border
        shapeRenderer.rect(exitButtonX, exitButtonY, buttonWidth, buttonHeight);
        shapeRenderer.end();

        batch.begin();
        font.draw(batch, "Exit", exitButtonX + 50, exitButtonY + 40);
        batch.end();

        // Check if Exit button is clicked
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();
            if (new Rectangle(exitButtonX, exitButtonY, buttonWidth, buttonHeight).contains(touchX, touchY)) {
                Gdx.app.exit(); // Exit the application
            }
        }
    }

    private void drawGrid() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1);

        // Vertical lines
        for (int col = 0; col <= gridCols; col++) {
            float x = col * cellSize;
            shapeRenderer.line(x, 0, x, gridRows * cellSize);
        }

        // Horizontal lines
        for (int row = 0; row <= gridRows; row++) {
            float y = row * cellSize;
            shapeRenderer.line(0, y, gridCols * cellSize, y);
        }

        shapeRenderer.end();
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float touchX = screenX;
        float touchY = Gdx.graphics.getHeight() - screenY; // Convert to in-game coordinates 

        // Check if Exit button is clicked
        if (exitButton.contains(touchX, touchY)) {
            Gdx.app.exit(); // Exit the application
            return true;
        }

        // Check if restart button is clicked in the congratulations screen
        if (showCongratulation && restartButton.contains(touchX, touchY)) {
            game.initialize();
            updatePiecesFromGame();
            isInitializing = true;
            showCongratulation = false;
            return true;
        }

        // Check if restart button is clicked in the main game
        if (restartButton.contains(touchX, touchY)) {
            System.err.println("Restart button clicked");
            game.initialize();
            updatePiecesFromGame();
            isInitializing = true;
            showCongratulation = false;
            return true;
        }

        // Check if auto button is clicked in the main game
        if (autoButton.contains(touchX, touchY)) {
            System.out.println(isAutoSolving);
            if (isAutoSolving) {
                isAutoSolving = false;
            }
            else {
                autoSolve();
            }
            return true;
        }

        // Check if hint button is clicked
        if (hintButton.contains(touchX, touchY)) {
            List<String> solution = KlotskiSolver.solve(game);
            if (solution != null && !solution.isEmpty()) {
                String move = solution.get(0);
                System.out.println("Hint: " + move);

                String[] parts = move.split(" ");
                int fromIndex = move.indexOf(" from ");
                String fromPart = move.substring(fromIndex + 6, move.indexOf(" to "));
                String toPart = move.substring(move.indexOf(" to ") + 4);

                int fromRow = Integer.parseInt(fromPart.substring(1, fromPart.indexOf(',')));
                int fromCol = Integer
                        .parseInt(fromPart.substring(fromPart.indexOf(',') + 1, fromPart.length() - 1));
                int toRow = Integer.parseInt(toPart.substring(1, toPart.indexOf(',')));
                int toCol = Integer.parseInt(toPart.substring(toPart.indexOf(',') + 1, toPart.length() - 1));

                hintBlock = getBlockIdentityByPosition(fromRow, fromCol);
                hintTargetX = toCol * cellSize;
                hintTargetY = (gridRows - toRow) * cellSize - blocks[hintBlock].height;
                colors[hintBlock] = Color.YELLOW;
                for (int i = 0; i < blocks.length; i++) {
                    if (i != hintBlock) {
                        colors[i] = Color.WHITE;
                    }
                }
                isHintAnimating = true;
            }
            return true;
        }

        return false;
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();

            // Handle block dragging
            if (draggedBlock == -1) {
                for (int i = 0; i < blocks.length; i++) {
                    if (blocks[i].contains(touchX, touchY)) {
                        draggedBlock = i;
                        dragOffset = new Vector2(
                                touchX - blocks[i].x,
                                touchY - blocks[i].y);
                        break;
                    }
                }
            } else {
                float newX = touchX - dragOffset.x;
                float newY = touchY - dragOffset.y;

                newX = Math.max(0, Math.min(newX, gridCols * cellSize - blocks[draggedBlock].width));
                newY = Math.max(0, Math.min(newY, gridRows * cellSize - blocks[draggedBlock].height));

                float[] boundary = getBoundaryForBlock(draggedBlock);
                // System.out.printf("%.2f, %.2f, %.2f, %.2f\n", boundary[0], boundary[1],
                // boundary[2], boundary[3]);
                if (newX + blocks[draggedBlock].width > boundary[1]) {
                    // dragOffset.x += newX + blocks[draggedBlock].width - boundary[1];
                    newX = boundary[1] - blocks[draggedBlock].width;
                } else if (newX < boundary[0]) {
                    // dragOffset.x += newX - boundary[0];
                    newX = boundary[0];
                }

                if (newY + blocks[draggedBlock].height > boundary[3]) {
                    // dragOffset.y += newY + blocks[draggedBlock].height - boundary[3];
                    newY = boundary[3] - blocks[draggedBlock].height;
                } else if (newY < boundary[2]) {
                    // dragOffset.y += newY - boundary[2];
                    newY = boundary[2];
                }

                blocks[draggedBlock].x = newX;
                blocks[draggedBlock].y = newY;

                blocks[draggedBlock].targetX = Math.round((blocks[draggedBlock].x / cellSize)) * cellSize;
                blocks[draggedBlock].targetY = Math.round((blocks[draggedBlock].y / cellSize)) * cellSize;
            }
        } else {
            draggedBlock = -1;
        }
    }

    public int getBlockIdentityByPosition(int row, int col) {
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i].getGridPosition(cellSize)[0] == row && blocks[i].getGridPosition(cellSize)[1] == col) {
                return i;
            }
        }
        return -1;
    }

    public boolean deltaSnapBlocks(float ratio) {
        boolean allDone = true;

        for (int i = 0; i < blocks.length; i++) {
            if (i == draggedBlock)
                continue;

            float targetX = blocks[i].targetX;
            float targetY = blocks[i].targetY;
            float deltaX = targetX - blocks[i].x;
            float deltaY = targetY - blocks[i].y;

            if (Math.abs(deltaX) > 1.0f || Math.abs(deltaY) > 1.0f) {
                blocks[i].x += deltaX / ratio;
                blocks[i].y += deltaY / ratio;
                allDone = false;
            } else {
                blocks[i].x = targetX;
                blocks[i].y = targetY;
            }
        }
        if (allDone && !isInitializing && !isHintAnimating) {
            updateGame();
        }
        return allDone;
    }

    public boolean deltaSnapBlocksSeparately(float ratio) {
        for (int i = 0; i < blocks.length; i++) {
            if (i == draggedBlock)
                continue;

            float targetX = blocks[i].targetX;
            float targetY = blocks[i].targetY;
            float deltaX = targetX - blocks[i].x;
            float deltaY = targetY - blocks[i].y;

            if (Math.abs(deltaX) > 1.0f || Math.abs(deltaY) > 1.0f) {
                blocks[i].x += deltaX / ratio;
                blocks[i].y += deltaY / ratio;
                return false;
            } else {
                blocks[i].x = targetX;
                blocks[i].y = targetY;
                return true;
            }
        }
        return true;
    }

    public void snapBlockToCoordinate(int blockid, float x, float y) {
        blocks[blockid].targetX = x;
        blocks[blockid].targetY = y;
    }

    public void snapBlockToGrid(int blockid, int row, int col) {
        int x = col;
        int y = gridRows - row;
        blocks[blockid].targetX = x * cellSize;
        blocks[blockid].targetY = y * cellSize - blocks[blockid].height;
    }

    public float[] getBoundaryForBlock(int blockid) {
        float minX = 0;
        float minY = 0;
        float maxX = gridCols * cellSize;
        float maxY = gridRows * cellSize;
        float x = blocks[blockid].x;
        float y = blocks[blockid].y;
        float width = blocks[blockid].width;
        float height = blocks[blockid].height;
        for (int i = 0; i < blocks.length; i++) {
            if (i == blockid) {
                continue;
            }
            if (y + height > blocks[i].y && blocks[i].y + blocks[i].height > y) {
                if (blocks[i].x + blocks[i].width <= x) {
                    minX = Math.max(minX, blocks[i].x + blocks[i].width);
                } else if (x + width <= blocks[i].x) {
                    maxX = Math.min(maxX, blocks[i].x);
                }
            }
            if (x + width > blocks[i].x && blocks[i].x + blocks[i].width > x) {
                if (blocks[i].y + blocks[i].height <= y) {
                    minY = Math.max(minY, blocks[i].y + blocks[i].height);
                } else if (y + height <= blocks[i].y) {
                    maxY = Math.min(maxY, blocks[i].y);
                }
            }
        }
        return new float[] { minX, maxX, minY, maxY };
    }

    private void autoSolve() {
        autoStep = 0;
        List<String> solution = KlotskiSolver.solve(game);
        autoMoves = new int[solution.size()][4];
        if (solution != null && !solution.isEmpty()) {
            for (int i = 0; i < solution.size(); i++) {
                String move = solution.get(i);
                System.out.println("Solution: " + move);

                String[] parts = move.split(" ");
                int fromIndex = move.indexOf(" from ");
                String fromPart = move.substring(fromIndex + 6, move.indexOf(" to "));
                String toPart = move.substring(move.indexOf(" to ") + 4);

                int fromRow = Integer.parseInt(fromPart.substring(1, fromPart.indexOf(',')));
                int fromCol = Integer
                        .parseInt(fromPart.substring(fromPart.indexOf(',') + 1, fromPart.length() - 1));
                int toRow = Integer.parseInt(toPart.substring(1, toPart.indexOf(',')));
                int toCol = Integer.parseInt(toPart.substring(toPart.indexOf(',') + 1, toPart.length() - 1));

                autoMoves[i][0] = fromRow;
                autoMoves[i][1] = fromCol;
                autoMoves[i][2] = toRow;
                autoMoves[i][3] = toCol;

                isAutoSolving = true;
                // hintBlock = getBlockIdentityByPosition(fromRow, fromCol);
                // hintTargetX = toCol * cellSize;
                // hintTargetY = (gridRows - toRow) * cellSize - blocks[hintBlock].height;
                // colors[hintBlock] = Color.YELLOW;
                // isHintAnimating = true;
            }
        }
    }

    // For these following abtract methods
    // return true -> the job isn't done, need other processing
    // return false -> the job is done, no need for other processing
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Handle touch release if needed
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Handle dragging logic if needed
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
