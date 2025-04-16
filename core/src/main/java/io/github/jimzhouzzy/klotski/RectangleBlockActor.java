package io.github.jimzhouzzy.klotski;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import io.github.jimzhouzzy.klotski.GameScreen;
import io.github.jimzhouzzy.klotski.Klotski;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

public class RectangleBlockActor extends Actor {
    private Rectangle rectangle;
    private Color color;
    private static ShapeRenderer shapeRenderer = new ShapeRenderer();
    public int pieceId; // ID of the corresponding game piece
    private KlotskiGame game; // Reference to the game logic
    private float snappedX;
    private float snappedY;

    public RectangleBlockActor(float x, float y, float width, float height, Color color, int pieceId, KlotskiGame game) {
        this.rectangle = new Rectangle(x, y, width, height);
        this.color = color;
        this.pieceId = pieceId;
        this.game = game;

        setBounds(x, y, width, height);

        addListener(new InputListener() {
            private float offsetX, offsetY;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Klotski klotski = (Klotski) Gdx.app.getApplicationListener();
                GameScreen gameScreen = klotski.gameScreen;
                if (gameScreen.isAutoSolving()) {
                    gameScreen.stopAutoSolving(); // Stop auto-solving if the user interacts with a block
                }

                // Record the initial logical position
                float cellSize = Math.min(Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 5f);
                int oldRow = 5 - (int) (getY() / cellSize) - (int) (getHeight() / cellSize);
                int oldCol = (int) (getX() / cellSize);

                // Store the old position in the block's user data
                setUserObject(new int[]{oldRow, oldCol});

                offsetX = x;
                offsetY = y;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                Klotski klotski = (Klotski) Gdx.app.getApplicationListener();
                GameScreen gameScreen = klotski.gameScreen;
                if (gameScreen.isAutoSolving()) {
                    gameScreen.stopAutoSolving(); // Stop auto-solving if the user interacts with a block
                }

                float newX = getX() + x - offsetX;
                float newY = getY() + y - offsetY;

                // Get the boundaries for this block
                float[] boundaries = gameScreen.getBoundaryForBlock(RectangleBlockActor.this);
                float minX = boundaries[0];
                float maxX = boundaries[1];
                float minY = boundaries[2];
                float maxY = boundaries[3];

                // Constrain the block within the calculated boundaries
                newX = Math.max(minX, Math.min(newX, maxX));
                newY = Math.max(minY, Math.min(newY, maxY));

                setPosition(newX, newY);
                rectangle.setPosition(newX, newY);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Klotski klotski = (Klotski) Gdx.app.getApplicationListener();
                GameScreen gameScreen = klotski.gameScreen;
                float cellSize = Math.min(Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 5f);

                // Calculate the snapped position
                snappedX = Math.round(getX() / cellSize) * cellSize;
                snappedY = Math.round(getY() / cellSize) * cellSize;

                // Get the boundaries for this block
                float[] boundaries = gameScreen.getBoundaryForBlock(RectangleBlockActor.this);
                float minX = boundaries[0];
                float maxX = boundaries[1];
                float minY = boundaries[2];
                float maxY = boundaries[3];

                // Constrain the snapped position within the boundaries
                snappedX = Math.max(minX, Math.min(snappedX, maxX));
                snappedY = Math.max(minY, Math.min(snappedY, maxY));

                // Calculate the logical position after snapping
                int newRow = 5 - (int) (snappedY / cellSize) - (int) (getHeight() / cellSize);
                int newCol = (int) (snappedX / cellSize);

                // Retrieve the old position from user data
                int[] oldPosition = (int[]) getUserObject();
                int oldRow = oldPosition[0];
                int oldCol = oldPosition[1];

                // Animate the block's movement to the snapped position
                addAction(Actions.sequence(
                    Actions.moveTo(snappedX, snappedY, 0.1f), // Smooth snapping animation
                    Actions.run(() -> {
                        // Update the rectangle's position
                        // TODO: support 2-step movement
                        rectangle.setPosition(snappedX, snappedY);

                        // Update the game logic
                        game.getPiece(pieceId).setPosition(new int[]{newRow, newCol});

                        // Apply the action and record the move
                        gameScreen.getGame().applyAction(new int[]{oldRow, oldCol}, new int[]{newRow, newCol});
                        gameScreen.recordMove(new int[]{oldRow, oldCol}, new int[]{newRow, newCol});
                    })
                ));
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end(); // End the batch to use ShapeRenderer

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        // Draw the filled rectangle (block)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();

        // Draw the border (outline)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK); // Border color
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();

        batch.begin(); // Restart the batch
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        rectangle.setSize(width, height);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}