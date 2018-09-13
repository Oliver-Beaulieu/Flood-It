// Assignment 9, Part 2
// Angell-James Will
// willangelljames
// Heath Kassidy
// kassidy25
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tester.*;
import javalib.impworld.*;

import java.awt.Color;

import javalib.worldimages.*;

// main world class
class Flood extends World {
  ArrayList<Cell> cells;

  // CHANGE GRID SIZE HERE //
  static final int gridsize = 20;
  // CHANGE GRIDE SIZE HERE //

  int howManyColors;
  // hash contains all the available colours
  HashMap<Integer, Color> hash;
  static final int cellSize = 20;
  Random rand = new Random();
  int numOfClicks;

  Flood(ArrayList<Cell> cells, int howManyColors, HashMap<Integer, Color> hash) {
    this.cells = cells;
    this.howManyColors = howManyColors;
    this.hash = hash;
    this.numOfClicks = Flood.gridsize + (Math.floorDiv(Flood.gridsize, 2));
    // these two methods below need to be in the contructor, not in makeScene,
    // otherwise it keeps getting called on each tick
    this.makePosn();
    this.makePartner();
  }

  // the background image of the game
  static final WorldImage background = new RectangleImage(Flood.gridsize * Flood.gridsize,
      Flood.gridsize * Flood.gridsize, OutlineMode.SOLID, Color.WHITE);

  // renders the game. Places each cell on the background at the point specified
  // by each cell.
  public WorldScene makeScene() {
    WorldScene acc = this.getEmptyScene();
    acc.placeImageXY(background, (gridsize * gridsize) / 2, (gridsize * gridsize) / 2);
    for (Cell c : cells) {
      // the purpose of the arithmatic here is to scale the cells to appear on
      // the top left initially - they appear in the middle naturally
      // c.color has already been set in makePosn ^^
      acc.placeImageXY(c.drawCell(c.color), c.x * Flood.cellSize + Flood.cellSize / 2, c.y
          * Flood.cellSize + Flood.cellSize / 2);
      acc.placeImageXY((new TextImage(Integer.toString(this.goodNumOfClicks()), cellSize,
          Color.WHITE)), gridsize * cellSize / 2, cellSize / 2);
    }
    return acc;
  }

  public void onKeyEvent(String key) {
    if (key.equals("x")) {
      this.makeFinalScene("Goodbye!");
      this.makeScene();
    }
    else {
      if (key.equals("r")) {
        new Flood(this.cells, this.howManyColors, this.hash);
        this.numOfClicks = Flood.gridsize + (Math.floorDiv(Flood.gridsize, 2));
        this.makeScene();
      }
    }
  }

  // make posn for each cell
  public void makePosn() {
    for (int row = 0; row < (gridsize * gridsize) / gridsize; row++) {
      for (int col = 0; col < (gridsize * gridsize) / gridsize; col++) {
        this.cells.add(new Cell(col, row, this.hash.get(rand.nextInt(this.goodNumOfColors())),
            false));
      }
    }
  }

  // the end of the world
  public WorldEnd worldEnds() {
    if (this.allCellsFlooded() && (this.goodNumOfClicks() > 0)) {
      return new WorldEnd(true, this.makeFinalScene("You win!"));
    }
    else {
      new WorldEnd(false, this.makeScene());
    }
    if (this.goodNumOfClicks() == 0) {
      return new WorldEnd(true, this.makeFinalScene("Game Over - Out of Clicks"));
    }
    return new WorldEnd(false, this.makeScene());
  }

  public WorldScene makeFinalScene(String s) {
    WorldScene acc = this.getEmptyScene();
    acc.placeImageXY(background, gridsize * cellSize / 2, gridsize * cellSize / 2);
    acc.placeImageXY((new TextImage(s, cellSize, Color.BLACK)), gridsize * cellSize / 2, gridsize
        * cellSize / 2);
    return acc;
  }

  public void onMouseClicked(Posn pos) {
    this.mainClick(getClickedCell(pos));
    this.numOfClicks -= 1;
  }

  // get clicked cell/its color
  // new method that goes through the board
  // check if each cell is flooded,
  // if so, change the color and check if its neighbors are the same color as
  // the cell you clicked on
  // if it is, set those to flooded as well
  public void mainClick(Cell clickedcell) {
    for (Cell c : cells) {
      if (c.isFlooded) {
        c.color = clickedcell.color;
        if (c.left != null && c.left.color.equals(clickedcell.color)) {
          c.left.isFlooded = true;
        }
        if (c.right != null && c.right.color.equals(clickedcell.color)) {
          c.right.isFlooded = true;
        }
        if (c.bottom != null && c.bottom.color.equals(clickedcell.color)) {
          c.bottom.isFlooded = true;
        }
        if (c.top != null && c.top.color.equals(clickedcell.color)) {
          c.top.isFlooded = true;
        }
      }
    }
  }

