import java.util.ArrayList;

// ======================================================================
// FILE:        MyAI.java
//
// AUTHOR:      Abdullah Younis
//
// DESCRIPTION: This file contains your agent class, which you will
//              implement. You are responsible for implementing the
//              'getAction' function and any helper methods you feel you
//              need.
//
// NOTES:       - If you are having trouble understanding how the shell
//                works, look at the other parts of the code, as well as
//                the documentation.
//
//              - You are only allowed to make changes to this portion of
//                the code. Any changes to other portions of the code will
//                be lost when the tournament runs your code.
// ======================================================================

public class MyAI extends Agent {
	private static class Cell {
		enum PIECE_STATUS {
			maybe, there, not_there;
		};

		Cell() {
			pitConflictLists = new ArrayList[4];
			pit = PIECE_STATUS.maybe;
			wumpus = PIECE_STATUS.maybe;
			wumpusConflictList = new ArrayList<Cell>();
		}

		public PIECE_STATUS pit, wumpus;
		boolean safe;
		ArrayList<Cell>[] pitConflictLists;
		ArrayList<Cell> wumpusConflictList;

		public void isSafe() {
			if (pit == PIECE_STATUS.not_there && (wumpus == PIECE_STATUS.not_there || wumpusKilled)) {
				safe = true;
			}

		}

		public void handleStenchNotPresent() {
			wumpus = PIECE_STATUS.not_there;
		}

		public void handleStenchPresent(ArrayList<Cell> listOfConflicts) {
			if (wumpus == PIECE_STATUS.not_there) {
				return;
			}
			wumpusConflictList.addAll(listOfConflicts);
		}

		public void checkAndUpdateCellWumpusStatus() {
			if (wumpus == PIECE_STATUS.there) {
				// wumpus is present
				return;
			}

			if (wumpusFound && wumpus == PIECE_STATUS.maybe)
				wumpus = PIECE_STATUS.not_there;

			if (wumpus == PIECE_STATUS.not_there) {
				// not there; clear the list
				wumpusConflictList.clear();
			}
			if (!wumpusConflictList.isEmpty()) {
				for (int i = 0; i < wumpusConflictList.size(); i++) {
					Cell xyz = wumpusConflictList.get(i);
					if (xyz.wumpus == PIECE_STATUS.not_there) {
						wumpusConflictList.remove(xyz);
						i--;
					}
				}
				if (wumpusConflictList.isEmpty()) {
					wumpus = PIECE_STATUS.there;
					wumpusFound = true;
				}
			}
		}

		public void handleBreezeNotPresent() {
			pit = PIECE_STATUS.not_there;
		}

		public void handleBreezePresent(ArrayList<Cell> listOfConflicts) {
			if (pit == PIECE_STATUS.not_there) {
				return;
			}

			for (int i = 0; i < 4; i++) {
				if (pitConflictLists[i].isEmpty()) {
					pitConflictLists[i] = listOfConflicts;
					break;
				}
			}

		}

		public void checkAndUpdateCellPitStatus() {
			if (pit == PIECE_STATUS.there) {
				return;
			}
			if (pit == PIECE_STATUS.not_there) {
				for (int i = 0; i < 4; i++)
					pitConflictLists[i].clear();
			}

			for (int k = 0; k < 4; k++) {
				if (!pitConflictLists[k].isEmpty()) {
					for (int i = 0; i < pitConflictLists[k].size(); i++) {
						Cell xyz = pitConflictLists[k].get(i);
						if (xyz.pit == PIECE_STATUS.not_there) {
							pitConflictLists[k].remove(xyz);
							i--;
						}
					}
					if (pitConflictLists[k].isEmpty()) {
						pit = PIECE_STATUS.there;
					}
				}
			}
		}
		
	};

	public static boolean wumpusFound, wumpusKilled;
	public static Cell[][] myWorld;
	public MyAI() {
		wumpusFound = false;
		wumpusKilled = false;
		myWorld = new Cell[7][7];
		// Create a map of 7x7

		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================

		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}

	public Action getAction(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {
		System.out.println("Stench is" + stench);
		System.out.println("Breeze is" + breeze);
		System.out.println("glitter is" + glitter);
		System.out.println("bump is" + bump);
		System.out.println("scream is" + scream);
		if (glitter) {
			// traceback information
			return Action.GRAB;
			
			
		}
		updateMap(stench, breeze, bump, scream, myWorld);
		runIntelligence(myWorld);
		//return getNextMove();
		// Task 1. Use percepts to update your map
		// Task 2. Once the map is updated

		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================

		return Action.CLIMB;
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}

	public void runIntelligence(Cell[][] map) {
		for(int i=0;i<7;i++)
			for(int k=0;k<7;k++) {
				map[i][k].checkAndUpdateCellPitStatus();
				map[i][k].checkAndUpdateCellWumpusStatus();
			}
	}

	public void updateMap(boolean stench, boolean breeze, boolean bump, boolean scream, Cell[][] map) {
		// Minimal AI
		if (breeze) {
			// get neighbors - A,B,C  (first thing that needs to be taken care of)
			// A.handleBreezePresent(new ArrayList(B,C))
			// B.handleBreezePresent(new ArrayList(A,C))
			// C.handleBreezePresent(new ArrayList(B,A))
		} else {
			// get neighbors - A,B,C
			//A.handleBreezeNotPresent()
			//B.handleBreezeNotPresent()
			//C.handleBreezeNotPresent()
		}
		if (stench) {
			// get neighbors - A,B,C
						// A.handleStenchPresent(new ArrayList(B,C))
						// B.handleStenchPresent(new ArrayList(A,C))
						// C.handleStenchPresent(new ArrayList(B,A))
		} else {
			// get neighbors - A,B,C
						//A.handleStecnchNotPresent()
						//B.handleStecnchNotPresent()
						//C.handleStecnchNotPresent()
		}
		if (bump) 
		{
			//second important thing that needs to be handled
			// note down the size of world and take a step
		}
		if (scream) {
			// wumpus is killed; put the flag as true
			wumpusKilled = true;
		}
	}
	// ======================================================================
	// YOUR CODE BEGINS
	// ======================================================================

	// ======================================================================
	// YOUR CODE ENDS
	// ======================================================================
}