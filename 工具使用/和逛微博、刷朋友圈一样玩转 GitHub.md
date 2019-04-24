自打毕业之后，可以说每天打开 Github 或Email 看有没有 watch 项目的消息或者自己项目的 issue，然后在Explore 看看社区内项目的走势，紧接着开始写代码搬砖的工作，偶尔也会关注下 Github 的 Blog, 看看有没有大新闻，亦或扫一眼 Github 的 Tip，闲的蛋疼的时候也会一时兴起去搜索下有意思的项目，看到有趣的人就会去他的博客看看，晚上要睡觉之前会考虑是不是要写篇博客，然后 push上去显得自己的 Github 绿一点......久而久之，也学到了一些 Github 的使用技巧，下边毫无保留的分享给大家。

### 两分钟把Github项目变成前端网站

此处默认你有 Github 账号、安装了 Git 并且熟悉基本的 Git 操作，只是需要寻求部署 Github Pages 方面的知识。GitHub Pages 大家可能都知道，常用的做法，是建立一个 gh-pages 的分支，通过 Setting 里的设置的GitHub Pages 模块可以自动创建该项目的网站。

这里经常遇到的痛点是，master 遇到变更，经常需要去 sync 到 gh-pages，特别是纯 web 前端项目，这样的痛点是非常地痛。

Github官方可能嗅觉到了该痛点，出了个 master 当作网站是选项，太有用了。

下边具体来看下操作步骤：

#### 第一步 进入 Settings 设置 Github Page 模块

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1my9yqcf4j30ye0n3q4y.jpg)

#### 第二步 选择对应分支

选择完 master branch 之后，master 自动变成了网站。master 所有的提交会自动更新到网站。

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1myc8nzuuj30nu0i6taf.jpg)

#### 第三步 选择对应主题

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1mycmf2h4j30lt0beq3h.jpg)

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1mycpikc7j30ss0b9tca.jpg)

#### 第四步 等待部署成功

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1mydc1cm1j30mg0ibjsn.jpg)

如上图所示，则代表部署成功！此时便可以使用 username.github.io/仓库名称 进行访问了，我在此处的访问地址是：https://bylant.github.io/DWR-Spring/

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1mygzmkjhj30v10ion3l.jpg)

### 精准分享关键代码

比如你有一个文件里的某一行代码写得非常酷炫或者关键，想分享一下。可以在 URL 后面加上 **#L 行号**

比如，点击下面这个 URL 

```
https://github.com/Bylant/DWR-Spring/blob/master/src/main/java/top/zhoudl/dwr/MessagePusher.java#L47
```

此时便会直接跳转到这行代码的位置，高亮显示

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1myn3mx3ij30ut0fsabk.jpg)

有的朋友此处可能会想，分享一行代码有什么用，我的关键代码那可是以段来论的，看不起你这一行，不急，同样有办法，我们在 URL 后边加上 **L开始行号-L结束行号**

如以下 URL ：

```
https://github.com/Bylant/DWR-Spring/blob/master/src/main/java/top/zhoudl/dwr/MessagePusher.java#L47-L70
```



![](http://ww1.sinaimg.cn/large/007GTbzZgy1g1mypwprerj30ww0o6dig.jpg)

其实我们也不用去死记硬背这些东西，因为 Github  有方式帮我们自动生成这些链接

* 分享单行代码

  直接点击代码区的行号，URL 会随之跳转，大家可以观察下图的 URL 变化

  ![](http://ww1.sinaimg.cn/large/007GTbzZly1g1myz3jetig30wk0lgjyn.gif)

* 分享多行代码

  多行代码和单行代码分享的操作步骤基本一致，只不过区别在于在选择完代码块开始行之后，点击结束行的同时需要按住 Shift 键，大家观察以下动图 URL 的变化

  ![](http://ww1.sinaimg.cn/large/007GTbzZly1g1mz0k6mh5g30wk0lgaic.gif)

  

此时我们可以直接复制拿到的 URL 去别的地方分享了。

### 通过提交的 message 自动关闭 issues

比如有人提交了个issues <https://github.com/AlloyTeam/AlloyTouch/issues/6>
 然后你去主干上改代码，改完之后提交填msg的时候，填入：

```
fix  https://github.com/Bylant/DWR-Spring/issues/1
```

这个 issues 会自动被关闭。当然不仅仅是 fix 这个关键字，下面这些关键字也可以：

* close

* closes

* closed

* fixes

* fixed

* resolve

* resolves

* resolved

### gitattributes设置项目语言

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1mzdensrzj30dy046glo.jpg)

如上图所示，Github 会根据相关文件代码的数量来自动识别你这个项目是 HTML项目、avascript 项目或者 Java 项目。但是这就会带来了一个问题，比如 DWR-Spring 最开始被识别成 JavaScript 项目。因为 项目刚开始可能前端代码很多。

怎么办呢？gitattributes 可以来帮助你搞定这个问题。我们在项目的根目录下添加如下 .gitattributes 文件便可

文件内容如下：

```md
*.js linguist-language=Java
```

主要意思是把所有 js 文件后缀的代码识别成 Java 文件。

### 查看自己项目的访问数据

在自己的项目下，点击 Graphs，然后再点击 Traffic 如下所示：

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n1f1a76yj30t80o3wfn.jpg)

更多统计留给你自己去探索哦

### trending 排行榜

经常玩微博的人都知道，每天都有个热搜榜，知乎也有个知乎热榜，当然，Github 也有某类型语言的每日排行榜。比如 Java 每日排行榜：

```
https://github.com/trending/javascript?since=daily
```



![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n1jneltzj30yd0ostbb.jpg)

### Github 推荐

```
https://github.com/explore
```

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n1m48a66j310v0rrwiw.jpg)

