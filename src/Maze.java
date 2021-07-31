import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


/*
 * pressing "d" will start the depth-first search
 * 
 * pressing "b" will start the breadth-first search
 * 
 * pressing the arrow keys will allow the user to manually 
 * traverse the maze 
 * 
 * pressing "r" will display a new maze 
 * 
 * if you have already run the dfs, bfs, or manual on this maze 
 * and want to try dfs, bfs, or manaul on this maze again,
 * just press the corresponding key again 
 * 
 */


//Represents a single square of the game area
class Cell {
  int name;
  int x;
  int y;
  Color color;
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
  boolean leftEdge;
  boolean topEdge;
  boolean rightEdge;
  boolean bottomEdge;

  // constructor
  Cell(int name, int x, int y, Color color, Cell left, Cell top, Cell right, Cell bottom,
      boolean leftEdge, boolean topEdge, boolean rightEdge, boolean bottomEdge) {
    this.name = name;
    this.x = x;
    this.y = y;
    this.color = color;
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.leftEdge = leftEdge;
    this.topEdge = topEdge;
    this.rightEdge = rightEdge;
    this.bottomEdge = bottomEdge;
  }

  // convenience constructor
  Cell(int name, int x, int y) {
    this.name = name;
    this.x = x;
    this.y = y;
    this.color = Color.gray;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
    this.leftEdge = false;
    this.topEdge = false;
    this.rightEdge = false;
    this.bottomEdge = false;
  }

  // EFFECT: draws this single cell
  void drawCell(WorldScene ws, int cellSize) {
    WorldImage cellImage = new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, this.color);
    cellImage = cellImage.movePinholeTo(new Posn(0, 0));

    if (!leftEdge) {
      cellImage = new OverlayImage(new LineImage(new Posn(0, cellSize), Color.BLACK)
          .movePinholeTo(new Posn(cellSize / 2, 0)), cellImage);
    }
    if (!topEdge) {
      cellImage = new OverlayImage(
          new LineImage(new Posn(cellSize, 0), Color.BLACK).movePinhole(0, cellSize / 2),
          cellImage);
    }
    if (!rightEdge) {
      cellImage = new OverlayImage(
          new LineImage(new Posn(0, cellSize), Color.BLACK).movePinhole(-cellSize / 2 + 1, 0),
          cellImage);
    }
    if (!bottomEdge) {
      cellImage = new OverlayImage(
          new LineImage(new Posn(cellSize, 0), Color.BLACK).movePinhole(0, -cellSize / 2 + 1),
          cellImage);
    }

    ws.placeImageXY(cellImage, this.x, this.y);
  }

  // EFFECT: creates an Edge fron the name of this cell to
  // the name of that cell
  void makeEdge(Cell that, ArrayList<Edge> worklist) {
    if (that != null) {
      worklist.add(new Edge(that.name, this.name));
    }
  }

  // EFFECT: changes color field of this Cell to that Color
  void changeColor(Color that) {
    this.color = that;
  }

  // EFFECT: opens the border between this cell and the given cell
  // assume "to cell" is either to the right or below this cell
  void union(Cell to) {
    if (this.right == to) {
      this.rightEdge = true;
      to.leftEdge = true;
    }
    else {
      this.bottomEdge = true;
      to.topEdge = true;
    }
  }

  // returns the cells that is possible to travel to from this cell
  ArrayList<Cell> neighbors() {
    ArrayList<Cell> listOfNeighbors = new ArrayList<Cell>();

    if (this.topEdge) {
      listOfNeighbors.add(this.top);
    }
    if (this.leftEdge) {
      listOfNeighbors.add(this.left);
    }
    if (this.bottomEdge) {
      listOfNeighbors.add(this.bottom);
    }
    if (this.rightEdge) {
      listOfNeighbors.add(this.right);
    }
    return listOfNeighbors;
  }

  // returns the cell in the direction indicated by the key
  Cell findNext(String key) {
    Cell next = null;

    if ("left".equals(key)) {
      next = this.left;
    }
    else if ("right".equals(key)) {
      next = this.right;
    }
    else if ("up".equals(key)) {
      next = this.top;
    }
    else if ("down".equals(key)) {
      next = this.bottom;
    }
    if (this.neighbors().contains(next)) {
      return next;
    }
    else {
      return null;
    }
  }

}

// methods used to manipulate an ArrayList
class ArrayListUtils {

  // creates a grid of cells using the given size
  ArrayList<Cell> initilizeBoard(int length, int height, int cellSize, int seed) {
    ArrayList<Cell> accum = new ArrayList<Cell>();

    for (int i = 0; i < height; i = i + 1) {
      for (int j = 0; j < length; j = j + 1) {
        accum.add(
            new Cell(i * length + j, cellSize * j + cellSize / 2, cellSize * i + cellSize / 2));
        new ArrayListUtils().connectCells(accum, j, i, length, i * length + j);
      }
    }

    accum.get(0).changeColor(Color.pink);
    accum.get(accum.size() - 1).changeColor(Color.green);

    new ArrayListUtils().createMaze(accum, seed);
    return accum;
  }

  // EFFECT: connects the cells corresponding to the given parameters
  // in the given grid of cells
  void connectCells(ArrayList<Cell> board, int j, int i, int length, int place) {
    if (j > 0) {
      board.get(place).left = board.get(place - 1);
      board.get(place - 1).right = board.get(place);
    }
    if (i > 0) {
      board.get(place).top = board.get(place - length);
      board.get(place - length).bottom = board.get(place);
    }
  }

  // EFFECT: connects cells from this board in a such a way
  // that represents a valid maze (minimum spanning tree)
  void createMaze(ArrayList<Cell> board, int seed) {
    HashMap<Integer, Integer> representatives = new HashMap<Integer, Integer>();
    ArrayList<Edge> worklist = new ArrayList<Edge>(); // all edges in graph

    for (Cell c : board) {
      // add cell to hashmap of cells (represented by ints)
      representatives.put(c.name, c.name);
      // add cell's edges to list of edges
      for (Cell neighbor : new ArrayList<Cell>(Arrays.asList(c.left, c.top))) {
        c.makeEdge(neighbor, worklist);
      }
    }

    int numEdges = 0; // edges used
    int numNodesMinus1 = board.size() - 1;
    Random rand = new Random(seed);

    while (numEdges < numNodesMinus1) {
      Edge e = worklist.get(rand.nextInt(worklist.size() - 1));

      int fromRep = new HashMapUtils().find(representatives, e.from);
      int toRep = new HashMapUtils().find(representatives, e.to);
      if (fromRep != toRep) {
        representatives.replace(toRep, fromRep); // union
        e.unionCells(board);
        worklist.remove(e);
        numEdges = numEdges + 1;
      }
    }
  }

}

//Represents a mutable collection of items
interface ICollection<T> {
  // Is this collection empty?
  boolean isEmpty();

  // EFFECT: adds the item to the collection
  void add(T item);

  // Returns the first item of the collection
  // EFFECT: removes that first item
  T remove();

}

// represents a stack, first in last out
class Stack<T> implements ICollection<T> {
  Deque<T> contents;

  Stack() {
    this.contents = new ArrayDeque<T>();
  }

  // is this stack empty?
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // Returns the first item of the stack
  // EFFECT: removes that first item
  public T remove() {
    return this.contents.removeFirst();
  }

  // EFFECT: adds the item to the stack
  public void add(T item) {
    this.contents.addFirst(item);
  }

}

//represents a queue, first in first out
class Queue<T> implements ICollection<T> {
  Deque<T> contents;

  Queue() {
    this.contents = new ArrayDeque<T>();
  }

  // is this queue empty?
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // Returns the first item of the queue
  // EFFECT: removes that first item
  public T remove() {
    return this.contents.removeFirst();
  }

  // EFFECT: adds the item to the queue
  public void add(T item) {
    this.contents.addLast(item); // NOTE: Different from Stack!
  }

}

