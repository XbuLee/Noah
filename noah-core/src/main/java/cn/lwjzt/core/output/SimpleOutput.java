package cn.lwjzt.core.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Leexiaobu
 * @date 2021-01-19 19:04
 */
public class SimpleOutput implements OutPut {
  private FileWriter fileWriter;


  public SimpleOutput(Properties properties) {
    try {
      fileWriter =
          new FileWriter(openFile(properties.getProperty("log")), true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  @Override
  public boolean out(Object value) {
    try {
      System.out.println(value.toString());
      fileWriter.write( value.toString() +"\r\n");
      fileWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }


  private File openFile(String rootDir) {
    try {
      if (rootDir == null || rootDir.trim().equals("")) {
        rootDir = System.getProperty("user.dir") + "/logs/";
      }
      File root = new File(rootDir);
      if (!root.exists() || !root.isDirectory()) {
        root.mkdirs();
      }
      File file = new File(root, "apm-agent.log");
      if (file.exists()) {
        file.createNewFile();
      }
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}