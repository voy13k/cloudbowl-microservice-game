package hello;

import static hello.Action.F;
import static hello.Action.L;
import static hello.Action.R;
import static hello.Direction.E;
import static hello.Direction.N;
import static hello.Direction.S;
import static hello.Direction.W;

import org.junit.Test;

public class TargetTest extends ApplicationTestBase {

  public TargetTest() {
    super(8, 6);
  }

  @Test
  public void testEastToNorthTarget() {
    setUp(
        "EastToNorth", false,
        6, 5, E,
        6, 2, E);
    verify(L);
  }

  @Test
  public void testEastToNorthOutOfRange() {
    setUp(
        "EastToNorthOutOfRange", false,
        6, 5, E,
        6, 1, E);
    verify(F);
  }

  @Test
  public void testEastToNorthShooter() {
    setUp(
        "EastToNorthShooter", false,
        6, 5, E,
        6, 2, S);
    verify(F);
  }

  @Test
  public void testWestToNorthTarget() {
    setUp(
        "WestToNorth", false,
        6, 5, W,
        6, 2, E);
    verify(R);
  }

  @Test
  public void testWestToNorthOutOfRange() {
    setUp(
        "WestToNorthOutOfRange", false,
        6, 5, W,
        6, 1, E);
    verify(F);
  }

  @Test
  public void testWestToNorthShooter() {
    setUp(
        "WestToNorthShooter", false,
        6, 5, W,
        6, 2, S);
    verify(F);
  }

  @Test
  public void testNorthToEastTarget() {
    setUp(
        "NorthToEast", false,
        3, 5, N,
        6, 5, E);
    verify(R);
  }

  @Test
  public void testNorthToEastOutOfRange() {
    setUp(
        "NorthToEastOutOfRange", false,
        3, 5, N,
        7, 5, E);
    verify(F);
  }

  @Test
  public void testNorthToEastShooter() {
    setUp(
        "NorthToEastShooter", false,
        3, 5, N,
        6, 5, W);
    verify(F);
  }

}
