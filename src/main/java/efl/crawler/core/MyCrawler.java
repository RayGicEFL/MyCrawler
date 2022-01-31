package efl.crawler.core;

import efl.crawler.tools.SQLiter;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class MyCrawler {
	private final static String VERSION = "1.1.0";

	public static String jarPath = Objects.requireNonNull(MyCrawler.class.getClassLoader().getResource("")).getPath();
	public static Config config = new Config();
	public static SQLiter db = new SQLiter();

	public static void main(String[] args) {
		printAbout();

		config.setJarPath(jarPath);
		config.saveDefaultConfig("config.yml");

		// 锁住配置文件
		File cfgFile = new File(jarPath + "config.yml");
		try {
			FileChannel.open(cfgFile.toPath(), StandardOpenOption.READ);
		} catch (IOException e) {
			System.out.println("上锁配置文件失败，请在接下来的过程中勿编辑配置文件。");
		}

		config.load("config.yml");
		// 设置代理
		String proxyHost = config.getString("Proxy.Host");
		String proxyPort = config.getString("Proxy.Port");
		if (!proxyHost.isEmpty()) {
			System.setProperty("proxyHost", proxyHost);
		}
		if (!proxyPort.isEmpty()) {
			System.setProperty("proxyPort", proxyPort);
		}

		// 设置数据库
		db.setJarPath(jarPath);
		db.load();

		// 设置爬虫
		Crawler crawler = new Crawler(config, db);
		crawler.setJarPath(jarPath);
		String cookie = config.getString("Cookie");
		crawler.addCookie("PHPSESSID", cookie);
		String url = config.getString("StartPage");
		while (true) {
			url = crawler.resolveListPage(url);
			System.out.println("已完成当前列表并成功获取到下一页: " + url);
		}
	}

	private static void printAbout() {
		String content = "=============== MyCrawler =============== \n" +
				"\n  版本: " + VERSION + "\n" +
				"\n  作者: EFL \n" +
				"\n  GitHub: https://github.com/RayGicEFL/MyCrawler \n";
		System.out.print(content);
	}

}
