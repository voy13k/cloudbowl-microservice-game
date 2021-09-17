package hello;

import static org.junit.Assert.*;

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

public abstract class ApplicationTestBase {

  Logger logger = LoggerFactory.getLogger(getClass());
  Application application = new Application();
  ArenaUpdate arenaUpdate = new ArenaUpdate();
  int playerCounter;

  protected ApplicationTestBase(int arenaWidth, int arenaHeight) {
    arenaUpdate._links = new Links();
    arenaUpdate._links.self = new Self();
    arenaUpdate._links.self.href = "p0";
    arenaUpdate.arena = new Arena();
    arenaUpdate.arena.dims = Arrays.asList(arenaWidth, arenaHeight);
    arenaUpdate.arena.state = new HashMap<>();
  }

  protected void setUp(boolean selfWasHit, Object... data) {
    for (int i = 0; i < data.length; i += 3) {
      int x = (int) data[i];
      int y = (int) data[i + 1];
      Direction direction = (Direction) data[i + 2];
      addPlayer(x, y, direction, selfWasHit);
    }
  }

  protected void addPlayer(int x, int y, Direction direction, boolean ...selfWasHit) {
    arenaUpdate.arena.state.put("p" + playerCounter++, playerState(x, y, direction, selfWasHit));
  }

  protected void verify(Action... expectedAlternatives) {
    List<Action> expected = Arrays.asList(expectedAlternatives);
    Action actualMove = Action.valueOf(application.index(arenaUpdate));
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
