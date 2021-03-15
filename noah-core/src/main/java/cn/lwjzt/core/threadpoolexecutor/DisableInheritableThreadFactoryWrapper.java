package cn.lwjzt.core.threadpoolexecutor;


import cn.lwjzt.core.threadpoolexecutor.TransmittableThreadLocal.Transmitter;
import java.util.concurrent.ThreadFactory;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.10.0
 */
class DisableInheritableThreadFactoryWrapper {

  private final ThreadFactory threadFactory;

  DisableInheritableThreadFactoryWrapper(ThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
  }

  public Thread newThread(Runnable r) {
    //线程执行前
    final Object backup = Transmitter.clear();
    try {
      Thread thread = threadFactory.newThread(r);

      return new Thread(thread);
    } finally {
      //线程执行后
      Transmitter.restore(backup);
    }
  }

  public ThreadFactory unwrap() {
    return threadFactory;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DisableInheritableThreadFactoryWrapper that = (DisableInheritableThreadFactoryWrapper) o;

    return threadFactory.equals(that.threadFactory);
  }

  @Override
  public int hashCode() {
    return threadFactory.hashCode();
  }

  @Override
  public String toString() {
    return this.getClass().getName() + " - " + threadFactory.toString();
  }
}
