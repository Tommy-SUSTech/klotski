# KlotskiGame.java

This file implements the logic of Klotski Game.

## Classes
this file contains two classees
- class KlotskiPiece (这个class也可以单独成文件)
- public class KlotskiGame
- enum PieceType

### PieceType
```java
// PieceType enumerates the piece's name with its id

public enum Block {
    CAO_CAO("Cao Cao", 2, 2, 1),      // 2x2, 1 piece
    GUAN_YU("Guan Yu", 2, 1, 1),       // 2x1, 1 piece
    GENERAL("General", 1, 2, 4),       // 1x2, 4 pieces
    SOLDIER("Soldier", 1, 1, 4);       // 1x1, 4 pieces

    private final String name;
    private final int width;
    private final int height;
    private final int count;

    Block(String name, int width, int height, int count) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.count = count;
    }

    // Getters
    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getCount() { return count; }

    @Override
    public String toString() {
        return String.format("%s (Size: %dx%d, Count: %d)", name, width, height, count);
    }
}
```

### KlotskiPiece
```
Properties:
private int        this.width:  width of the piece
private int        this.height: height of the piece
private int[2]     this.position: position of the top left corner of the piece
public  int        this.id: idendity of the piece, must be unique.

Methods:
getPosition(): return int[2] this.position
setPosition(int[2] position): let this.position = the new position
getShape(): return int[2] [width, height]
getWidth(): return this.width
getShape(): return this.height
getSize():  return this.width * this.height
```
### KlotskiGame
```
Properties:

Methods:

```
