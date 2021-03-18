package cn.lwjzt.core;

import java.io.Serializable;

/**
 * @author Leexiaobu
 * @date 2021-02-18 15:52
 */
public class TraceNode implements Serializable {

  private static final long serialVersionUID = 1L;
  String traceId;
  String spanId;
  String rpcId;
  String parentId;

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getRpcId() {
    return rpcId;
  }

  public void setRpcId(String rpcId) {
    this.rpcId = rpcId;
  }


  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  public String getSpanId() {
    return spanId;
  }

  public void setSpanId(String spanId) {
    this.spanId = spanId;
  }
}