// methods used to manipulate a HashMap
class HashMapUtils {

  // finds the representative of the given name in the HashMap
  Integer find(HashMap<Integer, Integer> representatives, int name) {
    if (representatives.get(name) == name) {
      return name;
    }
    else {
      return find(representatives, representatives.get(name));
    }
  }

  // returns the path back from current to start
  // EFFECT: changes colors of the cells in the path
  Deque<Cell> findCorrectPath(HashMap<Cell, Cell> cameFrom, Cell start, Cell current) {
    if (current == start) {
      Deque<Cell> temp = new ArrayDeque<Cell>();
      start.changeColor(Color.green);
      temp.addFirst(start);
      return temp;
    }
    else {
      Deque<Cell> temp = new HashMapUtils().findCorrectPath(cameFrom, start, cameFrom.get(current));
      current.changeColor(Color.green);
      temp.addFirst(current);
      return temp;
    }
  }

}

// represents an Edge between two Cells 
// represented by ints (cell's name)
class Edge {
  int from;
  int to;

  // constructor
  Edge(int from, int to) {
    this.from = from;
    this.to = to;
  }

  // EFFECT: opens the border represented by this edge
  void unionCells(ArrayList<Cell> board) {
    Cell fromCell = board.get(this.from);
    Cell toCell = board.get(this.to);

    fromCell.union(toCell);
  }

}

// represents a maze game
class Maze extends World {
  ArrayList<Cell> board;
  int length;
  int height;
  int cellSize;
  IState state;
  ICollection<Cell> worklist;
  HashMap<Cell, Boolean> visited;
  HashMap<Cell, Cell> cameFrom;
  Cell target;

  // constructor
  Maze(ArrayList<Cell> board, int length, int height, int cellSize) {
    this.board = board;
    this.length = length;
    this.height = height;
    this.cellSize = cellSize;
    this.state = new Rest();
    // add others?
  }

  // convenience constructor
  Maze(int length, int height) {
    this.length = length;
    this.height = height;
    this.cellSize = Math.min(1000 / length, 600 / height);
    this.board = new ArrayListUtils().initilizeBoard(length, height, cellSize,
        new Random().nextInt(9999));
    this.state = new Rest();
    // add others?
  }

  // draws this maze
  public WorldScene makeScene() {
    int sceneLength = 1000;
    int sceneHeight = 600;
    WorldScene ws = new WorldScene(sceneLength, sceneHeight);

    for (Cell c : this.board) {
      c.drawCell(ws, this.cellSize);
    }
    return ws;
  }

  // runs a step in the maze for each tick during dfs and bfs
  public void onTick() {
    if (this.state.isAuto()) {
      this.mazeStep();
    }
  }

  // key handler for this maze game
  public void onKeyEvent(String key) {
    if ("d".equals(key)) {
      this.state = new Auto();
      resetSolution(new Stack<Cell>());
    }
    else if ("b".equals(key)) {
      this.state = new Auto();
      resetSolution(new Queue<Cell>());
    }
    else if ("r".equals(key)) {
      this.board = new ArrayListUtils().initilizeBoard(length, height, cellSize,
          new Random().nextInt(9999));
      this.state = new Rest();

    }

    else if ("left".equals(key)) {
      this.arrowPress(key);
    }
    else if ("right".equals(key)) {
      this.arrowPress(key);
    }
    else if ("up".equals(key)) {
      this.arrowPress(key);
    }
    else if ("down".equals(key)) {
      this.arrowPress(key);
    }

  }

  // EFFECT: performs one step in this maze
  // by evaluating one cell off the worklist
  void mazeStep() {
    if (!this.state.isRest()) {
      Cell c = worklist.remove();

      if (visited.get(c)) {
        // do nothing
      }
      else if (c == target) {
        new HashMapUtils().findCorrectPath(cameFrom, board.get(0), target);
        this.state = new Rest();
      }
      else {
        visited.put(c, true);
        c.changeColor(Color.pink);

        for (Cell n : c.neighbors()) {
          if (!visited.get(n)) {
            cameFrom.putIfAbsent(n, c);
            if (this.state.isAuto()) {
              worklist.add(n);
            }
          }
        }
      }
    }
  }

  // EFFECT: evaluates the press of an arrow key
  // and changes color if moved and prepares for and
  // initiates the next step in the maze
  void arrowPress(String key) {
    if (this.state.isManual()) {
      Cell prev = this.worklist.remove();
      Cell c = prev.findNext(key);

      if (c != null) {
        prev.changeColor(new Color(255, 141, 161));
        c.changeColor(Color.pink);

        this.cameFrom.putIfAbsent(c, prev);
        this.worklist.add(c);
        this.worklist.add(c);
        // adding twice is the most efficient way to
        // keep c on the worklist after calling mazeStep

        this.mazeStep();
      }
      else {
        this.worklist.add(prev);
      }
    }
    // can interrupt auto to start manual; resets solve
    else {
      this.state = new Manual();
      resetSolution(new Queue<Cell>());
    }
  }

  // EFFECT: resets the board and solution related fields in this maze game
  void resetSolution(ICollection<Cell> worklist) {
    this.worklist = worklist;
    this.visited = new HashMap<Cell, Boolean>();
    this.cameFrom = new HashMap<Cell, Cell>();
    this.target = board.get(board.size() - 1);

    for (Cell c : board) {
      visited.put(c, false);
      c.changeColor(Color.gray);
    }

    this.worklist.add(board.get(0));
    cameFrom.put(board.get(0), board.get(0));

    board.get(0).changeColor(Color.pink);
    board.get(board.size() - 1).changeColor(Color.green);

  }

}

// represents the 3 states in our game
interface IState {

  // is this state auto?
  boolean isAuto();

  // is this state manual?
  boolean isManual();

  // is this state at rest?
  boolean isRest();

}

// represents the 3 states in our game
abstract class AState implements IState {

  // is this state auto?
  public boolean isAuto() {
    return false;
  }

  // is this state manual?
  public boolean isManual() {
    return false;
  }

  // is this state at rest?
  public boolean isRest() {
    return false;
  }
}

// represents an automatic solution
class Auto extends AState {
  Auto() {
  }

  // is this auto state auto?
  public boolean isAuto() {
    return true;
  }

}

// represents a manual solution
class Manual extends AState {
  Manual() {
  }

  // is this manual state manual?
  public boolean isManual() {
    return true;
  }
}

// Represents when our maze game is at rest
class Rest extends AState {
  Rest() {
  }

  // is this rest state at rest?
  public boolean isRest() {
    return true;
  }
}

class ExamplesMaze {

  HashMapUtils HMU = new HashMapUtils();
  ArrayListUtils ALU = new ArrayListUtils();

  Maze maze1 = new Maze(3, 3);
  Maze maze2 = new Maze(10, 10);
  Maze maze3 = new Maze(20, 12);
  Maze maze4 = new Maze(100, 60);

  Edge edge1 = new Edge(0, 1);
  Edge edge2 = new Edge(1, 2);
  Edge edge3 = new Edge(3, 4);
  Edge edge4 = new Edge(4, 5);
  Edge edge5 = new Edge(6, 7);
  Edge edge6 = new Edge(7, 8);
  Edge edge7 = new Edge(0, 3);
  Edge edge8 = new Edge(1, 4);
  Edge edge9 = new Edge(2, 5);
  Edge edge10 = new Edge(3, 6);
  Edge edge11 = new Edge(4, 7);
  Edge edge12 = new Edge(5, 8);
  ArrayList<Edge> listEdge;

  HashMap<Integer, Integer> rep1 = new HashMap<Integer, Integer>();

  Cell cell0;
  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;
  Cell cell6;
  Cell cell7;
  Cell cell8;
  ArrayList<Cell> list123456789;

