package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
@RestController
public class Application {
  enum Direction {
    N("W", "E", "S", 0, -1),
    S("E", "W", "N", 0, 1),
    E("N", "S", "W", 1, 0),
    W("S", "N", "E", -1, 0);

    public final int yStep;
    public final int xStep;
    private String left;
    private String right;
    private String opposite;

    Direction(String left, String right, String opposite, int xStep, int yStep) {
      this.left = left;
      this.right = right;
      this.opposite = opposite;
      this.xStep = xStep;
      this.yStep = yStep;
    }

    Direction getLeft() {
      return valueOf(left);
    }

    Direction getRight() {
      return valueOf(right);
    }

    Direction getOpposite() {
      return valueOf(opposite);
    }

    Direction getNewDirection(Action action) {
      switch(action) {
        case L:
          return getLeft();
        case R:
          return getRight();
        default:
          return this;
      }
    }
  }

  enum Action {
    F(null),
    L("R"),
    R("L"),
    T(null);

    private String opposite;

    Action(String opposite) {
      this.opposite = opposite;
    }

    Action getOpposite() {
      return valueOf(opposite);
    }
  }

  static class Self {
    public String href;
  }

  static class Links {
    public Self self;
  }

  static class PlayerState {
    public Integer x;
    public Integer y;
    public Direction direction;
    public Boolean wasHit;
    public Integer score;
    int getNewX(Action turn) {
      return x + direction.getNewDirection(turn).xStep;
    }
    int getNewY(Action turn) {
      return y + direction.getNewDirection(turn).yStep;
    }
  }

  static class Arena {
    public List<Integer> dims;
    public Map<String, PlayerState> state;
  }

  static class ArenaUpdate {
    public Links _links;
    public Arena arena;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.initDirectFieldAccess();
  }

  @GetMapping("/")
  public String index() {
    return "Let the battle begin!";
  }

  @PostMapping("/**")
  public String index(@RequestBody ArenaUpdate arenaUpdate) {
    return new Worker(arenaUpdate).work().name();
  }

  interface PlayerPairFunctor<R> {
    R check(PlayerState p1, PlayerState p2);
  }

  class Worker {
    ArenaUpdate arenaUpdate;
    Map<Direction, PlayerState> targets = new EnumMap<>(Direction.class);
    PlayerState self;
    boolean forwardPossible;

    Worker(ArenaUpdate arenaUpdate) {
      this.arenaUpdate = arenaUpdate;
      self = arenaUpdate.arena.state.get(arenaUpdate._links.self.href);
      gatherTargets();
      forwardPossible = isPossible(Action.F);
    }

    private boolean isPossible(Action action) {
      int forwardX = self.getNewX(action);
      if (forwardX < 0 || forwardX >= arenaUpdate.arena.dims.get(0)) {
        return false;
      }
      int forwardY = self.getNewY(action);
      if (forwardY < 0 || forwardY >= arenaUpdate.arena.dims.get(1)) {
        return false;
      }
      PlayerState frontTarget = targets.get(self.direction);
      return frontTarget == null
        || (forwardX != frontTarget.x && forwardY != frontTarget.y);
    }

    private void gatherTargets() {
      for (PlayerState next : arenaUpdate.arena.state.values()) {
        if (next.x == self.x) {
          // same column
          if (next.y > self.y && next.y - self.y < 4) {
            // below
            selectIfBetter(targets, next, Direction.S, (p, n) -> p.y > n.y);
          } else if (next.y < self.y  && self.y - next.y < 4) {
            // above
            selectIfBetter(targets, next, Direction.N, (p, n) -> p.y < n.y);
          }
        } else if (next.y == self.y) {
          // same row
          if (next.x < self.x && self.x - next.x < 4) {
            // left
            selectIfBetter(targets, next, Direction.W, (p, n) -> p.x < n.x);
          } else if (next.x > self.x && next.x - self.x < 4) {
            // right
            selectIfBetter(targets, next, Direction.E, (p, n) -> p.x > n.x);
          }
        }
      }
    }

    Action work() {
      if (self.wasHit) {
        Action action = random(Action.L, Action.R);
        if (isPossible(action)) {
          return action;
        }
        return action.getOpposite();
      }
      if (targets.get(self.direction) != null) {
        return Action.T;
      }
      Action action = handleNeighbour(Action.L);
      if (action != null) {
        return action;
      }
      action = handleNeighbour(Action.R);
      if (action != null) {
        return action;
      }
      if (forwardPossible) {
        return Action.F;
      }
      if (isPossible(Action.R)) {
        return Action.R;
      }
      return Action.L;
    }

    private Action handleNeighbour(Action turn) {
      Direction newDirection = self.direction.getNewDirection(turn);
      PlayerState target = targets.get(newDirection);
      if (target != null) {
        // someone there
        if (target.direction == newDirection.getOpposite()) {
          // aiming at us - run away
          if (forwardPossible) {
            return Action.F;
          }
          // can't runaway - lets face them for a shootout
        }
        return turn;
      }
      // no target on this side
      return null;
    }

    private Action random(Action...actions) {
      return actions[new Random().nextInt(actions.length)];
    }
  
    private void selectIfBetter(Map<Direction, PlayerState> targets, PlayerState next,
      Direction direction, PlayerPairFunctor<Boolean> criteria) {
      PlayerState previous = targets.get(direction);
      if (previous == null || criteria.check(previous, next)) {
        targets.put(direction, next);
      }
    }
  
  }
}
