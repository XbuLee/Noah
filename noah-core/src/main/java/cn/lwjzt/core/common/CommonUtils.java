package cn.lwjzt.core.common;

import cn.lwjzt.core.TraceNode;
import cn.lwjzt.core.collect.SimpleCollect;

/**
 * @author Leexiaobu
 * @date 2021-01-27 14:34
 */
public class CommonUtils {

  public static String getFirstSpanId() {
    return "0.0";
  }
  public static String getFirstRpcId() {
    return "0";
  }
  public static String getFirstParentId() {
    return "-1";
  }
  public static String getNextSpanId(String spanId) {
    int i = spanId.lastIndexOf(".");
    if (i == -1) {
      return Integer.parseInt(spanId) + 1 + "";
    }
    String substring = spanId.substring(i + 1);
    return spanId.substring(0, i + 1) + (Integer.parseInt(substring) + 1);
  }


  public static TraceNode moveNext(TraceNode traceNode) {
    String spanId = traceNode.getSpanId();
    int i = spanId.lastIndexOf(".");
    String substring = spanId.substring(0, i);
    String nextSpanId = getNextSpanId(substring) + ".0";
    traceNode.setSpanId(nextSpanId);
    traceNode.setParentId(spanId);
    traceNode.setRpcId("0");
    return traceNode;
  }

  public static TraceNode getNextRpcTraceNode(TraceNode traceNode) {
    String rpcId = Integer.parseInt(traceNode.getRpcId()) + 1 + "";
    String spanId = traceNode.getSpanId() + "." + rpcId + ".0.0";
    traceNode.setRpcId(rpcId);
    TraceNode newNode = new TraceNode();
    newNode.setTraceId(traceNode.getTraceId());
    newNode.setSpanId(spanId);
    newNode.setParentId(traceNode.getSpanId());
    newNode.setRpcId("0");
    JsonUtil.toJson(newNode);
    return newNode;
  }

  public static void main(String[] args) {
    TraceNode traceNode = new TraceNode();
    traceNode.setTraceId("123");
    traceNode.setSpanId("0.0");
    traceNode.setRpcId("0");
    for (int i = 0; i < 3; i++) {
      traceNode=  testSpId(traceNode);
    }
  }

  static TraceNode testSpId(TraceNode traceNode) {
    System.out.println(JsonUtil.toJson(moveNext(traceNode)));
    System.out.println(JsonUtil.toJson(moveNext(traceNode)));
    System.out.println(JsonUtil.toJson(moveNext(traceNode)));
    System.out.println(JsonUtil.toJson(moveNext(traceNode)));
    System.out.println(JsonUtil.toJson(getNextRpcTraceNode(traceNode)));
    System.out.println(JsonUtil.toJson(getNextRpcTraceNode(traceNode)));
    TraceNode nextRpcTraceNode = getNextRpcTraceNode(traceNode);
    System.out.println("=====");
    System.out.println(JsonUtil.toJson(nextRpcTraceNode));
    TraceNode result = moveNext(nextRpcTraceNode);
    System.out.println(JsonUtil.toJson(result));
    System.out.println("=====");
    TraceNode nextRpcTraceNode1 = CommonUtils
        .getNextRpcTraceNode(SimpleCollect.traceNodeInheritableThreadLocal.get());
    return result;
  }
}