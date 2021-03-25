import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Represents a game of Othello. Plays on a board of 8x8 tiles, white goes first in this case.
 * Used a minimax algorithm to play the best move at each time. May implement ANN for further analysis,
 * but is quite redundant as the game doesn't have immense complexities, unlike something like Go.
 * <p>
 * I created my own button class that has 3 states: EMPTY, BLACK, WHITE. Those were stored in an enum.
 */
public class Othello {

    /**
     * Declare a few constants and final objects.
     */
    private static final char BLACK = 'b';
    private static final char WHITE = 'w';
    private static final char EMPTY = 'o';
    private static final int BOARD_SIZE = 8;
    private final GFG gameAI = new GFG();

    /**
     * Some string constants that won't be changed, includes the win messages, draw messages, etc.
     * Also the tile size constant.
     */
    private static final String BLACK_WIN_MSG = "Black wins: ";
    private static final String WHITE_WIN_MSG = "White wins: ";
    private static final String NO_MORE_TURNS_LEFT = "No more turns left, ";
    public static final String TURN_MSG = " click on tile to place piece";
    public static final String NO_TURN_MSG = " not your turn yet";
    public static final int TILE_SIZE = 45;

    /**
     * The boards: a physical for the white player and a physical for the black player.
     * We also have the logical board that is the same board as the white player.
     */
    public Tile[][] whiteBoard = new Tile[BOARD_SIZE][BOARD_SIZE]; // physical for white
    public Tile[][] blackBoard = new Tile[BOARD_SIZE][BOARD_SIZE]; // physical for black
    public char[][] boardState = new char[BOARD_SIZE][BOARD_SIZE]; // logical

    /**
     * Java Swing components for the actual UI. Two separate frames: white and black.
     * If I was to remake it, I would most likely actually make the game on one frame instead of two,
     * as it seems quite redundant.
     */
    private JFrame whitePlayerFrame;
    private JFrame blackPlayerFrame;
    private JLabel whiteTurnLabel = new JLabel("WHITE PLAYER - " + TURN_MSG);
    private JLabel blackTurnLabel = new JLabel("BLACK PLAYER - " + NO_TURN_MSG);
    private JButton autoPlayWhiteButton = new JButton("AI Move - WHITE");
    private JButton autoPlayBlackButton = new JButton("AI Move - BLACK");

    /**
     * Current move, this changes each time a turn actually goes through.
     */
    public char move = WHITE;

    /**
     * The constructor. The logical and physical boards are created and setup.
     * Then, the starting tiles are also set-up.
     * Any Listeners are added to the buttons that need them.
     */
    public Othello() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardState[i][j] = EMPTY;

                whiteBoard[i][j] = new Tile(TILE_SIZE, TILE_SIZE, Tile.TileState.EMPTY);
                whiteBoard[i][j].addActionListener(new TileListener());