  Cell c0;
  Cell c1;
  Cell c2;
  Cell c3;
  ArrayList<Cell> list2x2;

  ArrayDeque<Cell> deq1;
  ArrayDeque<Cell> deq2;
  ICollection<Cell> stack1;
  ICollection<Cell> stack2;
  ICollection<Cell> queue1;
  ICollection<Cell> queue2;

  IState manual1 = new Manual();
  IState rest1 = new Rest();
  IState auto1 = new Auto();

  
  void testBigBang(Tester t) {
    Maze w = new Maze(20, 12);
    int worldWidth = 1000;
    int worldHeight = 600;
    double tickRate = 0.01;
    w.bigBang(worldWidth, worldHeight, tickRate);
  }
   

  void initData() {

    rep1.clear();
    for (int i = 0; i < 9; i = i + 1) {
      rep1.put(i, i);
    }

    this.cell0 = new Cell(0, 100, 100);
    this.cell0.color = Color.pink;
    this.cell1 = new Cell(1, 300, 100);
    this.cell2 = new Cell(2, 500, 100);
    this.cell3 = new Cell(3, 100, 300);
    this.cell4 = new Cell(4, 300, 300);
    this.cell5 = new Cell(5, 500, 300);
    this.cell6 = new Cell(6, 100, 500);
    this.cell7 = new Cell(7, 300, 500);
    this.cell8 = new Cell(8, 500, 500);
    this.cell8.color = Color.green;
    this.list123456789 = new ArrayList<Cell>(Arrays.asList(this.cell0, this.cell1, this.cell2,
        this.cell3, this.cell4, this.cell5, this.cell6, this.cell7, this.cell8));
    ALU.connectCells(list123456789, 1, 0, 3, 1);
    ALU.connectCells(list123456789, 2, 0, 3, 2);
    ALU.connectCells(list123456789, 0, 1, 3, 3);
    ALU.connectCells(list123456789, 1, 1, 3, 4);
    ALU.connectCells(list123456789, 2, 1, 3, 5);
    ALU.connectCells(list123456789, 0, 2, 3, 6);
    ALU.connectCells(list123456789, 1, 2, 3, 7);
    ALU.connectCells(list123456789, 2, 2, 3, 8);

    listEdge = new ArrayList<Edge>(Arrays.asList(edge1, edge2, edge3, edge4, edge5, edge6, edge7,
        edge8, edge9, edge10, edge11, edge12));

    this.c0 = new Cell(0, 150, 150);
    this.c0.color = Color.pink;
    this.c1 = new Cell(1, 450, 150);
    this.c2 = new Cell(2, 150, 450);
    this.c3 = new Cell(3, 450, 450);
    this.c3.color = Color.green;
    this.list2x2 = new ArrayList<Cell>(Arrays.asList(this.c0, this.c1, this.c2, this.c3));
    ALU.connectCells(list2x2, 1, 0, 2, 1);
    ALU.connectCells(list2x2, 0, 1, 2, 2);
    ALU.connectCells(list2x2, 1, 1, 2, 3);

    deq1 = new ArrayDeque<Cell>(Arrays.asList(cell0, cell1, cell2));
    deq2 = new ArrayDeque<Cell>(Arrays.asList(cell2, cell4, cell6));
    stack1 = new Stack<Cell>();
    ((Stack<Cell>) stack1).contents = deq1;
    stack2 = new Stack<Cell>();
    ((Stack<Cell>) stack2).contents = deq2;
    queue1 = new Queue<Cell>();
    ((Queue<Cell>) queue1).contents = deq1;
    queue2 = new Queue<Cell>();
    ((Queue<Cell>) queue2).contents = deq2;

  }

  void testOnKeyEvent(Tester t) {
    Maze exMaze = new Maze(5, 3);
    exMaze.board = ALU.initilizeBoard(5, 3, 200, 444);
    exMaze.resetSolution(new Queue<Cell>());

    exMaze.onKeyEvent("b");

    t.checkExpect(exMaze.state, new Auto());
    ICollection<Cell> exQueue = new Queue<Cell>();
    exQueue.add(exMaze.board.get(0));
    t.checkExpect(exMaze.worklist, exQueue);

    exMaze.onKeyEvent("d");

    t.checkExpect(exMaze.state, new Auto());
    ICollection<Cell> exStack = new Stack<Cell>();
    exStack.add(exMaze.board.get(0));
    t.checkExpect(exMaze.worklist, exStack);

    Maze exMaze2 = new Maze(5, 3);
    exMaze2.board = ALU.initilizeBoard(5, 3, 200, 444);
    exMaze2.resetSolution(new Queue<Cell>());

    exMaze.onKeyEvent("up");
    exMaze2.arrowPress("up");

    t.checkExpect(exMaze.board, exMaze2.board);

    exMaze.onKeyEvent("down");
    exMaze2.arrowPress("down");

    t.checkExpect(exMaze.board, exMaze2.board);

    exMaze.onKeyEvent("left");
    exMaze2.arrowPress("left");

    t.checkExpect(exMaze.board, exMaze2.board);

    exMaze.onKeyEvent("right");
    exMaze2.arrowPress("right");

    t.checkExpect(exMaze.board, exMaze2.board);

    ArrayList<Cell> saveBoard = new ArrayList<Cell>(exMaze.board);
    exMaze.onKeyEvent("r");
    t.checkFail(exMaze.board, saveBoard);

  }

  void testOnTick(Tester t) {
    initData();

    Maze exMaze1 = new Maze(5, 3);
    exMaze1.board = ALU.initilizeBoard(5, 3, 200, 476);
    exMaze1.resetSolution(new Queue<Cell>());
    Maze exMaze2 = new Maze(5, 3);
    exMaze2.board = ALU.initilizeBoard(5, 3, 200, 476);
    exMaze2.resetSolution(new Queue<Cell>());

    t.checkExpect(exMaze1.board, exMaze2.board);

    exMaze1.state = new Auto();
    exMaze2.state = new Auto();
    exMaze1.onTick();
    exMaze2.mazeStep();

    t.checkExpect(exMaze1.board, exMaze2.board);

    exMaze1.state = new Rest();
    exMaze1.onTick();
    // no change when not auto

    t.checkExpect(exMaze1.board, exMaze2.board);

    exMaze2.mazeStep();
    t.checkFail(exMaze1.board, exMaze2.board);

  }

  void testResetSolution(Tester t) {
    initData();

    t.checkExpect(maze2.worklist, null);
    t.checkExpect(maze2.visited, null);
    t.checkExpect(maze2.cameFrom, null);
    t.checkExpect(maze2.target, null);

    ICollection<Cell> mtStack = new Stack<Cell>();
    maze2.resetSolution(mtStack);

    mtStack.add(maze2.board.get(0));
    t.checkExpect(maze2.worklist, mtStack);
    HashMap<Cell, Boolean> mtHashMap = new HashMap<Cell, Boolean>();
    for (Cell c : maze2.board) {
      mtHashMap.put(c, false);
    }
    t.checkExpect(maze2.visited, mtHashMap);
    HashMap<Cell, Cell> mtCameFrom = new HashMap<Cell, Cell>();
    mtCameFrom.put(maze2.board.get(0), maze2.board.get(0));
    t.checkExpect(maze2.cameFrom, mtCameFrom);
    t.checkExpect(maze2.target, maze2.board.get(99));
    t.checkExpect(maze2.board.get(99).color, Color.green);
    t.checkExpect(maze2.board.get(0).color, Color.pink);
    // all others gray
    t.checkExpect(maze2.board.get(50).color, Color.gray);
    t.checkExpect(maze2.board.get(1).color, Color.gray);
    t.checkExpect(maze2.board.get(98).color, Color.gray);
  }

