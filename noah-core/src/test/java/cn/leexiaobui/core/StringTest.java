package cn.leexiaobui.core;

/**
 * @author Leexiaobu
 * @date 2021-02-04 11:29
 */
public class StringTest {

  public static void main(String[] args) {
    String format = String.format(
        "$%d = com.alibaba.ttl.threadpool.TtlExecutors.getDisableInheritableThreadFactory($%<d);",
        1);
    System.out.println(format);
  }
}