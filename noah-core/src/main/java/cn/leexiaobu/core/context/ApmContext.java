package cn.leexiaobu.core.context;

import cn.leexiaobu.core.collect.AbstractByteTransformCollect;
import cn.leexiaobu.core.collect.Collect;
import cn.leexiaobu.core.collect.SimpleCollect;
import cn.leexiaobu.core.collect.ThreadExecutorCollect;
import cn.leexiaobu.core.filter.DefaultFilter;
import cn.leexiaobu.core.filter.Filter;
import cn.leexiaobu.core.output.OutPut;
import cn.leexiaobu.core.output.SimpleOutput;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;

/**
 * @author Leexiaobu
 * @date 2021-01-19 17:31
 */
public class ApmContext implements Context {

  private Instrumentation instrumentation;

  private Properties properties;

  List<Collect> collects = new ArrayList();

  Filter filter;

  OutPut output;

  public Instrumentation getInstrumentation() {
    return instrumentation;
  }

  public void setInstrumentation(Instrumentation instrumentation) {
    this.instrumentation = instrumentation;
  }

  public Properties getProperties() {
    return properties;
  }

  public ApmContext(Instrumentation instrumentation, Properties properties) {
    if (properties == null) {
      throw new RuntimeException("properties 不能为空");
    }
    this.properties = properties;
    this.instrumentation = instrumentation;
    // 注册采集器 IOC
    collects.add(new ThreadExecutorCollect(this, instrumentation));
    collects.add(new SimpleCollect(this, instrumentation));
    //filter 注册
//    filter = new DefaultFilter();
//    //输出器注册
//    output = new SimpleOutput(properties);
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public List<Collect> getCollects() {
    return collects;
  }

  public void setCollects(List<Collect> collects) {
    this.collects = collects;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public OutPut getOutput() {
    return output;
  }

  public void setOutput(OutPut output) {
    this.output = output;
  }


}