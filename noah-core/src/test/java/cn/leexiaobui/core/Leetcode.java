
package cn.leexiaobui.core;

/**
 * @author Leexiaobu
 * @date 2021-02-09 17:35
 */
public class Leetcode {

  public static void main(String[] args) {
    String aacabdkacaa = new Leetcode().longestPalindrome("aaaa");
    System.out.println(aacabdkacaa);
  }

  public String longestPalindrome(String s) {
    int left = 0;
    int right = 0;
    int max = 0;
    if (s == null || s.length() == 0) {
      return null;
    }
    if (s.length() == 1) {
      return s;
    }
    //先查中间为一个
    for (int i = 1; i < s.length(); i++) {
      char c = s.charAt(i);
      for (int offset = 1; (i - offset) >= 0 && (i + offset) < s.length(); offset++) {
        if (s.charAt(i - offset) == s.charAt(i + offset)) {
          if (max < 2 * offset + 1) {
            max = 2 * offset + 1;
            left = i - offset;
            right = i + offset;
          }
        }else {
          break;
        }
      }
    }

    //查中间为2个的情况
    for (int i = 0; i < s.length() - 1; i++) {
      for (int offset = 0; (i - offset) >= 0 && (i + 1 + offset) < s.length(); offset++) {
        if (s.charAt(i - offset) == s.charAt(i + 1 + offset)) {
          if (max < 2 * offset + 2) {
            max = 2 * offset + 2;
            left = i - offset;
            right = i + 1 + offset;
          }
        }else {
          break;
        }
      }
    }

    return s.substring(left, right + 1);
  }
}