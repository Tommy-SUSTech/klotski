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
    CAO_CAO("Cao Cao", 2, 2, 1, 'C'),      // 2x2, 1 piece
    GUAN_YU("Guan Yu", 2, 1, 1, 'Y'),       // 2x1, 1 piece
    GENERAL("General", 1, 2, 4, 'G'),       // 1x2, 4 pieces
    SOLDIER("Soldier", 1, 1, 4, 'S');       // 1x1, 4 pieces

    private final String name;
    private final int width;
    private final int height;
    private final int count;
    private final char abbreviation;

    Block(String name, int width, int height, int count, char abbreviation) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.count = count;
        this.abbreviation = abbreviation;
    }

    // Getters
    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getCount() { return count; }
    public char getAbbreviation() { return abbreviation; }

    @Override
    public String toString() {
        return String.format("%s (Size: %dx%d, Count: %d, Abbreviation: %c)", name, width, height, count, abbreviation);
    }
}
```

### KlotskiPiece
```
Properties:
public  final int  this.id: idendity of the piece, must be unique.
public  final String this.name: name of the piece. e.g. CaoCao
public  final char this.abbreviation: abbreviation of the piece. e.g. 'C' for CaoCao
public  final int  this.width:  width of the piece
public  final int  this.height: height of the piece
public  int[2]     this.position: position of the top left corner of the piece

Methods:
getPosition(): return int[2] this.position
setPosition(int[2] position): let this.position = the new position
getShape(): return int[2] [width, height]
getSize():  return this.width * this.height
```

### KlotskiGame
```
Properties:
public KlotskiPiece[] pieces: all pieces in the board
public int moveCount:           how many moves we have made

Methods:
initialize():                   re-initialize the game
applyAction(int[2] from. int[2] to):   apply action (position as coordinate position[0] position[1])
    Modifies: this.moveCount
applyAction(int from, int to):      apply action (position as index).
    Modifies: this.moveCount
isLegalAction(int[2] from, int[2] to): return true when the action is valid
isLegalAction(int from, int to):    return true when the action is valid
getLegalAtions():               return all legal actions as list of positions coordinates
@Override toString():           return the board with piece abbreviations as string
private coordinateToIndex(coordinate): return index of the coordinate.
private indexToCoordinate(index): return coordinate of the index.
```
