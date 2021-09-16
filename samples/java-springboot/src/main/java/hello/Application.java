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

  static {
    for (Direction direction: Direction.values()) {
      direction.left = Direction.valueOf(direction.leftStr);
      direction.right = Direction.valueOf(direction.rightStr);
      direction.opposite = Direction.valueOf(direction.oppositeStr);
    }
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
    Action action = new Worker(arenaUpdate).work();
    System.out.println("return "+ action);
    return action.name();
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
      System.out.println("targets:" + targets);
      System.out.println("shooters:" + shooters);
      System.out.println("space:" + space);
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
      checkSpace(Direction.N, self.y == 0, (t) -> self.y - t.y);
      checkSpace(Direction.S, self.y + 1 >= arenaUpdate.arena.dims.get(1), (t) -> t.y - self.y);
      checkSpace(Direction.W, self.x == 0, (t) -> t.x - self.x);
      checkSpace(Direction.E, self.x + 1 >= arenaUpdate.arena.dims.get(0), (t) -> self.x - t.x);
    }

    private void checkSpace(Direction direction, boolean onBoundary, Function<PlayerState, Integer> targetDistanceCalc) {
      PlayerState target = targets.get(direction);
      Integer distance = target == null ? null : targetDistanceCalc.apply(target);
      System.out.printf("checkSpace: %s, %s, %s, %s\n", direction, onBoundary, target, distance);
      if (!onBoundary && (target == null || distance > 1)) {
        space.add(direction);
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
      Action action = random(Action.L, Action.R);
      action = handleNeighbour(Action.L);
      if (action != null) {
        return action;
      }
      action = handleNeighbour(Action.R);
      if (action != null) {
        return action;
      }
      if (isPossible(Action.F)) {
        return Action.F;
      }
      action = random(Action.L, Action.R);
      if (isPossible(action)) {
        return action;
      }
      action = action.getOpposite();
      if (isPossible(action)) {
        return action;
      }
      return Action.T;
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
