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

class RectangleBlock extends Rectangle {
    public float targetX;
    public float targetY;

    public RectangleBlock(float x, float y, float width, float height, float targetX, float targetY) {
        super(x, y, width, height);
        this.targetX = targetX;
        this.targetY = targetY;
    }
}

public class Klotski extends ApplicationAdapter {
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

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        // Calculate cell size based on window dimensions
        cellSize = Math.min(Gdx.graphics.getWidth() / gridCols, Gdx.graphics.getHeight() / gridRows);

        // Create blocks with labels
        blocks = new RectangleBlock[3];
        colors = new Color[3];
        labels = new String[3];

        // Block 1 (2x2 square)
        blocks[0] = new RectangleBlock(1*cellSize, 1*cellSize, 2*cellSize, 2*cellSize, 0.0f, 0.0f);
        colors[0] = Color.RED;
        labels[0] = "Big\nBlock";

        // Block 2 (2x1 horizontal)
        blocks[1] = new RectangleBlock(0*cellSize, 3*cellSize, 2*cellSize, 1*cellSize, 0.0f, 0.0f);
        colors[1] = Color.BLUE;
        labels[1] = "Wide\nBlock";

        // Block 3 (1x2 vertical)
        blocks[2] = new RectangleBlock(3*cellSize, 0*cellSize, 1*cellSize, 2*cellSize, 0.0f, 0.0f);
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

        // Block animation
        deltaSnapBlocks();
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
                newX = Math.max(0, Math.min(newX, gridCols * cellSize - blocks[draggedBlock].width));
                newY = Math.max(0, Math.min(newY, gridRows * cellSize - blocks[draggedBlock].height));

                float[] boundary = getBoundaryForBlock(draggedBlock);
                System.out.printf("%.2f, %.2f, %.2f, %.2f\n", boundary[0], boundary[1], boundary[2], boundary[3]);
                if (newX + blocks[draggedBlock].width > boundary[1]) {
                    newX = boundary[1] - blocks[draggedBlock].width ;
                } else if (newX < boundary[0]) {
                    newX = boundary[0];
                }

                if (newY + blocks[draggedBlock].height > boundary[3]) {
                    newY = boundary[3] - blocks[draggedBlock].height;
                } else if (newY < boundary[2]) {
                    newY = boundary[2];
                }

                blocks[draggedBlock].x = newX;
                blocks[draggedBlock].y = newY;

                blocks[draggedBlock].targetX = Math.round((blocks[draggedBlock].x / cellSize)) * cellSize;
                blocks[draggedBlock].targetY  = Math.round((blocks[draggedBlock].y / cellSize)) * cellSize;
            }
        } else {
            draggedBlock = -1;
        }
    }

    public boolean deltaSnapBlocks() {
        for (int i = 0; i < blocks.length; i++) {
            // Skip dragged block
            if (i == draggedBlock) {
                continue;
            }
            float targetx = blocks[i].targetX;
            float targety = blocks[i].targetY;
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

    public void snapBlockToCoordinate(int blockid, float x, float y) {
        blocks[blockid].targetX = x;
        blocks[blockid].targetY = y;
    }

    public void snapBlockToGrid(int blockid, int x, int y) {
        blocks[blockid].targetX = x * cellSize;
        blocks[blockid].targetY  = y * cellSize;
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
        for (int i = 0; i < blocks.length; i ++) {
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
        return new float[] {minX, maxX, minY, maxY};
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
