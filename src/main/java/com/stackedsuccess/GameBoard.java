package com.stackedsuccess;

import com.stackedsuccess.controllers.GameBoardController;
import com.stackedsuccess.tetriminos.*;

// This class defines the game board and functionality to check board state
public class GameBoard {
  private static final int DEFAULT_BOARD_WIDTH = 10;
  private static final int DEFAULT_BOARD_HEIGHT = 20;

  private final int[][] board;
  private Tetrimino currentTetrimino;
  private Tetrimino nextTetrimino;
  private int frameCount;
  private int score = 0;
  private int level = 1;
  private int linesCleared = 0;

  private GameBoardController controller;

  public GameBoard() {
    board = new int[DEFAULT_BOARD_HEIGHT][DEFAULT_BOARD_WIDTH];
    initializeBoard();
  }

  public GameBoard(int width, int height) {
    board = new int[width][height];
    initializeBoard();
  }

  public void setController(GameBoardController controller) {
    this.controller = controller;
  }

  /** Setup initial tetrimino pieces and board metrics. */
  private void initializeBoard() {
    currentTetrimino = TetriminoFactory.createRandomTetrimino();
    nextTetrimino = TetriminoFactory.createRandomTetrimino();
    frameCount = 0;
  }

  /** Update the state of the board. */
  public void update() {
    controller.setNextPieceView(nextTetrimino);
    frameCount++;
    // Stagger automatic tetrimino movement based on frame count
    if (frameCount % 100 == 0) {
      if (!checkCollision(currentTetrimino.xPos, currentTetrimino.yPos + 1)) {
        currentTetrimino.updateTetrimino(this, Action.MOVE_DOWN);
      } else {
        placeTetrimino(currentTetrimino);
        clearFullRows();
        currentTetrimino = nextTetrimino;
        nextTetrimino = TetriminoFactory.createRandomTetrimino();
      }
    }
  }

  /**
   * Get-type function.
   *
   * @return the current tetrimino for game board
   */
  public Tetrimino getCurrentTetrimino() {
    return currentTetrimino;
  }

  /**
   * Checks if current tetrimino will collide with borders or existing cells.
   *
   * @param x the x position to start check for collision
   * @param y the y position to start check for collision
   * @return true if current tetrimino will collide with border or existing cells
   */
  public boolean checkCollision(int x, int y) {
    int[][] layout = currentTetrimino.getTetriminoLayout();
    int newX, newY;

    for (int layoutY = 0; layoutY < currentTetrimino.getHeight(); layoutY++) {
      for (int layoutX = 0; layoutX < currentTetrimino.getWidth(); layoutX++) {
        if (layout[layoutY][layoutX] != 0) {
          newX = x + layoutX;
          newY = y + layoutY;

          // Check for out of bound collisions
          if (isOutOfBounds(newX, newY)) return true;

          // Check for existing tetrimino cells
          if (isCellOccupied(newX, newY)) return true;
        }
      }
    }
    return false;
  }

  /**
   * Appends new tetrimino to the game board.
   *
   * @param tetrimino the tetrimino to place on the game board.
   */
  private void placeTetrimino(Tetrimino tetrimino) {
    int[][] layout = tetrimino.getTetriminoLayout();
    for (int layoutY = 0; layoutY < tetrimino.getHeight(); layoutY++) {
      for (int layoutX = 0; layoutX < tetrimino.getWidth(); layoutX++) {
        if (layout[layoutY][layoutX] != 0) {

          int spawnX = tetrimino.xPos + layoutX;
          int spawnY = tetrimino.yPos + layoutY;
          // Check for collision at the spawn location
          if (isCellOccupied(spawnX, spawnY)) {
            controller.gameOver();
            return;
          }

          board[spawnY][spawnX] = layout[layoutY][layoutX];
        }
      }
    }
    if (controller != null) {
      controller.updateDisplayGrid(tetrimino);
    }
  }

  /** Clears full rows and moves rows above downwards. */
  private void clearFullRows() {
    int fullRows = 0;
    for (int y = 0; y < board.length; y++) {
      if (isRowFull(y, board[y])) {
        fullRows++;
        shiftRowsDown(y);
        if (controller != null) {
          controller.clearLine(y);
        }
      }
      linesCleared += fullRows;
      updateLevel(linesCleared);
      calculateScore(fullRows);
    }
  }

  /**
   * Updates the level based on the number of lines cleared.
   *
   * @param linesCleared the number of lines cleared
   */
  private void updateLevel(int linesCleared) {
    if (linesCleared >= 10) {
      linesCleared = 0;
      level++;
      controller.updateLevel(level);
    }
  }

  private void calculateScore(int linesCleared) {
    switch (linesCleared) {
      case 1:
        score += 40;
        controller.updateScore(score);
        break;
      case 2:
        score += 100;
        controller.updateScore(score);
        break;
      case 3:
        score += 300;
        controller.updateScore(score);
        break;
      case 4:
        score += 1200;
        controller.updateScore(score);
        break;
      default:
        break;
    }
  }

  /**
   * Moves rows above certain row downwards and creates empty line at top of game board.
   *
   * @param fromYAxis the start y-axis for moving subsequent rows downward
   */
  private void shiftRowsDown(int fromYAxis) {
    for (int y = fromYAxis; y > 0; y--) {
      System.arraycopy(board[y - 1], 0, board[y], 0, board[0].length);
    }

    for (int x = 0; x < board[0].length; x++) board[0][x] = 0;
  }

  /**
   * Checks if coordinates are outside the bounds of the game board.
   *
   * @param x the x position to check
   * @param y the y position to check
   * @return true if the coordinates are out of bounds
   */
  private boolean isOutOfBounds(int x, int y) {
    return x < 0 || x >= board[0].length || y < 0 || y >= board.length;
  }

  /**
   * Check if coordinates are occupied in the game board.
   *
   * @param x the x position to check
   * @param y the y position to check
   * @return true if cell is occupied in game board
   */
  private boolean isCellOccupied(int x, int y) {
    return board[y][x] != 0;
  }

  /**
   * Check if the contents within a row are full of tetrimino cells.
   *
   * @param rowY the y level or row number of given row
   * @param row the row to check
   * @return whether the row is full or not
   */
  private boolean isRowFull(int rowY, int[] row) {
    for (int x = 0; x < row.length; x++) {
      if (!isCellOccupied(x, rowY)) return false;
    }
    return true;
  }
}