  // helper function that compares an X and a Y value to an ArrayList<Cell3> and
  // returns the Cell3 from the ArrayList that touches the given X or Y.
  // this helps determine which Cell3 was clicked on.
  public Cell getClickedCell(Posn loc) { // 22 89
    return this.cells.get((Math.floorDiv(loc.x, Flood.cellSize))
        + (Math.floorDiv(loc.y, Flood.cellSize) * gridsize));
  }

  // a helper function that
  // checks if the number of colors is between 3 and 8 inclusive, else throws an
  // exception. The limit of the online game is 3 to 8 colors so we thought we
  // would use the same range.
  public int goodNumOfColors() {
    if (this.howManyColors <= 8 && this.howManyColors >= 3) {
      return this.howManyColors;
    }
    else {
      throw new RuntimeException("Please use between 3 & 9 colours");
    }
  }

  // helper to determine whether player has lost
  public int goodNumOfClicks() {
    if (this.numOfClicks < 0) {
      return 0;
    }
    else {
      return this.numOfClicks;
    }
  }

  // a helper function that checks if all cells in an ArrayList<Cell> are
  // flooded
  public boolean allCellsFlooded() {
    boolean isTrue = true;
    for (Cell c : cells) {
      if (!(c.isFlooded)) {
        isTrue = false;
      }
    }
    return isTrue;
  }

  // make top, left, right & bottom for the Cell3
  public void makePartner() {
    cells.get(0).isFlooded = true;
    for (int index = 0; index <= ((Flood.gridsize * Flood.gridsize) - 1); index++) {
      if (cells.get(index).x > 0) {
        cells.get(index).left = cells.get(index - 1);
      }
      else {
        cells.get(index).left = null;
      }
      if (cells.get(index).x < gridsize - 1) {
        cells.get(index).right = cells.get(index + 1);
      }
      else {
        cells.get(index).right = null;
      }
      if (cells.get(index).y > 0) {
        cells.get(index).top = cells.get(index - gridsize);
      }
      else {
        cells.get(index).top = null;
      }

      if (cells.get(index).y < gridsize - 1) {
        cells.get(index).bottom = (cells.get(index + gridsize));
      }
      else {
        cells.get(index).bottom = null;
      }
    }
  }
}

// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the
  // screen
  int x;
  int y;
  Color color;
  boolean isFlooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean isFlooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.isFlooded = isFlooded;
    this.left = null;
    this.right = null;
    this.top = null;
    this.bottom = null;
  }

  public WorldImage drawCell(Color color2) {
    return new RectangleImage(Flood.cellSize, Flood.cellSize, "solid", color2);
  }
}

class Examples {

  // some sample cells
  Cell maincell;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;

  ArrayList<Cell> testList = new ArrayList<Cell>();

  public void TestCellsVoid() {
    maincell = new Cell(9, 8, Color.CYAN, false);
    cell2 = new Cell(90, 90, Color.BLUE, false);
    cell3 = new Cell(3, 1, Color.GREEN, false);
  }

  // an empty list
  ArrayList<Cell> mtlist = new ArrayList<Cell>();

  // intial flood
  Flood f2;

  // hashmap of colours
  HashMap<Integer, Color> allcolors = new HashMap<Integer, Color>();
  // random
  Random rand = new Random();

  void createColors() {
    // here is the list of all possible colors. You can only go up
    // to 8 colors, just like in the online game.
    allcolors.put(7, Color.WHITE);
    allcolors.put(6, Color.BLACK);
    allcolors.put(3, Color.BLUE);
    allcolors.put(1, Color.YELLOW);
    allcolors.put(2, Color.RED);
    allcolors.put(4, new Color(225, 120, 0));
    allcolors.put(5, Color.magenta);
    allcolors.put(0, Color.GREEN);

  }

  // tests drawCell
  public boolean testDrawCell(Tester t) {
    this.TestCellsVoid();
    return t.checkExpect(maincell.drawCell(Color.BLUE), new RectangleImage(20, 20, "solid",
        Color.BLUE))
        && t.checkExpect(cell2.drawCell(Color.GREEN), new RectangleImage(20, 20, "solid",
            Color.GREEN));
  }

  // test randomColor
  public boolean testRandColor(Tester t) {
    this.createColors();
    new Flood(testList, 5, allcolors);
    Color c = testList.get(0).color;
    Color c2 = testList.get(1).color;
    return t.checkNumRange(c.getRed(), 0, 256) && t.checkNumRange(c2.getBlue(), 0, 256)
        && t.checkNumRange(c2.getGreen(), 0, 256);
  }