### 快速搜索项目文件功能

有时候我们要快速检索项目中的某个文件，这个时候如果一个一个去找很浪费时间。所以 Github 也为我们提供了一个搜索技巧。

* 打开 Github 底下任意仓库，然后打开仓库里面的代码
* 接着在项目页面，按住键盘 **T 键**，会在项目名旁边出现可以可以搜索的地方
* 输入想要查找的关键词，页面会根据输入的关键词进行快速搜索

![](http://ww1.sinaimg.cn/large/007GTbzZgy1g1n2hsbxqqg30rw0ns15z.gif)

#### 其他搜索技巧

##### 基本规则

默认搜索是从master分支搜索代码，搜索语句不能有特殊字符如. , : ; / \ ` ’ ” = * ! ? # $ & + ^ | ~ < > ( ) { } [ ].

##### 指定搜索方式

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n3dodgjwj30mm0fzglq.jpg)

##### Awesome + 关键字

Awesome 似乎已经成为不少 GitHub 项目喜爱的命名之一，他可以找到优秀的工具列表。比如前面提及要找到优秀的 Windows 软件，可以尝试搜索 `Awesome windows`，得到这样的搜索结果：

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n2q5wpisj313k0mldhr.jpg)

排名前列的结果出现了  **Windows/Awesome 项目**，这里集合了 Windows 上优质和精选的最佳应用程序及工具列表。

**此处小提几点**：我习惯的用法是灵活运用下面几个搜索条件：`stars:`、`language:`、`forks:`，其实就是设置项目收藏、开发语言、派生的搜索条件，比如输入 `stars:>=1000 language:java`，得到的结果就是收藏大于和等于 500 的 Java 项目。如下结果出来的都是 ES、SpringBoot 等经典项目

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n3gygg1bj30k20jkt9t.jpg)

除了以上所述之外，GitHub 提供高级搜索功能访问地址如下：

```
https://github.com/search/advanced
```

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n3jidj6gj30si0nj3zj.jpg)

具体使用方法参见 https://help.github.com/en/articles/searching-on-github 此处不再赘述了。

### 其他技巧

##### issue 中输入冒号 : 添加表情

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n1rdcu9uj30ke0bwmxk.jpg)

表情对应的代码参见网站

```
https://www.webfx.com/tools/emoji-cheat-sheet/
```

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n1s6x9jgj317t0jzgpg.jpg)

可以看到我们经常用的所有表情都会在这个地方。

##### 任意界面，shift + ？显示快捷键

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n1uobfidj30w00lzq5c.jpg)

##### issue 中选中文字，R 键快速引用

还是以上边新建的 issue 为例，我们看到选中 **文档太少** 四个字之后，然后按住 R 键，就会自动引用 **文档太少** 这四个字

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n1xivrkag30lo0ecgnm.gif)

### 最后

好了，我就会这么多，也是我经常使用的技巧。可以说 Github 的资源对于广大开发者来说真是个宝藏，很多时候我都在思考一个问题，我们每天遇到各种各样的问题，然后我们需要不断 Google、百度，达到我们解决问题的目的。但是在这个过程中，我们总是能够见到，对于同一个问题，总是有大量错误、copy 的博客、文库乃至"吊炸天"的百度知道，一次又一次减慢了我们解决问题的进度。更加糟糕的是，这种 copy 是保量不保质的，所以我们总是希望，每当我们 Google 或者百度的时候，都能够最快解决我们需要解决的问题。然而，这貌似是不切实际的，在人人都有权利发表文章的今天，要想禁止这种无意义的抄袭几乎是痴人说梦。清晰意识到这一点之后，我觉得更加有效的方式是每一个发表博客或者活跃于问答网站的人都应该把自己的博客或者解答做到精炼和准确，在某种意义上来说，这是一种义务和责任。

![](http://ww1.sinaimg.cn/large/007GTbzZly1g1n3q6pcvbj30b50aigo7.jpg)

也欢迎补充实用的技巧~~我会持续更新上去…