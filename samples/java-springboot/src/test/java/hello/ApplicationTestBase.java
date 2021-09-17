package hello;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hello.Application.Arena;
import hello.Application.ArenaUpdate;
import hello.Application.Links;
import hello.Application.PlayerState;
import hello.Application.Self;
import junit.framework.TestCase;

public abstract class ApplicationTestBase extends TestCase {

  Logger logger = LoggerFactory.getLogger(getClass());
  Application application = new Application();
  ArenaUpdate arenaUpdate = new ArenaUpdate();

  protected ApplicationTestBase(int arenaWidth, int arenaHeight) {
    arenaUpdate._links = new Links();
    arenaUpdate._links.self = new Self();
    arenaUpdate._links.self.href = "p0";
    arenaUpdate.arena = new Arena();
    arenaUpdate.arena.dims = Arrays.asList(arenaWidth, arenaHeight);
    arenaUpdate.arena.state = new HashMap<>();
  }

  protected void setUp(String caseName, boolean selfWasHit, Object... data) {
    logger.info("*** " + caseName + " ***");
    for (int i = 0; i < data.length; i += 3) {
      int x = (int) data[i];
      int y = (int) data[i + 1];
      Direction direction = Direction.valueOf((String) data[i + 2]);
      arenaUpdate.arena.state.put("p" + i, playerState(x, y, direction, selfWasHit));
    }
  }

  protected void verify(String... expectedAlternatives) {
    List<String> expected = Arrays.asList(expectedAlternatives);
    String actualMove = application.index(arenaUpdate);
    assertTrue(
        "expected on of: " + expected + ", was: " + actualMove, expected.contains(actualMove));
  }

  private PlayerState playerState(int x, int y, Direction direction, boolean... wasHit) {
    PlayerState playerState = new PlayerState();
    playerState.x = x;
    playerState.y = y;
    playerState.direction = direction;
    if (wasHit.length > 0) {
      playerState.wasHit = wasHit[0];
    }
    return playerState;
  }

}
