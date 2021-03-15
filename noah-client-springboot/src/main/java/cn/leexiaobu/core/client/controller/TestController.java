package cn.leexiaobu.core.client.controller;

import com.alibaba.fastjson.JSON;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Leexiaobu
 * @date 2021-01-21 17:25
 */
@RestController
public class TestController  {

  @GetMapping("/hello")
  public void hello(String name) {
    ClassLoader classLoader = this.getClass().getClassLoader();
    System.out.println("hello " + name);
  }

  @GetMapping("/hello2")
  public String hello2(String name) {
    ClassLoader classLoader = this.getClass().getClassLoader();
    return JSON.toJSONString(classLoader);
  }

  @GetMapping("/hello3")
  public int testTraceId(int a) {
    System.out.println("error");
    return 100 / a;
  }


  @GetMapping("/hello4")
  public String testThreadPool(int time) {
    System.out.println("123");
    ThreadPoolExecutor poolExecutor = new
        ThreadPoolExecutor(3, 5, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    for (int i = 0; i < 10; i++) {
      poolExecutor.execute(() -> testSleep(time));
    }
    return "over";
  }


  public void testSleep(int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}