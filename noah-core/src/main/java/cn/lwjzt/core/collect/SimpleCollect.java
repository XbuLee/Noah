package cn.lwjzt.core.collect;

import cn.lwjzt.core.TraceNode;
import cn.lwjzt.core.common.CommonUtils;
import cn.lwjzt.core.common.SnowflakeIdWorker;
import cn.lwjzt.core.context.ApmContext;
import cn.lwjzt.core.model.DefaultStatistics;
import cn.lwjzt.core.threadpoolexecutor.TransmittableThreadLocal;
import java.lang.instrument.Instrumentation;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * @author Leexiaobu
 * @date 2021-01-19 18:02
 */
public class SimpleCollect extends AbstractByteTransformCollect implements Collect {

  //  public static InheritableThreadLocal<DefaultStatistics> simpleThreadLocal = new InheritableThreadLocal<>();
  public static TransmittableThreadLocal<TraceNode> traceNodeInheritableThreadLocal = new TransmittableThreadLocal<>();
  public static SimpleCollect INSTANCE;
  private static final String beginSrc;
  private static final String endSrc;
  private static final String errorSrc;

  static {
    StringBuilder builder = new StringBuilder();
    builder.append("cn.lwjzt.core.collect.SimpleCollect instance= ");
    builder.append("cn.lwjzt.core.collect.SimpleCollect.INSTANCE;\r\n");
    builder.append(
        "cn.lwjzt.core.model.DefaultStatistics bean =instance.begin(\"%s\",\"%s\");");
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
    String aPackage = this.context.getProperties().getProperty("package");
    if (!className.startsWith(aPackage)) {
      return null;
    }
    System.out.println("ApmContext：" + className);
    byte[] bytes = buildClass(loader, className);
    return bytes;
  }


  private byte[] buildClass(ClassLoader loader, String className) {
    ClassPool pool = ClassPool.getDefault();
//    pool.insertClassPath(new ClassClassPath(this.getClass()));
    pool.insertClassPath(new LoaderClassPath(loader));
    try {
      CtClass ctClass = pool.get(className);
      // 跳过接口
      if (ctClass.isInterface()) {
        return null;
      }
      CtMethod[] methods = ctClass.getDeclaredMethods();
      AgentByteBuild agentByteBuild = new AgentByteBuild(className, loader, ctClass);
      for (CtMethod m : methods) {
        // 屏蔽非公共方法
        if (!Modifier.isPublic(m.getModifiers())) {
          continue;
        }
        // 屏蔽静态方法
        if (Modifier.isStatic(m.getModifiers())) {
          continue;
        }
        // 屏蔽本地方法
        if (Modifier.isNative(m.getModifiers())) {
          continue;
        }
        System.out.println("method:" + m.getName());
        AgentByteBuild.MethodSrcBuild build = new AgentByteBuild.MethodSrcBuild();
        build.setBeginSrc(String.format(beginSrc, className, m.getName()));
        build.setEndSrc(endSrc);
        build.setErrorSrc(errorSrc);
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


  public DefaultStatistics begin(String className, String methodName) {
//    SimpleStatistics span = simpleThreadLocal.get();
    DefaultStatistics span = new DefaultStatistics();
    span.setBegin(System.currentTimeMillis());
    span.setServiceName(className);
    span.setMethodName(methodName);
    span.setSimpleName(className.substring(className.lastIndexOf(".") + 1));
    if (traceNodeInheritableThreadLocal.get() == null
        || traceNodeInheritableThreadLocal.get().getTraceId() == null) {
      String traceId = String.valueOf(SnowflakeIdWorker.getSnowflakeId());
      TraceNode traceNode = new TraceNode();
      traceNode.setTraceId(traceId);
      traceNodeInheritableThreadLocal.set(traceNode);
    }
    span.setTraceId(traceNodeInheritableThreadLocal.get().getTraceId());
    if (traceNodeInheritableThreadLocal.get().getSpanId() == null) {
      String spanId = CommonUtils.getFirstSpanId();
      String rpcId = CommonUtils.getFirstRpcId();
      traceNodeInheritableThreadLocal.get().setSpanId(spanId);
      traceNodeInheritableThreadLocal.get().setRpcId(rpcId);
    } else {
//      traceNodeInheritableThreadLocal.get().setSpanId(
//          CommonUtils.getNextSpanId(traceNodeInheritableThreadLocal.get().getSpanId()));
      CommonUtils.moveNext(traceNodeInheritableThreadLocal.get());
    }
    span.setSpanId(traceNodeInheritableThreadLocal.get().getSpanId());
    span.setModelType("begin");
//    Logger.logger.info("【start】" + span.toString());
    context.submitCollectResult(span);
    return span;
  }

  public void error(DefaultStatistics span, Throwable e) {
//    SimpleStatistics span = simpleThreadLocal.get();
    span.setErrorType(e.getClass().getSimpleName());
    span.setErrorMsg(e.getMessage());
    span.setModelType("error");
//    Logger.logger.error(span.toString());
    context.submitCollectResult(span);
  }

  public void end(DefaultStatistics span) {
//    SimpleStatistics span = simpleThreadLocal.get();
    span.setEnd(System.currentTimeMillis());
    span.setUseTime(span.end - span.begin);
    span.setModelType("end");
//    Logger.logger.info("【end】" + span.toString());
    context.submitCollectResult(span);
  }


}