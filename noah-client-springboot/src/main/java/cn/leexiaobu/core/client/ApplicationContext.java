package cn.leexiaobu.core.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Leexiaobu
 * @date 2021-01-21 17:19
 */
@SpringBootApplication
public class ApplicationContext {

  public static void main(String[] args) {
    SpringApplication.run(ApplicationContext.class, args);
  }
}