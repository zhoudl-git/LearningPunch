> -- 2019-08-25

### 什么是 Git ?

Git 的来历

开源协议

cat 点评开源监控框架

### Git 和 SVN 的区别

#### CVCSs Centralized (集中式)

联网才能操作

server 硬盘坏了怎么办？

#### DVCSs 

保证完整性

### 安装 SSH

http://windows.github.com

http://git-scm.com/download/linux

https://git-scm.com/book/en/v2/Getting-Started-Installing-Git

#### 下载 git 

sudo yum install git-all

> -- 啃英文资料 会有很大的好处

#### 配置

git config -list

git config --global user.name '**'

git config --global user.email '**'

ssh-keygen -t rsa -C 'zhoudl@163.com'

### 基本命令

git status 查询当前状态

git log --branck 

##### push 改变到远端

```bash
git ac 'add 3.txt' //add commit 二合一
git push origin master //push 到远端
```

touch 4.txt

git status

git commit -m 'add 4.txt'

##### 常用命令

###### git status 

###### git remote 

* git clone git@***.com
* 本地项目推送到远端 git remote add 
  * git init  git 仓库初始化
  * git remote add origin git@***.com  和远端建立链接
  * git push -u origin master 本地代码推送到远端
  * git fetch  本地和远端同步一次 但是不会执行代码 观察有哪些分支 判断是否需要同步代码
  * git pull origin master 同步远端代码到本地分支
*  git remote  查看远端详细的分支信息等
  * git remote -v

###### git fetch/pull/push

> git fetch --help

> 本地回滚到某版本 (lastversion)
>
> git reset --hard lastversion

git push -f origin master

###### checkout

* 切换分支
  * git checkout -b dev-0419-demo(之后 -b 可以省略)
* 撤销更改
  * checkout . 可以恢复当前目录下的所有文件
  * checkout 1.txt 恢复 1.txt 文件

###### log

###### stash(不建议用)

###### merge

* 本地开发分支直接合并到远端 master ，review 之后再合并到 master 分支，这样本-地 master 可以保证最干净，否则有可能带一些污染代码到中央库

* 冲突解决：

###### rebase(建议少用)

###### tag 

版本

###### alias

> alias.ac = !git add -A && git commit -m 设置别名

可以用来组合命令，提高工作效率

### git-flow

团队管理

##### 一般分支大致花费为

```
tag 发布
master 正式
test 测试
devolopment 开发 
release 发布
```

##### 开发流程 

1. 从 master 拉取一个 dev(开发) 分支开始开发

2. 开发完成之后提交测试分支

3. 测试如果有 bug 继续在 dev 分支修复

4. 测试完之后提交回归测试分支

5. 回归测试过程中如果有 bug 继续在 dev 分支修改

6. 没有 bug 之后回归结束，开始合并 master

7. master 发布之后如有 bug，继续回 dev 分支开发

### gitlab

### git hooks

http://t.guahao.cn/jaaE3i



------

2019-09-01

-------

相关命令：

### git status 

* 红色：代表文件还没有被 git 管理

* git -help status : 查看 git status 的具体用法
* 绿色 ：代表文件存在于缓存区中



