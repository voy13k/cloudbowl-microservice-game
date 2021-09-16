package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.style.ToStringCreator;
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
    N("W", "E", "S"),
    S("E", "W", "N"),
    E("N", "S", "W"),
    W("S", "N", "E");

    public Direction left;
    public Direction right;
    public Direction opposite;

    private String leftStr;
    private String rightStr;
    private String oppositeStr;

    static {
      for (Direction direction: Direction.values()) {
        direction.left = Direction.valueOf(direction.leftStr);
        direction.right = Direction.valueOf(direction.rightStr);
        direction.opposite = Direction.valueOf(direction.oppositeStr);
      }
    }

    Direction(String left, String right, String opposite) {
      this.leftStr = left;
      this.rightStr = right;
      this.oppositeStr = opposite;
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
    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
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

  static private final Logger LOGGER = LoggerFactory.getLogger(Application.class);

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
    Action action = new Worker(arenaUpdate).work();
    LOGGER.debug("action: {}", action);
    return action.name();
  }

  class LocationData {
    Map<Direction, PlayerState> targets = new EnumMap<>(Direction.class);
    Map<Direction, PlayerState> shooters = new EnumMap<>(Direction.class);
    Set<Direction> space = EnumSet.noneOf(Direction.class);

    private int x, y;
    private ArenaUpdate arenaUpdate;

    LocationData(ArenaUpdate arenaUpdate, int x, int y) {
      this.arenaUpdate = arenaUpdate;
      this.x = x;
      this.y = y;
      analyseOponents();
      analyseSpace();
    }

    private void analyseOponents() {
      for (PlayerState nextOponent : arenaUpdate.arena.state.values()) {
        if (nextOponent.x == x) {
          // same column
          if (nextOponent.y > y && nextOponent.y - y < 4) {
            // below
            analyseOponent(nextOponent, Direction.S, (prev) -> prev.y > nextOponent.y);
          } else if (nextOponent.y < y  && y - nextOponent.y < 4) {
            // above
            analyseOponent(nextOponent, Direction.N, (prev) -> prev.y < nextOponent.y);
          }
        } else if (nextOponent.y == y) {
          // same row
          if (nextOponent.x < x && x - nextOponent.x < 4) {
            // left
            analyseOponent(nextOponent, Direction.W, (prev) -> prev.x < nextOponent.x);
          } else if (nextOponent.x > x && nextOponent.x - x < 4) {
            // right
            analyseOponent(nextOponent, Direction.E, (prev) -> prev.x > nextOponent.x);
          }
        }
      }
    }

    private void analyseOponent(PlayerState next, Direction directionToOpponent, Function<PlayerState, Boolean> criteria) {
      PlayerState previous = targets.get(directionToOpponent);
      if (previous == null || criteria.apply(previous)) {
        targets.put(directionToOpponent, next);
        if (next.direction == directionToOpponent.opposite) {
          shooters.put(directionToOpponent, next);
        } else {
          shooters.remove(directionToOpponent);
        }
      }
    }

    private void analyseSpace() {
      // assumes oponents analysed
      checkSpace(Direction.N, y == 0, (t) -> y - t.y);
      checkSpace(Direction.S, y + 1 >= arenaUpdate.arena.dims.get(1), (t) -> t.y - y);
      checkSpace(Direction.W, x == 0, (t) -> t.x - x);
      checkSpace(Direction.E, x + 1 >= arenaUpdate.arena.dims.get(0), (t) -> x - t.x);
    }

    private void checkSpace(Direction directionToSpace, boolean onBoundary, Function<PlayerState, Integer> targetDistanceCalc) {
      PlayerState target = targets.get(directionToSpace);
      Integer distance = target == null ? null : targetDistanceCalc.apply(target);
      LOGGER.debug("checkSpace: {}, {}, {}, {}", directionToSpace, onBoundary, target, distance);
      if (!onBoundary && (target == null || distance > 1)) {
        space.add(directionToSpace);
      }
    }
    @Override
    public String toString() {
      return new ToStringCreator(this)
        .append("x", x)
        .append("y", y)
        .append("targets", targets)
        .append("shooters", shooters)
        .append("space", space).toString();
    }

    boolean isSafe(Direction currentDirection, Action action) {
      Direction newDirection = currentDirection.getNewDirection(action);
      return shooters.get(newDirection) == null &&
          shooters.get(newDirection.opposite) == null;
    }

    private boolean isPossible(Direction currentDirection, Action action) {
      Direction newDirection = currentDirection.getNewDirection(action);
      return space.contains(newDirection);
    }

  }

  class Worker {
    LocationData locationData;
    ArenaUpdate arenaUpdate;
    PlayerState self;

    Worker(ArenaUpdate arenaUpdate) {
      this.arenaUpdate = arenaUpdate;
      this.self = arenaUpdate.arena.state.get(arenaUpdate._links.self.href);
      this.locationData = new LocationData(arenaUpdate, self.x, self.y);
      LOGGER.debug("{}", locationData);
    }

    Action work() {
      if (self.wasHit) {
        if (locationData.space.contains(self.direction)) {
          return Action.F;
        }
        Action action = random(Action.L, Action.R);
        if (locationData.isPossible(self.direction, action)) {
          return action;
        }
        return action.getOpposite();
      }
      if (locationData.targets.get(self.direction) != null) {
        return Action.T;
      }
      Action action = random(Action.L, Action.R);
      action = findTarget(Action.L);
      if (action != null) {
        return action;
      }
      action = findTarget(Action.R);
      if (action != null) {
        return action;
      }
      if (locationData.space.contains(self.direction)) {
        return Action.F;
      }
      action = random(Action.L, Action.R);
      if (locationData.isPossible(self.direction, action)) {
        return action;
      }
      action = action.getOpposite();
      if (locationData.isPossible(self.direction, action)) {
        return action;
      }
      return Action.T;
    }

    private Action findTarget(Action turn) {
      Direction newDirection = self.direction.getNewDirection(turn);
      PlayerState target = locationData.targets.get(newDirection);
      if (target != null) {
        // someone there
        if (locationData.shooters.get(newDirection) != null) {
          // aiming at us - run away
          if (locationData.space.contains(self.direction)) {
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
