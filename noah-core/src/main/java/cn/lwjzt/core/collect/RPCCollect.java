package cn.lwjzt.core.collect;

import cn.lwjzt.core.TraceNode;
import cn.lwjzt.core.common.JsonUtil;
import java.lang.instrument.Instrumentation;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

/**
 * @author Leexiaobu
 * @date 2021-03-15 13:36
 */
public class RPCCollect extends AbstractByteTransformCollect implements Collect {

  private static final String ABSTRACTINVOKER = "org.apache.dubbo.rpc.protocol.AbstractInvoker";
  private static final String INVOKE = "invoke";
  private static final String CONTEXTFILTER = "org.apache.dubbo.rpc.filter.ContextFilter";
  private static final String GETOBJECTATTACHMENTS = "invoke";


  public RPCCollect(Instrumentation instrumentation) {
    super(instrumentation);
  }

  @Override
  public byte[] transform(ClassLoader loader, String className) {
    if (ABSTRACTINVOKER.equals(className)) {
      System.out.println("RPCCollect:"+className);
      try {
        return buildInvokeClass(loader);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (CONTEXTFILTER.equals(className)) {
      System.out.println("RPCCollect:"+className);
      try {
        return buildInvocationClass(loader);
//        return null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  public byte[] buildInvokeClass(ClassLoader loader) throws Exception {
    ClassPool pool = new ClassPool();
    pool.insertClassPath(new LoaderClassPath(loader));
    CtClass ctClass = pool.get(ABSTRACTINVOKER);
    CtMethod oldMethod = ctClass.getDeclaredMethod(INVOKE);
    oldMethod.insertBefore(
        " ;"
            + "org.apache.dubbo.rpc.RpcContext.getContext().getObjectAttachments().put(\"noah_trace_id\", "
            + " cn.lwjzt.core.common.CommonUtils.getNextRpcTraceNode(cn.lwjzt.core.collect.SimpleCollect.traceNodeInheritableThreadLocal.get()));");
    return ctClass.toBytecode();
  }

  public byte[] buildInvocationClass(ClassLoader loader) throws Exception {
    ClassPool pool = new ClassPool();
    pool.insertClassPath(new LoaderClassPath(loader));

    CtClass ctClass = pool.get(CONTEXTFILTER);
    CtMethod oldMethod = ctClass.getDeclaredMethod(GETOBJECTATTACHMENTS);
//    if(invocation.getObjectAttachments()!=null&&invocation.getObjectAttachments().get("noah_trace_id")!=null){
//      cn.lwjzt.core.collect.SimpleCollect.traceNodeInheritableThreadLocal.set((cn.lwjzt.core.TraceNode)invocation.getObjectAttachments().get("noah_trace_id"));
//    }


//    System.out.println(cn.lwjzt.core.common.JsonUtil.toJson(cn.lwjzt.core.collect.SimpleCollect.traceNodeInheritableThreadLocal.get()));
//    oldMethod.insertBefore("if(invocation.getObjectAttachments()!=null&&invocation.getObjectAttachments().get(\"noah_trace_id\")!=null){\n"
//        + "      cn.lwjzt.core.collect.SimpleCollect.traceNodeInheritableThreadLocal.set((cn.lwjzt.core.TraceNode)invocation.getObjectAttachments().get(\"noah_trace_id\")); \n"
//        + "    }");
        oldMethod.insertBefore("    if(invocation.getObjectAttachments()!=null&&invocation.getObjectAttachments().get(\"noah_trace_id\")!=null){\n"
            + "      cn.lwjzt.core.collect.SimpleCollect.traceNodeInheritableThreadLocal.set((cn.lwjzt.core.TraceNode)invocation.getObjectAttachments().get(\"noah_trace_id\"));\n"
            + "    }\n"
            + "       System.out.println(cn.lwjzt.core.common.JsonUtil.toJson(cn.lwjzt.core.collect.SimpleCollect.traceNodeInheritableThreadLocal.get()));\n");
    return ctClass.toBytecode();
  }
}