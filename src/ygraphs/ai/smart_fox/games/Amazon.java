//package game;
package ygraphs.ai.smart_fox.games;


import javax.swing.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * For testing and demo purposes only. An GUI Amazon client for human players 
 * @author yong.gao@ubc.ca
 */
public class Amazon {
	private SearchTree search;
    private GameClient gameClient = GameClient.CLIENT_INSTANCE;
    private boolean gameStarted = false;
    private GameRules ourBoard = null;
    int turnCount = 0;

    public Amazon() {
	}

    
	/**
	 * Implements the abstract method defined in GamePlayer. Once the user joins a room, 
	 * all the game-related messages will be forwarded to this method by the GameClient.
	 * 
	 * See GameMessage.java 
	 * 
	 * @param messageType - the type of the message
	 * @param msgDetails - A HashMap info and data about a game action     
	 */

	public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails){
		if(ourBoard == null){
			Boolean isWhite = (Boolean) msgDetails.get(GameMessage.AI_TEAM);
			ourBoard = new GameRules(isWhite);
			//System.out.println("Initial Board");
			//ourBoard.printBoard();
			ourBoard.canEnemyMove();
			ourBoard.updateLegalQueenMoves();
			search = new SearchTree(new SearchTreeNode(ourBoard));
		}

		if(messageType.equals(GameMessage.GAME_ACTION_START)){
                turnCount++;
                SearchTreeNode ourBestMove = search.makeMove();
                Queen ourMove = ourBestMove.getQueen();
                Arrow ourArrow = ourBestMove.getArrowShot();
                ourBoard.canEnemyMove();
                ourBoard.updateLegalQueenMoves();
				//System.out.println("\nOur Move: [" + translateRow(ourMove.row) + ", " + translateCol(ourMove.col) + "]");
				//System.out.println("Our Arrow Shot: [" + translateRow(ourArrow.row) + ", " + translateCol(ourArrow.col) + "]\n");

                gameClient.sendMoveMessage(ourMove.combinedMove(translateRow(ourMove.previousRow), translateCol(ourMove.previousCol)),
                        ourMove.combinedMove(translateRow(ourMove.row), translateCol(ourMove.col)),
                        ourArrow.combinedMove(translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition())));
                //ourBoard.printBoard();
		}
		else if(messageType.equals(GameMessage.GAME_ACTION_MOVE)){

        	try {
				handleOpponentMove(msgDetails);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
        }
		return true;
	}
    
	//handle the event that the opponent makes a move. 
	private void handleOpponentMove(Map<String, Object> msgDetails) throws CloneNotSupportedException{
        boolean gameOver = false;
        turnCount++;
		//System.out.println("\nOpponentMove: " + msgDetails.get(GameMessage.Queen_POS_NEXT));
        //System.out.println("Opponent Arrow Shot: " + msgDetails.get(GameMessage.ARROW_POS) + "\n");
		ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(GameMessage.QUEEN_POS_CURR);
		ArrayList<Integer> qnew = (ArrayList<Integer>) msgDetails.get(GameMessage.Queen_POS_NEXT);
		ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(GameMessage.ARROW_POS);
        // Enemy move
		Queen enemyQueen = new Queen(convertRow(qnew.get(0)), convertCol(qnew.get(1)), true);
		enemyQueen.previousRow = convertRow(qcurr.get(0));
		enemyQueen.previousCol = convertCol(qcurr.get(1));
		Arrow enemyArrow = new Arrow(convertRow(arrow.get(0)), convertCol(arrow.get((1))));
		search.makeMoveOnRoot(enemyQueen, enemyArrow);

        ourBoard.canEnemyMove();
		ourBoard.updateLegalQueenMoves();
        //ourBoard.printBoard();

        // Check if we're at a goal node
        gameOver = ourBoard.goalTest();

        if(gameOver) {
            System.out.println("\n THE GAME IS NOW OVER \n");
        }

        // Our move
        turnCount++;
        SearchTreeNode ourBestMove = search.makeMove();
        Queen ourMove = ourBestMove.getQueen();
        Arrow ourArrow = ourBestMove.getArrowShot();
        ourBoard.canEnemyMove();
        ourBoard.updateLegalQueenMoves();
        //System.out.println("\nOur Move: [" + translateCol(ourMove.col) + ", " + translateRow(ourMove.row) + "]");

		//System.out.println("Our Arrow Shot: [" + translateRow(ourArrow.col) + ", " + translateCol(ourArrow.row) + "]\n");
        gameClient.sendMoveMessage(ourMove.combinedMove(translateRow(ourMove.previousRow), translateCol(ourMove.previousCol)),
				ourMove.combinedMove(translateRow(ourMove.row), translateCol(ourMove.col)),
				ourArrow.combinedMove(translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition())));
        //ourBoard.printBoard();
        gameOver = ourBoard.goalTest();

        if(gameOver) {
            System.out.println("\n THE GAME IS NOW OVER \n");
        }
	}

    private int convertRow(int row){
        return row; //Math.abs(row - 10);	// formula to convert server's row coordinate system to our Board's coordinate system
    }

    private int convertCol(int col){
        return col; //(col - 1);	      // formula to convert server's column coordinate system to our Board's coordinate system
    }

    private int translateCol(int col){
        return col; //(col + 1);	      // formula to translate our Board's column coordinate system to the server's coordinate system
    }

    private int translateRow(int row){
        return row; //Math.abs(10 - row);	      // formula to convert our Board's row coordinate system to the server's coordinate system
    }


    /**
     * handle a move made by this player --- send the info to the server.
     * @param x queen row index 
     * @param y queen col index
     * @param arow arrow row index
     * @param acol arrow col index
     * @param qfr queen original row
     * @param qfc queen original col
     */
	public void playerMove(int x, int y, int arow, int acol, int qfr, int qfc){		
		 
		int[] qf = new int[2];
		qf[0] = qfr;
		qf[1] = qfc;
		
		int[] qn = new int[2];
		qn[0] = x;
		qn[1] = y;
		
		int[] ar = new int[2];
		ar[0] = arow;
		ar[1] = acol;
		
		//To send a move message, call this method with the required data  
		this.gameClient.sendMoveMessage(qf, qn, ar);
		
	}
		
	public boolean handleMessage(String msg) {
		System.out.println("Time Out ------ " + msg); 
		return true;
	}

	
    /**
     * Constructor 
     * @param args
     */
	public static void main(String[] args) { 
		Amazon game = new Amazon();
		//Amazon game2 = new Amazon("Ronald V2", "cosc322");
    }
	
}//end of Amazon
