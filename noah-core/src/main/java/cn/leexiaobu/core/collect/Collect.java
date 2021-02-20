package cn.leexiaobu.core.collect;

/**
 * @author Leexiaobu
 * @date 2021-01-19 17:32
 */
public interface Collect {

  public byte[] transform(ClassLoader loader, String className);
}