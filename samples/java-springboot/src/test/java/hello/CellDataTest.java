package hello;

import static hello.Direction.E;
import static hello.Direction.N;
import static hello.Direction.S;
import static hello.Direction.W;
import static hello.TestUtils.cell;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import hello.Application.PlayerState;

public class CellDataTest extends ApplicationTestBase {

  private static final int X = 7;
  private static final int Y = 5;

  public CellDataTest() {
    super(15, 10);
  }

  @Before
  public void setUp() {
    super.setUp("***", false, X, Y, E);
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

    // Add players above
    addPlayer(7, 1, E);
    assertTargets();
    assertShooters();

    addPlayer(7, 2, S);
    assertTargets(N, 7, 2);
    assertShooters(N, 7, 2);

    addPlayer(7, 3, E);
    assertTargets(N, 7, 3);
    assertShooters();
    
    addPlayer(7, 4, N);
    assertTargets(N, 7, 4);
    assertShooters();

    // Add players below
    addPlayer(7, 9, E);
    assertTargets(N, 7, 4);
    assertShooters();

    addPlayer(7, 8, N);
    assertTargets(N, 7, 4, S, 7, 8);
    assertShooters(S, 7, 8);

    addPlayer(7, 7, W);
    assertTargets(N, 7, 4, S, 7, 7);
    assertShooters();
    
    addPlayer(7, 6, E);
    assertTargets(N, 7, 4, S, 7, 6);
    assertShooters();

    // Add players to the right
    addPlayer(11, 5, N);
    assertTargets(N, 7, 4, S, 7, 6);
    assertShooters();

    addPlayer(10, 5, W);
    assertTargets(N, 7, 4, S, 7, 6, E, 10, 5);
    assertShooters(E, 10, 5);

    addPlayer(9, 5, N);
    assertTargets(N, 7, 4, S, 7, 6, E, 9, 5);
    assertShooters();
    
    addPlayer(8, 5, N);
    assertTargets(N, 7, 4, S, 7, 6, E, 8, 5);
    assertShooters();

    // Assert players to the left
    addPlayer(3, 5, N);
    assertTargets(N, 7, 4, S, 7, 6, E, 8, 5);
    assertShooters();

    addPlayer(4, 5, E);
    assertTargets(N, 7, 4, S, 7, 6, E, 8, 5, W, 4, 5);
    assertShooters(W, 4, 5);

    addPlayer(5, 5, N);
    assertTargets(N, 7, 4, S, 7, 6, E, 8, 5, W, 5, 5);
    assertShooters();
    
    addPlayer(6, 5, N);
    assertTargets(N, 7, 4, S, 7, 6, E, 8, 5, W, 6, 5);
    assertShooters();
  }

  private void assertShooters(Object... expShootersData) {
    CellData cellData = new CellData(arenaUpdate, cell(X, Y));
    assertNeighbours(cellData.shooters, expShootersData);
  }

  private void assertTargets(Object... expTargetsData) {
    CellData cellData = new CellData(arenaUpdate, cell(X, Y));
    assertNeighbours(cellData.targets, expTargetsData);
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

}
