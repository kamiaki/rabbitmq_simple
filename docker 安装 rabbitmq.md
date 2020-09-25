# docker 安装 rabbitmq

## 安装

```shell
#安装带有界面的
docker pull rabbitmq:3.7.7-management

```



## 启动

```shell
docker run -d --name rabbitmq3.7.7 -p 5672:5672 -p 15672:15672 -v /docker/rabbitmq/data:/var/lib/rabbitmq --hostname myRabbit -e RABBITMQ_DEFAULT_VHOST=my_vhost -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin rabbitmq:3.7.7-management 

说明：
-d 后台运行容器；
--name 指定容器名；
-p 指定服务运行的端口（5672：应用访问端口；15672：控制台Web端口号）；
-v 映射目录或文件；
--hostname  就是docker配置的主机名 变成rabbitmq节点名
-e 指定环境变量：
RABBITMQ_DEFAULT_VHOST：默认虚拟机名；  这个东西springboot别忘了配置
RABBITMQ_DEFAULT_USER：默认的用户名；
RABBITMQ_DEFAULT_PASS：默认用户名的密码
```



## 管理页面

```shell
http://Server-IP:15672
```

