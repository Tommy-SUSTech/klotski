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

public class Klotski extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Rectangle[] blocks;
    private Color[] colors;
    private String[] labels;
    private int draggedBlock = -1;
    private Vector2 dragOffset;
    private int gridCols = 4;
    private int gridRows = 5;
    private float cellSize;
    private boolean isAnimating = false;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        // Calculate cell size based on window dimensions
        cellSize = Math.min(Gdx.graphics.getWidth()/gridCols, Gdx.graphics.getHeight()/gridRows);

        // Create blocks with labels
        blocks = new Rectangle[3];
        colors = new Color[3];
        labels = new String[3];

        // Block 1 (2x2 square)
        blocks[0] = new Rectangle(1*cellSize, 1*cellSize, 2*cellSize, 2*cellSize);
        colors[0] = Color.RED;
        labels[0] = "Big\nBlock";

        // Block 2 (2x1 horizontal)
        blocks[1] = new Rectangle(0*cellSize, 3*cellSize, 2*cellSize, 1*cellSize);
        colors[1] = Color.BLUE;
        labels[1] = "Wide\nBlock";

        // Block 3 (1x2 vertical)
        blocks[2] = new Rectangle(3*cellSize, 0*cellSize, 1*cellSize, 2*cellSize);
        colors[2] = Color.GREEN;
        labels[2] = "Tall\nBlock";
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
            shapeRenderer.setColor(colors[i]);
            shapeRenderer.rect(blocks[i].x, blocks[i].y, blocks[i].width, blocks[i].height);

            // Draw border
            //shapeRenderer.set(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(blocks[i].x, blocks[i].y, blocks[i].width, blocks[i].height);
            shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        }
        shapeRenderer.end();

        // Draw text
        batch.begin();
        for (int i = 0; i < blocks.length; i++) {
            Rectangle r = blocks[i];
            font.draw(batch, labels[i],
                r.x + r.width/4,
                r.y + r.height/2 + font.getCapHeight()/2);
        }
        batch.end();

        // Handle dragging
        handleInput();
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

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (draggedBlock == -1) {
                // Find which block was touched
                for (int i = 0; i < blocks.length; i++) {
                    if (blocks[i].contains(touchX, touchY)) {
                        draggedBlock = i;
                        dragOffset = new Vector2(
                            touchX - blocks[i].x,
                            touchY - blocks[i].y
                        );
                        break;
                    }
                }
            } else {
                float newX = touchX - dragOffset.x;
                float newY = touchY - dragOffset.y;

                // Keep within grid bounds
                newX = Math.max(0, Math.min(newX, gridCols*cellSize - blocks[draggedBlock].width));
                newY = Math.max(0, Math.min(newY, gridRows*cellSize - blocks[draggedBlock].height));

                blocks[draggedBlock].x = newX;
                blocks[draggedBlock].y = newY;

                // Trigger snapping animation
                if (!isAnimating) {
                    isAnimating = true;
                }
            }
        } else {
            if (isAnimating) {
                if(deltaSnapBlocks()) { isAnimating = false; };
            } else {
                draggedBlock = -1;
            }
        }
    }

    public boolean deltaSnapBlocks() {
        for (int i = 0; i < blocks.length; i++) {
            float targetx = Math.round((blocks[i].x / cellSize)) * cellSize;
            float targety = Math.round((blocks[i].y / cellSize)) * cellSize;
            float deltax = - blocks[i].x + targetx;
            float deltay = - blocks[i].y + targety;
            // System.out.printf("Block %d: %.8f %.8f %.8f \n", i, deltax, targetx, blocks[i].x);
            if (Math.abs(deltax) <= 0.1 && Math.abs(deltay) <= 0.1) {
                blocks[i].x = targetx;
                blocks[i].y = targety;
            } else {
                blocks[i].x += deltax / 3.0f;
                blocks[i].y += deltay / 3.0f;
                return false;
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // Recalculate cell size when window is resized
        cellSize = Math.min(width/gridCols, height/gridRows);
    }
}
