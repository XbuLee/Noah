**Noah**
==========


# 简介
**Noah** 是一个开源、无侵入的apm项目，目前支持微服务框架为dubbo。

主要采用的技术是javaagent + javassist，通过字节码修改，其中上下文传递参考了[transmittable-thread-local](https://github.com/alibaba/transmittable-thread-local/releases/tag/v2.12.1).


# 文档
下载最新jar包，在项目执行命令中添加-javaagent:noah.jar=package=(你需要监控的包)，目前日志存在本地文件，可以通过logstash+es+kibana查看。
# 下载
[releases page](https://github.com/Leexiaobu/Noah/releases) 

# 联系我们
* Mail list: **leexiaobu.lix@gmail.com**. 