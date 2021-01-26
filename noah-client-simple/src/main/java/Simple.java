

/**
 * @author Leexiaobu
 * @date 2021-01-21 17:19
 */
public class Simple {

  public static void main(String[] args) throws InterruptedException {
    System.out.println("simple");
    new Simple().test();
  }

  void test() throws InterruptedException {
    System.out.println("test");
    Thread.sleep(1000);
    System.out.println("test end ");
  }
}