  void testArrowPress(Tester t) {
    initData();

    maze1.board = ALU.initilizeBoard(3, 3, 200, 4632);
    Cell m0 = maze1.board.get(0);
    Cell m1 = maze1.board.get(1);
    Cell m2 = maze1.board.get(2);
    Cell m3 = maze1.board.get(3);
    Cell m4 = maze1.board.get(4);
    Cell m5 = maze1.board.get(5);
    Cell m7 = maze1.board.get(7);

    maze1.state = new Manual();
    maze1.worklist = new Queue<Cell>();
    maze1.worklist.add(m0);
    maze1.visited = new HashMap<Cell, Boolean>();
    for (Cell c : maze1.board) {
      maze1.visited.put(c, false);
    }
    maze1.cameFrom = new HashMap<Cell, Cell>();
    maze1.cameFrom.put(m0, m0);

    t.checkExpect(m0.color, Color.pink);
    t.checkExpect(m1.color, Color.gray);

    maze1.arrowPress("left");

    t.checkExpect(m0.color, Color.pink);
    t.checkExpect(((Queue<Cell>) maze1.worklist).contents, new ArrayDeque<Cell>(Arrays.asList(m0)));

    maze1.arrowPress("right");

    t.checkExpect(m0.color, new Color(255, 141, 161));
    t.checkExpect(m1.color, Color.pink);
    t.checkExpect(((Queue<Cell>) maze1.worklist).contents, new ArrayDeque<Cell>(Arrays.asList(m1)));
    t.checkExpect(maze1.visited.get(m1), true);
    t.checkExpect(maze1.cameFrom.get(m1), m0);
    t.checkExpect(maze1.cameFrom.get(m2), m1);
    t.checkExpect(maze1.cameFrom.get(m4), m1);

    maze1.arrowPress("right");

    t.checkExpect(m0.color, new Color(255, 141, 161));
    t.checkExpect(m1.color, new Color(255, 141, 161));
    t.checkExpect(m2.color, Color.pink);
    t.checkExpect(((Queue<Cell>) maze1.worklist).contents, new ArrayDeque<Cell>(Arrays.asList(m2)));
    t.checkExpect(maze1.visited.get(m2), true);
    t.checkExpect(maze1.cameFrom.get(m2), m1);

  }

  void testMazeStep(Tester t) {
    initData();

    maze1.board = ALU.initilizeBoard(3, 3, 200, 4632);
    Cell m0 = maze1.board.get(0);
    Cell m1 = maze1.board.get(1);
    Cell m2 = maze1.board.get(2);
    Cell m3 = maze1.board.get(3);
    Cell m4 = maze1.board.get(4);
    Cell m5 = maze1.board.get(5);
    Cell m6 = maze1.board.get(6);
    Cell m7 = maze1.board.get(7);
    Cell m8 = maze1.board.get(8);
    maze1.state = new Auto();
    maze1.worklist = new Queue<Cell>();
    maze1.worklist.add(m0);
    maze1.visited = new HashMap<Cell, Boolean>();
    for (Cell c : maze1.board) {
      maze1.visited.put(c, false);
    }
    maze1.cameFrom = new HashMap<Cell, Cell>();
    maze1.cameFrom.put(m0, m0);
    maze1.target = m8;

    maze1.mazeStep();

    t.checkExpect(((Queue<Cell>) maze1.worklist).contents, new ArrayDeque<Cell>(Arrays.asList(m1)));
    t.checkExpect(maze1.visited.get(m0), true);
    t.checkExpect(m0.color, Color.pink);
    t.checkExpect(maze1.cameFrom.get(m1), m0);

    t.checkExpect(m1.color, Color.gray);

    maze1.mazeStep();

    t.checkExpect(((Queue<Cell>) maze1.worklist).contents,
        new ArrayDeque<Cell>(Arrays.asList(m4, m2)));
    t.checkExpect(maze1.visited.get(m1), true);
    t.checkExpect(m1.color, Color.pink);
    t.checkExpect(maze1.cameFrom.get(m4), m1);
    t.checkExpect(maze1.cameFrom.get(m2), m1);

    maze1.mazeStep();
    maze1.mazeStep();
    maze1.mazeStep();
    maze1.mazeStep();
    maze1.mazeStep();
    maze1.mazeStep();
    maze1.mazeStep();
    // brings us to the end of the maze

    t.checkExpect(maze1.state, new Rest());
    // part of solution
    t.checkExpect(m1.color, Color.green);
    t.checkExpect(m8.color, Color.green);
    t.checkExpect(m0.color, Color.green);
    t.checkExpect(m7.color, Color.green);
    t.checkExpect(m4.color, Color.green);
    // not part of solution
    t.checkExpect(m6.color, Color.pink);
    t.checkExpect(m2.color, Color.pink);
    t.checkExpect(m5.color, Color.pink);
    t.checkExpect(m3.color, Color.pink);

    maze1.state = new Manual();
    // same but the worklist works differently!

    maze1.board = ALU.initilizeBoard(3, 3, 200, 4632);
    m0 = maze1.board.get(0);
    m1 = maze1.board.get(1);
    m2 = maze1.board.get(2);
    m3 = maze1.board.get(3);
    m4 = maze1.board.get(4);
    m5 = maze1.board.get(5);
    m6 = maze1.board.get(6);
    m7 = maze1.board.get(7);
    m8 = maze1.board.get(8);
    maze1.worklist = new Queue<Cell>();
    maze1.worklist.add(m0);
    maze1.worklist.add(m0);
    maze1.visited = new HashMap<Cell, Boolean>();
    for (Cell c : maze1.board) {
      maze1.visited.put(c, false);
    }
    maze1.cameFrom = new HashMap<Cell, Cell>();
    maze1.cameFrom.put(m0, m0);
    maze1.target = m8;

    maze1.mazeStep();

    t.checkExpect(((Queue<Cell>) maze1.worklist).contents, new ArrayDeque<Cell>(Arrays.asList(m0)));
    t.checkExpect(maze1.visited.get(m0), true);
    t.checkExpect(m0.color, Color.pink);
    t.checkExpect(maze1.cameFrom.get(m1), m0);

    t.checkExpect(m1.color, Color.gray);
    maze1.worklist.remove();
    maze1.worklist.add(m1);
    maze1.worklist.add(m1);
    maze1.mazeStep();

    t.checkExpect(((Queue<Cell>) maze1.worklist).contents, new ArrayDeque<Cell>(Arrays.asList(m1)));
    t.checkExpect(maze1.visited.get(m1), true);
    t.checkExpect(m1.color, Color.pink);
    t.checkExpect(maze1.cameFrom.get(m4), m1);
    t.checkExpect(maze1.cameFrom.get(m2), m1);

  }

  void testIsAuto(Tester t) {
    t.checkExpect(auto1.isAuto(), true);
    t.checkExpect(manual1.isAuto(), false);
    t.checkExpect(rest1.isAuto(), false);

  }

  void testIsManual(Tester t) {
    t.checkExpect(auto1.isManual(), false);
    t.checkExpect(manual1.isManual(), true);
    t.checkExpect(rest1.isManual(), false);
  }

  void testIsRest(Tester t) {
    t.checkExpect(auto1.isRest(), false);
    t.checkExpect(manual1.isRest(), false);
    t.checkExpect(rest1.isRest(), true);
  }

