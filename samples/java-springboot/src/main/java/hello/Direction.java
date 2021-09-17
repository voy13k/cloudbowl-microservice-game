package hello;

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

  static {
    for (Direction direction: Direction.values()) {
      direction.left = Direction.valueOf(direction.leftStr);
      direction.right = Direction.valueOf(direction.rightStr);
      direction.opposite = Direction.valueOf(direction.oppositeStr);
    }
  }

  Direction(String left, String right, String opposite) {
    this.leftStr = left;
    this.rightStr = right;
    this.oppositeStr = opposite;
  }

  Direction getNewDirection(Action action) {
    switch (action) {
    case L:
      return left;
    case R:
      return right;
    default:
      return this;
    }
  }

}
