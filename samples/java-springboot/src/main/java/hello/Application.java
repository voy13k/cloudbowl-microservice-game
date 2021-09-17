package hello;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.style.ToStringCreator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

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
      for (PlayerState nextPlayer: arenaUpdate.arena.state.values()) {
        if (nextPlayer.x == x) {
          // same column, opponent can be N or S
          if (!analyseNextPlayer(nextPlayer, Direction.S, (p) -> p.y - y)) {
            // opponent was not S
            analyseNextPlayer(nextPlayer, Direction.N, (p) -> y - p.y);
          }
        } else if (nextPlayer.y == y) {
          // same row, opponent may be E or W
          if (!analyseNextPlayer(nextPlayer, Direction.W, (p) -> x - p.x)) {
            // opponent was not W
            analyseNextPlayer(nextPlayer, Direction.E, (p) -> p.x - x);
          }
        }
      }
    }

    /**
     * @return true if the nextPlayer is in the directionFromUs, false otherwise.
     */
    private boolean analyseNextPlayer(PlayerState newPlayer, Direction directionFromUs,
        Function<PlayerState, Integer> distanceFromSelfCalc) {
      int newDistance = distanceFromSelfCalc.apply(newPlayer);
      if (newDistance <= 0) {
        // New player was on the other side (or it was actually us)
        return false;
      }

      if (newDistance < 4) {
        // New player in range.

        PlayerState previous = targets.get(directionFromUs);
        if (previous == null || newDistance < distanceFromSelfCalc.apply(previous)) {
          // New target.
          targets.put(directionFromUs, newPlayer);

          // Check if they are a potential shooter (looking at us).
          if (newPlayer.direction == directionFromUs.opposite) {
            shooters.put(directionFromUs, newPlayer);
          } else {
            // the new target is closer, but is not looking at us,
            // so there is no shooter in this direction.
            shooters.remove(directionFromUs);
          }
        }
      }
      return true;
    }

    private void analyseSpace() {
      // assumes opponents analysed
      if (y != 0) {
        checkSpace(Direction.N, (t) -> y - t.y);
      }
      if (y < arenaUpdate.arena.dims.get(1) - 1) {
        checkSpace(Direction.S, (t) -> t.y - y);
      }
      if (x != 0) {
        checkSpace(Direction.W, (t) -> t.x - x);
      }
      if (x < arenaUpdate.arena.dims.get(0) - 1) {
        checkSpace(Direction.E, (t) -> x - t.x);
      }
    }

    private void checkSpace(Direction direction,
        Function<PlayerState, Integer> targetDistanceCalc) {
      PlayerState target = targets.get(direction);
      Integer distance = target == null ? null : targetDistanceCalc.apply(target);
      if (target == null || distance > 1) {
        space.add(direction);
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
    }

    Action work() {
      if (self.wasHit || locationData.shooters.size() > 1) {
        if (locationData.space.contains(self.direction)
            && locationData.shooters.get(self.direction) == null) {
          return Action.F;
        }
        Action action = Action.L;
        if (locationData.isPossible(self.direction, action)) {
          return action;
        }
        return action.opposite;
      }
      if (locationData.targets.get(self.direction) != null) {
        return Action.T;
      }
      Action action = findTarget(Action.L);
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
      action = Action.R;
      if (locationData.isPossible(self.direction, action)) {
        return action;
      }
      if (locationData.isPossible(self.direction, action.opposite)) {
        return action.opposite;
      }
      return Action.T;
    }

    private Action findTarget(Action turn) {
      Direction newDirection = self.direction.getNewDirection(turn);
      if (locationData.targets.get(newDirection) == null) {
        // no target on this side
        return null;
      }
      if (locationData.shooters.get(newDirection) != null) {
        // aiming at us - run away
        if (locationData.space.contains(self.direction)) {
          return Action.F;
        } // can't runaway
      } // not a shooter
      // shootout
      return turn;
    }

  }

}
