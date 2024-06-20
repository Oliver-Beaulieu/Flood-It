import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import tester.*;

// Represents a cell 
class Cell {
  int x;
  int y;
  Color color;
  boolean flooded;
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = false;
  }

  // Draws each cell individually
  WorldImage drawCell() {
    return new RectangleImage(20, 20, OutlineMode.SOLID, this.color);
  }

  // EFFECT: Changes left and right to given cell
  // Helper function for initBoard, sets left to a given cell and the given cell.right to this
  void setPointersLeft(Cell that) {
    this.left = that;
    that.right = this;
  }

  // EFFECT: Changes top and bottom to given cell
  //Helper function for initBoard, sets top to a given cell, and the given bottom to this
  void setPointersTop(Cell that) {
    this.top = that;
    that.bottom = this;
  }
}

// Represents a flood it world
class FloodItWorld extends World {
  ArrayList<Cell> board;
  int size;
  int numColors;
  int cellSize = 20;
  int steps;
  int time;
  int tickCount;
  int maxSteps;
  boolean gameWon;
  boolean gameLost;

  FloodItWorld(int size, int numColors) {
    this.size = size;
    this.numColors = numColors;
    this.board = new ArrayList<Cell>();
    this.steps = 0;
    this.gameLost = false;
    this.initBoard();
  }
  
  FloodItWorld(int size, int numColors, int maxSteps) {
    this.size = size;
    this.numColors = numColors;
    this.board = new ArrayList<Cell>();
    this.steps = 0;
    this.time = 0;
    this.tickCount = 0;
    this.maxSteps = this.size + this.numColors;
    this.gameLost = false;
    this.initBoard();
  }

  // EFFECT: initilizes the board with cells
  // Initialize the board with cells
  void initBoard() {
    ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE,
        Color.ORANGE, Color.MAGENTA, Color.PINK));

    Random rand = new Random();
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        Color color = colors.get(rand.nextInt(numColors));
        Cell cell = new Cell(x, y, color);
        this.board.add(cell);
        if (x > 0) {
          cell.setPointersLeft(this.board.get(this.board.size() - 2));
        }
        if (y > 0) {
          cell.setPointersTop(this.board.get(this.board.size() - size - 1));
        }
      }
    }
    Cell originalCell = this.getCell(0, 0);
    originalCell.flooded = true;
    this.floodHelper(originalCell, originalCell.color);
    System.out.println(this.board.size());
  }

  // Returns the cell at a given x and y
  Cell getCell(int x, int y) {
    return this.board.get(y * this.size + x);
  }
  
  // EFFECT: Updates scene every tick
  // Updates flood game scene every tick
  public void onTick() {
    this.tickCount ++;
    if (this.tickCount % 15 == 0) {
      this.time++;
    }
    this.makeScene();
  }

  // Draws the game scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.size * this.cellSize, this.size * this.cellSize); 
    if (this.gameWon) {
     scene.placeImageXY(new TextImage("You win!", 40, Color.black),
         this.size * this.cellSize / 2, this.size * this.cellSize / 2);
     return scene;
    }
    if (this.gameLost) {
      scene.placeImageXY(new TextImage("You lose!", 40, Color.red),
          this.size * this.cellSize / 2, this.size * this.cellSize / 2);
      return scene;
    }
    for (Cell cell : this.board) {
      scene.placeImageXY(cell.drawCell(), cell.x * this.cellSize + this.cellSize / 2, 
          cell.y * this.cellSize + this.cellSize / 2);
    }
    
    scene.placeImageXY(new TextImage("Steps taken: " + this.steps,
        20, Color.BLACK), 100, this.size * this.cellSize + 20);
    
    scene.placeImageXY(new TextImage("Max steps: " + this.maxSteps,
        20, Color.BLACK), 100, this.size * this.cellSize + 50);
    
    scene.placeImageXY(new TextImage("Time Elapsed: " + this.time,
        20, Color.BLACK), 100, this.size * this.cellSize + 80);
    
    return scene;
}
    
  // EFFECT: floods cells on click
  // Convert grid cells on click
  public void onMouseClicked(Posn pos) {
    int x = pos.x / this.cellSize;
    int y = pos.y / this.cellSize;
    Cell clicked = this.getCell(x, y);
    Color original = this.getCell(0, 0).color;
    
    // Only flood if the clicked cell is not the same color as the original flooded area
    if (!clicked.color.equals(original)) {
      System.out.println("CLICK");
      this.flood(clicked.color);
      this.steps++;
      
      if (this.allFlooded()) {
        this.gameWon = true;
        System.out.println("WON");
      }
      if (this.steps > this.maxSteps) {
        this.gameLost = true;
      }
    }
  }
  
  
  // EFFECT: Re-inits board
  // Resets board if "r" key is pressed
  public void onKeyEvent(String keyPress) {
    if (keyPress.equals("r")) {
      this.steps = 0;
      this.gameWon = false;
      this.gameLost = false;
      this.time = 0;
      this.board.clear();
      this.initBoard();
    }
  }

  // EFFECT: adds touching cells to flood
  // Fills the flood in the grid
  void flood(Color target) {
    Color original = this.getCell(0, 0).color;
    if (original.equals(target)) {
      return;
    }
    ArrayList<Cell> toFlood = new ArrayList<Cell>();
    for (Cell cell : this.board) {
      if (cell.flooded) {
        toFlood.add(cell);
      }
    }
    for (Cell cell : toFlood) {
      cell.color = target;
      this.floodHelper(cell, target);
    }
  }

  // EFFECT: Changes color of cells of the same original color
  // Helper method for flood fill that spreads the color to all cells of the same original color
  void floodHelper(Cell cell, Color target) {
    if (cell != null && (cell.color.equals(target))) {
      cell.color = target;
      cell.flooded = true;
      if (cell.left != null && !cell.left.flooded) {
        this.floodHelper(cell.left, target);
      }
      if (cell.top != null && !cell.top.flooded) {
        this.floodHelper(cell.top, target);
      }
      if (cell.right != null && !cell.right.flooded) {
        this.floodHelper(cell.right, target);
      }
      if (cell.bottom != null && !cell.bottom.flooded) {
        this.floodHelper(cell.bottom, target);
      }
    }
  }

  //Returns whether or not the entire board is flooded
  boolean allFlooded() {
    System.out.println("WORKING");
    for (Cell cell : this.board) {
      if (!cell.flooded) {
        return false;
      }
    }
    return true;
  }
}


