package hello;

import static hello.Direction.E;
import static hello.Direction.N;
import static hello.Direction.S;
import static hello.Direction.W;
import static org.junit.Assert.*;

import org.junit.Test;

public class DirectionTest {

  @Test
  public void sides() {
    assertSame(W, N.left);
    assertSame(E, N.right);
    assertSame(S, N.opposite);

    assertSame(S, W.left);
    assertSame(N, W.right);
    assertSame(E, W.opposite);

    assertSame(E, S.left);
    assertSame(W, S.right);
    assertSame(N, S.opposite);

    assertSame(N, E.left);
    assertSame(S, E.right);
    assertSame(W, E.opposite);
  }

  @Test
  public void northDistance() {
    assertEquals(-2, N.distance.fromTo(Cell.at(0, 4), Cell.at(0, 6)));
    assertEquals(-1, N.distance.fromTo(Cell.at(0, 4), Cell.at(0, 5)));
    assertEquals(0, N.distance.fromTo(Cell.at(0, 4), Cell.at(0, 4)));
    assertEquals(1, N.distance.fromTo(Cell.at(0, 4), Cell.at(0, 3)));
    assertEquals(2, N.distance.fromTo(Cell.at(0, 4), Cell.at(0, 2)));
  }

  @Test
  public void westDistance() {
    assertEquals(-2, W.distance.fromTo(Cell.at(4, 0), Cell.at(6, 0)));
    assertEquals(-1, W.distance.fromTo(Cell.at(4, 0), Cell.at(5, 0)));
    assertEquals(0, W.distance.fromTo(Cell.at(4, 0), Cell.at(4, 0)));
    assertEquals(1, W.distance.fromTo(Cell.at(4, 0), Cell.at(3, 0)));
    assertEquals(2, W.distance.fromTo(Cell.at(4, 0), Cell.at(2, 0)));
  }

  @Test
  public void southDistance() {
    assertEquals(-2, S.distance.fromTo(Cell.at(0, 4), Cell.at(0, 2)));
    assertEquals(-1, S.distance.fromTo(Cell.at(0, 4), Cell.at(0, 3)));
    assertEquals(0, S.distance.fromTo(Cell.at(0, 4), Cell.at(0, 4)));
    assertEquals(1, S.distance.fromTo(Cell.at(0, 4), Cell.at(0, 5)));
    assertEquals(2, S.distance.fromTo(Cell.at(0, 4), Cell.at(0, 6)));
  }

  @Test
  public void eastDistance() {
    assertEquals(-2, E.distance.fromTo(Cell.at(4, 0), Cell.at(2, 0)));
    assertEquals(-1, E.distance.fromTo(Cell.at(4, 0), Cell.at(3, 1)));
    assertEquals(0, E.distance.fromTo(Cell.at(4, 0), Cell.at(4, 2)));
    assertEquals(1, E.distance.fromTo(Cell.at(4, 0), Cell.at(5, 3)));
    assertEquals(2, E.distance.fromTo(Cell.at(4, 0), Cell.at(6, 4)));
  }

  @Test
  public void directionAfter() {
    assertSame(N, N.getDirectionAfter(Action.F));
    assertSame(W, N.getDirectionAfter(Action.L));
    assertSame(E, N.getDirectionAfter(Action.R));
    assertSame(N, N.getDirectionAfter(Action.T));
  }

}
