package cn.leexiaobu.core.collect;

import cn.leexiaobu.core.context.ApmContext;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * @author Leexiaobu
 * @date 2021-02-04 12:25
 */
public class ThreadExecutorCollect extends AbstractByteTransformCollect implements Collect {

  private ApmContext context;

  public ThreadExecutorCollect(ApmContext context, Instrumentation instrumentation) {
    super(instrumentation);
    this.context = context;
  }

  private static final Set<String> EXECUTOR_CLASS_NAMES = new HashSet<String>();
  private static final Map<String, String> PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS = new HashMap<String, String>();

  final static String THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ThreadPoolExecutor";
  final static String TOMCAT_THREAD_POOL_EXECUTOR_CLASS_NAME = "org.apache.tomcat.util.threads.ThreadPoolExecutor";
  final static String SCHEDULED_THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ScheduledThreadPoolExecutor";

  private static final String RUNNABLE_CLASS_NAME = "java.lang.Runnable";
  private static final String CALLABLE_CLASS_NAME = "java.util.concurrent.Callable";


  static {
    EXECUTOR_CLASS_NAMES.add(THREAD_POOL_EXECUTOR_CLASS_NAME);
    EXECUTOR_CLASS_NAMES.add(SCHEDULED_THREAD_POOL_EXECUTOR_CLASS_NAME);
    EXECUTOR_CLASS_NAMES.add(TOMCAT_THREAD_POOL_EXECUTOR_CLASS_NAME);
    PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS
        .put(RUNNABLE_CLASS_NAME, "cn.leexiaobu.core.threadpoolexecutor.NoahRunnable");
    PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS
        .put(CALLABLE_CLASS_NAME, "cn.leexiaobu.core.threadpoolexecutor.NoahCallable");
  }

  @Override
  public byte[] transform(ClassLoader loader, String className) {
    if (!EXECUTOR_CLASS_NAMES.contains(className)) {
      return null;
    }
    byte[] bytes = buildClass(loader, className);
    return bytes;
  }

  private byte[] buildClass(ClassLoader loader, String className) {
    ClassPool pool = ClassPool.getDefault();
    try {
      CtClass ctClass = pool.get(className);

      for (CtMethod method : ctClass.getDeclaredMethods()) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) {
          continue;
        }
        CtClass[] parameterTypes = method.getParameterTypes();
        StringBuilder insertCode = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
          final String paramTypeName = parameterTypes[i].getName();
          if (PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS.containsKey(paramTypeName)) {
            String code = String.format(
                "    $%d = %s.get($%1$d);",
                i + 1, PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS.get(paramTypeName));
            insertCode.append(code);
          }
        }
        if (insertCode.length() > 0) {
          System.out.println("==="+className+method+"===");
          method.insertBefore(insertCode.toString());
        }
      }
      return ctClass.toBytecode();
    } catch (NotFoundException nfe) {
      System.out.println(loader + className + " class not found");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}