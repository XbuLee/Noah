package cn.leexiaobu.core.threadpoolexecutor;

import cn.leexiaobu.core.TraceNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransmittableThreadLocal<T> extends InheritableThreadLocal<T> implements TtlCopier<T> {

  private static final Logger logger = Logger.getLogger(TransmittableThreadLocal.class.getName());

  private final boolean disableIgnoreNullValueSemantics;

  public TransmittableThreadLocal() {
    this(false);
  }

  public TransmittableThreadLocal(boolean disableIgnoreNullValueSemantics) {
    this.disableIgnoreNullValueSemantics = disableIgnoreNullValueSemantics;
  }

  public T copy(T parentValue) {
    return parentValue;
  }

  protected void beforeExecute() {
  }

  protected void afterExecute() {
  }

  @Override
  public final T get() {
    T value = super.get();
    if (disableIgnoreNullValueSemantics || null != value) {
      addThisToHolder();
    }
    return value;
  }

  @Override
  public final void set(T value) {
    if (!disableIgnoreNullValueSemantics && null == value) {
      // may set null to remove value
      remove();
    } else {
      super.set(value);
      addThisToHolder();
    }
  }

  @Override
  public final void remove() {
    removeThisFromHolder();
    super.remove();
  }

  private void superRemove() {
    super.remove();
  }

  private T copyValue() {
    return copy(get());
  }

  private static final InheritableThreadLocal<WeakHashMap<TransmittableThreadLocal<Object>, ?>> holder =
      new InheritableThreadLocal<WeakHashMap<TransmittableThreadLocal<Object>, ?>>() {
        @Override
        protected WeakHashMap<TransmittableThreadLocal<Object>, ?> initialValue() {
          return new WeakHashMap<TransmittableThreadLocal<Object>, Object>();
        }

        @Override
        protected WeakHashMap<TransmittableThreadLocal<Object>, ?> childValue(
            WeakHashMap<TransmittableThreadLocal<Object>, ?> parentValue) {
          return new WeakHashMap<TransmittableThreadLocal<Object>, Object>(parentValue);
        }
      };

  private void addThisToHolder() {
    if (!holder.get().containsKey(this)) {
      holder.get()
          .put((TransmittableThreadLocal<Object>) this, null); // WeakHashMap supports null value.
    }
  }

  private void removeThisFromHolder() {
    holder.get().remove(this);
  }

  private static void doExecuteCallback(boolean isBefore) {
    for (TransmittableThreadLocal<Object> threadLocal : holder.get().keySet()) {
      try {
        if (isBefore) {
          threadLocal.beforeExecute();
        } else {
          threadLocal.afterExecute();
        }
      } catch (Throwable t) {
        if (logger.isLoggable(Level.WARNING)) {
          logger.log(Level.WARNING,
              "TTL exception when " + (isBefore ? "beforeExecute" : "afterExecute") + ", cause: "
                  + t.toString(), t);
        }
      }
    }
  }

  static void dump(String title) {
    if (title != null && title.length() > 0) {
      System.out.printf("Start TransmittableThreadLocal[%s] Dump...%n", title);
    } else {
      System.out.println("Start TransmittableThreadLocal Dump...");
    }

    for (TransmittableThreadLocal<Object> threadLocal : holder.get().keySet()) {
      System.out.println(threadLocal.get());
    }
    System.out.println("TransmittableThreadLocal Dump end!");
  }

  public static TraceNode removeTraceId() {
    for (final Iterator<TransmittableThreadLocal<Object>> iterator = holder.get().keySet()
        .iterator(); iterator.hasNext(); ) {
      TransmittableThreadLocal<Object> threadLocal = iterator.next();
      if (threadLocal.get() instanceof TraceNode) {
        TraceNode temp = (TraceNode) threadLocal.get();
        iterator.remove();
        threadLocal.superRemove();
        return temp;
      }
    }
    return null;
  }

  static void dump() {
    dump(null);
  }

  public static class Transmitter {

    public static Object capture() {
      return new Snapshot(captureTtlValues(), captureThreadLocalValues());
    }

    private static HashMap<TransmittableThreadLocal<Object>, Object> captureTtlValues() {
      HashMap<TransmittableThreadLocal<Object>, Object> ttl2Value = new HashMap<TransmittableThreadLocal<Object>, Object>();
      for (TransmittableThreadLocal<Object> threadLocal : holder.get().keySet()) {
        ttl2Value.put(threadLocal, threadLocal.copyValue());
      }
      return ttl2Value;
    }

    private static HashMap<ThreadLocal<Object>, Object> captureThreadLocalValues() {
      final HashMap<ThreadLocal<Object>, Object> threadLocal2Value = new HashMap<ThreadLocal<Object>, Object>();
      for (Map.Entry<ThreadLocal<Object>, TtlCopier<Object>> entry : threadLocalHolder.entrySet()) {
        final ThreadLocal<Object> threadLocal = entry.getKey();
        final TtlCopier<Object> copier = entry.getValue();

        threadLocal2Value.put(threadLocal, copier.copy(threadLocal.get()));
      }
      return threadLocal2Value;
    }

    public static Object replay(Object captured) {
      final Snapshot capturedSnapshot = (Snapshot) captured;
      return new Snapshot(replayTtlValues(capturedSnapshot.ttl2Value),
          replayThreadLocalValues(capturedSnapshot.threadLocal2Value));
    }

    private static HashMap<TransmittableThreadLocal<Object>, Object> replayTtlValues(
        HashMap<TransmittableThreadLocal<Object>, Object> captured) {
      HashMap<TransmittableThreadLocal<Object>, Object> backup = new HashMap<TransmittableThreadLocal<Object>, Object>();

      for (final Iterator<TransmittableThreadLocal<Object>> iterator = holder.get().keySet()
          .iterator(); iterator.hasNext(); ) {
        TransmittableThreadLocal<Object> threadLocal = iterator.next();

        // backup
        backup.put(threadLocal, threadLocal.get());

        // clear the TTL values that is not in captured
        // avoid the extra TTL values after replay when run task
        if (!captured.containsKey(threadLocal)) {
          iterator.remove();
          threadLocal.superRemove();
        }
      }

      // set TTL values to captured
      setTtlValuesTo(captured);

      // call beforeExecute callback
      doExecuteCallback(true);

      return backup;
    }

    private static HashMap<ThreadLocal<Object>, Object> replayThreadLocalValues(
        HashMap<ThreadLocal<Object>, Object> captured) {
      final HashMap<ThreadLocal<Object>, Object> backup = new HashMap<ThreadLocal<Object>, Object>();

      for (Map.Entry<ThreadLocal<Object>, Object> entry : captured.entrySet()) {
        final ThreadLocal<Object> threadLocal = entry.getKey();
        backup.put(threadLocal, threadLocal.get());

        final Object value = entry.getValue();
        if (value == threadLocalClearMark) {
          threadLocal.remove();
        } else {
          threadLocal.set(value);
        }
      }

      return backup;
    }

    public static Object clear() {

      final HashMap<TransmittableThreadLocal<Object>, Object> ttl2Value = new HashMap<TransmittableThreadLocal<Object>, Object>();

      final HashMap<ThreadLocal<Object>, Object> threadLocal2Value = new HashMap<ThreadLocal<Object>, Object>();
      for (Map.Entry<ThreadLocal<Object>, TtlCopier<Object>> entry : threadLocalHolder.entrySet()) {
        final ThreadLocal<Object> threadLocal = entry.getKey();
        threadLocal2Value.put(threadLocal, threadLocalClearMark);
      }

      return replay(new Snapshot(ttl2Value, threadLocal2Value));
    }

    public static void restore(Object backup) {
      final Snapshot backupSnapshot = (Snapshot) backup;
      restoreTtlValues(backupSnapshot.ttl2Value);
      restoreThreadLocalValues(backupSnapshot.threadLocal2Value);
    }

    private static void restoreTtlValues(HashMap<TransmittableThreadLocal<Object>, Object> backup) {
      // call afterExecute callback
      doExecuteCallback(false);

      for (final Iterator<TransmittableThreadLocal<Object>> iterator = holder.get().keySet()
          .iterator(); iterator.hasNext(); ) {
        TransmittableThreadLocal<Object> threadLocal = iterator.next();

        // clear the TTL values that is not in backup
        // avoid the extra TTL values after restore
        if (!backup.containsKey(threadLocal)) {
          iterator.remove();
          threadLocal.superRemove();
        }
      }

      // restore TTL values
      setTtlValuesTo(backup);
    }

    private static void setTtlValuesTo(
        HashMap<TransmittableThreadLocal<Object>, Object> ttlValues) {
      for (Map.Entry<TransmittableThreadLocal<Object>, Object> entry : ttlValues.entrySet()) {
        TransmittableThreadLocal<Object> threadLocal = entry.getKey();
        threadLocal.set(entry.getValue());
      }
    }

    private static void restoreThreadLocalValues(HashMap<ThreadLocal<Object>, Object> backup) {
      for (Map.Entry<ThreadLocal<Object>, Object> entry : backup.entrySet()) {
        final ThreadLocal<Object> threadLocal = entry.getKey();
        threadLocal.set(entry.getValue());
      }
    }

    private static class Snapshot {

      final HashMap<TransmittableThreadLocal<Object>, Object> ttl2Value;
      final HashMap<ThreadLocal<Object>, Object> threadLocal2Value;

      private Snapshot(HashMap<TransmittableThreadLocal<Object>, Object> ttl2Value,
          HashMap<ThreadLocal<Object>, Object> threadLocal2Value) {
        this.ttl2Value = ttl2Value;
        this.threadLocal2Value = threadLocal2Value;
      }
    }

    public static <R> R runSupplierWithCaptured(Object captured, Supplier<R> bizLogic) {
      final Object backup = replay(captured);
      try {
        return bizLogic.get();
      } finally {
        restore(backup);
      }
    }

    public static <R> R runSupplierWithClear(Supplier<R> bizLogic) {
      final Object backup = clear();
      try {
        return bizLogic.get();
      } finally {
        restore(backup);
      }
    }

    public static <R> R runCallableWithCaptured(Object captured, Callable<R> bizLogic)
        throws Exception {
      final Object backup = replay(captured);
      try {
        return bizLogic.call();
      } finally {
        restore(backup);
      }
    }

    public static <R> R runCallableWithClear(Callable<R> bizLogic) throws Exception {
      final Object backup = clear();
      try {
        return bizLogic.call();
      } finally {
        restore(backup);
      }
    }

    private static volatile WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>> threadLocalHolder = new WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>>();
    private static final Object threadLocalHolderUpdateLock = new Object();
    private static final Object threadLocalClearMark = new Object();

    public static <T> boolean registerThreadLocal(ThreadLocal<T> threadLocal, TtlCopier<T> copier) {
      return registerThreadLocal(threadLocal, copier, false);
    }

    public static <T> boolean registerThreadLocalWithShadowCopier(ThreadLocal<T> threadLocal) {
      return registerThreadLocal(threadLocal, (TtlCopier<T>) shadowCopier, false);
    }


    public static <T> boolean registerThreadLocal(ThreadLocal<T> threadLocal, TtlCopier<T> copier,
        boolean force) {
      if (threadLocal instanceof TransmittableThreadLocal) {
        logger.warning("register a TransmittableThreadLocal instance, this is unnecessary!");
        return true;
      }

      synchronized (threadLocalHolderUpdateLock) {
        if (!force && threadLocalHolder.containsKey(threadLocal)) {
          return false;
        }

        WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>> newHolder = new WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>>(
            threadLocalHolder);
        newHolder.put((ThreadLocal<Object>) threadLocal, (TtlCopier<Object>) copier);
        threadLocalHolder = newHolder;
        return true;
      }
    }


    public static <T> boolean registerThreadLocalWithShadowCopier(ThreadLocal<T> threadLocal,
        boolean force) {
      return registerThreadLocal(threadLocal, (TtlCopier<T>) shadowCopier, force);
    }

    public static <T> boolean unregisterThreadLocal(ThreadLocal<T> threadLocal) {
      if (threadLocal instanceof TransmittableThreadLocal) {
        logger.warning("unregister a TransmittableThreadLocal instance, this is unnecessary!");
        return true;
      }

      synchronized (threadLocalHolderUpdateLock) {
        if (!threadLocalHolder.containsKey(threadLocal)) {
          return false;
        }

        WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>> newHolder = new WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>>(
            threadLocalHolder);
        newHolder.remove(threadLocal);
        threadLocalHolder = newHolder;
        return true;
      }
    }

    private static final TtlCopier<Object> shadowCopier = new TtlCopier<Object>() {
      @Override
      public Object copy(Object parentValue) {
        return parentValue;
      }
    };

    private Transmitter() {
      throw new InstantiationError("Must not instantiate this class");
    }
  }
}
