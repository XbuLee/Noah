# Noah
Noah是一个无侵入、java的分布式调用链项目，目前支持dubbo，的数据是存在本地，通过filebeat同步到es中,在通过Kibana展示。
主要采用的是javaagent + javassist技术，通过字节码修改，
