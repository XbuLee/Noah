package cn.lwjzt.core.context;

import cn.lwjzt.core.collect.Collect;
import cn.lwjzt.core.collect.RPCCollect;
import cn.lwjzt.core.collect.SimpleCollect;
import cn.lwjzt.core.collect.ThreadExecutorCollect;
import cn.lwjzt.core.common.JsonUtil;
import cn.lwjzt.core.filter.DefaultFilter;
import cn.lwjzt.core.filter.Filter;
import cn.lwjzt.core.output.OutPut;
import cn.lwjzt.core.output.SimpleOutput;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    collects.add(new RPCCollect(instrumentation));
    collects.add(new SimpleCollect(this, instrumentation));
    //filter 注册
    filter = new DefaultFilter();
//    //输出器注册
    output = new SimpleOutput(properties);
  }

  public void submitCollectResult(Object value) {
    // TODO 基于线程后台执行
//    value = filter.doFilter(value);
    output.out(JsonUtil.toJson(value));
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