  void testFindCorrectPath(Tester t) {
    initData();
    ALU.createMaze(list123456789, 4632);
    HashMap<Cell, Cell> cameFrom1 = new HashMap<Cell, Cell>();
    cameFrom1.put(cell0, cell0);
    cameFrom1.put(cell1, cell0);
    cameFrom1.put(cell2, cell1);
    cameFrom1.put(cell4, cell1);
    cameFrom1.put(cell3, cell4);
    cameFrom1.put(cell5, cell4);
    cameFrom1.put(cell7, cell4);
    cameFrom1.put(cell6, cell7);
    cameFrom1.put(cell8, cell7);
    // cameFrom1 represents the cameFrom map that would be produced during the
    // process of solving the maze of list123456789

    t.checkExpect(cell0.color, Color.pink);
    t.checkExpect(cell1.color, Color.gray);
    t.checkExpect(cell4.color, Color.gray);
    t.checkExpect(cell7.color, Color.gray);
    t.checkExpect(cell8.color, Color.green);
    Deque<Cell> path1 = HMU.findCorrectPath(cameFrom1, cell0, cell8);
    t.checkExpect(path1, new ArrayDeque<Cell>(Arrays.asList(cell8, cell7, cell4, cell1, cell0)));
    // now check effects!
    t.checkExpect(cell0.color, Color.green);
    t.checkExpect(cell1.color, Color.green);
    t.checkExpect(cell4.color, Color.green);
    t.checkExpect(cell7.color, Color.green);
    t.checkExpect(cell8.color, Color.green);

  }

  void testNeighbors(Tester t) {
    initData();

    t.checkExpect(cell0.neighbors(), new ArrayList<Cell>());
    cell0.rightEdge = true;
    cell0.bottomEdge = true;
    t.checkExpect(cell0.neighbors(), new ArrayList<Cell>(Arrays.asList(cell3, cell1)));
    cell0.topEdge = true;
    cell0.leftEdge = true;
    t.checkExpect(cell0.neighbors(), new ArrayList<Cell>(Arrays.asList(null, null, cell3, cell1)));

    t.checkExpect(cell4.neighbors(), new ArrayList<Cell>());
    cell4.rightEdge = true;
    cell4.topEdge = true;
    t.checkExpect(cell4.neighbors(), new ArrayList<Cell>(Arrays.asList(cell1, cell5)));
    cell4.bottomEdge = true;
    cell4.leftEdge = true;
    t.checkExpect(cell4.neighbors(),
        new ArrayList<Cell>(Arrays.asList(cell1, cell3, cell7, cell5)));
  }

  void testFindNext(Tester t) {
    initData();

    t.checkExpect(cell0.findNext("left"), null);
    t.checkExpect(cell0.findNext("right"), null);
    cell0.leftEdge = true;
    cell0.rightEdge = true;
    t.checkExpect(cell0.findNext("left"), null);
    t.checkExpect(cell0.findNext("right"), cell1);

    t.checkExpect(cell4.findNext("up"), null);
    t.checkExpect(cell4.findNext("down"), null);
    cell4.topEdge = true;
    cell4.bottomEdge = true;
    t.checkExpect(cell4.findNext("up"), cell1);
    t.checkExpect(cell4.findNext("down"), cell7);
  }

  void testIsEmpty(Tester t) {
    initData();

    t.checkExpect(new Stack<Cell>().isEmpty(), true);
    t.checkExpect(new Queue<Cell>().isEmpty(), true);
    t.checkExpect(stack1.isEmpty(), false);
    t.checkExpect(queue1.isEmpty(), false);
  }

  void testAdd(Tester t) {
    initData();

    ICollection<Cell> emptyStack = new Stack<Cell>();
    ICollection<Cell> emptyQueue = new Queue<Cell>();

    t.checkExpect(emptyStack.isEmpty(), true);
    emptyStack.add(cell1);
    t.checkExpect(emptyStack.isEmpty(), false);
    t.checkExpect(((Stack<Cell>) emptyStack).contents, new ArrayDeque<Cell>(Arrays.asList(cell1)));
    emptyStack.add(cell8);
    t.checkExpect(((Stack<Cell>) emptyStack).contents,
        new ArrayDeque<Cell>(Arrays.asList(cell8, cell1)));

    t.checkExpect(emptyQueue.isEmpty(), true);
    emptyQueue.add(cell6);
    t.checkExpect(emptyQueue.isEmpty(), false);
    t.checkExpect(((Queue<Cell>) emptyQueue).contents, new ArrayDeque<Cell>(Arrays.asList(cell6)));
    emptyQueue.add(cell7);
    t.checkExpect(((Queue<Cell>) emptyQueue).contents,
        new ArrayDeque<Cell>(Arrays.asList(cell6, cell7)));
  }

  void testRemove(Tester t) {
    initData();

    t.checkExpect(((Stack<Cell>) stack1).contents,
        new ArrayDeque<Cell>(Arrays.asList(cell0, cell1, cell2)));
    Cell stack1remove = stack1.remove();
    t.checkExpect(stack1remove, cell0);
    t.checkExpect(((Stack<Cell>) stack1).contents,
        new ArrayDeque<Cell>(Arrays.asList(cell1, cell2)));

    t.checkExpect(((Stack<Cell>) stack2).contents,
        new ArrayDeque<Cell>(Arrays.asList(cell2, cell4, cell6)));
    Cell stack2remove = stack2.remove();
    t.checkExpect(stack2remove, cell2);
    t.checkExpect(((Stack<Cell>) stack2).contents,
        new ArrayDeque<Cell>(Arrays.asList(cell4, cell6)));

    initData();

    t.checkExpect(((Queue<Cell>) queue1).contents,
        new ArrayDeque<Cell>(Arrays.asList(cell0, cell1, cell2)));
    Cell queue1remove = queue1.remove();
    t.checkExpect(queue1remove, cell0);
    t.checkExpect(((Queue<Cell>) queue1).contents,
        new ArrayDeque<Cell>(Arrays.asList(cell1, cell2)));

    t.checkExpect(((Queue<Cell>) queue2).contents,
        new ArrayDeque<Cell>(Arrays.asList(cell2, cell4, cell6)));
    Cell queue2remove = queue2.remove();
    t.checkExpect(queue2remove, cell2);
    t.checkExpect(((Queue<Cell>) queue2).contents,
        new ArrayDeque<Cell>(Arrays.asList(cell4, cell6)));

  }

