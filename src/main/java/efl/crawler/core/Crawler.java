package efl.crawler.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import efl.crawler.tools.SQLiter;
import efl.crawler.tools.Util;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Crawler {

	private final Map<String, String> cookies = new HashMap<>();
	private final Config config;
	private String jarPath;
	public SQLiter db;

	public void setJarPath(String path) {
		this.jarPath = path;
	}

	public Crawler(Config config, SQLiter db) {
		this.config = config;
		this.db = db;
	}

	public void addCookie(String key, String value) {
		cookies.put(key, value);
	}

	public String resolveListPage(String url) {
		String nextPageURL = null;

		try {
			Response res;

			while (true) {
				try {
					res = Jsoup.connect(url).cookies(cookies).method(Method.GET).execute();
					break;
				} catch (SocketTimeoutException e) {
					System.out.println("请求图片列表页面超时，将重试。");
				} catch (SSLHandshakeException e) {
					System.out.println("请求图片列表页面被拒绝，将重试。");
				} catch (SSLException e) {
					System.out.println("请求图片列表页面被关闭，将重试。");
				} catch (ConnectException e) {		// cookie错误将会超时
					System.out.println("连接超时，请检查 cookie 是否错误或过期。");
				} catch (SocketException e) {
					System.out.println("意外结束，将重试。");
				} catch (HttpStatusException e) {	// cookie不正确或没有将返回错误码
					System.out.println("HTTP 状态错误" + e.getStatusCode() + " 请填写正确的 cookie。");
				}
			}

			Document doc = res.parse();
			Elements pages = doc.select("#wrapper").select("div.layout-body").select("div")
					.select("div.ui-fixed-container").select("div").select("nav:nth-child(2)").select("ul")
					.select("li.after").select("a");
			Element nextPage = null;

			try {
				nextPage = pages.get(0);
			} catch (IndexOutOfBoundsException e) {
				System.out.println("网页格式错误，请检查 cookie 是否已经过期 (或者该榜单已被爬取完毕)。");
				System.exit(1);
			}
			nextPageURL = nextPage.absUrl("href");
			Elements images = doc.select("#wrapper").select("div.layout-body").select("div")
					.select("div.ranking-items-container").select("div.ranking-items.adjust")
					.select("section.ranking-item");
			for (Element image : images) {
				String dataID = image.attr("data-id");
				Element imgPage = image.select("div.ranking-image-item").select("a").get(0);
				String imgPageURL = imgPage.absUrl("href");
				System.out.println("=============== " + dataID + " ===============");
				System.out.println("正在爬取: " + imgPageURL);
				resolveImagePage(imgPageURL, dataID);
			}
			config.setValue("StartPage", url);
			config.Save();
		} catch (IOException e) {
			e.printStackTrace();
			return nextPageURL;
		}
		System.out.println(nextPageURL);
		return nextPageURL;
	}

	public void resolveImagePage(String url, String dataID) {
		int amount = db.checkArtworks(Integer.parseInt(dataID));	// 尝试获取一个值
		if (amount == 0) {											// 如果为0说明数据库中没有这个数据
			System.out.println("数据库中未查到此图片页面的信息，继续下载操作。");
		} else {													// 如果不是0说明这些图片已经下载过了
			System.out.println("数据库中已查到此图片页面的信息，图片数量为" + amount + ", 自动跳过。");
			return;
		}
		try {
			Response res;
			while (true) {
				try {
					res = Jsoup.connect(url).cookies(cookies).method(Method.GET).execute();
					break;
				} catch (SocketTimeoutException e) {
					System.out.println("请求图片页面超时，将重试。");
				} catch (SSLHandshakeException e) {
					System.out.println("请求图片页面被拒绝，将重试。");
				} catch (SSLException e) {
					System.out.println("请求图片页面被关闭，将重试。");
				} catch (ConnectException e) {						// cookie错误将会超时, 但是其实可以不带cookie
					System.out.println("连接超时，请检查cookie是否错误或过期。");
				} catch (SocketException e) {
					System.out.println("意外结束，将重试。");
				}
			}

			Document doc = res.parse();
			Element meta = doc.select("#meta-preload-data").first();

			assert meta != null;
			String content = meta.attr("content");
			JSONObject obj = JSON.parseObject(content);

			int pageCount = obj.getJSONObject("illust").getJSONObject(dataID).getIntValue("pageCount");
			String p0url = obj.getJSONObject("illust").getJSONObject(dataID).getJSONObject("urls")
					.getString("original");

			boolean singleImg;
			for (int i = 0; i < pageCount; i++) {
				String imgURL;
				if (i == 0) {
					imgURL = p0url;
					singleImg = true;
				} else {
					imgURL = p0url.replaceAll("p0", "p" + i);
					singleImg = false;
				}
				String filename = imgURL.substring(imgURL.lastIndexOf("/") + 1);

				String imgSavePath = config.getString("ImgSavePath").replace("%HERE%",
						// 去掉 jarPath 末尾的 '/'
						jarPath.substring(0, jarPath.length() - 1));

				File imgSaveDir = new File(imgSavePath);
				imgSaveDir.mkdir();

				Response resImg;
				while (true) {
					BufferedInputStream in = null;
					BufferedOutputStream out = null;
					try {
						File workDir, workFile;
						resImg = Jsoup.connect(imgURL).cookies(cookies).ignoreContentType(true).maxBodySize(1073741824)
								.referrer("https://www.pixiv.net/artworks/" + dataID).execute();

						if (singleImg) {
							String[] parts = filename.split("_", 2);
							if (parts.length < 2) {
								System.out.println("无效文件名: " + filename);
								continue;
							}
							filename = parts[0] + ".jpg";

							workFile = new File(imgSaveDir, filename);
						}
						else {
							workDir = new File(imgSaveDir, dataID);
							if (!workDir.exists() || !workDir.isDirectory())
								if (!workDir.mkdir()) {
									System.out.println("无法创建文件夹: " + workDir);
									continue;
								}

							String[] parts = filename.split("_", 2);
							if (parts.length < 2) {
								System.out.println("无效文件名: " + filename);
								continue;
							}
							filename = parts[1];

							workFile = new File(workDir, filename);
						}

						in = resImg.bodyStream();
						out = new BufferedOutputStream(new FileOutputStream(workFile));

						byte[] bytes = new byte[1024];
						int total = 0;
						int count;
						while ((count = in.read(bytes)) != -1) {
							out.write(bytes, 0, count);
							total = total + count;
						}
						System.out.println("文件 " + filename + " 保存完成，共收到" + total + "字节。");
						break;
					} catch (SocketTimeoutException e) {
						System.out.println("请求图片超时，将重试。");
					} catch (SSLHandshakeException e) {
						System.out.println("请求图片被拒绝，将重试。");
					} catch (SSLException e) {
						System.out.println("请求图片被关闭，将重试。");
					} catch (ConnectException e) {		// cookie错误将会超时, 但是其实可以不带cookie
						System.out.println("连接超时，请检查cookie是否错误或过期。");
					} catch (SocketException e) {
						System.out.println("意外结束，将重试。 ");
					} catch (HttpStatusException e) {	// 防止有画师上传的同一个帖子内文件格式不同
						if (imgURL.contains(".jpg")) {
							imgURL = imgURL.replaceAll(".jpg", ".png");
						} else if (imgURL.contains(".png")) {
							imgURL = imgURL.replaceAll(".png", ".jpg");
						}
						System.out.println("HTTP 状态错误:" + e.getStatusCode() + " 将尝试另一后缀名。");
					} finally {
						try {
							in.close();
							out.close();
						} catch (NullPointerException | SSLException ignored){}
					}
				}
			}

			db.addArtworks(Integer.parseInt(dataID), pageCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

