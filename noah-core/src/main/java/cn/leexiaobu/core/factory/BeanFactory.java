package cn.leexiaobu.core.factory;

import cn.leexiaobu.core.exception.MyException;

/**
 * @author Leexiaobu
 * @date 2021-01-19 17:19
 */
public interface BeanFactory {


  String FACTORY_BEAN_PREFIX = "&";

  Object getBean(String name) throws MyException;


  <T> T getBean(String name, Class<T> requiredType) throws MyException;


}
