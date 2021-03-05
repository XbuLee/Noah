package cn.leexiaobu.core.model;


import cn.leexiaobu.core.common.CommonUtils;
import java.net.InetAddress;

public class BaseStatistics implements java.io.Serializable {

  private String traceId;
  private String spanId;
  private String modelType;
  private long recordTime;
  private String hostIp;
  private String hostName;

  public String getSpanId() {
    return spanId;
  }

  public void setSpanId(String spanId) {
    this.spanId = spanId;
  }

  public long getRecordTime() {
    return recordTime;
  }

  public void setRecordTime(long recordTime) {
    this.recordTime = recordTime;
  }

  public String getModelType() {
    return modelType;
  }

  public void setModelType(String modelType) {
    this.modelType = modelType;
  }

  public String getHostIp() {
    return hostIp;
  }

  public void setHostIp(String hostIp) {
    this.hostIp = hostIp;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

//  @Override
//  public String toString() {
//    return "BaseStatistics{" +
//        "traceId='" + traceId + '\'' +
//        ", spanId='" + spanId + '\'' +
//        ", modelType='" + modelType + '\'' +
//        ", recordTime=" + recordTime +
//        ", hostIp='" + hostIp + '\'' +
//        ", hostName='" + hostName + '\'' +
//        '}';
//  }

  public BaseStatistics() {
    this.setRecordTime(System.currentTimeMillis());
    try {
      this.setHostIp(InetAddress.getLocalHost().getHostAddress());
      this.setHostName(InetAddress.getLocalHost().getHostName());
    } catch (Exception e) {
        e.printStackTrace();
    }
  }
}
