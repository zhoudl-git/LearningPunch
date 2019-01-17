## Nginx 应用实战

### 反向代理

Nginx 反向代理 不需要新增额外的模块，默认自带 `proxy_pass` 指令，只需要修改配置文件就可以实现**反向代理**

#### 反向代理实战

打开 `conf/nginx.conf` 文件 做如下配置：

```conf
server {
    listen 80;
    server_name localhost;
    location / {
            proxy_pass http://127.0.0.1:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            root   html;
            index  index.html index.htm;
        }
}
```

> `proxy_pass` 既可以是 IP 地址，也可以是域名，同时还可以是端口

### 负载均衡

网络负载均衡的大致原理是利用一定的分配策略将网络负载平衡地分摊到网络集群的各个操作单元上，使得单个重
负载任务能够分担到多个单元上并行处理，使得大量并发访问或数据流量分担到多个单元上分别处理，从而减少用
户的等待响应时间

#### 负载均衡实战

#### 其他配置信息

### Nginx 动静分离

#### 什么是动静分离

#### 静态资源的类型

#### 动静分离实战

#### 动静分离的好处

##### 缓存

##### Nginx缓存配置

##### 压缩

###### 配置信息

##### 实战

### 防盗链

#### 防盗链配置

### 跨域访问

解决办法

