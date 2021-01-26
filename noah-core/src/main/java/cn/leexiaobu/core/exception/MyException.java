package cn.leexiaobu.core.exception;

/**
 * @author Leexiaobu
 * @date 2021-01-19 17:27
 */
public class MyException extends NestedRuntimeException {

  public MyException(String msg) {
    super(msg);
  }

  public MyException(String msg, Throwable cause) {
    super(msg, cause);
  }
}