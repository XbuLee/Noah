package cn.leexiaobui.core;

/**
 * @author Leexiaobu
 * @date 2021-02-03 11:30
 */
public class ThreadLocalTest {

  public static void main(String[] args) throws InterruptedException {
    for (int i = 0; i < 10; i++) {
      new Thread() {
        @Override
        public void run() {
          ThreadLocal<String> test = new ThreadLocal<>();
          Thread thread = Thread.currentThread();
          System.out.println(thread.getName());
          test.set(thread.getName());
          System.out.println(thread.getName());
        }
      }.start();
    }
    Thread.currentThread().wait();
  }

}