package hello;

import static hello.Action.F;
import static hello.Action.L;
import static hello.Action.R;
import static hello.Direction.E;
import static hello.Direction.N;
import static hello.Direction.S;
import static hello.Direction.W;

import org.junit.Test;

public class BoundaryTest extends ApplicationTestBase {

  public BoundaryTest() {
    super(8, 6);
  }

  @Test
  public void forward() {
    setUp(
        "Forward", false,
        6, 5, E,
        5, 6, N);
    verify(F);
  }

  @Test
  public void topBoundary() {
    setUp(
        "TopBoundary", false,
        6, 0, N,
        5, 6, N);
    verify(L, R);
  }

  @Test
  public void bottomBoundary() {
    setUp(
        "BottomBoundary", false,
        6, 5, S,
        5, 6, N);
    verify(L, R);
  }

  @Test
  public void leftBoundary() {
    setUp(
        "LeftBoundary", false,
        0, 5, W,
        5, 6, N);
    verify(L, R);
  }

  @Test
  public void rightBoundary() {
    setUp(
        "RightBoundary", false,
        7, 5, E,
        5, 6, N);
    verify(L, R);
  }

}
