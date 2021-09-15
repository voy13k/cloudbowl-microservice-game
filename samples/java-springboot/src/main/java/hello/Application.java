package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

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
    public String direction;
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
    if (!arenaUpdate.arena.state.get(arenaUpdate._links.self.href).wasHit &&
      worthShooting(arenaUpdate)) {
      return "T";
    }
    String[] commands = new String[] {
      "F", "R", "L"
    };
    int i = new Random().nextInt(3);
    return commands[i];
  }

  private boolean worthShooting(ArenaUpdate arenaUpdate) {
    int _x = arenaUpdate.arena.state.get(arenaUpdate._links.self.href).x;
    int _y = arenaUpdate.arena.state.get(arenaUpdate._links.self.href).y;
    String _d = arenaUpdate.arena.state.get(arenaUpdate._links.self.href).direction;
    
    Predicate<PlayerState> checkTarget;
    switch (_d) {
    case "N":
      checkTarget = (ps) -> ps.x == _x && ps.y < _y && _y - ps.y < 4;
      break;
    case "S":
      checkTarget = (ps) -> ps.x == _x && ps.y > _y && ps.y - _y < 4;
      break;
    case "E":
      checkTarget = (ps) -> ps.y == _y && ps.x > _x && ps.x - _x < 4;
      break;
    default:
      checkTarget = (ps) -> ps.y == _y && ps.x < _x && _x - ps.x < 4;
      break;
    }
    for(PlayerState target: arenaUpdate.arena.state.values()) {
      if (checkTarget.test(target)) {
        return true;
      }
    }
    return false;
  }

}
