package cn.leexiaobu.core.filter;

/**
 * @author Leexiaobu
 * @date 2021-01-19 18:18
 */
public class DefaultFilter implements Filter{

  @Override
  public Object doFilter(Object value) {
    System.out.println("filter ");
    return null;
  }
}