  void testCreateMaze(Tester t) {
    initData();

    t.checkExpect(cell0.leftEdge, false);
    t.checkExpect(cell0.rightEdge, false);
    t.checkExpect(cell0.topEdge, false);
    t.checkExpect(cell0.bottomEdge, false);

    t.checkExpect(cell1.leftEdge, false);
    t.checkExpect(cell1.rightEdge, false);
    t.checkExpect(cell1.topEdge, false);
    t.checkExpect(cell1.bottomEdge, false);

    t.checkExpect(cell2.leftEdge, false);
    t.checkExpect(cell2.rightEdge, false);
    t.checkExpect(cell2.topEdge, false);
    t.checkExpect(cell2.bottomEdge, false);

    t.checkExpect(cell3.leftEdge, false);
    t.checkExpect(cell3.rightEdge, false);
    t.checkExpect(cell3.topEdge, false);
    t.checkExpect(cell3.bottomEdge, false);

    t.checkExpect(cell4.leftEdge, false);
    t.checkExpect(cell4.rightEdge, false);
    t.checkExpect(cell4.topEdge, false);
    t.checkExpect(cell4.bottomEdge, false);

    t.checkExpect(cell5.leftEdge, false);
    t.checkExpect(cell5.rightEdge, false);
    t.checkExpect(cell5.topEdge, false);
    t.checkExpect(cell5.bottomEdge, false);

    t.checkExpect(cell6.leftEdge, false);
    t.checkExpect(cell6.rightEdge, false);
    t.checkExpect(cell6.topEdge, false);
    t.checkExpect(cell6.bottomEdge, false);

    t.checkExpect(cell7.leftEdge, false);
    t.checkExpect(cell7.rightEdge, false);
    t.checkExpect(cell7.topEdge, false);
    t.checkExpect(cell7.bottomEdge, false);

    t.checkExpect(cell8.leftEdge, false);
    t.checkExpect(cell8.rightEdge, false);
    t.checkExpect(cell8.topEdge, false);
    t.checkExpect(cell8.bottomEdge, false);

    ALU.createMaze(list123456789, 99);

    t.checkExpect(cell0.leftEdge, false);
    t.checkExpect(cell0.rightEdge, true);
    t.checkExpect(cell0.topEdge, false);
    t.checkExpect(cell0.bottomEdge, false);

    t.checkExpect(cell1.leftEdge, true);
    t.checkExpect(cell1.rightEdge, true);
    t.checkExpect(cell1.topEdge, false);
    t.checkExpect(cell1.bottomEdge, true);

    t.checkExpect(cell2.leftEdge, true);
    t.checkExpect(cell2.rightEdge, false);
    t.checkExpect(cell2.topEdge, false);
    t.checkExpect(cell2.bottomEdge, true);

    t.checkExpect(cell3.leftEdge, false);
    t.checkExpect(cell3.rightEdge, true);
    t.checkExpect(cell3.topEdge, false);
    t.checkExpect(cell3.bottomEdge, true);

    t.checkExpect(cell4.leftEdge, true);
    t.checkExpect(cell4.rightEdge, false);
    t.checkExpect(cell4.topEdge, true);
    t.checkExpect(cell4.bottomEdge, true);

    t.checkExpect(cell5.leftEdge, false);
    t.checkExpect(cell5.rightEdge, false);
    t.checkExpect(cell5.topEdge, true);
    t.checkExpect(cell5.bottomEdge, false);

    t.checkExpect(cell6.leftEdge, false);
    t.checkExpect(cell6.rightEdge, false);
    t.checkExpect(cell6.topEdge, true);
    t.checkExpect(cell6.bottomEdge, false);

    t.checkExpect(cell7.leftEdge, false);
    t.checkExpect(cell7.rightEdge, true);
    t.checkExpect(cell7.topEdge, true);
    t.checkExpect(cell7.bottomEdge, false);

    t.checkExpect(cell8.leftEdge, true);
    t.checkExpect(cell8.rightEdge, false);
    t.checkExpect(cell8.topEdge, false);
    t.checkExpect(cell8.bottomEdge, false);

    /*
     * list123456789 maze 
        ___ ___ ___ 
       | 0   1   2 | 
       |___        | 
       | 3   4 | 5 | 
       |       |___| 
       | 6 | 7   8 | 
       |___|___ ___|
     * 
     * it works!!!
     * 
     */

    t.checkExpect(c0.leftEdge, false);
    t.checkExpect(c0.rightEdge, false);
    t.checkExpect(c0.topEdge, false);
    t.checkExpect(c0.bottomEdge, false);

    t.checkExpect(c1.leftEdge, false);
    t.checkExpect(c1.rightEdge, false);
    t.checkExpect(c1.topEdge, false);
    t.checkExpect(c1.bottomEdge, false);

    t.checkExpect(c2.leftEdge, false);
    t.checkExpect(c2.rightEdge, false);
    t.checkExpect(c2.topEdge, false);
    t.checkExpect(c2.bottomEdge, false);

    t.checkExpect(c3.leftEdge, false);
    t.checkExpect(c3.rightEdge, false);
    t.checkExpect(c3.topEdge, false);
    t.checkExpect(c3.bottomEdge, false);

    ALU.createMaze(list2x2, 109);

    t.checkExpect(c0.leftEdge, false);
    t.checkExpect(c0.rightEdge, true);
    t.checkExpect(c0.topEdge, false);
    t.checkExpect(c0.bottomEdge, true);

    t.checkExpect(c1.leftEdge, true);
    t.checkExpect(c1.rightEdge, false);
    t.checkExpect(c1.topEdge, false);
    t.checkExpect(c1.bottomEdge, false);

    t.checkExpect(c2.leftEdge, false);
    t.checkExpect(c2.rightEdge, true);
    t.checkExpect(c2.topEdge, true);
    t.checkExpect(c2.bottomEdge, false);

    t.checkExpect(c3.leftEdge, true);
    t.checkExpect(c3.rightEdge, false);
    t.checkExpect(c3.topEdge, false);
    t.checkExpect(c3.bottomEdge, false);

    /*
     * list2x2 maze 
     *  ___ ___ 
     * | 0   1 |  
     * |    ___| 
     * | 2   3 |
     * |___ ___|
     * 
     * it works!!!
     * 
     */

    initData();

    t.checkExpect(cell0.leftEdge, false);
    t.checkExpect(cell0.rightEdge, false);
    t.checkExpect(cell0.topEdge, false);
    t.checkExpect(cell0.bottomEdge, false);

    t.checkExpect(cell1.leftEdge, false);
    t.checkExpect(cell1.rightEdge, false);
    t.checkExpect(cell1.topEdge, false);
    t.checkExpect(cell1.bottomEdge, false);

    t.checkExpect(cell2.leftEdge, false);
    t.checkExpect(cell2.rightEdge, false);
    t.checkExpect(cell2.topEdge, false);
    t.checkExpect(cell2.bottomEdge, false);

    t.checkExpect(cell3.leftEdge, false);
    t.checkExpect(cell3.rightEdge, false);
    t.checkExpect(cell3.topEdge, false);
    t.checkExpect(cell3.bottomEdge, false);

    t.checkExpect(cell4.leftEdge, false);
    t.checkExpect(cell4.rightEdge, false);
    t.checkExpect(cell4.topEdge, false);
    t.checkExpect(cell4.bottomEdge, false);

    t.checkExpect(cell5.leftEdge, false);
    t.checkExpect(cell5.rightEdge, false);
    t.checkExpect(cell5.topEdge, false);
    t.checkExpect(cell5.bottomEdge, false);

    t.checkExpect(cell6.leftEdge, false);
    t.checkExpect(cell6.rightEdge, false);
    t.checkExpect(cell6.topEdge, false);
    t.checkExpect(cell6.bottomEdge, false);

    t.checkExpect(cell7.leftEdge, false);
    t.checkExpect(cell7.rightEdge, false);
    t.checkExpect(cell7.topEdge, false);
    t.checkExpect(cell7.bottomEdge, false);

    t.checkExpect(cell8.leftEdge, false);
    t.checkExpect(cell8.rightEdge, false);
    t.checkExpect(cell8.topEdge, false);
    t.checkExpect(cell8.bottomEdge, false);

    ALU.createMaze(list123456789, 4632);

    t.checkExpect(cell0.leftEdge, false);
    t.checkExpect(cell0.rightEdge, true);
    t.checkExpect(cell0.topEdge, false);
    t.checkExpect(cell0.bottomEdge, false);

    t.checkExpect(cell1.leftEdge, true);
    t.checkExpect(cell1.rightEdge, true);
    t.checkExpect(cell1.topEdge, false);
    t.checkExpect(cell1.bottomEdge, true);

    t.checkExpect(cell2.leftEdge, true);
    t.checkExpect(cell2.rightEdge, false);
    t.checkExpect(cell2.topEdge, false);
    t.checkExpect(cell2.bottomEdge, false);

    t.checkExpect(cell3.leftEdge, false);
    t.checkExpect(cell3.rightEdge, true);
    t.checkExpect(cell3.topEdge, false);
    t.checkExpect(cell3.bottomEdge, false);

    t.checkExpect(cell4.leftEdge, true);
    t.checkExpect(cell4.rightEdge, true);
    t.checkExpect(cell4.topEdge, true);
    t.checkExpect(cell4.bottomEdge, true);

    t.checkExpect(cell5.leftEdge, true);
    t.checkExpect(cell5.rightEdge, false);
    t.checkExpect(cell5.topEdge, false);
    t.checkExpect(cell5.bottomEdge, false);

    t.checkExpect(cell6.leftEdge, false);
    t.checkExpect(cell6.rightEdge, true);
    t.checkExpect(cell6.topEdge, false);
    t.checkExpect(cell6.bottomEdge, false);

    t.checkExpect(cell7.leftEdge, true);
    t.checkExpect(cell7.rightEdge, true);
    t.checkExpect(cell7.topEdge, true);
    t.checkExpect(cell7.bottomEdge, false);

    t.checkExpect(cell8.leftEdge, true);
    t.checkExpect(cell8.rightEdge, false);
    t.checkExpect(cell8.topEdge, false);
    t.checkExpect(cell8.bottomEdge, false);

    /*
     * list123456789 maze with 4632 seed 
        ___ ___ ___ 
       | 0   1   2 | 
       |___     ___| 
       | 3   4   5 |
     * |___     ___| 
       | 6   7   8 | 
       |___ ___ ___|
     * 
     * different but still valid maze!!!!
     * 
     */

  }

