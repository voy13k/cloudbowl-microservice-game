package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

@SpringBootApplication
@RestController
public class Application {
  enum Direction {
    N("W", "E", "S", 0, -1),
    S("E", "W", "N", 0, 1),
    E("N", "S", "W", 1, 0),
    W("S", "N", "E", -1, 0);

    static {
      for (Direction direction: values()) {
        direction.left = valueOf(direction.leftStr);
        direction.right = valueOf(direction.rightStr);
        direction.left = valueOf(direction.oppositeStr);
      }
    }

    public Direction left;
    public Direction right;
    public Direction opposite;
    public final int yStep;
    public final int xStep;
    private String leftStr;
    private String rightStr;
    private String oppositeStr;

    Direction(String left, String right, String opposite, int xStep, int yStep) {
      this.leftStr = left;
      this.rightStr = right;
      this.oppositeStr = opposite;
      this.xStep = xStep;
      this.yStep = yStep;
    }

    Direction getNewDirection(Action action) {
      switch(action) {
        case L:
          return left;
        case R:
          return right;
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

  Map<Direction, PlayerState> targets = new EnumMap<>(Direction.class);
  Map<Direction, PlayerState> shooters = new EnumMap<>(Direction.class);
  Set<Direction> space = EnumSet.noneOf(Direction.class);

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
    PlayerState self;

    Worker(ArenaUpdate arenaUpdate) {
      this.arenaUpdate = arenaUpdate;
      self = arenaUpdate.arena.state.get(arenaUpdate._links.self.href);
      analyse();
    }

    private void analyse() {
      targets.clear();
      shooters.clear();
      space.clear();
      analyseOponents();
      analyseSpace();
    }

    private void analyseOponents() {
      for (PlayerState opponent : arenaUpdate.arena.state.values()) {
        if (opponent.x == self.x) {
          // same column
          if (opponent.y > self.y && opponent.y - self.y < 4) {
            // below
            analyseOponent(opponent, Direction.S, (prev) -> prev.y > opponent.y);
          } else if (opponent.y < self.y  && self.y - opponent.y < 4) {
            // above
            analyseOponent(opponent, Direction.N, (prev) -> prev.y < opponent.y);
          }
        } else if (opponent.y == self.y) {
          // same row
          if (opponent.x < self.x && self.x - opponent.x < 4) {
            // left
            analyseOponent(opponent, Direction.W, (prev) -> prev.x < opponent.x);
          } else if (opponent.x > self.x && opponent.x - self.x < 4) {
            // right
            analyseOponent(opponent, Direction.E, (prev) -> prev.x > opponent.x);
          }
        }
      }
    }

    private void analyseOponent(PlayerState next, Direction direction, Function<PlayerState, Boolean> criteria) {
      PlayerState previous = targets.get(direction);
      if (previous == null || criteria.apply(previous)) {
        targets.put(direction, next);
        if (next.direction == direction.opposite) {
          shooters.put(direction, next);
        } else {
          shooters.remove(direction);
        }
      }
    }

    private void analyseSpace() {
      // assumes oponents analysed
      if (self.y > 0 && self.y - targets.get(Direction.S).y > 1) {
        space.add(Direction.N);
      }
      if (self.y + 1 < arenaUpdate.arena.dims.get(1) && targets.get(Direction.N).y - self.y > 1) {
        space.add(Direction.S);
      }
      if (self.x > 0 && targets.get(Direction.E).x - self.x > 1) {
        space.add(Direction.W);
      }
      if (self.x + 1 < arenaUpdate.arena.dims.get(0) && self.x - targets.get(Direction.W).x > 1) {
        space.add(Direction.E);
      }
    }
  
    private boolean isPossible(Action action) {
      Direction newDirection = self.direction.getNewDirection(action);
      return space.contains(newDirection) &&
          shooters.get(newDirection) == null &&
          shooters.get(newDirection.opposite) == null;
    }

    Action work() {
      if (self.wasHit) {
        if (isPossible(Action.F)) {
          return Action.F;
        }
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
        if (shooters.get(newDirection) != null) {
          // aiming at us - run away
          if (space.contains(self.direction)) {
            return Action.F;
          } // can't runaway
        } // not a shooter
        // shootout
        return turn;
      }
      // no target on this side
      return null;
    }

    private Action random(Action...actions) {
      return actions[new Random().nextInt(actions.length)];
    }
  
  }
}
