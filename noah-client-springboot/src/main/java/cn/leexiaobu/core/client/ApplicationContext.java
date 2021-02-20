package cn.leexiaobu.core.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * @author Leexiaobu
 * @date 2021-01-21 17:19
 */
@SpringBootApplication
public class ApplicationContext extends SpringBootServletInitializer {

  public static void main(String[] args) {
    System.out.println("Noah=====");
    SpringApplication.run(ApplicationContext.class, args);
  }
  @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApplicationContext.class);
    }

}

