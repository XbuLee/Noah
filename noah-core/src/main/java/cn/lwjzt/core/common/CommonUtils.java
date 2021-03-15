package cn.lwjzt.core.common;

/**
 * @author Leexiaobu
 * @date 2021-01-27 14:34
 */
public class CommonUtils {

  public static String getZero() {
    return "0";
  }

  public static String getNextSpanId(String spanId) {
    int i = spanId.lastIndexOf(".");
    if (i == -1) {
      return Integer.parseInt(spanId) + 1 + "";
    }
    String substring = spanId.substring(i + 1);
    return spanId.substring(0, i + 1) + (Integer.parseInt(substring) + 1);
  }

  public static void main(String[] args) {
    System.out.println(getNextSpanId("0"));
    System.out.println(getNextSpanId("0.1"));
    System.out.println(getNextSpanId("1.2"));
  }
}