package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.jimzhouzzy.klotski.KlotskiGame;
import io.github.jimzhouzzy.klotski.KlotskiSolver;
import io.github.jimzhouzzy.klotski.RectangleBlockActor;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.Group;

import java.util.ArrayList;
import java.util.List;

public class Klotski extends ApplicationAdapter {
    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;
    private float cellSize;
    private final int rows = 5;
    private final int cols = 4;
    private List<RectangleBlockActor> blocks; // List of all blocks
    private Group congratulationsGroup;

    private int[][] autoMoves;
    private int autoStep;
    public boolean isAutoSolving;
    private boolean isTerminal = false;

    private List<int[][]> moveHistory; // Stores the history of moves
private int currentMoveIndex; // Tracks the current move in the history

    private KlotskiGame game; // Reference to the game logic
    private List<String> solution; // Stores the current solution
    private int solutionIndex; // Tracks the current step in the solution

    private TextButton autoButton;

    @Override
    public void create() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("skins/default/skin/uiskin.json"));
        shapeRenderer = new ShapeRenderer();

        blocks = new ArrayList<>(); // Initialize the list of blocks

        // Calculate cellSize dynamically based on the screen size
        cellSize = Math.min(Gdx.graphics.getWidth() / (float) cols, Gdx.graphics.getHeight() / (float) rows);

        // Initialize the game logic
        game = new KlotskiGame();

        // Create a root table for layout
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // Left side: Grid container
        Table gridTable = new Table();
        gridTable.setFillParent(false);

        // Right side: Button column
        Table buttonTable = new Table();
        String[] buttonNames = { "Restart", "Hint", "Auto", "Undo", "Redo", "Exit" };

        // Add buttons with listeners
        for (String name : buttonNames) {
            TextButton button = new TextButton(name, skin);
            buttonTable.add(button).width(100).pad(10);
            buttonTable.row();

            if (name == "Auto") {
                autoButton = button;
            }

            // Add functionality to each button
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    switch (name) {
                        case "Restart":
                            handleRestart(game);
                            break;
                        case "Hint":
                            handleHint(game);
                            break;
                        case "Auto":
                            handleAutoSolve(game, button);
                            break;
                        case "Undo":
                            handleUndo();
                            break;
                        case "Redo":
                            handleRedo();
                            break;
                        case "Exit":
                            handleExit();
                            break;
                    }
                }
            });
        }

        // Add grid and buttons to the root table
        rootTable.add(gridTable).expand().fill().left().padRight(20); // Grid on the left
        rootTable.add(buttonTable).top().right(); // Buttons on the right

        // Create blocks based on the game pieces
        for (KlotskiGame.KlotskiPiece piece : game.getPieces()) {
            // Convert logical position to graphical position
            float x = piece.position[1] * cellSize; // Column to x-coordinate
            float y = (rows - piece.position[0] - piece.height) * cellSize; // Invert y-axis and adjust for height
            float width = piece.width * cellSize;
            float height = piece.height * cellSize;

            // Create a block with a unique color for each piece
            Color color = getColorForPiece(piece.id);
            RectangleBlockActor block = new RectangleBlockActor(x, y, width, height, color, piece.id, game);

            blocks.add(block); // Add block to the list
            stage.addActor(block); // Add block to the stage
        }
        
        // Create the congratulations screen
        createCongratulationsScreen();

        moveHistory = new ArrayList<>();
        currentMoveIndex = -1; // No moves yet
    }

    // Helper method to assign colors to pieces
    private Color getColorForPiece(int id) {
        switch (id) {
            case 0:
                return Color.RED; // Cao Cao
            case 1:
                return Color.BLUE; // Guan Yu
            case 2:
            case 3:
            case 4:
            case 5:
                return Color.GREEN; // Generals
            case 6:
            case 7:
            case 8:
            case 9:
                return Color.YELLOW; // Soldiers
            default:
                return Color.GRAY; // Default color
        }
    }

    public List<RectangleBlockActor> getBlocks() {
        return blocks;
    }

    public float[] getBoundaryForBlock(RectangleBlockActor block) {
        float minX = 0;
        float minY = 0;
        float maxX = cols * cellSize - block.getWidth();
        float maxY = rows * cellSize - block.getHeight();

        for (RectangleBlockActor other : blocks) {
            if (other == block) {
                continue;
            }

            float x = block.getX();
            float y = block.getY();
            float width = block.getWidth();
            float height = block.getHeight();

            // Check horizontal overlap
            if (y + height > other.getY() && other.getY() + other.getHeight() > y) {
                // Block is to the right of the other block
                if (x >= other.getX() + other.getWidth()) {
                    minX = Math.max(minX, other.getX() + other.getWidth());
                }
                // Block is to the left of the other block
                if (x + width <= other.getX()) {
                    maxX = Math.min(maxX, other.getX() - block.getWidth());
                }

            }

            // Check vertical overlap
            if (x + width > other.getX() && other.getX() + other.getWidth() > x) {
                // Block is above the other block
                if (y >= other.getY() + other.getHeight()) {
                    minY = Math.max(minY, other.getY() + other.getHeight());
                }
                // Block is below the other block
                if (y + height <= other.getY()) {
                    maxY = Math.min(maxY, other.getY() - block.getHeight());
                }
            }
        }

        return new float[] { minX, maxX, minY, maxY };
    }

    private void updateBlocksFromGame(KlotskiGame game) {
        for (RectangleBlockActor block : blocks) {
            KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
            float x = piece.position[1] * cellSize; // Column to x-coordinate
            float y = (rows - piece.position[0] - piece.height) * cellSize; // Invert y-axis and adjust for height
            block.setPosition(x, y);
        }
    }

    @Override
    public void render() {
        // Update cell size dynamically to ensure the grid stays square
        cellSize = Math.min(Gdx.graphics.getWidth() / (float) cols, Gdx.graphics.getHeight() / (float) rows);

        // Clear the screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Check if the game is in a terminal state (win condition)
        if (this.isTerminal) {
            System.out.println("Game is in a terminal state.");
            showCongratulationsScreen(); // Show the congratulations screen
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
            return;
        }

        // Draw grid lines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);

        for (int row = 0; row <= rows; row++) {
            float y = row * cellSize;
            shapeRenderer.line(0, y, cols * cellSize, y);
        }

        for (int col = 0; col <= cols; col++) {
            float x = col * cellSize;
            shapeRenderer.line(x, 0, x, rows * cellSize);
        }

        shapeRenderer.end();

        // Auto-solving logic
        if (isAutoSolving && solution != null && solutionIndex < solution.size()) {
            // Check if the previous animation has finished
            boolean allAnimationsFinished = true;
            for (RectangleBlockActor block : blocks) {
                if (block.getActions().size > 0) {
                    allAnimationsFinished = false;
                    break;
                }
            }

            if (allAnimationsFinished) {
                // Parse the current move
                String move = solution.get(solutionIndex);
                System.out.println("Auto-solving step: " + move);

                String[] parts = move.split(" ");
                int fromIndex = move.indexOf(" from ");
                String fromPart = move.substring(fromIndex + 6, move.indexOf(" to "));
                String toPart = move.substring(move.indexOf(" to ") + 4);

                int fromRow = Integer.parseInt(fromPart.substring(1, fromPart.indexOf(',')));
                int fromCol = Integer.parseInt(fromPart.substring(fromPart.indexOf(',') + 1, fromPart.length() - 1));
                int toRow = Integer.parseInt(toPart.substring(1, toPart.indexOf(',')));
                int toCol = Integer.parseInt(toPart.substring(toPart.indexOf(',') + 1, toPart.length() - 1));

                // Find the block at the starting position
                for (RectangleBlockActor block : blocks) {
                    KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
                    if (piece.position[0] == fromRow && piece.position[1] == fromCol) {
                        // Animate the block's movement to the target position
                        float targetX = toCol * cellSize;
                        float targetY = (rows - toRow - piece.height) * cellSize; // Invert y-axis
                        block.addAction(Actions.sequence(
                                Actions.moveTo(targetX, targetY, 0.1f), // Smooth animation
                                Actions.run(() -> {
                                    // Update game logic after animation
                                    piece.setPosition(new int[] { toRow, toCol });
                                    this.isTerminal = game.isTerminal(); // Check if the game is in a terminal state
                                    game.applyAction(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                                    recordMove(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                                })));
                        break;
                    }
                }

                // Move to the next step
                solutionIndex++;
                if (solutionIndex >= solution.size()) {
                    isAutoSolving = false; // Stop auto-solving when all steps are completed
                }
            }
        }

        // Update and draw the stage
        stage.act(Gdx.graphics.getDeltaTime());
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
        shapeRenderer.dispose();
    }

    private void handleRestart(KlotskiGame game) {
        // Stop auto-solving if active
        stopAutoSolving();

        // Stop all animations
        for (RectangleBlockActor block : blocks) {
            block.clearActions(); // Clear all actions for this block
        }

        // Reset the game logic
        game.initialize();

        // Update the blocks to match the game state
        updateBlocksFromGame(game);

        // Reset terminal state
        isTerminal = false;

        System.out.println("Game restarted.");
    }

    private void handleHint(KlotskiGame game) {
        // Get the solution from the solver
        List<String> solution = KlotskiSolver.solve(game);

        if (solution != null && !solution.isEmpty()) {
            // Parse the first move from the solution
            String move = solution.get(0);
            System.out.println("Hint: " + move);

            String[] parts = move.split(" ");
            int fromIndex = move.indexOf(" from ");
            String fromPart = move.substring(fromIndex + 6, move.indexOf(" to "));
            String toPart = move.substring(move.indexOf(" to ") + 4);

            int fromRow = Integer.parseInt(fromPart.substring(1, fromPart.indexOf(',')));
            int fromCol = Integer.parseInt(fromPart.substring(fromPart.indexOf(',') + 1, fromPart.length() - 1));
            int toRow = Integer.parseInt(toPart.substring(1, toPart.indexOf(',')));
            int toCol = Integer.parseInt(toPart.substring(toPart.indexOf(',') + 1, toPart.length() - 1));

            // Find the block at the starting position
            for (RectangleBlockActor block : blocks) {
                KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
                System.out.printf("Block ID: %d, Position: (%d, %d)\n", piece.id, piece.position[0], piece.position[1]);
                if (piece.position[0] == fromRow && piece.position[1] == fromCol) {
                    // Animate the block's movement to the target position
                    float targetX = toCol * cellSize;
                    float targetY = (rows - toRow - piece.height) * cellSize; // Invert y-axis
                    block.addAction(Actions.sequence(
                            Actions.moveTo(targetX, targetY, 0.1f), // Smooth animation
                            Actions.run(() -> {
                                // Update game logic after animation
                                piece.setPosition(new int[] { toRow, toCol });
                                this.isTerminal = game.isTerminal(); // Check if the game is in a terminal state
                                game.applyAction(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                                recordMove(new int[] { fromRow, fromCol }, new int[] { toRow, toCol });
                            })));
                    break;
                }
            }
        } else {
            System.out.println("No solution found or no hint available.");
        }
    }

    private void handleAutoSolve(KlotskiGame game, TextButton autoButton) {
        if (isAutoSolving()) {
            stopAutoSolving(); // Stop auto-solving if already active
            autoButton.setText("Auto"); // Change button text back to "Auto"
        } else {
            solution = null; // Clear the previous solution
            solutionIndex = 0; // Reset the solution index
            List<String> newSolution = KlotskiSolver.solve(game); // Get the new solution

            if (newSolution != null && !newSolution.isEmpty()) {
                solution = newSolution; // Store the solution
                isAutoSolving = true; // Enable auto-solving mode
                autoButton.setText("Stop"); // Change button text to "Stop"
                System.out.println("Auto-solving started.");
            } else {
                System.out.println("No solution found.");
            }
        }
    }

    private void handleExit() {
        Gdx.app.exit(); // Exit the application
    }

    public boolean isAutoSolving() {
        return isAutoSolving;
    }

    public void stopAutoSolving() {
        if (isAutoSolving) {
            isAutoSolving = false;
            System.out.println("Auto-solving stopped.");
            updateAutoButtonText(autoButton); 
        }
    }

    private void createCongratulationsScreen() {
        // Create a group for the congratulations screen
        congratulationsGroup = new Group();

        // Set position of the group to the center of the stage
        float stageWidth = stage.getWidth();
        float stageHeight = stage.getHeight();
        congratulationsGroup.setPosition(stageWidth / 2f, stageHeight / 2f);

        // Ensure the group is drawn on top
        congratulationsGroup.setZIndex(Integer.MAX_VALUE);

        Table congratsTable = new Table();
        congratsTable.setFillParent(true);
        congratsTable.center(); // Ensure the table is centered

        // Add congratulatory message
        Label congratsLabel = new Label("Congratulations! You Win!", skin);
        congratsLabel.setFontScale(2); // Make the text larger
        congratsTable.add(congratsLabel).padBottom(20).row();

        // Add time usage placeholder
        Label timeLabel = new Label("Time: 00:00", skin); // Placeholder for time usage
        timeLabel.setFontScale(1.5f);
        congratsTable.add(timeLabel).padBottom(20).row();

        // Add restart button
        TextButton restartButton = new TextButton("Restart", skin);
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleRestart(game); // Restart the game
                congratulationsGroup.setVisible(false); // Hide the congratulations screen
            }
        });
        congratsTable.add(restartButton).width(150).height(50);

        // Add the table to the group
        congratulationsGroup.addActor(congratsTable);

        // Initially hide the group
        congratulationsGroup.setVisible(false);

        // Add the group to the stage
        stage.addActor(congratulationsGroup);
    }

    private void showCongratulationsScreen() {
        congratulationsGroup.setVisible(true); // Show the congratulations screen
    }

    private void updateAutoButtonText(TextButton autoButton) {
        if (isAutoSolving) {
            autoButton.setText("Stop");
        } else {
            autoButton.setText("Auto");
        }
    }

    public void recordMove(int[] from, int[] to) {
        // Remove any redo history if we are making a new move
        while (moveHistory.size() > currentMoveIndex + 1) {
            moveHistory.remove(moveHistory.size() - 1);
        }
    
        // Add the move to the history
        moveHistory.add(new int[][] { from, to });
        currentMoveIndex++;
    }

    private void handleUndo() {
        if (currentMoveIndex >= 0) {
            int[][] lastMove = moveHistory.get(currentMoveIndex);
            int[] from = lastMove[1]; // Reverse the move
            int[] to = lastMove[0];

            // Find the block at the current position
            for (RectangleBlockActor block : blocks) {
                KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
                if (piece.position[0] == from[0] && piece.position[1] == from[1]) {
                    // Animate the block's movement to the previous position
                    float targetX = to[1] * cellSize;
                    float targetY = (rows - to[0] - piece.height) * cellSize; // Invert y-axis
                    block.addAction(Actions.sequence(
                        Actions.moveTo(targetX, targetY, 0.1f), // Smooth animation
                        Actions.run(() -> {
                            // Update game logic after animation
                            piece.setPosition(to);
                            game.applyAction(from, to); // Apply the reverse move
                            updateBlocksFromGame(game); // Update the blocks
                            currentMoveIndex--; // Move back in history
                            System.out.println("Undo performed.");
                        })
                    ));
                    break;
                }
            }
        } else {
            System.out.println("No moves to undo.");
        }
    }

    private void handleRedo() {
        if (currentMoveIndex < moveHistory.size() - 1) {
            currentMoveIndex++;
            int[][] nextMove = moveHistory.get(currentMoveIndex);
            int[] from = nextMove[0];
            int[] to = nextMove[1];

            // Find the block at the current position
            for (RectangleBlockActor block : blocks) {
                KlotskiGame.KlotskiPiece piece = game.getPiece(block.pieceId);
                if (piece.position[0] == from[0] && piece.position[1] == from[1]) {
                    // Animate the block's movement to the next position
                    float targetX = to[1] * cellSize;
                    float targetY = (rows - to[0] - piece.height) * cellSize; // Invert y-axis
                    block.addAction(Actions.sequence(
                        Actions.moveTo(targetX, targetY, 0.1f), // Smooth animation
                        Actions.run(() -> {
                            // Update game logic after animation
                            piece.setPosition(to);
                            game.applyAction(from, to); // Apply the move
                            updateBlocksFromGame(game); // Update the blocks
                            System.out.println("Redo performed.");
                        })
                    ));
                    break;
                }
            }
        } else {
            System.out.println("No moves to redo.");
        }
    }

    public KlotskiGame getGame() {
        return game;
    }
}