// Represents examples of Flood It game
class ExamplesGame {

  Cell cellOne;
  Cell cellTwo;
  FloodItWorld worldOne;
  FloodItWorld worldTwo;

  void initData() {
    cellOne = new Cell(10, 10, Color.RED);
    cellTwo = new Cell(20, 20, Color.BLUE);
    worldOne = new FloodItWorld(50, 6, 10);
    worldTwo = new FloodItWorld(25, 3, 10);
  }

  void testGame(Tester t) {
    int size = 10;
    
    FloodItWorld world = new FloodItWorld(size, 5, 100);
    world.bigBang(300, 300, 0.1); 
  }

  void testSetPointersLeft(Tester t) {
    Cell c1 = new Cell(0, 0, Color.RED);
    Cell c2 = new Cell(1, 0, Color.RED);
    c2.setPointersLeft(c1);
    t.checkExpect(c2.left, c1);
    t.checkExpect(c1.right, c2);
  }

  void testSetPointersTop(Tester t) {
    Cell c1 = new Cell(0, 0, Color.RED);
    Cell c2 = new Cell(0, 1, Color.RED);
    c2.setPointersTop(c1);
    t.checkExpect(c2.top, c1);
    t.checkExpect(c1.bottom, c2);  
  }

  void testInitBoard(Tester t) {
    FloodItWorld testWorld1 = new FloodItWorld(50, 6, 10);
    t.checkExpect(testWorld1.board.size(), 2500);
    FloodItWorld testWorld2 = new FloodItWorld(100, 5, 10);
    t.checkExpect(testWorld2.board.size(), 10000);
  }

  boolean testDrawCell(Tester t) {
    initData();
    WorldImage image1 = cellOne.drawCell();
    WorldImage image2 = cellTwo.drawCell();
    WorldImage sceneExpected = new RectangleImage(20, 20, OutlineMode.SOLID, Color.red);
    WorldImage sceneExpected2 = new RectangleImage(20, 20, OutlineMode.SOLID, Color.blue);
    return t.checkExpect(image1, sceneExpected) 
        &&
        t.checkExpect(image2, sceneExpected2);
  }

  void testGetCell(Tester t) {
    initData();
    t.checkExpect(worldOne.getCell(0, 0).flooded, true);
    t.checkExpect(worldOne.getCell(1, 1).flooded, false);
  }

  boolean testOnMouseClicked(Tester t) {
    initData();
    Cell supercell = worldOne.getCell(0, 0);
    Cell supercell2 = worldOne.getCell(0, 0);
    supercell.color = Color.RED;
    supercell2.color = Color.yellow;
    worldOne.getCell(1, 0).color = Color.blue;
    worldOne.onMouseClicked(new Posn(25, 15));
    return t.checkExpect(supercell.color, Color.BLUE) 
        &&
        t.checkExpect(supercell2.color, Color.BLUE);
  }

  boolean makeScene(Tester t) {
    initData();
    WorldScene scene1 = worldOne.makeScene();
    WorldScene scene2 = worldTwo.makeScene();
    return t.checkExpect(scene1.width, worldOne.size * worldOne.cellSize) 
        &&
        t.checkExpect(scene1.height, worldOne.size * worldOne.cellSize) 
        &&
        t.checkExpect(scene2.width, worldOne.size * worldOne.cellSize) 
        &&
        t.checkExpect(scene2.height, worldOne.size * worldOne.cellSize);
  }

  boolean testFlood(Tester t) {
    initData();
    worldOne.getCell(0, 0).color = Color.RED;
    worldOne.getCell(1, 0).color = Color.RED;
    worldOne.getCell(0, 1).color = Color.RED;
    worldOne.flood(Color.BLUE);
    return t.checkExpect(worldOne.getCell(0, 0).color, Color.BLUE) 
        &&
        t.checkExpect(worldOne.getCell(1, 0).color, Color.RED);
  }

  boolean testFloodHelper(Tester t) {
    initData();
    Cell cell1 = worldOne.getCell(0, 0);
    Cell cell2 = worldOne.getCell(1, 0);
    Cell cell3 = worldOne.getCell(0, 1);
    cell1.color = Color.RED;
    cell2.color = Color.RED;
    cell3.color = Color.RED;
    cell1.flooded = true;
    worldOne.floodHelper(cell1, Color.BLUE);
    return t.checkExpect(cell1.color, Color.BLUE) 
        && t.checkExpect(cell2.color, Color.BLUE) 
        && t.checkExpect(cell3.color, Color.BLUE);
  }

  void testAllFlooded(Tester t) {
    initData();
    for (Cell cell : worldOne.board) { 
      cell.flooded = true;
    }

    t.checkExpect(worldOne.allFlooded(), true);

    worldOne.getCell(0, 0).flooded = false;
    t.checkExpect(worldOne.allFlooded(), false);
  }
}