  void testInitilizeBoard(Tester t) {

    Cell exCell0 = new Cell(0, 150, 150);
    Cell exCell1 = new Cell(1, 450, 150);
    Cell exCell2 = new Cell(2, 150, 450);
    Cell exCell3 = new Cell(3, 450, 450);
    exCell0.color = Color.pink;
    exCell3.color = Color.green;
    ArrayList<Cell> exList2x2 = new ArrayList<Cell>(
        Arrays.asList(exCell0, exCell1, exCell2, exCell3));

    t.checkFail(ALU.initilizeBoard(2, 2, 300, 777), exList2x2);

    ALU.connectCells(exList2x2, 1, 0, 2, 1);
    ALU.connectCells(exList2x2, 0, 1, 2, 2);
    ALU.connectCells(exList2x2, 1, 1, 2, 3);

    t.checkFail(ALU.initilizeBoard(2, 2, 300, 777), exList2x2);

    ALU.createMaze(exList2x2, 777);

    t.checkExpect(ALU.initilizeBoard(2, 2, 300, 777), exList2x2);

    initData();

    t.checkFail(ALU.initilizeBoard(3, 3, 200, 345), list123456789);

    ALU.createMaze(list123456789, 345);

    t.checkExpect(ALU.initilizeBoard(3, 3, 200, 345), list123456789);
  }

  void testMakeScene(Tester t) {
    initData();

    WorldScene ex = new WorldScene(1000, 600);

    Maze exMaze = new Maze(list123456789, 3, 3, 200);

    t.checkFail(exMaze.makeScene(), ex);

    this.cell0.drawCell(ex, 200);
    this.cell1.drawCell(ex, 200);
    this.cell2.drawCell(ex, 200);
    this.cell3.drawCell(ex, 200);
    this.cell4.drawCell(ex, 200);
    this.cell5.drawCell(ex, 200);
    this.cell6.drawCell(ex, 200);
    this.cell7.drawCell(ex, 200);
    this.cell8.drawCell(ex, 200);

    t.checkExpect(exMaze.makeScene(), ex);

    WorldScene ex1 = new WorldScene(1000, 600);

    Cell c0 = new Cell(0, 150, 150);
    Cell c1 = new Cell(1, 450, 150);
    Cell c2 = new Cell(2, 150, 450);
    Cell c3 = new Cell(3, 450, 450);
    c0.leftEdge = true;
    c0.rightEdge = true;
    c1.leftEdge = true;
    c1.rightEdge = true;
    c2.leftEdge = true;
    c2.rightEdge = true;
    c3.leftEdge = true;
    c3.rightEdge = true;
    // only draw horiz lines

    ArrayList<Cell> list1234 = new ArrayList<Cell>(Arrays.asList(c0, c1, c2, c3));

    Maze exMaze1 = new Maze(list1234, 2, 2, 300);

    t.checkFail(exMaze1.makeScene(), ex1);

    c0.drawCell(ex1, 300);
    c1.drawCell(ex1, 300);
    c2.drawCell(ex1, 300);
    c3.drawCell(ex1, 300);

    t.checkExpect(exMaze1.makeScene(), ex1);

  }

  void testDrawCell(Tester t) {
    initData();

    WorldScene ws = new WorldScene(1000, 600);
    WorldScene ws2 = new WorldScene(1000, 600);

    t.checkExpect(ws, ws2);

    cell0.drawCell(ws, 200);

    t.checkFail(ws, ws2);

    WorldImage cell0Image = new RectangleImage(200, 200, OutlineMode.SOLID, Color.pink);
    cell0Image = cell0Image.movePinholeTo(new Posn(0, 0));
    cell0Image = new OverlayImage(
        new LineImage(new Posn(0, 200), Color.BLACK).movePinholeTo(new Posn(100, 0)), cell0Image);
    cell0Image = new OverlayImage(new LineImage(new Posn(200, 0), Color.BLACK).movePinhole(0, 100),
        cell0Image);
    cell0Image = new OverlayImage(new LineImage(new Posn(0, 200), Color.BLACK).movePinhole(-99, 0),
        cell0Image);
    cell0Image = new OverlayImage(new LineImage(new Posn(200, 0), Color.BLACK).movePinhole(0, -99),
        cell0Image);
    ws2.placeImageXY(cell0Image, 100, 100);

    t.checkExpect(ws, ws2);

    cell1.leftEdge = true;
    cell1.rightEdge = true;
    cell1.topEdge = true;
    // bottom still false

    cell1.drawCell(ws, 200);

    t.checkFail(ws, ws2);

    WorldImage cell1Image = new RectangleImage(200, 200, OutlineMode.SOLID, Color.gray);
    cell1Image = cell1Image.movePinholeTo(new Posn(0, 0));
    // only draw bottom border
    cell1Image = new OverlayImage(new LineImage(new Posn(200, 0), Color.BLACK).movePinhole(0, -99),
        cell1Image);
    ws2.placeImageXY(cell1Image, 300, 100);

    t.checkExpect(ws, ws2);

  }

  void testMakeEdge(Tester t) {
    initData();

    ArrayList<Edge> MtListEdge = new ArrayList<Edge>();

    t.checkExpect(cell0.name, 0);
    t.checkExpect(cell1.name, 1);

    t.checkExpect(MtListEdge.size(), 0);

    cell0.makeEdge(cell1, MtListEdge);
    t.checkExpect(MtListEdge.size(), 1);
    t.checkExpect(MtListEdge.get(0), new Edge(1, 0));

    cell0.makeEdge(cell3, MtListEdge);
    t.checkExpect(MtListEdge.size(), 2);
    t.checkExpect(MtListEdge.get(1), new Edge(3, 0));

    Cell cell10 = null;
    cell0.makeEdge(cell10, MtListEdge);
    // since cell10 is null, add nothing
    t.checkExpect(MtListEdge.size(), 2);

  }

  void testChangeColor(Tester t) {

    Cell cellTest = new Cell(1, 50, 50);
    cellTest.color = Color.red;

    t.checkExpect(cellTest.color, Color.red);
    cellTest.changeColor(Color.blue);
    t.checkExpect(cellTest.color, Color.blue);
    cellTest.changeColor(Color.green);
    t.checkExpect(cellTest.color, Color.green);

  }

  void testFind(Tester t) {
    initData();

    t.checkExpect(HMU.find(rep1, 1), 1);
    t.checkExpect(HMU.find(rep1, 2), 2);
    t.checkExpect(HMU.find(rep1, 8), 8);
    t.checkExpect(HMU.find(rep1, 5), 5);

    rep1.replace(2, 5);
    t.checkExpect(HMU.find(rep1, 2), 5);
    t.checkExpect(HMU.find(rep1, 5), 5);
    rep1.replace(5, 8);
    t.checkExpect(HMU.find(rep1, 2), 8);
    t.checkExpect(HMU.find(rep1, 5), 8);
    t.checkExpect(HMU.find(rep1, 8), 8);
  }

