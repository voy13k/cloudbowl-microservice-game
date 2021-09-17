package hello;

enum Direction {

  N("W", "E", "S", (from, to) -> from.getY() - to.getY()),
  S("E", "W", "N", (from, to) -> to.getY() - from.getY()),
  E("N", "S", "W", (from, to) -> to.getX() - from.getX()),
  W("S", "N", "E", (from, to) -> from.getX() - to.getX());

  public Direction left;
  public Direction right;
  public Direction opposite;
  public CellDistanceCalc distance;

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

  private Direction(String left, String right, String opposite, CellDistanceCalc distanceCalc) {
    this.leftStr = left;
    this.rightStr = right;
    this.oppositeStr = opposite;
    this.distance = distanceCalc;
  }

  public Direction getDirectionAfter(Action action) {
    switch (action) {
    case L:
      return left;
    case R:
      return right;
    default:
      return this;
    }
  }

  public interface CellDistanceCalc {
    int fromTo(Cell p1, Cell p2);
  }

}
