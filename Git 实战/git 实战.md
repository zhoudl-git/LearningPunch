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

* 本地开发分支直接合并到远端 master ，review 之后再合并到 master 分支，这样本地 master 可以保证最干净，否则有可能带一些污染代码到中央库

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

--------------------

--------------------

2019-09-12

再次重新学习 git 教程 

------------

### 时光机穿梭

#### 版本回退

1. HEAD 指向的版本就是当前版本，因此 git 可以让我们在版本历史之间穿梭，使用 git reset --hard commit_id；
2. 穿梭前可以使用 git log 查看提交历史，以便确定要回退到哪个版本；
3. 要重返未来，使用 git reflog 查看命令历史，以便确定要回到未来哪几个版本；

#### 工作区和缓存区

1. git add 实际上就是把文件修改添加到暂存区；
2. git commit 提交更改，实际上就是把暂存区的所有内容提交到当前分支；
3. git diff 比较的是工作区文件和暂存区文件的区别（上次 git add 的内容）git diff --cached 比较的是暂存区的文件与仓库分支里（上次 git commit 后的内容）的区别；
4. 用`git diff HEAD -- readme.txt`命令可以查看工作区和版本库里面最新版本的区别；
5. git restore 回到最近一次 git add 或者 git commit 的状态；
6. git restore --staged readme.txt / git reset HEAD readme.txt 可以把暂存区的修改撤销掉，重新放回工作区；
7. 从版本库删除数据 git rm test.txt

>git pull 失败 ,提示：`fatal: refusing to merge unrelated histories`
>
>其实这个问题是因为 两个 根本不相干的 git 库， 一个是本地库， 一个是远端库， 然后本地要去推送到远端， 远端觉得这个本地库跟自己不相干， 所以告知无法合并
>
>具体的方法， 一个种方法： 是 从远端库拉下来代码 ， 本地要加入的代码放到远端库下载到本地的库， 然后提交上去 ， 因为这样的话， 你基于的库就是远端的库， 这是一次[update](https://www.centos.bz/tag/update/)了
>
>第二种方法：
>使用这个强制的方法
>
>```
>git pull origin master --allow-unrelated-histories
>```
>
>后面加上 `--allow-unrelated-histories` ， 把两段不相干的 分支进行强行合并
>
>后面再push就可以了 `git push gitlab master:init`

8. 要关联一个远程库，使用命令`git remote add origin git@server-name:path/repo-name.git`；

   关联后，使用命令`git push -u origin master`第一次推送master分支的所有内容；

   此后，每次本地提交后，只要有必要，就可以使用命令`git push origin master`推送最新修改；

### 分支管理

#### 创建与合并分支

1. 创建并切换分支除了可以使用 git checkout name 还可以使用 git switch -c name ；
2. 合并分支使用 get merge；
3. 删除分支 git branch -d；

#### 解决冲突

1. 合并前必须解决冲突；
2. 可以使用 git log --graph 查看分支变化路线；

#### 分支管理策略

一般情况下 git 合并分支会使用 fast forward 模式，但是这种模式下，删除分之后，会丢掉分支信息。

如果我们强制禁用 fast foward 模式，git 就会在  Merge 时生成一个新的 commit，这样，从分支历史上还是可以看到已删除的分支信息。

git merge --no-ff -m '' 可以避免 Fast foward 模式的合并

实际开发中：

master 分支应该是非常稳定的，也就是仅用来发布新版本，平时不能在上面干活。

一般情况下干活都是在 dev 分支上，也就是说 dev 分支是不稳定的，到某个时候需要发布的时候再把 dev 分支合并到 master 分支上，然后在 master 分支上发布新版本。

> 合并分支时，加上 --no-off 参数就可以使用普通模式合并，合并后的历史所有分支，都能看出来曾经做过的合并信息，而 Fast foward 合并就看不出来曾经做过合并。

#### Bug 分支

平时开发过程中都会出现 bug ，如果有一天你正在开发某个新功能，结果突然出现了 issue-01 bug 需要你去修复，可是你手头的工作还没有做完，也就是说在 dev 上进行的工作还没有提交 。

这个时候我们需要保存当前的工作区，可以使用 git stash 把当前工作现场储藏起来，以后有时间了可以恢复现场继续干活。

经过一段紧张而又刺激的修复之后你解决完了问题，现在想开始继续开发之前没有完成的工作，此时可以用`git stash list`命令看看，git 把我们的工作现场存哪儿了，

然后进行恢复：

* git stash apply ：恢复后 stash 内容并不删除，需要使用 git stash drop 来删除；
* git stash pop : 恢复的同时把 stash 内容也删除了；

> 综上：需要使用的命令有 
>
> git stash 
>
> git stash pop

#### Feature 分支

一些实验性的功能可以使用 feature 分支的方式来开发，添加一个新功能时，最好新建一个 feature 分支开发，开发完成后合并，最后删除该 feature 分支。

### 多人协作

#### 推送分支

git push origin master 可以把 mster 分支推送到远端

但是并不是所有本地分支都需要往远程推送：

* master 分支是主要分支，需要时刻和远程同步；
* dev 分支是开发分支，所有成员都需要在上边工作，也需要与远程同步；
* bug 分支只用于本地修复 bug，可以不推送到远程；
* feature 分支是否推到远程，取决于你是否和你的团队成员合作在上面开发。

#### 抓取分支

新的团队成员首先 git clone 仓库地址，此时在本地只能看到 master 分支，所以我们需要新建一个本地分支并且和远端分支关联

git checkout -b dev origin/dev

现在就可以在 Dev 分支上修改东西，并 push 到远程分支上去。

git pull 拉取分支最新变化，拉取的过程中如果有冲突，需要再本地合并并解决冲突之后再推送。

```bash
$ git pull
There is no tracking information for the current branch.
Please specify which branch you want to merge with.
See git-pull(1) for details.

    git pull <remote> <branch>

If you wish to set tracking information for this branch you can do so with:

    git branch --set-upstream-to=origin/<branch> dev
```



此时你会发现 git  pull 失败了，原因是没有指定本地 dev 分支与远程 origin/dev 分支的链接，根据提示设置 dev 和 origin/dev 的链接：

```
git branch -set-upstream-to=origin/dev dev
```

继续 git pull，解决冲突，提交，最后 push。

##### 小结

多人协作的工作模式一般如下描述：

1. 尝试使用 git push origin <branch-name>推送自己的修改；
2. 如果推送失败，则因为远程分支比你的本地更新，需要先用 git pull 试图合并；
3. 如果合并有冲突，则解决冲突，并在本地提交；
4. 没有冲突或者解决掉冲突后，再使用 git push origin <brnach-name> 推送。

如果 git pull 是 no tracking information，则说明本地分支和远程分支的链接关系没有创建，用命令 git branch --set-uopstream-to<branch-name>origin<branch-name>

