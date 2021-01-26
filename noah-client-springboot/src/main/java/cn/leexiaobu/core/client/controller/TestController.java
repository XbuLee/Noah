package cn.leexiaobu.core.client.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Leexiaobu
 * @date 2021-01-21 17:25
 */
@RestController
public class TestController {

  @GetMapping("/hello")
  public void hello(String name) {
    ClassLoader classLoader = this.getClass().getClassLoader();
    System.out.println("hello "+name);
  }

  @GetMapping("/hello2")
  public String hello2(String name) {
    ClassLoader classLoader = this.getClass().getClassLoader();
    return ("hello2 "+name);
  }
}