  void testUnionCells(Tester t) {
    initData();

    t.checkExpect(list123456789.get(0).rightEdge, false);
    t.checkExpect(list123456789.get(0).leftEdge, false);
    t.checkExpect(list123456789.get(0).topEdge, false);
    t.checkExpect(list123456789.get(0).bottomEdge, false);
    t.checkExpect(list123456789.get(1).rightEdge, false);
    t.checkExpect(list123456789.get(1).leftEdge, false);
    t.checkExpect(list123456789.get(1).topEdge, false);
    t.checkExpect(list123456789.get(1).bottomEdge, false);

    edge1.unionCells(list123456789);

    t.checkExpect(list123456789.get(0).rightEdge, true);
    t.checkExpect(list123456789.get(0).leftEdge, false);
    t.checkExpect(list123456789.get(0).topEdge, false);
    t.checkExpect(list123456789.get(0).bottomEdge, false);
    t.checkExpect(list123456789.get(1).rightEdge, false);
    t.checkExpect(list123456789.get(1).leftEdge, true);
    t.checkExpect(list123456789.get(1).topEdge, false);
    t.checkExpect(list123456789.get(1).bottomEdge, false);

    t.checkExpect(list123456789.get(5).rightEdge, false);
    t.checkExpect(list123456789.get(5).leftEdge, false);
    t.checkExpect(list123456789.get(5).topEdge, false);
    t.checkExpect(list123456789.get(5).bottomEdge, false);
    t.checkExpect(list123456789.get(8).rightEdge, false);
    t.checkExpect(list123456789.get(8).leftEdge, false);
    t.checkExpect(list123456789.get(8).topEdge, false);
    t.checkExpect(list123456789.get(8).bottomEdge, false);

    edge12.unionCells(list123456789);

    t.checkExpect(list123456789.get(5).rightEdge, false);
    t.checkExpect(list123456789.get(5).leftEdge, false);
    t.checkExpect(list123456789.get(5).topEdge, false);
    t.checkExpect(list123456789.get(5).bottomEdge, true);
    t.checkExpect(list123456789.get(8).rightEdge, false);
    t.checkExpect(list123456789.get(8).leftEdge, false);
    t.checkExpect(list123456789.get(8).topEdge, true);
    t.checkExpect(list123456789.get(8).bottomEdge, false);

  }

  void testUnion(Tester t) {
    initData();

    t.checkExpect(cell0.rightEdge, false);
    t.checkExpect(cell0.leftEdge, false);
    t.checkExpect(cell0.topEdge, false);
    t.checkExpect(cell0.bottomEdge, false);
    t.checkExpect(cell1.rightEdge, false);
    t.checkExpect(cell1.leftEdge, false);
    t.checkExpect(cell1.topEdge, false);
    t.checkExpect(cell1.bottomEdge, false);

    cell0.union(cell1);

    t.checkExpect(cell0.rightEdge, true);
    t.checkExpect(cell0.leftEdge, false);
    t.checkExpect(cell0.topEdge, false);
    t.checkExpect(cell0.bottomEdge, false);
    t.checkExpect(cell1.rightEdge, false);
    t.checkExpect(cell1.leftEdge, true);
    t.checkExpect(cell1.topEdge, false);
    t.checkExpect(cell1.bottomEdge, false);

    t.checkExpect(cell5.rightEdge, false);
    t.checkExpect(cell5.leftEdge, false);
    t.checkExpect(cell5.topEdge, false);
    t.checkExpect(cell5.bottomEdge, false);
    t.checkExpect(cell8.rightEdge, false);
    t.checkExpect(cell8.leftEdge, false);
    t.checkExpect(cell8.topEdge, false);
    t.checkExpect(cell8.bottomEdge, false);

    cell5.union(cell8);

    t.checkExpect(cell5.rightEdge, false);
    t.checkExpect(cell5.leftEdge, false);
    t.checkExpect(cell5.topEdge, false);
    t.checkExpect(cell5.bottomEdge, true);
    t.checkExpect(cell8.rightEdge, false);
    t.checkExpect(cell8.leftEdge, false);
    t.checkExpect(cell8.topEdge, true);
    t.checkExpect(cell8.bottomEdge, false);

  }

  void testConnectCells(Tester t) {

    Cell cell0two = new Cell(0, 100, 100);
    cell0two.color = Color.pink;
    Cell cell1two = new Cell(1, 300, 100);
    Cell cell2two = new Cell(2, 500, 100);
    Cell cell3two = new Cell(3, 100, 300);
    Cell cell4two = new Cell(4, 300, 300);
    Cell cell5two = new Cell(5, 500, 300);
    Cell cell6two = new Cell(6, 100, 500);
    Cell cell7two = new Cell(7, 300, 500);
    Cell cell8two = new Cell(8, 500, 500);
    cell8two.color = Color.green;
    ArrayList<Cell> list123456789two = new ArrayList<Cell>(Arrays.asList(cell0two, cell1two,
        cell2two, cell3two, cell4two, cell5two, cell6two, cell7two, cell8two));

    // testing that none of these cells are connected yet
    t.checkExpect(cell0two.left, null);
    t.checkExpect(cell0two.top, null);
    t.checkExpect(cell0two.right, null);
    t.checkExpect(cell0two.bottom, null);
    t.checkExpect(cell1two.left, null);
    t.checkExpect(cell1two.top, null);
    t.checkExpect(cell1two.right, null);
    t.checkExpect(cell1two.bottom, null);

    // some random tests
    t.checkExpect(cell2two.left, null);
    t.checkExpect(cell3two.top, null);
    t.checkExpect(cell7two.right, null);
    t.checkExpect(cell3two.bottom, null);
    t.checkExpect(cell8two.left, null);

    ALU.connectCells(list123456789two, 1, 0, 3, 1);

    t.checkExpect(cell0two.left, null);
    t.checkExpect(cell0two.top, null);
    t.checkExpect(cell0two.right, cell1two);
    t.checkExpect(cell0two.bottom, null);
    t.checkExpect(cell1two.left, cell0two);
    t.checkExpect(cell1two.top, null);
    t.checkExpect(cell1two.right, null);
    t.checkExpect(cell1two.bottom, null);

    t.checkExpect(cell2two.left, null);
    t.checkExpect(cell3two.top, null);
    t.checkExpect(cell7two.right, null);
    t.checkExpect(cell3two.bottom, null);
    t.checkExpect(cell8two.left, null);

    ALU.connectCells(list123456789two, 2, 0, 3, 2);

    t.checkExpect(cell0two.right, cell1two);
    t.checkExpect(cell1two.left, cell0two);
    t.checkExpect(cell1two.right, cell2two);
    t.checkExpect(cell2two.left, cell1two);

    t.checkExpect(cell3two.top, null);
    t.checkExpect(cell7two.right, null);
    t.checkExpect(cell3two.bottom, null);
    t.checkExpect(cell8two.left, null);

    // connect cell8two (bottom right)

    t.checkExpect(cell8two.left, null);
    t.checkExpect(cell8two.top, null);
    t.checkExpect(cell8two.right, null);
    t.checkExpect(cell8two.bottom, null);
    t.checkExpect(cell7two.left, null);
    t.checkExpect(cell7two.top, null);
    t.checkExpect(cell7two.right, null);
    t.checkExpect(cell7two.bottom, null);
    t.checkExpect(cell5two.left, null);
    t.checkExpect(cell5two.top, null);
    t.checkExpect(cell5two.right, null);
    t.checkExpect(cell5two.bottom, null);

    ALU.connectCells(list123456789two, 2, 2, 3, 8);

    t.checkExpect(cell8two.left, cell7two);
    t.checkExpect(cell8two.top, cell5two);
    t.checkExpect(cell8two.right, null);
    t.checkExpect(cell8two.bottom, null);
    t.checkExpect(cell7two.left, null);
    t.checkExpect(cell7two.top, null);
    t.checkExpect(cell7two.right, cell8two);
    t.checkExpect(cell7two.bottom, null);
    t.checkExpect(cell5two.left, null);
    t.checkExpect(cell5two.top, null);
    t.checkExpect(cell5two.right, null);
    t.checkExpect(cell5two.bottom, cell8two);

  }

}