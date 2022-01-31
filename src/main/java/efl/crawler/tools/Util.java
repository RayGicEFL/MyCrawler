package efl.crawler.tools;

import java.io.File;
import java.io.IOException;

public class Util {

	public static boolean createFolder(String folderPath) {
		File folder = new File(folderPath);
		if (folder.exists() && folder.isDirectory())
			return true;
		return folder.mkdirs();
	}

	public static File createFile(String parent, String fileName) {
		if (!createFolder(parent)) {
			System.out.println("无法创建文件夹: " + parent);
			System.exit(1);
		}

		File folder = new File(parent);
		File file = new File(folder, fileName);
		try {
			if (file.exists()) {
				if (!file.delete()) {
					System.out.println("无法删除文件: " + file);
					System.exit(1);
				}
			}
			if (!file.createNewFile()) {
				System.out.println("无法创建文件: " + file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}

	public static void createFile(File file) {
		File parent = file.getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				System.out.println("无法创建文件夹: " + parent);
				System.exit(1);
			}
		}

		if (file.exists()) {
			if (!file.delete()) {
				System.out.println("无法删除文件: " + file);
				System.exit(1);
			}
		}
	}
}
