package cn.leexiaobu.core;

import cn.leexiaobu.core.context.ApmContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

/**
 * @author Leexiaobu
 * @date 2021-01-19 17:09 性能消耗低 应用透明，也就是代码的侵入性小 可扩展性 数据的分析
 */
public class Agent {

  public static void premain(String args, Instrumentation instrumentation) {
    System.out.println("进入Agent");
    Properties properties = new Properties();
    // 装载配置文件
    if (args != null && !args.trim().equals("")) {
      try {
        properties.load(new ByteArrayInputStream(
            args.replaceAll(",", "\n").getBytes()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    ApmContext context = new ApmContext(instrumentation, properties);
  }

}