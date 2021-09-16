package hello;

import java.util.Arrays;
import java.util.HashMap;

import hello.Application.Arena;
import hello.Application.ArenaUpdate;
import hello.Application.Direction;
import hello.Application.Links;
import hello.Application.PlayerState;
import hello.Application.Self;
import junit.framework.TestCase;

public class ApplicationTest extends TestCase {

    Application application = new Application();
    ArenaUpdate arenaUpdate = new ArenaUpdate();
    
    @Override
    protected void setUp() throws Exception {
        setUpArena("self_id", 20, 10);
    }
    
    public void testForward() {
        System.out.println("*** testForward()");
        addPlayer("p1", playerState(5, 6, Direction.N));
        addPlayer("self_id", playerState(6, 5, Direction.E, false));
        assertEquals("F", application.index(arenaUpdate));
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
