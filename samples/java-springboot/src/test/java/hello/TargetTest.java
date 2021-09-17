package hello;

public class TargetTest extends ApplicationTestBase {

  public TargetTest() {
    super(8, 6);
  }

  public void testEastToNorthTarget() {
    setUp(
        "EastToNorth", false,
        6, 5, "E",
        6, 2, "E");
    verify("L");
  }

  public void testEastToNorthOutOfRange() {
    setUp(
        "EastToNorthOutOfRange", false,
        6, 5, "E",
        6, 1, "E");
    verify("F");
  }

  public void testEastToNorthShooter() {
    setUp(
        "EastToNorthShooter", false,
        6, 5, "E",
        6, 2, "S");
    verify("F");
  }

  public void testWestToNorthTarget() {
    setUp(
        "WestToNorth", false,
        6, 5, "W",
        6, 2, "E");
    verify("R");
  }

  public void testWestToNorthOutOfRange() {
    setUp(
        "WestToNorthOutOfRange", false,
        6, 5, "W",
        6, 1, "E");
    verify("F");
  }

  public void testWestToNorthShooter() {
    setUp(
        "WestToNorthShooter", false,
        6, 5, "W",
        6, 2, "S");
    verify("F");
  }

  public void testNorthToEastTarget() {
    setUp(
        "NorthToEast", false,
        3, 5, "N",
        6, 5, "E");
    verify("R");
  }

  public void testNorthToEastOutOfRange() {
    setUp(
        "NorthToEastOutOfRange", false,
        3, 5, "N",
        7, 5, "E");
    verify("F");
  }

  public void testNorthToEastShooter() {
    setUp(
        "NorthToEastShooter", false,
        3, 5, "N",
        6, 5, "W");
    verify("F");
  }

}