                blackBoard[7 - i][7 - j] = new Tile(TILE_SIZE, TILE_SIZE, Tile.TileState.EMPTY);
                blackBoard[7 - i][7 - j].addActionListener(new TileListener());
            }
        }
        boardState[3][3] = WHITE;
        whiteBoard[3][3].setState(Tile.TileState.WHITE);
        blackBoard[4][4].setState(Tile.TileState.WHITE);

        boardState[3][4] = BLACK;
        whiteBoard[3][4].setState(Tile.TileState.BLACK);
        blackBoard[4][3].setState(Tile.TileState.BLACK);

        boardState[4][3] = BLACK;
        whiteBoard[4][3].setState(Tile.TileState.BLACK);
        blackBoard[3][4].setState(Tile.TileState.BLACK);

        boardState[4][4] = WHITE;
        whiteBoard[4][4].setState(Tile.TileState.WHITE);
        blackBoard[3][3].setState(Tile.TileState.WHITE);

        autoPlayWhiteButton.addActionListener(new GreedyMoveListener());
        autoPlayBlackButton.addActionListener(new GreedyMoveListener());
    }

    /**
     * Main function, execution entry and terminates here.
     */
    public static void main(String[] args) {
        Othello game = new Othello();

        game.createGameGUI();
        game.printBoardState();
    }

    /**
     * Function that sets up all the components. The frames are instantiated and have options configured.
     * Panels are created and added to the frames also.
     * This function is just to deal with what the players can see.
     */
    public void createGameGUI() {
        whitePlayerFrame = new JFrame("Othello - WHITE PLAYER");
        blackPlayerFrame = new JFrame("Othello - BLACK PLAYER");
        whitePlayerFrame.setLocationRelativeTo(null);

        whitePlayerFrame.setResizable(false); // don't allow resizing for frames
        blackPlayerFrame.setResizable(false);


        whitePlayerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        blackPlayerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel whiteFrameTopPanel = new JPanel(new BorderLayout()); // white turn display
        JPanel blackFrameTopPanel = new JPanel(new BorderLayout()); // black turn display

        whiteFrameTopPanel.add(whiteTurnLabel);
        blackFrameTopPanel.add(blackTurnLabel);

        whitePlayerFrame.add(whiteFrameTopPanel, BorderLayout.PAGE_START);
        blackPlayerFrame.add(blackFrameTopPanel, BorderLayout.PAGE_START);

        JPanel blackFrameMainPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE)); // white game board
        JPanel whiteFrameMainPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE)); // black game board

        for (int i = 0; i < BOARD_SIZE; i++) { // add tiles to panels
            for (int j = 0; j < BOARD_SIZE; j++) {
                whiteFrameMainPanel.add(whiteBoard[i][j]);
                blackFrameMainPanel.add(blackBoard[i][j]);
            }
        }

        whitePlayerFrame.add(whiteFrameMainPanel); // add board to frame centre
        blackPlayerFrame.add(blackFrameMainPanel);

        JPanel whiteFrameBottomPanel = new JPanel(new BorderLayout()); // create panel for frame bottom
        JPanel blackFrameBottomPanel = new JPanel(new BorderLayout());

        whiteFrameBottomPanel.add(autoPlayWhiteButton); // add button to panel
        blackFrameBottomPanel.add(autoPlayBlackButton);

        whitePlayerFrame.add(whiteFrameBottomPanel, BorderLayout.PAGE_END); // add panel to frame bottom
        blackPlayerFrame.add(blackFrameBottomPanel, BorderLayout.PAGE_END);

        whitePlayerFrame.pack(); // packing
        blackPlayerFrame.pack();

        Point whiteFrameLoc = whitePlayerFrame.getLocation(); // locations
        Point blackFrameLoc = new Point(whiteFrameLoc.x + whitePlayerFrame.getWidth(),
                whiteFrameLoc.y);
        blackPlayerFrame.setLocation(blackFrameLoc);

        whitePlayerFrame.setVisible(true); // set visibility
        blackPlayerFrame.setVisible(true);
    }

    /**
     * This function makes the move. It checks it to see if the game has ended, and also if there are moves left.
     * Reason why both are required is that the two functions are different. isGameEnded counts the squares whereas
     * anyMovesLeft actually checks the squares to see that for all squares, there are no available moves.
     * <p>
     * If I were to work on this project more, I would definitely rework the logic to make the code run more efficiently.
     */
    public void makeMove(int row, int col, char turn) {
        if (!isGameEnded() & anyMovesLeft()) { // game isn't ended and there are moves still
            if (!isValidMove(turn, row, col)) {
                System.out.println("Invalid move!");
                return;
            }

            // move taken
            boardState[row][col] = turn;
            if (turn == WHITE) {
                whiteBoard[row][col].setState(Tile.TileState.WHITE);
                blackBoard[BOARD_SIZE - 1 - row][BOARD_SIZE - 1 - col].setState(Tile.TileState.WHITE);
            } else if (turn == BLACK) {
                whiteBoard[row][col].setState(Tile.TileState.BLACK);
                blackBoard[BOARD_SIZE - 1 - row][BOARD_SIZE - 1 - col].setState(Tile.TileState.BLACK);
            }

            // above below
            checkDirection(row, col, turn, 0, -1);
            checkDirection(row, col, turn, 0, 1);

            // left right
            checkDirection(row, col, turn, 1, 0);
            checkDirection(row, col, turn, -1, 0);

            // diagonals
            checkDirection(row, col, turn, 1, 1);
            checkDirection(row, col, turn, 1, -1);
            checkDirection(row, col, turn, -1, 1);
            checkDirection(row, col, turn, -1, -1);

            if (move == WHITE) {
                move = BLACK;
                whiteTurnLabel.setText("WHITE PLAYER - " + NO_TURN_MSG);
                blackTurnLabel.setText("BLACK PLAYER - " + TURN_MSG);
            } else {
                move = WHITE;
                whiteTurnLabel.setText("WHITE PLAYER - " + TURN_MSG);
                blackTurnLabel.setText("BLACK PLAYER - " + NO_TURN_MSG);
            }
            printBoardState();
            whitePlayerFrame.repaint();
            blackPlayerFrame.repaint();
        } else {
            endGame();
        }
    }

    /**
     * This function traverses in the direction until it hits an empty square.
     * This works because when a move is made, the piece is already placed, so all pieces that are between the new move
     * and any other pieces that matches the turn's will have their colours changed.
     *
     * @param row    row value
     * @param col    column value
     * @param colour whose move
     * @param colDir the column vector
     * @param rowDir the row vector
     *               <p>
     *               colDir: -1, rowDir: 0, cardinal direction: N
     *               colDir: -1, rowDir: 1, cardinal direction: NE
     *               colDir: 0, rowDir: 1, cardinal direction: E
     *               colDir: 1, rowDir: 1, cardinal direction: SE
     *               colDir: 1, rowDir: 0, cardinal direction: S
     *               colDir: -1, rowDir: 1, cardinal direction: SW
     *               colDir: 0, rowDir: -1, cardinal direction: W
     *               colDir: -1, rowDir: -1, cardinal direction: NW
     *               </p>
     */
    private void checkDirection(int row, int col, char colour, int colDir, int rowDir) {
        int currentRow = row + rowDir;
        int currentCol = col + colDir;

        if (currentRow > 7 || currentRow < 0 || currentCol > 7 || currentCol < 0) {
            return;
        }

        while (boardState[currentRow][currentCol] == BLACK || boardState[currentRow][currentCol] == WHITE) {
            if (boardState[currentRow][currentCol] == colour) {
                while (!(row == currentRow && col == currentCol)) {
                    boardState[currentRow][currentCol] = colour;
                    if (colour == WHITE) {
                        whiteBoard[currentRow][currentCol].setState(Tile.TileState.WHITE);
                        blackBoard[7 - currentRow][7 - currentCol].setState(Tile.TileState.WHITE);
                    } else if (colour == BLACK) {
                        whiteBoard[currentRow][currentCol].setState(Tile.TileState.BLACK);
                        blackBoard[7 - currentRow][7 - currentCol].setState(Tile.TileState.BLACK);
                    }
                    currentRow -= rowDir;
                    currentCol -= colDir;
                }
                break;
            } else {
                currentRow += rowDir;
                currentCol += colDir;
            }

            if (currentRow < 0 || currentRow > 7 || currentCol < 0 || currentCol > 7) {
                break;
            }
        }
    }

    /**
     * As described, counts tiles to see if it matches 64, if so return true, else false;
     *
     * @return true if tiles are filled with pieces, else false.
     */
    private boolean isGameEnded() { // different from actually having any moves left, as the game may be finished with tiles still free
        int count = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (boardState[i][j] == WHITE || boardState[i][j] == BLACK) {
                    count++;
                }
            }
        }

        return count == 64;
    }

    /**
     * Mainly for debugging and gameplay analytics, to see if pieces were placed in the correct places.
     */
    private void printBoardState() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.print(boardState[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Checks if a move is valid. In short, the algorithm I used was:
     * - See if the square selected is empty; if so continue, else return.
     * - Check each cardinal direction, whilst the piece is an opponents and within board range, keep traversing.
     * - If the board[row][col] has the current turn's piece, then return true, as it is a valid move.
     *
     * @param turn whose turn
     * @param row  row value
     * @param col  column value
     * @return true if there is a valid move else false
     */
    private boolean isValidMove(char turn, int row, int col) {
        char opponent = WHITE;
        int tempRow, tempCol;
        boolean ret = false;

        if (turn == WHITE) {
            opponent = BLACK;
        }

        if (boardState[row][col] == EMPTY) {
            if (row + 1 < BOARD_SIZE && col + 1 < BOARD_SIZE && boardState[row + 1][col + 1] == opponent) { // south-east
                tempRow = row;
                tempCol = col;

                do {
                    tempRow++;
                    tempCol++;
                } while (tempRow < BOARD_SIZE && tempCol < BOARD_SIZE && boardState[tempRow][tempCol] == opponent);
                if (tempRow > 7 || tempCol > 7) {
                    ret = false;
                } else if (boardState[tempRow][tempCol] == turn) {
                    return true;
                }
            }
            if (row + 1 < BOARD_SIZE && boardState[row + 1][col] == opponent) { // south
                tempRow = row;

                do {
                    tempRow++;
                } while (tempRow < BOARD_SIZE && boardState[tempRow][col] == opponent);
                if (tempRow > 7) {
                    ret = false;
                } else if (boardState[tempRow][col] == turn) {
                    return true;
                }
            }
            if (col + 1 < BOARD_SIZE && boardState[row][col + 1] == opponent) { // east
                tempCol = col;

                do {
                    tempCol++;
                } while (tempCol < BOARD_SIZE && boardState[row][tempCol] == opponent);
                if (tempCol > 7) {
                    ret = false;
                } else if (boardState[row][tempCol] == turn) {
                    return true;
                }
            }
            if (col - 1 > -1 && boardState[row][col - 1] == opponent) { // west
                tempCol = col;

                do {
                    tempCol--;
                } while (tempCol - 1 > -1 && boardState[row][tempCol] == opponent);
                if (tempCol < 0) {
                    ret = false;
                } else if (boardState[row][tempCol] == turn) {
                    return true;
                }
            }
            if (row - 1 > -1 && col - 1 > -1 && boardState[row - 1][col - 1] == opponent) { // north-west
                tempRow = row;
                tempCol = col;

                do {
                    tempRow--;
                    tempCol--;
                } while (tempRow - 1 > -1 && tempCol - 1 > -1 && boardState[tempRow][tempCol] == opponent);
                if (tempRow < 0 || tempCol < 0) {
                    ret = false;
                } else if (boardState[tempRow][tempCol] == turn) {
                    return true;
                }
            }
            if (row - 1 > -1 && boardState[row - 1][col] == opponent) { // north
                tempRow = row;

                do {
                    tempRow--;
                } while (tempRow - 1 > -1 && boardState[tempRow][col] == opponent);
                if (tempRow < 0) {
                    ret = false;
                } else if (boardState[tempRow][col] == turn) {
                    return true;
                }
            }
            if (row - 1 > -1 && col + 1 < BOARD_SIZE && boardState[row - 1][col + 1] == opponent) { // north-east
                tempRow = row;
                tempCol = col;

                do {
                    tempRow--;
                    tempCol++;
                } while (tempRow - 1 > -1 && tempCol < BOARD_SIZE && boardState[tempRow][tempCol] == opponent);
                if (tempRow < 0 || tempCol > 7) {
                    ret = false;
                } else if (boardState[tempRow][tempCol] == turn) {
                    return true;
                }
            }
            if (row + 1 < BOARD_SIZE && col - 1 > -1 && boardState[row + 1][col - 1] == opponent) { // south-west
                tempRow = row;
                tempCol = col;

                do {
                    tempRow++;
                    tempCol--;
                } while (tempRow < BOARD_SIZE && tempCol - 1 > -1 && boardState[tempRow][tempCol] == opponent);
                if (tempRow > 7 || tempCol < 0) {
                    ret = false;
                } else if (boardState[tempRow][tempCol] == turn) {
                    return true;
                }
            }
        }
        return ret;
    }

    /**
     * Any moves left just checks until there is a valid move on the board, could be optimised.
     */
    private boolean anyMovesLeft() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (isValidMove(move, i, j)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This function is called when the game has ended due to a player winning or there not being any moves left.
     */
    public void endGame() {
        String gameEndMsg = "";
        int whiteScore = gameAI.evaluate(boardState, WHITE);
        int blackScore = gameAI.evaluate(boardState, BLACK);

        if (whiteScore + blackScore != 64) {
            gameEndMsg += NO_MORE_TURNS_LEFT;
        }

        if (whiteScore > blackScore) {
            gameEndMsg += WHITE_WIN_MSG + whiteScore + ":" + blackScore;
        } else if (blackScore > whiteScore) {
            gameEndMsg += BLACK_WIN_MSG + whiteScore + ":" + blackScore;
        } else {
            gameEndMsg += "Draw: " + whiteScore + ":" + blackScore;
        }

        JOptionPane.showMessageDialog(null, gameEndMsg);
        blackPlayerFrame.dispose();
        whitePlayerFrame.dispose();
        System.exit(0);
    }

    /**
     * Given to the tile buttons. This is real human input.
     */
    private class TileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (move == WHITE && whiteBoard[i][j] == e.getSource()) {
                        System.out.println("Move made by white: " + i + " " + j);
                        makeMove(i, j, WHITE);

                        break;
                    } else if (move == BLACK && blackBoard[7 - i][7 - j] == e.getSource()) {
                        System.out.println("Move made by black: " + i + " " + j);
                        makeMove(i, j, BLACK);

                        break;
                    }
                }
            }

            if (isGameEnded()) {
                endGame();
            }
        }

    }

    /**
     * My game play AI, using minimax without alpha/beta pruning, which would be an optimisation.
     * I've timed how long it takes for a move to be found and it is displayed each time the AI is chosen to play.
     * If a best move isn't found, a random one is chosen instead.
     */
    private class GreedyMoveListener implements ActionListener { // AI moves

        @Override
        public void actionPerformed(ActionEvent e) {
            if (move == WHITE && e.getSource() == autoPlayWhiteButton) {
                long startTime = System.nanoTime(); // timer

                int[] bestMove = gameAI.findBestMove(boardState, WHITE);

                if (bestMove[0] == -1 || bestMove[1] == -1) { // GFG failed, make random move instead
                    for (int i = 0; i < BOARD_SIZE; i++) {
                        for (int j = 0; j < BOARD_SIZE; j++) {
                            makeMove(i, j, WHITE);
                            System.out.println("Random move made by white AI: " + i + " " + j);
                        }
                    }
                } else {
                    makeMove(bestMove[0], bestMove[1], WHITE);
                    System.out.println("Best move made by white AI: " + bestMove[0] + " " + bestMove[1]);
                }

                long endTime = System.nanoTime(); // timer
                System.out.println("Took " + (endTime - startTime) / 1e6 + "ms to make move.");
            } else if (move == BLACK && e.getSource() == autoPlayBlackButton) {
                long startTime = System.nanoTime(); // timer
                int[] bestMove = gameAI.findBestMove(boardState, BLACK);


                if (bestMove[0] == -1 || bestMove[1] == -1) { // GFG failed, make random move instead
                    for (int i = 0; i < BOARD_SIZE; i++) {
                        for (int j = 0; j < BOARD_SIZE; j++) {
                            makeMove(i, j, BLACK);
                            System.out.println("Random move made by black AI: " + i + " " + j);
                        }
                    }
                } else {
                    makeMove(bestMove[0], bestMove[1], BLACK);
                    System.out.println("Best move made by black AI: " + bestMove[0] + " " + bestMove[1]);
                }

                long endTime = System.nanoTime(); // timer
                System.out.println("Took " + (endTime - startTime) / 1e6 + "ms to make move.");
            }

            if (isGameEnded()) {
                endGame();
            }
        }

    }

    /**
     * I've coded it as an inner class. This was grabbed from W3S from a tic-tac-toe game, but I changed it to
     * an Othello minimax.
     */
    private class GFG {

        public int evaluate(char[][] board, char turn) {
            int score = 0;

            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j] == turn) {
                        score += 1;
                    }
                }
            }

            return score;
        }

        public int minimax(char[][] board, int depth, boolean isMax, char turn) {
            char opponent = WHITE;
            if (turn == WHITE) {
                opponent = BLACK;
            }

            int score = evaluate(board, turn);

            if (score == 64) {
                return score;
            }

            if (score == 0) {
                return score;
            }

            if (!isGameEnded()) {
                return 0;
            }

            if (isMax) {
                int best = -1000;

                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) {
                        if (isValidMove(turn, i, j)) {
                            board[i][j] = turn;
                            best = Math.max(best, minimax(board, depth + 1, !isMax, turn));
                            board[i][j] = EMPTY;
                        }
                    }
                }
                return best;
            } else {
                int best = 1000;

                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) {
                        if (isValidMove(turn, i, j)) {
                            board[i][j] = opponent;
                            best = Math.min(best, minimax(board, depth + 1, !isMax, turn));
                            board[i][j] = EMPTY;
                        }
                    }
                }
                return best;
            }
        }

        private int[] findBestMove(char[][] board, char turn) {
            int bestVal = -1000;
            int[] bestMove = {-1, -1};

            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (isValidMove(turn, i, j)) {
                        board[i][j] = turn;

                        int moveVal = minimax(board, 0, false, turn);

                        board[i][j] = EMPTY;
                        if (moveVal > bestVal) {
                            bestMove[0] = i;
                            bestMove[1] = j;
                            bestVal = moveVal;
                        }
                    }
                }
            }

            return bestMove;
        }
    }
}