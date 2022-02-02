# MyCrawler v1.1.4
这是一个 [pixiv](https://www.pixiv.net/) 爬虫，修改自 [PixivCrawler](https://github.com/R-Josef/PixivCrawler/)。

## 特性
* 支持爬取各种排行榜
* 支持代理
* 采用SQLite数据库记录已经爬取过的图片，确保图片不重复下载

## 快速上手
1. 确保已[安装 JRE 11](https://www.azul.com/downloads/?version=java-11-lts&package=jre)
2. 从 [发布页面](https://github.com/RayGicEFL/MyCrawler/releases) 下载压缩包并解压
3. 运行`run.bat`(Windows) 或 `run.sh`(macOS 与 Linux)
4. 编辑`config.yml`配置文件
    * `Proxy:`
        * `Host:` 填写 Socks5 代理的主机 IP 地址
        * `Port:` 填写 Socks5 代理的端口
    * `Cookie:`
        1. 打开 [pixiv](https://www.pixiv.net/) 并登录
        2. 按下`F12`，上方菜单栏找到`Application`或`应用`
        3. 左侧存储找到`Cookie`，展开，找到`https://www.pixiv.net`
        4. 右侧表中复制`PHPSESSID`的值并填写到配置文件中
    * `StartPage:` 填写开始页面地址，例如`https://www.pixiv.net/ranking.php?mode=male_r18`
    * `ImgSavePath:` 填写图片储存目录，`%HERE%`为 MyCrawler 所在目录
6. 再次运行`run.bat`或`run.sh`
7. MyCrawler 开始运行，不出意料你的图片储存目录下会出现 ~~好康的~~ 图片

## 开源库引用
* [JSoup](https://jsoup.org/)
* [SQLite](https://github.com/xerial/sqlite-jdbc)
* [SnakeYaml](https://bitbucket.org/asomov/snakeyaml/src/default/)
* [FastJSON](https://github.com/alibaba/fastjson)