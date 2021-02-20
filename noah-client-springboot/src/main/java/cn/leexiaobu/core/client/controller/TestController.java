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
    System.out.println("hello " + name);
  }

  @GetMapping("/hello2")
  public String hello2(String name) {
    ClassLoader classLoader = this.getClass().getClassLoader();
    return ("hello2 " + name);
  }

  @GetMapping("/traceId")
  public void testTraceId() {
    method1();
  }
  private void method1() {
    method2();
  }

  private void method2() {
    method3();
  }

  private void method3() {
    method4();
  }

  private void method4() {
    method5();
  }

  private void method5() {
    method6();
  }

  private void method6() {
    System.out.println("end");
  }

}