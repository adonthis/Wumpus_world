import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

		@Override
		public String toString() {
			return "Cell [visit=" + visited + ", pit=" + pit + ", wumpus=" + wumpus + ", x=" + x + ", y=" + y + "]";
		}

		boolean visited;
		public PIECE_STATUS pit, wumpus;
		HashMap<Cell, ArrayList<Cell>> pitConflictMap;
		ArrayList<Cell> wumpusConflictList;
		int x, y;

		Cell(int x, int y) {
			pitConflictMap = new HashMap<Cell, ArrayList<Cell>>();
			visited = false;

			if (x == 0 && y == 0) {
				pit = PIECE_STATUS.not_there;
				wumpus = PIECE_STATUS.not_there;
			} else {
				pit = PIECE_STATUS.maybe;
				wumpus = PIECE_STATUS.maybe;
			}
			wumpusConflictList = new ArrayList<Cell>();
			this.x = x;
			this.y = y;
		}

		public boolean isSafe() {
			if (x >= colDimension || y >= rowDimension) {
				return false;
			}
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

		public void handleBreezePresent(ArrayList<Cell> listOfConflicts, Cell breezeFoundAt) {

			if (pit == PIECE_STATUS.not_there || listOfConflicts.isEmpty()
					|| pitConflictMap.containsKey(breezeFoundAt)) {
				return;
			}

			pitConflictMap.put(breezeFoundAt, listOfConflicts);
		}

		public void checkAndUpdateCellPitStatus() {
			if (pit == PIECE_STATUS.there) {
				return;
			}

			if (pit == PIECE_STATUS.not_there) {
				pitConflictMap.clear();
			}

			Iterator<Cell> iterator = pitConflictMap.keySet().iterator();
			while (iterator.hasNext()) {
				ArrayList<Cell> conflictList = pitConflictMap.get(iterator.next());
				for (int i = 0; i < conflictList.size(); i++) {
					Cell xyz = conflictList.get(i);
					if (xyz.pit == PIECE_STATUS.not_there) {
						conflictList.remove(xyz);
						i--;
					}
				}

				if (conflictList.isEmpty()) {
					pitConflictMap.clear();
					pit = PIECE_STATUS.there;
					break;
				}
			}
		}
	};

	// for printing custom world
	private void printBoardInfo() {
		for (int r = rowDimension - 1; r >= 0; --r) {
			for (int c = 0; c < colDimension; ++c)
				printTileInfo(c, r);
			System.out.println("");
			System.out.println("");
		}
	}

	private void printTileInfo(int c, int r) {
		StringBuilder tileString = new StringBuilder();

		if (myWorld[c][r].pit == Cell.PIECE_STATUS.there)
			tileString.append("P");
		if (myWorld[c][r].pit == Cell.PIECE_STATUS.maybe)
			tileString.append("P?");
		if (myWorld[c][r].wumpus == Cell.PIECE_STATUS.there)
			tileString.append("W");
		if (myWorld[c][r].wumpus == Cell.PIECE_STATUS.maybe)
			tileString.append("W?");
		if (myWorld[c][r].visited)
			tileString.append("V");

		// if (myWorld[c][r].wumpus) tileString.append("W");
		// if (myWorld[c][r].gold) tileString.append("G");
		// if (myWorld[c][r].breeze) tileString.append("B");
		// if (myWorld[c][r].stench) tileString.append("S");

		if (agentX == c && agentY == r)
			tileString.append("@");

		tileString.append(".");

		System.out.printf("%8s", tileString.toString());
	}

	public static boolean usingCustomMap;
	public static int colDimension;
	public static int rowDimension;
	public static int agentX;
	public static int agentY;
	public static int agentDir;
	public static boolean wumpusFound, wumpusKilled, bumped, returnNow, gold_grabbed, startCorner, endCorner, exitCave;
	public static Cell[][] myWorld;
	public static ArrayList<Action> backTrackSteps;
	//InputStreamReader isr;
	//BufferedReader br;

	public MyAI() {
		//isr = new InputStreamReader(System.in);
		//br = new BufferedReader(isr);

		moveList = new ArrayList<Action>();
		wumpusFound = false;
		wumpusKilled = false;
		myWorld = new Cell[7][7];

		for (int i = 0; i < 7; i++)
			for (int k = 0; k < 7; k++)
				myWorld[i][k] = new Cell(i, k);

		colDimension = 7;
		rowDimension = 7;
		agentX = 0;
		agentY = 0;
		agentDir = 0; // The direction the agent is facing: 0 - right, 1 - down,
		// 2 - left, 3 - up
		usingCustomMap = true;

		exitCave = false;
		startCorner = true;
		endCorner = false;
		bumped = false;
		returnNow = false;
		backTrackSteps = new ArrayList<Action>();
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
		if (agentDir == 3) {
			rowDimension = agentY;
			agentY--;
		} else if (agentDir == 0) {
			colDimension = agentX;
			agentX--;
		}
		System.out.println("Bump detected : row " + rowDimension + "col :" + colDimension);
	}

	boolean byeByeCave = false;

	public boolean doIHaveSafeUnVisitedSq() {
		for (int r = rowDimension - 1; r >= 0; --r)
			for (int c = 0; c < colDimension; ++c)
				if (myWorld[c][r].isSafe() && !myWorld[c][r].visited)
					return true;
		return false;
	}

	public Action getAction(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {
		myWorld[agentX][agentY].visited = true;

		if (byeByeCave) {
			if (agentX == 0 && agentY == 0)
				return Action.CLIMB;

			Action temp = backTrackSteps.remove(backTrackSteps.size() - 1);

			if (temp == Action.TURN_LEFT)
				temp = Action.TURN_RIGHT;
			else if (temp == Action.TURN_RIGHT)
				temp = Action.TURN_LEFT;

			updateLocationInfo(temp);
			return temp;
		}

		if (glitter) {
			byeByeCave = true;
			backTrackSteps.add(Action.TURN_LEFT);
			backTrackSteps.add(Action.TURN_LEFT);
			System.out.println("Glitter Found, time to Retrace the Steps");
			return Action.GRAB;
		}

		if (bump) {
			handleBump();
		}

		if (!moveList.isEmpty()) {
			Action temp = moveList.remove(0);
			updateLocationInfo(temp);
			backTrackSteps.add(temp);
			return temp;
		}

		updateMap(stench, breeze, bump, scream, myWorld, agentX, agentY, agentDir);
		runIntelligence(myWorld);
		printBoardInfo();
		Action temp = null;
		if (doIHaveSafeUnVisitedSq()) {
			generateNextMoves(stench, breeze, glitter, bump, scream);
			temp = moveList.remove(0);
			backTrackSteps.add(temp);
		} else {
			System.out.println("I Quit! Time to Retrace the Steps");
			if (agentX == 0 && agentY == 0)
				return Action.CLIMB;

			byeByeCave = true;
			temp = Action.TURN_LEFT;
			backTrackSteps.add(Action.TURN_RIGHT);
		}
		updateLocationInfo(temp);
		return temp;
	}

	public Action getAction1(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {
		// System.out.println("Stench is" + stench);
		// System.out.println("Breeze is" + breeze);
		// System.out.println("glitter is" + glitter);
		// System.out.println("bump is" + bump);
		// System.out.println("scream is" + scream);

		myWorld[agentX][agentY].visited = true;

		if (!moveList.isEmpty()) {
			Action temp = moveList.remove(0);
			updateLocationInfo(temp);
			return temp;
		}

		if (exitCave && agentX == 0 && agentY == 0) {
			System.out.println("X :" + agentX + " Y :" + agentY + " Action is :" + Action.CLIMB);
			return Action.CLIMB;
			// if at start point and exit true then climb
		}

		if (glitter && !gold_grabbed) {
			// traceback information
			exitCave = true;
			System.out.println("glitter is" + glitter);
			gold_grabbed = true;
			return Action.GRAB;
		}

		if (glitter) {
			return Action.GRAB;
		}

		if (bump) {
			handleBump();
		}

		updateMap(stench, breeze, bump, scream, myWorld, agentX, agentY, agentDir);
		runIntelligence(myWorld);
		printBoardInfo();
		generateNextMoves(stench, breeze, glitter, bump, scream);
		Action temp = moveList.remove(0);
		updateLocationInfo(temp);
		return temp;
	}

	ArrayList<Action> moveList;

	public void generateNextMoves(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {
		// get a list of all possible neighbor squares
		Cell left = getLeftNeighbor(agentX, agentY);
		Cell right = getRightNeighbor(agentX, agentY);
		Cell up = getUpNeighbor(agentX, agentY);
		Cell down = getDownNeighbor(agentX, agentY);

		System.out.println("Current Direction according to me is " + agentDir);
		System.out.println("LEFT SQ = " + left);
		System.out.println("RIGHT SQ = " + right);
		System.out.println("UP SQ = " + up);
		System.out.println("DOWN SQ = " + down);

		left = (left != null && left.isSafe()) ? left : null;
		right = (right != null && right.isSafe()) ? right : null;
		up = (up != null && up.isSafe()) ? up : null;
		down = (down != null && down.isSafe()) ? down : null;

		ArrayList<Cell> nonVisit = new ArrayList<Cell>();
		if (left != null && !left.visited)
			nonVisit.add(left);
		if (right != null && !right.visited)
			nonVisit.add(right);
		if (down != null && !down.visited)
			nonVisit.add(down);
		if (up != null && !up.visited)
			nonVisit.add(up);

		if (nonVisit.size() >= 1) {
			if (nonVisit.size() == 1)
				moveToCell(nonVisit.remove(0), left, right, up, down);
			else {
				boolean done = false;
				Cell temp = null;
				for (int i = 0; i < nonVisit.size() && !done; i++) {
					temp = nonVisit.remove(0);
					switch (agentDir) {
						case 0:
							// direction is right
							if (temp == right) {
								moveToCell(right, left, right, up, down);
								done = true;
							}
							break;
						case 1:
							// direction is down
							if (temp == down) {
								moveToCell(down, left, right, up, down);
								done = true;
							}
							break;
						case 2:
							// direction is left
							if (temp == left) {
								moveToCell(left, left, right, up, down);
								done = true;
							}
							break;
						case 3:
							// direction is up
							if (temp == up) {
								moveToCell(up, left, right, up, down);
								done = true;
							}
							break;
					}
				}

				if (!done)
					moveToCell(temp, left, right, up, down);
			}
		} else {
			// remove unsafe squares
			ArrayList<Cell> sqs = new ArrayList<Cell>();
			if (left != null && agentDir != 0) {
				if (agentDir == 2)
					sqs.add(0, left);
				else
					sqs.add(left);
			}

			if (right != null && agentDir != 2) {
				if (agentDir == 0)
					sqs.add(0, right);
				else
					sqs.add(right);
			}

			if (up != null && agentDir != 1) {
				if (agentDir == 3)
					sqs.add(0, up);
				else
					sqs.add(up);
			}

			if (down != null && agentDir != 3) {
				if (agentDir == 1)
					sqs.add(0, down);
				else
					sqs.add(down);
			}

			if (sqs.isEmpty()) {
				// turn back around and leave
				moveList.add(Action.TURN_LEFT);
				moveList.add(Action.TURN_LEFT);
				moveList.add(Action.FORWARD);
				System.out.println("Generating Steps to Turn Around");
			} else {
				moveToCell(sqs.remove(0), left, right, up, down);
			}
		}
	}

	public void moveToCell(Cell moveTo, Cell left, Cell right, Cell up, Cell down) {
		if (moveTo == left) {
			generateMovesForCell("left", 2);
		} else if (moveTo == right) {
			generateMovesForCell("right", 0);
		} else if (moveTo == up) {
			generateMovesForCell("up", 3);
		} else if (moveTo == down) {
			generateMovesForCell("down", 1);
		}
	}

	public void generateMovesForCell(String cellDirection, int directionNeeded) {
		switch (agentDir) {
			case 0:
				// current direction is right
				if (directionNeeded == 3) {
					// need to move to up square
					moveList.add(Action.TURN_LEFT);
				} else if (directionNeeded == 1) {
					// need to move to down square
					moveList.add(Action.TURN_RIGHT);
				}
				break;
			case 1:
				// current direction is down
				if (directionNeeded == 0) {
					// need to move to right square
					moveList.add(Action.TURN_LEFT);
				} else if (directionNeeded == 2) {
					// need to move to left square
					moveList.add(Action.TURN_RIGHT);
				}
				break;
			case 2:
				// current direction is left
				if (directionNeeded == 3) {
					// need to move to up square
					moveList.add(Action.TURN_RIGHT);
				} else if (directionNeeded == 1) {
					// need to move to down square
					moveList.add(Action.TURN_LEFT);
				}
				break;
			case 3:
				// current direction is up
				if (directionNeeded == 0) {
					// need to move to right square
					moveList.add(Action.TURN_RIGHT);
				} else if (directionNeeded == 2) {
					// need to move to left square
					moveList.add(Action.TURN_LEFT);
				}
				break;
		}
		System.out.println("Generated moves to move to " + cellDirection + "Square");
		moveList.add(Action.FORWARD);
	}

	public Action getNextMove(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {

		/*System.out.println("Press Enter: ");
		try {
			br.readLine();
		} catch (Exception E) {

		}*/

		Action retAction = null;

		if (exitCave && !gold_grabbed) {
			retAction = backTrackSteps.remove(backTrackSteps.size() - 1);
		} else if ((agentX < colDimension - 1) && !gold_grabbed && getRightNeighbor(agentX, agentY).isSafe()) {

			backTrackSteps.add(Action.FORWARD);

			retAction = Action.FORWARD;
		} else {
			exitCave = true;
			if (gold_grabbed)
				gold_grabbed = false;

			backTrackSteps.add(Action.TURN_RIGHT);

			retAction = Action.TURN_RIGHT;
		}

		System.out.println("Action taken :" + retAction);
		return retAction;

	}

	public void runIntelligence(Cell[][] map) {
		for (int i = 0; i < rowDimension; i++)
			for (int k = 0; k < colDimension; k++) {
				map[i][k].checkAndUpdateCellPitStatus();
				map[i][k].checkAndUpdateCellWumpusStatus();
			}
	}

	public static Cell getRightNeighbor(int x, int y) {
		if (x + 1 < colDimension)
			return myWorld[x + 1][y];
		return null;
	}

	public static Cell getLeftNeighbor(int x, int y) {
		if (x > 0)
			return myWorld[x - 1][y];
		return null;
	}

	public static Cell getUpNeighbor(int x, int y) {
		if (y + 1 < rowDimension)
			return myWorld[x][y + 1];
		return null;
	}

	public static Cell getDownNeighbor(int x, int y) {
		if (y > 0)
			return myWorld[x][y - 1];
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

		Cell current = map[agentX][agentY];
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
		if (B != null)
			listForC.add(B);
		if (A != null)
			listForC.add(A);

		if (breeze) {
			if (A != null)
				A.handleBreezePresent(listForA, current);
			if (B != null)
				B.handleBreezePresent(listForB, current);
			if (C != null)
				C.handleBreezePresent(listForC, current);
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
			wumpusKilled = true;
		}

	}
}