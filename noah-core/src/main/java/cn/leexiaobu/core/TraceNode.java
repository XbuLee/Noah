package cn.leexiaobu.core;

/**
 * @author Leexiaobu
 * @date 2021-02-18 15:52
 */
public class TraceNode {

  String traceIdThreadLocal;
  String spanIdThreadLocal;

  public String getTraceIdThreadLocal() {
    return traceIdThreadLocal;
  }

  public void setTraceIdThreadLocal(String traceIdThreadLocal) {
    this.traceIdThreadLocal = traceIdThreadLocal;
  }

  public String getSpanIdThreadLocal() {
    return spanIdThreadLocal;
  }

  public void setSpanIdThreadLocal(String spanIdThreadLocal) {
    this.spanIdThreadLocal = spanIdThreadLocal;
  }
}