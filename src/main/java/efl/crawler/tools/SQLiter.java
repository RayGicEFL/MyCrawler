package efl.crawler.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiter {

	private String jarPath;
	private Connection conn;

	public void setJarPath(String path) {
		this.jarPath = path;
	}

	public void load() {
		connect();
		createTableCrawledArtworks();
	}

	public void connect() {
		String driver = "org.sqlite.JDBC";
		String url = "jdbc:sqlite:" + jarPath + "database.db";
		try {
			Class.forName(driver);
			this.conn = DriverManager.getConnection(url);
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println("连接数据库时失败.");
			System.exit(1);
		}
	}

	public void createTableCrawledArtworks() {
		String sql = "CREATE TABLE IF NOT EXISTS `CrawledArtworks` ( `id` INT NOT NULL, `amount` SMALLINT NOT NULL, PRIMARY KEY(`id`) );";
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println("创建数据表时失败。");
			System.exit(1);
		}
	}

	public int checkArtworks(int id) {
		String sql = "SELECT `amount` from `CrawledArtworks` WHERE `id`=?;";
		int amount = 0;
		try {
			PreparedStatement pState = conn.prepareStatement(sql);
			pState.setInt(1, id);
			ResultSet rs = pState.executeQuery();

			if (!rs.isClosed())
				amount = rs.getInt("amount");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return amount;
	}

	public void addArtworks(int id, int amount) {
		String sql = "INSERT INTO `CrawledArtworks` (`id`, `amount`) VALUES (?, ?);";
		try {
			PreparedStatement pState = conn.prepareStatement(sql);
			pState.setInt(1, id);
			pState.setInt(2, amount);
			pState.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
