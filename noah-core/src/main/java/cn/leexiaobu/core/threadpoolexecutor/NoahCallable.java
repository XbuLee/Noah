package cn.leexiaobu.core.threadpoolexecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Leexiaobu
 * @date 2021-02-18 17:00
 */
public class NoahCallable<V> implements java.util.concurrent.Callable<V> {

  private Callable<V> callable;

  public NoahCallable(Callable<V> callable) {
    this.callable = callable;
  }

  @Override
  public V call() throws Exception {
    doBeford();
    return callable.call();
  }

  void doBeford() {
    TransmittableThreadLocal.removeTraceId();
  }

  static Map<Thread, NoahCallable> map = new HashMap<>();

  public void setCallable(Callable callable) {
    this.callable = callable;
  }


  public static NoahCallable get(Callable callable) {
    if (null == callable) {
      return null;
    }
    if (map.containsKey(Thread.currentThread())&&!(callable instanceof  NoahCallable)) {
      NoahCallable noahCallable = map.get(Thread.currentThread());
      noahCallable.setCallable(callable);
      return noahCallable;
    } else {
      NoahCallable noahCallable = new NoahCallable(callable);
      map.put(Thread.currentThread(), noahCallable);
      return noahCallable;
    }
  }


}