  // test makePartner
  public boolean testmakePartner(Tester t) {
    this.createColors();
    new Flood(testList, 6, allcolors);
    return t.checkExpect(testList.get(0).x, 0)
        && t.checkExpect(testList.get(Flood.gridsize + 1).x, 1)
        && t.checkExpect(testList.get(Flood.gridsize + 3).y, 1);
  }

  // test makePartner
  public boolean testmakePosn(Tester t) {
    this.createColors();
    new Flood(testList, 6, allcolors);
    return t.checkExpect(testList.get(0), testList.get(0));
  }

  // test good colours higher howManyColors than should be
  public boolean testgoodColours1(Tester t) {
    Flood f5 = new Flood(testList, 4, allcolors);
    int f4 = 9;
    f5.howManyColors = f4;
    return t.checkException(new RuntimeException("Please use between 3 & 9 colours"), f5,
        "goodNumOfColors");
  }

  // test good colours lower howManyColors than should be
  public boolean testgoodColours2(Tester t) {
    Flood f5 = new Flood(testList, 4, allcolors);
    int f4 = 1;
    f5.howManyColors = f4;
    return t.checkException(new RuntimeException("Please use between 3 & 9 colours"), f5,
        "goodNumOfColors");
  }

  // test get clickedcell
  public boolean testgetclickedCell(Tester t) {
    this.createColors();
    Flood f5 = new Flood(testList, 6, allcolors);
    return t.checkExpect(f5.getClickedCell(new Posn(40, 59)), testList.get(42))
        && t.checkExpect(f5.getClickedCell(new Posn(100, 60)), testList.get(65))
        && t.checkExpect(f5.getClickedCell(new Posn(22, 89)), testList.get(81));
  }

  // testing randomInt
  boolean testrandomInt(Tester t) {
    return t.checkOneOf("test randomInt", this.rand.nextInt(6), 0, 1, 2, 3, 4, 5, 6)
        && t.checkOneOf("test randomInt", this.rand.nextInt(13), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13)
            && t.checkNoneOf("test randomInt", this.rand.nextInt(10), 11, 12, 13, 14, 15, 16);
  }

  // test background, can change gridsize above in Flood class, and this test
  // won't be affected :)
  // The second test WILL be affected though, so change that
  // boolean testBackground(Tester t) {
  // // /Flood f6 = new Flood(testList, 6, allcolors, 20);
  // return t.checkExpect(Flood.background, new RectangleImage(Flood.gridsize *
  // 20,
  // Flood.gridsize * 20, OutlineMode.SOLID, Color.WHITE))
  // && t.checkExpect(Flood.background, new RectangleImage(400, 400,
  // OutlineMode.SOLID,
  // Color.WHITE));
  // }

  // HOW TO TEST FOR A WORLDSCENE?!
  // // test make a final scene, returns worldScene
  // boolean testmakeafinalScene(Tester t) {
  // this.createColors();
  // Flood f3 = new Flood(testList, 7, allcolors);
  // return t.checkExpect(f3.makeFinalScene("Hello!"), new
  // WorldScene.placeImageXY(Flood.background, Flood.gridsize * Flood.cellSize /
  // 2, Flood.gridsize * Flood.cellSize / 2);
  // .placeImageXY((new TextImage("Hello!", 40, Color.BLACK)), Flood.gridsize *
  // Flood.cellSize / 2, Flood.gridsize
  // * Flood.cellSize / 2));
  // }

  // test

  // test goodNumOfClicks
  boolean testGoodNumOfClicks(Tester t) {
    this.createColors();
    Flood f1 = new Flood(testList, 5, allcolors);
    return t.checkExpect(f1.goodNumOfClicks(), Flood.gridsize
        + (Math.floorDiv(Flood.gridsize, 2)))
        && t.checkExpect(f1.goodNumOfClicks() - 10, 20)
        && t.checkExpect(f1.goodNumOfClicks() - 30, 0);
  }

  // test on key event
  boolean testonKeyEvent(Tester t) {
    this.createColors();
    Flood f1 = new Flood(testList, 5, allcolors);
    f1.onKeyEvent("r");
    return t.checkExpect(f1.howManyColors, 5);
  }

  void initData() {
    this.createColors();
    // 8 colours, no more than 8, no less than 3
    f2 = new Flood(mtlist, 3, allcolors);
  }

  void testGame(Tester t) {
    this.initData();
    f2.bigBang(Flood.gridsize * Flood.gridsize, Flood.gridsize * Flood.gridsize, 0.1);

  }
}