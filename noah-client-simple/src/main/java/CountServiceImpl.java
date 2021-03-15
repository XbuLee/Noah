/**
 * @author Leexiaobu
 * @date 2021-03-15 11:31
 */
public class CountServiceImpl implements CountService {

  private int count = 0;

  public int count() {
    return count ++;
  }
}