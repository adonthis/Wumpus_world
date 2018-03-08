import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

	/**
	 * Data Structure used to maintain the knowledge base; and to save the
	 * current agent information w.r.t to the game
	 *
	 */
	private static class Cell {
		enum PIECE_STATUS {
			maybe, there, not_there;
		};

		@Override
		public String toString() {
			return "Cell [visit=" + visited + ", pit=" + pit + ", wumpus=" + wumpus + ", x=" + x + ", y=" + y + "]";
		}

		/* to depict if the cell has been visited atleast once or not */
		boolean visited;

		/*
		 * to keep track of current status of our knowledge of pit/wumpus
		 * presence for the given cell
		 */
		public PIECE_STATUS pit, wumpus;

		/*
		 * A HashMap of conflicts that have arisen because of the Breezes;
		 * mainly breeze found in one cell can be responsible for Pit's in 4
		 * Cell
		 */
		HashMap<Cell, ArrayList<Cell>> pitConflictMap;

		/*
		 * An ArrayList to keep track of conflict list arisen because of Stench
		 */
		ArrayList<Cell> wumpusConflictList;

		/* The X and Y Co-ordinate of the given cell */
		int x, y;

		Cell(int x, int y) {
			/* Giving init values */
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

			/* Cell is within the boundaries */
			if (x >= colDimension || y >= rowDimension) {
				return false;
			}

			/* There is no pit or wumpus in the cell */
			return (pit == PIECE_STATUS.not_there && (wumpus == PIECE_STATUS.not_there || wumpusKilled));
		}

		/*
		 * If Stench isn't found in neighboring cell => There is no Wumpus in
		 * this Cell
		 */
		public void handleStenchNotPresent() {
			wumpus = PIECE_STATUS.not_there;
		}

		/*
		 * If Stench is found in the neighboring cell, maintain the conflict
		 * list, but if 2 separate conflict lists are generated for the same
		 * cell => Wumpus is present for sure
		 */
		public void handleStenchPresent(ArrayList<Cell> listOfConflicts) {
			if (wumpus == PIECE_STATUS.not_there) {
				return;
			}

			if (wumpusConflictList.isEmpty() || wumpusConflictList.containsAll(listOfConflicts))
				wumpusConflictList.addAll(listOfConflicts);
			else {
				wumpus = PIECE_STATUS.there;
				wumpusFound = true;
			}
		}

		/* Run inference to figure out if Wumpus Present */
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

		/*
		 * If Breeze isn't found in neighboring cell => There is no Pit in this
		 * Cell
		 */
		public void handleBreezeNotPresent() {
			pit = PIECE_STATUS.not_there;
		}

		/*
		 * If Breeze is found in the neighboring cell, maintain the conflict
		 * list
		 */
		public void handleBreezePresent(ArrayList<Cell> listOfConflicts, Cell breezeFoundAt) {

			if (pit == PIECE_STATUS.not_there || listOfConflicts.isEmpty()
					|| pitConflictMap.containsKey(breezeFoundAt)) {
				return;
			}

			pitConflictMap.put(breezeFoundAt, listOfConflicts);
		}

		/* Run inference to figure out if Pit Present */
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

		if (agentX == c && agentY == r)
			tileString.append("@");

		tileString.append(".");

		System.out.printf("%8s", tileString.toString());
	}

	static boolean DEBUG;
	ArrayList<Action> moveList;
	public static int colDimension;
	public static int rowDimension;
	public static int agentX;
	public static int agentY;
	public static int agentDir;
	public static boolean wumpusFound, wumpusKilled, bumped, returnNow, gold_grabbed, startCorner, endCorner, exitCave;
	public static Cell[][] myWorld;
	HashSet<Cell> visited;
	int costToHome;
	ArrayList<Cell> pathToCell;
	boolean byeByeCave = false;
	boolean pathToHomeGenerated = false;
	ArrayList<Action> actionsForPathToHome = new ArrayList<Action>();
	int visitedNodeCount = 0;

	public MyAI() {
		DEBUG = false;
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

		exitCave = false;
		startCorner = true;
		endCorner = false;
		bumped = false;
		returnNow = false;
	}

	/* Function to keep track of the agent's current whereabouts */
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

	/* Function to handle Bump, when detected */
	public static void handleBump() {
		if (agentDir == 3) {
			rowDimension = agentY;
			agentY--;
		} else if (agentDir == 0) {
			colDimension = agentX;
			agentX--;
		}

		if (DEBUG)
			System.out.println("Bump detected : row " + rowDimension + "col :" + colDimension);
	}

	/* Function that will return A SAFE but unvisited Sq/cell */
	public Cell doIHaveSafeUnVisitedSq() {
		for (int r = rowDimension - 1; r >= 0; --r)
			for (int c = 0; c < colDimension; ++c)
				if (myWorld[c][r].isSafe() && !myWorld[c][r].visited)
					return myWorld[c][r];
		return null;
	}

	/*
	 * Function will generate a Cell path from current cell to dest cell, and
	 * save it in pathToCell
	 */
	public void generatePathForDestination(Cell dest) {
		visited = new HashSet<Cell>();
		costToHome = Integer.MAX_VALUE;
		pathToCell = new ArrayList<Cell>();
		dfs(myWorld[agentX][agentY], dest, agentDir, new ArrayList<Cell>(), 0);
	}

	public void dfs(Cell current, Cell destination, int currentDir, ArrayList<Cell> currentPath, int cost) {
		if (current == destination) {
			/* We have reached the destination */
			currentPath.add(destination);

			/* If the cost is cheaper than the previous path, save this path */
			if (cost < costToHome || costToHome == Integer.MAX_VALUE) {
				pathToCell = new ArrayList<Cell>(currentPath);
				costToHome = cost;
			}
			return;
		}

		if (cost > costToHome) {
			/*
			 * If the cost has already exceeded the lowest cost we know about,
			 * ignore this path -> It is not getting any more cheaper
			 */
			return;
		}

		/*
		 * At this point we haven't reached the destination, but there is still
		 * hope for a useful path
		 */

		/* Add the current node to the current path */
		currentPath.add(current);

		/* Get all 4 neighbors */
		Cell left = getLeftNeighbor(current.x, current.y);
		Cell right = getRightNeighbor(current.x, current.y);
		Cell down = getDownNeighbor(current.x, current.y);
		Cell up = getUpNeighbor(current.x, current.y);

		int newCost = cost;

		/* Try Visiting Left First */
		if (left != null && left.isSafe() && !currentPath.contains(left)) {
			if (currentDir == 0) {
				// we are facing right
				newCost += 2;
			} else if (currentDir == 1) {
				// we are facing down
				newCost += 1;
			} else if (currentDir == 2) {
				// we are facing left
				newCost += 0;
			} else {
				// we are facing up
				newCost += 1;
			}
			dfs(left, destination, 2, currentPath, newCost);
			currentPath.remove(left);
		}

		newCost = cost;
		/* Try Visiting Down next */
		if (down != null && down.isSafe() && !currentPath.contains(down)) {
			if (currentDir == 0) {
				// we are facing right
				newCost += 1;
			} else if (currentDir == 1) {
				// we are facing down
				newCost += 0;
			} else if (currentDir == 2) {
				// we are facing left
				newCost += 1;
			} else {
				// we are facing up
				newCost += 2;
			}
			dfs(down, destination, 1, currentPath, newCost);
			currentPath.remove(down);
		}

		newCost = cost;
		/* Try visiting up next */
		if (up != null && up.isSafe() && !currentPath.contains(up)) {
			if (currentDir == 0) {
				// we are facing right
				newCost += 1;
			} else if (currentDir == 1) {
				// we are facing down
				newCost += 2;
			} else if (currentDir == 2) {
				// we are facing left
				newCost += 1;
			} else {
				// we are facing up
				newCost += 0;
			}
			dfs(up, destination, 3, currentPath, newCost);
			currentPath.remove(up);
		}

		newCost = cost;
		/* Try visiting right next */
		if (right != null && right.isSafe() && !currentPath.contains(right)) {
			if (currentDir == 0) {
				// we are facing right
				newCost += 0;
			} else if (currentDir == 1) {
				// we are facing down
				newCost += 1;
			} else if (currentDir == 2) {
				// we are facing left
				newCost += 2;
			} else {
				// we are facing up
				newCost += 1;
			}
			dfs(right, destination, 0, currentPath, newCost);
			currentPath.remove(right);
		}
	}

	public Action getActionImpl(ArrayList<Action> actionList) {
		Action temp = actionList.remove(0);
		updateLocationInfo(temp);
		return temp;
	}

	/* Function called by the game to get the next Action to perform */
	public Action getAction(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {

		if (myWorld[agentX][agentY].visited)
			visitedNodeCount++;
		else {
			/* We are visiting a node for the first time, hence counter reset */
			visitedNodeCount = 0;
		}

		myWorld[agentX][agentY].visited = true;

		if (byeByeCave) {
			/*
			 * This case is activated when we have made the decision to exit the
			 * game, our only goal is to reach Cell - 0,0 ASAP and leave
			 */

			if (agentX == 0 && agentY == 0)
				return Action.CLIMB;

			if (!pathToHomeGenerated) {
				/*
				 * If we don't have path to 0,0 {HOME} then we generate the path
				 * here
				 */
				generatePathForDestination(myWorld[0][0]);
				if (DEBUG)
					System.out.println("Path to Home " + pathToCell);
				actionsForPathToHome.clear();
			}

			pathToHomeGenerated = true;

			if (!actionsForPathToHome.isEmpty()) {
				/*
				 * We have an Action List to perform, we shall perform this
				 * blindly
				 */
				return getActionImpl(actionsForPathToHome);
			}

			/*
			 * At this point we know that we want to go home, we have the path
			 * (Cell -> Cell) but we do not have the Action List, hence we shall
			 * generate an Action List
			 */
			Cell current = pathToCell.remove(0);
			Cell destination = pathToCell.get(0);
			moveToCell(destination, getLeftNeighbor(current.x, current.y), getRightNeighbor(current.x, current.y),
					getUpNeighbor(current.x, current.y), getDownNeighbor(current.x, current.y), actionsForPathToHome);

			return getActionImpl(actionsForPathToHome);
		}

		if (glitter) {
			/*
			 * We have found glitter, we shall pick it up, and then exit the
			 * cave
			 */
			byeByeCave = true;
			if (DEBUG)
				System.out.println("Glitter Found, time to Retrace the Steps");
			return Action.GRAB;
		}

		if (bump) {
			handleBump();
		}

		if (!moveList.isEmpty()) {
			/*
			 * There are a list of moves pending, time to perform them first and
			 * then figure out what cell to go to next
			 */
			return getActionImpl(moveList);
		}

		if (pathToCell != null && !pathToCell.isEmpty() && pathToCell.size() != 1) {
			/*
			 * We DO NOT have move list, but we do have a path (Cell -> Cell),
			 * so we generate moves to next Cell in the path
			 */

			Cell current = pathToCell.remove(0);
			Cell destination = pathToCell.get(0);
			moveToCell(destination, getLeftNeighbor(current.x, current.y), getRightNeighbor(current.x, current.y),
					getUpNeighbor(current.x, current.y), getDownNeighbor(current.x, current.y), moveList);

			return getActionImpl(moveList);
		}

		/* At this point, we DO NOT even know which cell to visit next */

		/* So we update the map with Percepts */
		updateMap(stench, breeze, bump, scream, myWorld, agentX, agentY, agentDir);

		/*
		 * We run the intelligence on the board, to figure out Pits/Wumpus
		 * locations
		 */
		runIntelligence(myWorld);

		if (DEBUG)
			printBoardInfo();

		Action temp = null;

		/*
		 * Figure out if there is a cell which is safe, and we haven't visited
		 * yet
		 */
		Cell destination = doIHaveSafeUnVisitedSq();
		if (DEBUG)
			System.out.println("Destination to visit = " + destination);

		if (destination != null) {
			/* Such Cell Exists */
			if (visitedNodeCount >= 4) {
				/*
				 * If I have visited 4 consecutive visited nodes, we change the
				 * node selection logic to DFS (where I plot a path course to a
				 * random NON VISITED - SAFE Cell
				 */
				if (DEBUG)
					System.out.println("Trigger, moving to " + destination);

				/*
				 * This function generates path {Cell -> Cell} from current cell
				 * to the given destination
				 */
				generatePathForDestination(destination);
				if (DEBUG)
					System.out.println("Path is " + pathToCell);

				Cell current = pathToCell.remove(0);
				destination = pathToCell.get(0);
				moveToCell(destination, getLeftNeighbor(current.x, current.y), getRightNeighbor(current.x, current.y),
						getUpNeighbor(current.x, current.y), getDownNeighbor(current.x, current.y), moveList);

				return getActionImpl(moveList);

			} else {
				/*
				 * We can afford to use the cheap, but less effective logic at
				 * this point
				 */

				/*
				 * This function will generate all the moves required to move to
				 * the destination cell.
				 */
				generateNextMoves(stench, breeze, glitter, bump, scream);
			}
			temp = moveList.remove(0);
		} else {
			/* NO Such Cell Exists, that means it is time to exit the game */
			if (DEBUG)
				System.out.println("I Quit! Time to Retrace the Steps");

			if (agentX == 0 && agentY == 0)
				return Action.CLIMB;

			byeByeCave = true;
			temp = Action.TURN_LEFT;
		}
		updateLocationInfo(temp);
		return temp;
	}

	/*
	 * Less Effective, but cheap logic to select which neighboring cell to visit
	 * next, and generate moves to move to that cell
	 */
	public void generateNextMoves(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {
		// get a list of all possible neighbor squares
		Cell left = getLeftNeighbor(agentX, agentY);
		Cell right = getRightNeighbor(agentX, agentY);
		Cell up = getUpNeighbor(agentX, agentY);
		Cell down = getDownNeighbor(agentX, agentY);

		// DEBUG INFO
		if (DEBUG) {
			System.out.println("Current Direction according to me is " + agentDir);
			System.out.println("LEFT SQ = " + left);
			System.out.println("RIGHT SQ = " + right);
			System.out.println("UP SQ = " + up);
			System.out.println("DOWN SQ = " + down);
		}

		/*
		 * If a cell is not safe, we simply make the variable null, to avoid
		 * checking for the same thing again
		 */
		left = (left != null && left.isSafe()) ? left : null;
		right = (right != null && right.isSafe()) ? right : null;
		up = (up != null && up.isSafe()) ? up : null;
		down = (down != null && down.isSafe()) ? down : null;

		/* Priority 1 - Visit a NON-VISITED Cell first */
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
			if (nonVisit.size() == 1) {
				/* We have EXACTLY 1 NON-VISITED Cell, time to visit it */
				moveToCell(nonVisit.remove(0), left, right, up, down, moveList);
			} else {
				/*
				 * We have MULTIPLE NON-VISITED Cells, we visit the one which is
				 * cheaper to visit
				 */
				boolean doWeHaveAnUnVisitedCellInTheSameDirectionAsTheCurrentDirection = false;
				Cell temp = null;
				for (int i = 0; i < nonVisit.size()
						&& !doWeHaveAnUnVisitedCellInTheSameDirectionAsTheCurrentDirection; i++) {
					temp = nonVisit.remove(0);
					switch (agentDir) {
						case 0:
							// current direction is right
							if (temp == right) {
								moveToCell(right, left, right, up, down, moveList);
								doWeHaveAnUnVisitedCellInTheSameDirectionAsTheCurrentDirection = true;
							}
							break;
						case 1:
							// current direction is down
							if (temp == down) {
								moveToCell(down, left, right, up, down, moveList);
								doWeHaveAnUnVisitedCellInTheSameDirectionAsTheCurrentDirection = true;
							}
							break;
						case 2:
							// current direction is left
							if (temp == left) {
								moveToCell(left, left, right, up, down, moveList);
								doWeHaveAnUnVisitedCellInTheSameDirectionAsTheCurrentDirection = true;
							}
							break;
						case 3:
							// current direction is up
							if (temp == up) {
								moveToCell(up, left, right, up, down, moveList);
								doWeHaveAnUnVisitedCellInTheSameDirectionAsTheCurrentDirection = true;
							}
							break;
					}
				}

				if (!doWeHaveAnUnVisitedCellInTheSameDirectionAsTheCurrentDirection) {
					/* We don't, so we just go to any random UNVISITED Cell */
					moveToCell(temp, left, right, up, down, moveList);
				}
			}
		} else {
			/* All the neighbors have been visited at least once */

			/* Place the cell in the same direction as us first */
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
				/*
				 * We don't have any visitable sqaure, so we need to turn back
				 * around and leave
				 */
				moveList.add(Action.TURN_LEFT);
				moveList.add(Action.TURN_LEFT);
				moveList.add(Action.FORWARD);
				if (DEBUG)
					System.out.println("Generating Steps to Turn Around");
			} else {
				moveToCell(sqs.remove(0), left, right, up, down, moveList);
			}
		}
	}

	/*
	 * This function will generate moves to move to the destination cell and
	 * save them in moveList
	 */
	public void moveToCell(Cell destinationCell, Cell leftOfCurrent, Cell rightOfCurrent, Cell upOfCurrent,
						   Cell downOfCurrent, ArrayList<Action> moveList) {
		if (destinationCell == leftOfCurrent) {
			moveToCellImpl("left", 2, moveList);
		} else if (destinationCell == rightOfCurrent) {
			moveToCellImpl("right", 0, moveList);
		} else if (destinationCell == upOfCurrent) {
			moveToCellImpl("up", 3, moveList);
		} else if (destinationCell == downOfCurrent) {
			moveToCellImpl("down", 1, moveList);
		}
	}

	public void moveToCellImpl(String cellDirection, int directionNeeded, ArrayList<Action> moveList) {
		switch (agentDir) {
			case 0:
				// current direction is right
				if (directionNeeded == 3) {
					// need to move to up square
					moveList.add(Action.TURN_LEFT);
				} else if (directionNeeded == 1) {
					// need to move to down square
					moveList.add(Action.TURN_RIGHT);
				} else if (directionNeeded == 2) {
					// need to move to left square
					moveList.add(Action.TURN_RIGHT);
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
				} else if (directionNeeded == 3) {
					// need to move up sqaure
					moveList.add(Action.TURN_RIGHT);
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
				} else if (directionNeeded == 0) {
					// need to move right square
					moveList.add(Action.TURN_RIGHT);
					moveList.add(Action.TURN_RIGHT);
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
				} else if (directionNeeded == 1) {
					// need to move down square
					moveList.add(Action.TURN_RIGHT);
					moveList.add(Action.TURN_RIGHT);
				}
				break;
		}
		if (DEBUG)
			System.out.println("Generated moves to move to " + cellDirection + "Square");
		moveList.add(Action.FORWARD);
	}

	/*
	 * Run Intelligence, to check and update Pit and Wumpus Status for every
	 * Cell
	 */
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

	/*
	 * Get the 3 neighbors of the current Cell (4 neighbors - 1 (where I just
	 * came from))
	 */
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

	/* Function to update the map information using the percepts */
	public void updateMap(boolean stench, boolean breeze, boolean bump, boolean scream, Cell[][] map, int agentX,
						  int agentY, int agentDir) {

		Cell neighbors[] = getNeighbors(agentX, agentY, agentDir);

		Cell current = map[agentX][agentY];

		/*
		 * We shall call the 3 neighbors (4 - 1(the one where came from)) as
		 * A/B/C
		 */
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