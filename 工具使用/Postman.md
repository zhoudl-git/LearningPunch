Postman 是大家常用的 API 管理及测试工具，个人最近研究了一波，简单总结一下，他可能比你想象中的更强大。

首先 Postman 工具有 Chrome 扩展和独立客户端，推荐安装独立客户端。

Postman 有个 workspace 的概念，workspace 分 personal 和 team 类型。Personal workspace 只能自己查看的 API，Team workspace 可添加成员和设置成员权限，成员之间可共同管理 API。

![img](https:////upload-images.jianshu.io/upload_images/71414-e56b0ffce470a77a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

每个 workspace 可管理多个 collection，我们可以发布 collection，即生成在线 API 文档。

![img](https:////upload-images.jianshu.io/upload_images/71414-58f464ccf5925497.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

collection 及每个 collection 包含的 API 中的描述支持 markdown

每个 API 支持写测试用例，下图 snippet 提供了很多测试示例

![img](https:////upload-images.jianshu.io/upload_images/71414-424b0f2f52bfd009.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

Postman 提供了一个专门跑 API 测试的 GUI 工具，叫 Runner， 配好循环次数，测试之间的时间间隔，然后针对某 collection 中的目录或上传 collection 就可以进行测试了。

![img](https:////upload-images.jianshu.io/upload_images/71414-4639d62d7c26983f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

Postman 本身提供了一套 Postman API 可以操作 collection，environment 等，不过要先申请一个 api_key, 通过他可以以请求的方式操作自己写的 API。

GUI 工具需要我们手动点击触发跑测试，还无法做到完全自动化，好在 Postman 提供了 CLI 工具叫做 newman，是一个 NodeJS 项目。

下面的代码非常简单，配好要测试的 collection 和 environment，执行 `node index.js` 就能看到测试用例的结果。这里配置的是在命令行和 html 中显示报告。

![img](https:////upload-images.jianshu.io/upload_images/71414-4a9a1bdd2589006d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)


这里我找了漂亮的 Postman Report Html 模板

![img](https:////upload-images.jianshu.io/upload_images/71414-fe83daa1d32e00e0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)

这样基本可以实现了 API 测试自动化

#### 注意事项

使用 Postman 要注意有配额限制，尤其是 team workspace 和调用 API, 超出后需要掏钱升级, team 中的 member 越多，收费越高。

![img](https:////upload-images.jianshu.io/upload_images/71414-20f7b55dd5f2dbb7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/930/format/webp)不过免费的一般基本够用。

Postman的功能不止如此，还支持Fork, pull request，monitor监控等功能，大家可以查看官方文档。

官方文档https://www.getpostman.com/pricing

<https://www.jianshu.com/p/f79af87c6956>

https://www.npmjs.com/package/newman

https://github.com/MarcosEllys/awesome-newman-html-template