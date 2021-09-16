package hello;

import java.util.Arrays;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hello.Application.Arena;
import hello.Application.ArenaUpdate;
import hello.Application.Direction;
import hello.Application.Links;
import hello.Application.PlayerState;
import hello.Application.Self;
import junit.framework.TestCase;

public class ApplicationTest extends TestCase {

    Logger logger = LoggerFactory.getLogger(getClass());
    Application application = new Application();
    ArenaUpdate arenaUpdate = new ArenaUpdate();
    
    @Override
    protected void setUp() throws Exception {
        setUpArena("self_id", 8, 6);
    }
    
    public void testForward() {
        logger.info("*** testForward()");
        addPlayer("p1", playerState(5, 6, Direction.N));
        addPlayer("self_id", playerState(6, 5, Direction.E, false));
        assertEquals("F", application.index(arenaUpdate));
    }

    public void testTopBoundary() {
        logger.info("*** testTopBoundary()");
        addPlayer("p1", playerState(5, 6, Direction.N));
        addPlayer("self_id", playerState(6, 0, Direction.N, false));
        assertTrue(Arrays.asList("L", "R").contains(application.index(arenaUpdate)));
    }

    public void testBottomBoundary() {
        logger.info("*** testBottomBoundary()");
        addPlayer("p1", playerState(5, 6, Direction.N));
        addPlayer("self_id", playerState(6, 5, Direction.S, false));
        assertTrue(Arrays.asList("L", "R").contains(application.index(arenaUpdate)));
    }

    public void testLeftBoundary() {
        logger.info("*** testLeftBoundary()");
        addPlayer("p1", playerState(5, 6, Direction.N));
        addPlayer("self_id", playerState(0, 5, Direction.W, false));
        assertTrue(Arrays.asList("L", "R").contains(application.index(arenaUpdate)));
    }

    public void testRightBoundary() {
        logger.info("*** testRightBoundary()");
        addPlayer("p1", playerState(5, 6, Direction.N));
        addPlayer("self_id", playerState(7, 5, Direction.E, false));
        assertTrue(Arrays.asList("L", "R").contains(application.index(arenaUpdate)));
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
    
    private void addPlayer(String key, PlayerState player) {
        arenaUpdate.arena.state.put(key, player);
    }

    private void setUpArena(String selfHref, int width, int height) {
        arenaUpdate._links = new Links();
        arenaUpdate._links.self = new Self();
        arenaUpdate._links.self.href = selfHref;
        arenaUpdate.arena = new Arena();
        arenaUpdate.arena.dims = Arrays.asList(width, height);
        arenaUpdate.arena.state = new HashMap<>();
    }

}
