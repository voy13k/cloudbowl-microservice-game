package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

@SpringBootApplication
@RestController
public class Application {
  enum Direction {
    N,S,E,W
  }

  enum Action {
    F,L,R,T
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
    if (arenaUpdate.arena.state.get(arenaUpdate._links.self.href).wasHit) {
      return escape(arenaUpdate);
    }
    if (worthShooting(arenaUpdate)) {
      return "T";
    }
    return random(Action.F, Action.R, Action.L);
  }

  private String escape(ArenaUpdate arenaUpdate) {
    return random(Action.F, Action.L, Action.R);
  }

  private boolean worthShooting(ArenaUpdate arenaUpdate) {
    PlayerState self = arenaUpdate.arena.state.get(arenaUpdate._links.self.href);
    PlayerPositionFunctor f = TARGETTING.get(self.direction);
    for(PlayerState target: arenaUpdate.arena.state.values()) {
      if (f.check(target, self)) {
        return true;
      }
    }
    return false;
  }

  private String random(Action...actions) {
    return actions[new Random().nextInt(actions.length)].name();
  }

  Map<Direction, PlayerPositionFunctor> TARGETTING = new EnumMap<>(Direction.class);
  {
    TARGETTING.put(Direction.N, (o, s) -> o.x == s.x && o.y < s.y && s.y - o.y < 4);
    TARGETTING.put(Direction.S, (o, s) -> o.x == s.x && o.y > s.y && o.y - s.y < 4);
    TARGETTING.put(Direction.E, (o, s) -> o.y == s.y && o.x > s.x && o.x - s.x < 4);
    TARGETTING.put(Direction.W, (o, s) -> o.y == s.y && o.x < s.x && s.x - o.x < 4);
  }

  interface PlayerPositionFunctor {
    boolean check(PlayerState other, PlayerState self);
  }
}
