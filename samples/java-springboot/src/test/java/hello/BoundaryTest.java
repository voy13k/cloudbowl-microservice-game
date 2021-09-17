package hello;

public class BoundaryTest extends ApplicationTestBase {

  public BoundaryTest() {
    super(8, 6);
  }

  public void testForward() {
    setUp(
        "Forward", false,
        6, 5, "E",
        5, 6, "N");
    verify("F");
  }

  public void testTopBoundary() {
    setUp(
        "TopBoundary", false,
        6, 0, "N",
        5, 6, "N");
    verify("L", "R");
  }

  public void testBottomBoundary() {
    setUp(
        "BottomBoundary", false,
        6, 5, "S",
        5, 6, "N");
    verify("L", "R");
  }

  public void testLeftBoundary() {
    setUp(
        "LeftBoundary", false,
        0, 5, "W",
        5, 6, "N");
    verify("L", "R");
  }

  public void testRightBoundary() {
    setUp(
        "RightBoundary", false,
        7, 5, "E",
        5, 6, "N");
    verify("L", "R");
  }

}
