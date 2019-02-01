![Everything是国民最爱软件](https://upload-images.jianshu.io/upload_images/2346551-cf440b8ac3c91878.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

------

摘要：Everything几乎是每个职场人必备的效率工具，但同事们都只用它的一两个基本功能，并没有发挥出该软件的真正效率。实际上，把Everything的功能用到极致能够成倍的提升我们的工作效率，本文尝试详述那些藏在角落里的功能，帮您把它的潜力发挥到极致。

------

## 0. Everything的前世今生

Everything是澳大利亚人David Carpenter开发的免费文件检索工具，自从问世以来，因其占用内存小，搜索迅捷，获得了全世界windows用户的追捧，是职场同仁们必备的利器。拿我自己来说吧，我的电脑里有60万个文件夹和文件，如果没有everything，我不知道找到需要的文件要多么痛苦。

下面我们就按使用场景说说Everything的用法。

## 1. Everything的使用场景

### 1.1 搜索包含某个关键词的文件名，怎么办？

这个是同学们最常用的功能了，即在搜索框输入你要查询的关键字，例如，我想查询包含coffee到文件名，就直接在搜索框里输入coffee，就可以了，效果如下。

![搜索包含coffee的文件名](https://upload-images.jianshu.io/upload_images/2346551-9a40981c21bca250.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/848/format/webp)

### 1.2 要搜索的文件名中同时包含多个关键词怎么办？

如果想搜索的文件名中同时包含多个关键词，可以在搜索框中顺序敲入那几个关键词，中间用空格分开，例如包含hpe和win的文件，可以输入hpe然后跟着一个空格，然后输入win，结果如下图所示：

![搜索包含两个关键词的文件名](https://upload-images.jianshu.io/upload_images/2346551-2c11dab4b349e415.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/844/format/webp)

但这种方法查出来的结果文件，名字中的关键词出现的顺序不一定是你输入的关键词的顺序。如果你想查询的结果必须按照你输入的顺序，可以使用*关键词*关键词*的方式。例如，*hpe*win*，你看，下面出现的结果，都是hpe在前，win在后的文件名。

![](https://upload-images.jianshu.io/upload_images/2346551-e0ba4b2e70cbe26d.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/845/format/webp)

另一种输入多关键词的方法，可以确保结果中关键词的顺序

### 1.3 想要搜索的文件名中包含多个关键词中的一个就可以，怎么办？

如果我想查询的文件名中，包含关键词1，**或**，包含关键词二，可以在两个关键词中间加竖线（注意竖线前后都有空格），例如coffee | orange

![](https://upload-images.jianshu.io/upload_images/2346551-95f7f5b6385d0833.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/860/format/webp)

两个关键词满足一个就可以，使用“或”的关系

### 1.4 只搜索目录名，不想要文件名怎么办？

如果只想显示符合条件的目录，鼠标点选右上角的下拉框，把显示范围从Everything（全部），改为folder（文件夹）

![只显示符合条件的文件夹](https://upload-images.jianshu.io/upload_images/2346551-f7bade6adac99e16.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/849/format/webp)

### 1.5 只搜索某目录下的文件怎么办？

如果只想搜索某目录下的文件，则可以用目录加反斜杠，加空格，加关键词的方式进行搜索，例如：downloads\ 华为

![只搜索某目录下的满足条件的文件](https://upload-images.jianshu.io/upload_images/2346551-cf50517c83645f63.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/844/format/webp)

### 1.6 只搜索某种类型的文件怎么办？

如果想搜索某种特定类型的文件，可以使用*.加文件类型后缀的方式，例如downloads\ *.pdf，就只显示该目录下的pdf文件，其他文件就不显示了。

![搜索某种特定类型的文件](https://upload-images.jianshu.io/upload_images/2346551-11568ee05e6f2d7c.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/846/format/webp)

搜索某种特定类型的文件，使用*.后缀就可以了

### 1.7 如何使用通配符？

刚才我们已经举了两种使用通配符*的例子，简单讲*代表任意字符，问号(?)是代表一个字符（任意字符），例如：downloads\ *.p??，会显示downloads目录下的pdf文件，ppt文件等等，但不会显示pptx文件

![通配符?能够匹配任意一个字符](https://upload-images.jianshu.io/upload_images/2346551-d14ce197df94dd58.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/841/format/webp)

通配符?能够匹配任意一个字符

### 1.8 搜索不包含某关键词的文件名怎么办？

不包含由!代表，例如，我们想搜索downloads目录下不是pdf文件的其他文件，可以这样写：downloads\ !*.pdf !*.docx

![叹号代表否定](https://upload-images.jianshu.io/upload_images/2346551-1aed361c72869189.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/843/format/webp)

叹号代表否定

### 1.9 搜索的关键词中包含空格怎么办？

上面你可能已经看出来了，查询关键字中的空格，代表“且”的意思，即两个关键词中间如果有空格的话，代表同时包含两个关键词。但如果我想查询的关键词本身包含空格怎么办呢？ 可以用双引号把它们括起来，这样everything就会把它看待成一个词了。例如，我想查询downloads目录下包含university of bath的文件，可以写： downloads\ "university of"

![](https://upload-images.jianshu.io/upload_images/2346551-d08a02722dc1166e.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/848/format/webp)

如果查询的关键字本身包含空格，应该用双引号把它引起来

## 2. Everything的web服务器

好了，everything的功能介绍完了，希望大家都能把它的功能用到极致，也不枉作者好心开发这么好的工具，免费放出来给大家用，也不枉那么那么多的小伙伴在网路上推荐它呀

最后，Everything还有一个功能提一下，就是可以从手机或平板上通过浏览器访问它，这样的话，您就可以从手机上搜索自己电脑上的电影看了。打开everything的网站功能：从菜单Tools（工具）-Options（选项）的对话框，选择Http Server（Http服务器），然后勾选第一个勾选框，下面第三行有一个端口号要记住，假设是10000，那么你在手机浏览器的地址栏输入http://电脑IP：10000就可以访问了。



![](https://upload-images.jianshu.io/upload_images/2346551-be12b5e8eeab5f9d.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/885/format/webp)

打开Everything的web服务器可以从手机上访问您的文件哦