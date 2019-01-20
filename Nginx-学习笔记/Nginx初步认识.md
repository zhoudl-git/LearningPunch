### Nginx

#### 什么是 Nginx

Nginx 是一个高性能的**反向代理服务器**

> 正向代理：代理的是客服端
>
> 反向代理：代理的是服务端

### 安装

* 下载 tar 包 

* 解压 ` tar -zxvf  nginx.tar.gz`

* `./configure[--prefix]`
* `make && make install`

### 运行

`./nginx`

### 停止

`./nginx -s stop`

### 配置

配置文件 `server.conf`主要有以下三部分内容

* Main

* event

* http

#### 虚拟主机配置

##### 基于IP的虚拟主机

```conf
server {
    listen 
}
```

##### 基于端口号的虚拟主机

```conf
server {
    listen		 	8080;
    server_name 	localhost;
    location / {
        root  html;
        index index.html;
    }
}
```

##### 基于域名的虚拟主机

```conf
server {
    listen		 	80;
    server_name 	www.zhoudl.top;
    location / {
        root  html;
        index index.html;
    }
}
```

```conf
server {
    listen		 	80;
    server_name 	img.zhoudl.top;
    location / {
        root  html;
        index index.html;
    }
}
```

#### location

##### 配置语法

`location[ = | ~*^~/url/{...}]`

##### 配置规则

`location = /url `精准匹配

`location ^~ /url` 前缀匹配

`location ~ /url` 正则匹配

`location /` 通用匹配

##### 规则的优先级

1. 精准匹配优先级最高

2. 普通匹配（长度最长的一个匹配）

3. 正则匹配

##### 实际使用建议

```
location = / {

}

```

### Nginx 模块

#### 模块分类

1. 核心模块 `ngx_http_core_module`
2. 标准模块 http 模块
3. 第三方模块

##### ngx_http_core_module

1. `server {}`

2. `error_page`

##### ngx_http_access_module

1. allow

2. deny

#### 如何添加第三方模块

1. 原来所安装的配置，必须在重新安装新模块的时候，手动加上

2. 不能直接 `make install`

##### 安装方法

##### http_stub_status_module

监控nginx状态

##### http_rendom_index_module

随机首页

