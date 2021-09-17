package hello;

public class TestUtils {

  static Cell cell(final int x, final int y) {
    return new Cell() {
  
      @Override
      public int getY() {
        return y;
      }
  
      @Override
      public int getX() {
        return x;
      }
    };
  }

}
