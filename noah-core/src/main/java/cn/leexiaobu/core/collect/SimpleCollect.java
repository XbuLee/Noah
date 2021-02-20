package cn.leexiaobu.core.collect;

import cn.leexiaobu.core.TraceNode;
import cn.leexiaobu.core.common.CommonUtils;
import cn.leexiaobu.core.common.SnowflakeIdWorker;
import cn.leexiaobu.core.context.ApmContext;
import cn.leexiaobu.core.logger.Logger;
import cn.leexiaobu.core.model.SimpleStatistics;
import cn.leexiaobu.core.threadpoolexecutor.TransmittableThreadLocal;
import java.lang.instrument.Instrumentation;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * @author Leexiaobu
 * @date 2021-01-19 18:02
 */
public class SimpleCollect extends AbstractByteTransformCollect implements Collect {

  //  public static InheritableThreadLocal<SimpleStatistics> simpleThreadLocal = new InheritableThreadLocal<>();
  public static TransmittableThreadLocal<TraceNode> traceNodeInheritableThreadLocal = new TransmittableThreadLocal<>();
  public static SimpleCollect INSTANCE;
  private static final String beginSrc;
  private static final String endSrc;
  private static final String errorSrc;

  static {
    StringBuilder builder = new StringBuilder();
    builder.append("cn.leexiaobu.core.collect.SimpleCollect instance= ");
    builder.append("cn.leexiaobu.core.collect.SimpleCollect.INSTANCE;\r\n");
    builder.append(
        "cn.leexiaobu.core.model.SimpleStatistics bean =instance.begin(\"%s\",\"%s\");");
    beginSrc = builder.toString();
    builder = new StringBuilder();
    builder.append("instance.end(bean);");
    endSrc = builder.toString();
    builder = new StringBuilder();
    builder.append("instance.error(bean,e);");
    errorSrc = builder.toString();

  }

  private ApmContext context;

  public SimpleCollect(ApmContext context, Instrumentation instrumentation) {
    super(instrumentation);
    this.context = context;
    INSTANCE = this;
  }

  @Override
  public byte[] transform(ClassLoader loader, String className) {
    //过滤筛选
    if (!className.startsWith("cn.leexiaobu.core.client")) {
      return null;
    }
    byte[] bytes = buildClass(loader, className);
    return bytes;
  }


  private byte[] buildClass(ClassLoader loader, String className) {
    ClassPool pool = ClassPool.getDefault();
//    pool.insertClassPath(new LoaderClassPath(loader));
    try {
      CtClass ctClass = pool.get(className);
      CtMethod[] methods = ctClass.getDeclaredMethods();
      AgentByteBuild agentByteBuild = new AgentByteBuild(className, loader, ctClass);
      for (CtMethod m : methods) {
        // 屏蔽非公共方法
//        if (!Modifier.isPublic(m.getModifiers())) {
//          continue;
//        }
        // 屏蔽静态方法
        if (Modifier.isStatic(m.getModifiers())) {
          continue;
        }
        // 屏蔽本地方法
        if (Modifier.isNative(m.getModifiers())) {
          continue;
        }
        AgentByteBuild.MethodSrcBuild build = new AgentByteBuild.MethodSrcBuild();
        build.setBeginSrc(String.format(beginSrc, className, m.getName()));
        build.setEndSrc(endSrc);
        build.setErrorSrc(errorSrc);
        System.out.println(ctClass.getSimpleName() + "  " + m.getName());
        agentByteBuild.updateMethod(m, build);
      }
      return ctClass.toBytecode();
    } catch (NotFoundException nfe) {
      System.out.println(loader + className + " class not found");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  public SimpleStatistics begin(String className, String methodName) {
//    SimpleStatistics bean = simpleThreadLocal.get();
    SimpleStatistics bean = new SimpleStatistics();
    bean.setBegin(System.currentTimeMillis());
    bean.setServiceName(className);
    bean.setMethodName(methodName);
    bean.setSimpleName(className.substring(className.lastIndexOf(".")));
    bean.setServiceName("simple");
    if (traceNodeInheritableThreadLocal.get() == null
        || traceNodeInheritableThreadLocal.get().getTraceIdThreadLocal() == null) {
      String traceId = String.valueOf(SnowflakeIdWorker.getSnowflakeId());
      TraceNode traceNode = new TraceNode();
      traceNode.setTraceIdThreadLocal(traceId);
      traceNodeInheritableThreadLocal.set(traceNode);
    }
    bean.setTraceId(traceNodeInheritableThreadLocal.get().getTraceIdThreadLocal());
    if (traceNodeInheritableThreadLocal.get().getSpanIdThreadLocal() == null) {
      String spanId = CommonUtils.getZero();
      traceNodeInheritableThreadLocal.get().setSpanIdThreadLocal(spanId);
    } else {
      traceNodeInheritableThreadLocal.get().setSpanIdThreadLocal(
          CommonUtils.getNextSpanId(traceNodeInheritableThreadLocal.get().getSpanIdThreadLocal()));
    }
    bean.setSpanId(traceNodeInheritableThreadLocal.get().getSpanIdThreadLocal());
    Logger.logger.info("【start】" + bean.toString());
    return bean;
  }

  public void error(SimpleStatistics bean, Throwable e) {
//    SimpleStatistics bean = simpleThreadLocal.get();
    bean.setErrorType(e.getClass().getSimpleName());
    bean.setErrorMsg(e.getMessage());
    Logger.logger.error(bean.toString());
  }

  public void end(SimpleStatistics bean) {
//    SimpleStatistics bean = simpleThreadLocal.get();
    bean.setEnd(System.currentTimeMillis());
    bean.setUseTime(bean.end - bean.begin);
    Logger.logger.info("【end】" + bean.toString());
  }


}