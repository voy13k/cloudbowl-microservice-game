package hello;

import static hello.Action.F;
import static hello.Action.L;
import static hello.Action.R;
import static hello.Action.T;
import static hello.Direction.E;
import static hello.Direction.N;
import static hello.Direction.S;
import static hello.Direction.W;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import hello.Application.PlayerState;

public class CellDataTest extends ApplicationTestBase {

  private static final int WIDTH = 15;
  private static final int HEIGHT = 10;
  private static final int X = 7;
  private static final int Y = 5;

  public CellDataTest() {
    super(WIDTH, HEIGHT);
  }

  @Before
  public void setUp() {
    super.setUp(false, X, Y, E);
  }

  @Test
  public void neighbours() {
    // Add irrelevant players
    addPlayer(8, 6, E);
    addPlayer(8, 4, E);
    addPlayer(6, 6, E);
    addPlayer(6, 4, E);
    assertTargets();
    assertShooters();
    assertSpace(N, W, S, E);

    // Add players above
    addPlayer(7, 1, E);
    assertTargets();
    assertShooters();
    assertSpace(N, W, S, E);

    addPlayer(7, 2, S);
    assertTargets(N, 7, 2);
    assertShooters(N, 7, 2);
    assertSpace(N, W, S, E);

    addPlayer(7, 3, E);
    assertTargets(N, 7, 3);
    assertShooters();
    assertSpace(N, W, S, E);

    addPlayer(7, 4, N);
    assertTargets(N, 7, 4);
    assertShooters();
    assertSpace(W, S, E);

    // Add players below
    addPlayer(7, 9, E);
    assertTargets(N, 7, 4);
    assertShooters();
    assertSpace(W, S, E);

    addPlayer(7, 8, N);
    assertTargets(N, 7, 4, S, 7, 8);
    assertShooters(S, 7, 8);
    assertSpace(W, S, E);

    addPlayer(7, 7, W);
    assertTargets(N, 7, 4, S, 7, 7);
    assertShooters();
    assertSpace(W, S, E);

    addPlayer(7, 6, E);
    assertTargets(N, 7, 4, S, 7, 6);
    assertShooters();
    assertSpace(W, E);

    // Add players to the right
    addPlayer(11, 5, N);
    assertTargets(N, 7, 4, S, 7, 6);
    assertShooters();
    assertSpace(W, E);

    addPlayer(10, 5, W);
    assertTargets(N, 7, 4, S, 7, 6, E, 10, 5);
    assertShooters(E, 10, 5);
    assertSpace(W, E);

    addPlayer(9, 5, N);
    assertTargets(N, 7, 4, S, 7, 6, E, 9, 5);
    assertShooters();
    assertSpace(W, E);

    addPlayer(8, 5, N);
    assertTargets(N, 7, 4, S, 7, 6, E, 8, 5);
    assertShooters();
    assertSpace(W);

    // Assert players to the left
    addPlayer(3, 5, N);
    assertTargets(N, 7, 4, S, 7, 6, E, 8, 5);
    assertShooters();
    assertSpace(W);

    addPlayer(4, 5, E);
    assertTargets(N, 7, 4, S, 7, 6, E, 8, 5, W, 4, 5);
    assertShooters(W, 4, 5);
    assertSpace(W);

    addPlayer(5, 5, N);
    assertTargets(N, 7, 4, S, 7, 6, E, 8, 5, W, 5, 5);
    assertShooters();
    assertSpace(W);

    addPlayer(6, 5, N);
    assertTargets(N, 7, 4, S, 7, 6, E, 8, 5, W, 6, 5);
    assertShooters();
    assertSpace();
  }

  @Test
  public void space() {
    assertSpace(0, 0, S, E);
    assertSpace(0, 1, S, E, N);
    assertSpace(0, HEIGHT - 2, S, E, N);
    assertSpace(0, HEIGHT - 1, E, N);
    assertSpace(1, HEIGHT - 1, E, N, W);
    assertSpace(WIDTH - 2, HEIGHT - 1, E, N, W);
    assertSpace(WIDTH - 1, HEIGHT - 1, N, W);
    assertSpace(WIDTH - 1, HEIGHT - 2, N, W, S);
    assertSpace(WIDTH - 1, 1, N, W, S);
    assertSpace(WIDTH - 1, 0, W, S);
    assertSpace(WIDTH - 2, 0, W, S, E);
    assertSpace(1, 0, W, S, E);
  }

  @Test
  public void isPossible() {
    assertIsPossible(0, 0, N, R);
    assertIsPossible(0, 0, W, L);
    assertIsPossible(0, 0, S, L, F, T);
    assertIsPossible(0, 0, E, F, T, R);

    assertIsPossible(0, 1, N, F, T, R);
    assertIsPossible(0, 1, W, R, L);
    assertIsPossible(0, 1, S, L, F, T);
    assertIsPossible(0, 1, E, F, T, R, L);

    assertIsPossible(1, 0, N, R, L);
    assertIsPossible(1, 0, W, L, F, T);
    assertIsPossible(1, 0, S, R, L, F, T);
    assertIsPossible(1, 0, E, R, F, T);

    assertIsPossible(1, 1, N, R, L, F, T);
    assertIsPossible(1, 1, W, R, L, F, T);
    assertIsPossible(1, 1, S, R, L, F, T);
    assertIsPossible(1, 1, E, R, L, F, T);
  }

  private void assertIsPossible(int x, int y, Direction heading, Action... expActions) {
    List<Action> expectedActions = Arrays.asList(expActions);
    for (Action action: Action.values()) {
      boolean isExpected = expectedActions.contains(action);
      assertEquals(
          "Expected " + action + (isExpected ? "" : " not") + " possible",
          isExpected,
          createCellData(x, y).isPossible(heading, action));
    }
  }

  private void assertSpace(Direction... expSpaceDirections) {
    assertSpace(X, Y, expSpaceDirections);
  }

  private void assertSpace(int x, int y, Direction... expSpaceDirections) {
    Set<Direction> spaceSet = createCellData(x, y).space;
    assertEquals(expSpaceDirections.length, spaceSet.size());
    for (Direction expDirection: expSpaceDirections) {
      assertTrue(expDirection + "", spaceSet.contains(expDirection));
    }
  }

  private void assertShooters(Object... expShootersData) {
    assertNeighbours(createCellData(X, Y).shooters, expShootersData);
  }

  private void assertTargets(Object... expTargetsData) {
    assertNeighbours(createCellData(X, Y).targets, expTargetsData);
  }

  private void assertNeighbours(Map<Direction, PlayerState> neighboursMap,
      Object... expPlayersData) {
    assertEquals(expPlayersData.length / 3, neighboursMap.size());
    for (int i = 0; i < expPlayersData.length; i += 3) {
      Direction d = (Direction) expPlayersData[i];
      int x = (int) expPlayersData[i + 1];
      int y = (int) expPlayersData[i + 2];
      PlayerState target = neighboursMap.get(d);
      String message = "expected [" + x + "," + y + "]";
      assertNotNull(message, target);
      assertEquals(message + " was " + target, x, target.x);
      assertEquals(message + " was " + target, y, target.y);
    }
  }

  private CellData createCellData(int x, int y) {
    return new CellData(arenaUpdate, Cell.at(x, y));
  }

}
