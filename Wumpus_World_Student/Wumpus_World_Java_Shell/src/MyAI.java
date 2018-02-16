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
		ArrayList<Cell>[] pitConflictLists;
		ArrayList<Cell> wumpusConflictList;

		public void markSafe() {
			pit = PIECE_STATUS.not_there;
			wumpus = PIECE_STATUS.not_there;
		}

		public boolean isSafe() {
			return (pit == PIECE_STATUS.not_there && (wumpus == PIECE_STATUS.not_there || wumpusKilled));
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

	public static boolean usingCustomMap;
	public static int colDimension;
	public static int rowDimension;
	public static int agentX;
	public static int agentY;
	public static int agentDir;
	public static boolean wumpusFound, wumpusKilled;
	public static Cell[][] myWorld;

	public MyAI() {
		wumpusFound = false;
		wumpusKilled = false;
		myWorld = new Cell[7][7];
		colDimension = 7;
		rowDimension = 7;
		agentX = 0;
		agentY = 0;
		agentDir = 0; // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
		usingCustomMap = true;
		// Create a map of 7x7

		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================

		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}

	public void updateLocationInfo(Action lastAction) {
		switch (lastAction) {
		case TURN_LEFT:
			if (--agentDir < 0)
				agentDir = 3;
			break;

		case TURN_RIGHT:
			if (++agentDir > 3)
				agentDir = 0;
			break;

		case FORWARD:
			if (agentDir == 0 && agentX + 1 < colDimension)
				++agentX;
			else if (agentDir == 1 && agentY - 1 >= 0)
				--agentY;
			else if (agentDir == 2 && agentX - 1 >= 0)
				--agentX;
			else if (agentDir == 3 && agentY + 1 < rowDimension)
				++agentY;

			break;

		default:
		}
	}

	public static void handleBump() {
		if (agentDir == 1) {
			rowDimension = agentY;
			agentY--;
		} else if (agentDir == 0) {
			colDimension = agentX;
			agentX--;
		}
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
		if (bump) {
			handleBump();
		}
		updateMap(stench, breeze, bump, scream, myWorld, agentX, agentY, agentDir);
		runIntelligence(myWorld);
		if (usingCustomMap) {
			Action temp = getNextMove();
			updateLocationInfo(temp);
			if (temp == Action.TURN_LEFT) {
				temp = Action.TURN_RIGHT;
			} else if (temp == Action.TURN_RIGHT) {
				temp = Action.TURN_LEFT;
			}
			return temp;
		} else {
			return getNextMove();
		}
	}

	public Action getNextMove() {
		return Action.CLIMB;
	}

	public void runIntelligence(Cell[][] map) {
		for (int i = 0; i < rowDimension; i++)
			for (int k = 0; k < colDimension; k++) {
				map[i][k].checkAndUpdateCellPitStatus();
				map[i][k].checkAndUpdateCellWumpusStatus();
			}
	}

	public static Cell getRightNeighbor(int x, int y) {
		if (y < colDimension - 1)
			return myWorld[x][y + 1];
		return null;
	}

	public static Cell getLeftNeighbor(int x, int y) {
		if (y > 0)
			return myWorld[x][y - 1];
		return null;
	}

	public static Cell getUpNeighbor(int x, int y) {
		if (x < rowDimension - 1)
			return myWorld[x + 1][y];
		return null;
	}

	public static Cell getDownNeighbor(int x, int y) {
		if (x > 0)
			return myWorld[x - 1][y];
		return null;
	}

	public Cell[] getNeighbors(int x, int y, int agentDir) {
		Cell[] neighbors = new Cell[3];
		switch (agentDir) {
		case 0:
			// right Direction; hence do not need to pick up the left neighbor;
			neighbors[0] = getRightNeighbor(x, y);
			// down ngbor
			neighbors[1] = getDownNeighbor(x, y);
			// up ngbor
			neighbors[2] = getUpNeighbor(x, y);

			break;

		case 1:
			// down Direction; hence do not need to pick up the up neighbor;

			// right ngbor
			neighbors[0] = getRightNeighbor(x, y);
			// left ngbor
			neighbors[1] = getLeftNeighbor(x, y);
			// down ngbor
			neighbors[2] = getDownNeighbor(x, y);

			break;

		case 2:
			// left Direction; hence do not need to pick up the right neighbor;

			// left ngbor
			neighbors[0] = getLeftNeighbor(x, y);
			// down ngbor
			neighbors[1] = getDownNeighbor(x, y);
			// up ngbor
			neighbors[2] = getUpNeighbor(x, y);

			break;

		case 3:
			// up Direction; hence do not need to pick up the down neighbor;
			// right ngbor
			neighbors[0] = getRightNeighbor(x, y);
			// left ngbor
			neighbors[1] = getLeftNeighbor(x, y);
			// up ngbor
			neighbors[2] = getUpNeighbor(x, y);

		}

		return neighbors;
	}

	public void updateMap(boolean stench, boolean breeze, boolean bump, boolean scream, Cell[][] map, int agentX,
			int agentY, int agentDir) {
		// Minimal AI
		Cell neighbors[] = getNeighbors(agentX, agentY, agentDir);

		Cell A = neighbors[0];
		Cell B = neighbors[1];
		Cell C = neighbors[2];
		ArrayList<Cell> listForA = new ArrayList<Cell>();
		if (B != null)
			listForA.add(B);
		if (C != null)
			listForA.add(C);

		ArrayList<Cell> listForB = new ArrayList<Cell>();
		if (A != null)
			listForB.add(A);
		if (C != null)
			listForB.add(C);

		ArrayList<Cell> listForC = new ArrayList<Cell>();
		if (C != null)
			listForC.add(B);
		if (A != null)
			listForC.add(A);

		if (breeze) {
			if (A != null)
				A.handleBreezePresent(listForA);
			if (B != null)
				B.handleBreezePresent(listForB);
			if (C != null)
				C.handleBreezePresent(listForC);
		} else {
			if (A != null)
				A.handleBreezeNotPresent();
			if (B != null)
				B.handleBreezeNotPresent();
			if (C != null)
				C.handleBreezeNotPresent();
		}
		if (stench) {
			if (A != null)
				A.handleStenchPresent(listForA);
			if (B != null)
				B.handleStenchPresent(listForB);
			if (C != null)
				C.handleStenchPresent(listForC);
		} else {
			if (A != null)
				A.handleStenchNotPresent();
			if (B != null)
				B.handleStenchNotPresent();
			if (C != null)
				C.handleStenchNotPresent();
		}
		if (scream) {
			// wumpus is killed; put the flag as true
			wumpusKilled = true;
		}

	}
}