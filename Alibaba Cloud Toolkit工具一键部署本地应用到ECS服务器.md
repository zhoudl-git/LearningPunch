## Alibaba Cloud Toolkit工具一键部署本地应用到ECS服务器

![Alibaba Cloud Toolkit](https://img-blog.csdnimg.cn/20181209133458931.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1lDbGltYg==,size_16,color_FFFFFF,t_70)

### 什么是 Alibaba Cloud Toolkit

Alibaba Cloud Toolkit （后文简称 Cloud Toolkit）是阿里云针对 IDE 平台为开发者提供的一款插件，用于帮助开发者高效开发并部署适合在云端运行的应用。在本地完成应用程序的开发、调试和测试后，可以使用在 IED （如 Eclipse 或 IntelliJ）中安装的 Cloud Toolkit 插件，通过图形配置的方式连接到云端部署环境并将应用程序快如部署到云端。

> 说明：目前 Cloud Toolkit 仅支持 Eclipse、Intellij 等其它开发环境开发中，请您持续关注 Cloud Tookit 动态。

### 安装

官方有提供的文档说明:

```
https://help.aliyun.com/product/29966.html
```

### 使用IDEA安装和配置Cloud Toolkit

#### 在idea上安装Alibaba Cloud Toolkit插件

到 Idea 插件库中进行下载，如下图：

![](http://pkon92vqd.bkt.clouddn.com/1.png)

如果插件下载速度比较慢，稍等一会，因为是国外网站，下载完成后需要重启 idea 应用后生效。

#### 配置Alibaba Cloud Toolkit的Account信息

![](http://pkon92vqd.bkt.clouddn.com/2.png)

![](http://pkon92vqd.bkt.clouddn.com/3.png)

其中 Access Key 和 Access Key Secret 信息可以到阿里云控制台查询获得

![](http://pkon92vqd.bkt.clouddn.com/3.5.png)

创建完成并配置好Account后，在 ECS on Alibaba Cloud 视图中可以看到，会检索到你的实例

![](http://pkon92vqd.bkt.clouddn.com/4.png)

#### 开始部署项目到ECS服务器

鼠标选中你的项目，然后右键，找到 Alibaba Cloud 选项

![](http://pkon92vqd.bkt.clouddn.com/5.png)



然后填写其他信息，项目部署位置等，如果你的Account配置没有问题，则会自动账户显示对应的ECS服务器，在发布时，需要手动选择某台服务器，一定要选择哦！

对于Command的编写，可以参考官方文档

```
https://yq.aliyun.com/articles/665693
```



![](http://pkon92vqd.bkt.clouddn.com/6.png)



配置成功后，可以点击Run运行程序，此时会自动为我们编译并上传到阿里云服务器中，发布到地址就是上图中的Deploy Location中的路径，发布前如果需要Maven执行，一定不要忘记配置上图中Maven的命令，中间的Command是在上传到服务器成功后执行的命令，主要用于应用的启动停止重启等。

### 总结

好了，到此如果没有其他问题的话，你就会已经发布成功了，从此失去手动打包、上传再发布的繁琐过程了，这个插件针对个人开发用户来说是极其友好的，因为个人开发者一般不会像企业级开发一样部署 Jekins 等大型部署工具，而这个时候如果使用 Alibaba Cloud Toolkit ,在信息配置好的情况可以说基本就是一键部署！

再贴一下官网地址：

```
https://www.aliyun.com/product/cloudtoolkit
```

官网介绍的更加详细，也有官方钉钉群，大家感兴趣的可以加入进行深入交流。

![5685e931e06cd61faa41dee0ad46bf251fe56837](https://yqfile.alicdn.com/5685e931e06cd61faa41dee0ad46bf251fe56837.png)