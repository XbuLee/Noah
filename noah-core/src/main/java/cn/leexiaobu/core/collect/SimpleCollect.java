package cn.leexiaobu.core.collect;

import cn.leexiaobu.core.context.ApmContext;
import java.lang.instrument.Instrumentation;
import java.util.UUID;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;

/**
 * @author Leexiaobu
 * @date 2021-01-19 18:02
 */
public class SimpleCollect extends AbstractByteTransformCollect implements Collect {

  public static SimpleCollect INSTANCE;

  private ApmContext context;

  public SimpleCollect(ApmContext context, Instrumentation instrumentation) {
    super(instrumentation);
    this.context = context;
    INSTANCE = this;
  }

  @Override
  public byte[] transform(ClassLoader loader, String className) throws Exception {
    //过滤筛选
    if (!className.startsWith("cn.leexiaobu")) {
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
      for (int i = 0; i < methods.length; i++) {
        CtMethod method = methods[i];
        CtMethod copy = CtNewMethod.copy(method, ctClass, null);
        String name = method.getName();
        if (method.getAnnotations() != null) {
          method.insertBefore("System.out.println(\n"
              + "        this.getClass().getName() + Thread.currentThread().getStackTrace()[1].getMethodName());");
          System.out.println("--annotation-" + name);
          continue;
        }
        method.setName(name + "$agent");
        try {
          if (Modifier.isNative(method.getModifiers())) {
            continue;
          }
        } catch (Exception e) {
          System.out.println(className + "." + name + "has an error modifiers");
          continue;
        }
        if (method.getReturnType().getName().equals("void")) {
          copy.setBody(name + "$agent($$);");
          copy.insertBefore("System.out.println(\n"
              + "        this.getClass().getName() + Thread.currentThread().getStackTrace()[1].getMethodName());");
          copy.insertAfter("System.out.println(\"after\");");
          System.out.println("--void-" + name);

        } else {
          copy.setBody("return ($r)($w)" + name + "$agent($$);");
          copy.insertBefore("System.out.println(\"before\");");
          System.out.println("--return-" + name);

        }
        ctClass.addMethod(copy);
      }
      return ctClass.toBytecode();
    } catch (Exception e) {
      e.printStackTrace();
    }

//    copy.insertBefore("   if(MDC.get(\"traceId\")==null){\n"
//        + "      MDC.put(\"traceId\", SnowflakeIdWorker.getSnowflakeId()+\"\");\n"
//        + "    }\n"
//        + "    System.out.println(MDC.get(\"traceId\"));");
//    copy.insertBefore(" System.out.println(MDC.get(\"traceId\"));");
    return null;
  }


  void begin() {
//    ThreadLocal<String> id = UUID.randomUUID().toString();
//    System.out.println(
//        this.getClass().getName() + Thread.currentThread().getStackTrace()[1].getMethodName());

  }

  void end()

  {

  }


}