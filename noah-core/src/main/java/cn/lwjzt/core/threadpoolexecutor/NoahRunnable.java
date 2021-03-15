package cn.lwjzt.core.threadpoolexecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Leexiaobu
 * @date 2021-02-18 15:27
 */
public class NoahRunnable implements Runnable {

  private Runnable runnable;

  public NoahRunnable(Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public void run() {
    doBeford();
    runnable.run();
  }

  void doBeford() {
    TransmittableThreadLocal.removeTraceId();
  }

  static Map<Thread, NoahRunnable> map = new HashMap<>();

  public void setRunnable(Runnable runnable) {
    this.runnable = runnable;
  }

  public static NoahRunnable get(Runnable runnable) {

    if (null == runnable) {
      return null;
    }
    if (map.containsKey(Thread.currentThread())&&!(runnable instanceof  NoahRunnable)) {
      NoahRunnable noahRunnable = map.get(Thread.currentThread());
      noahRunnable.setRunnable(runnable);
      return noahRunnable;
    } else {
      NoahRunnable noahRunnable = new NoahRunnable(runnable);
      map.put(Thread.currentThread(), noahRunnable);
      return noahRunnable;
    }
  }
}