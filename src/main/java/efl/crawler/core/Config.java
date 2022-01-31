package efl.crawler.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import efl.crawler.tools.Util;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class Config {
	public Map<String, Object> map = new LinkedHashMap<>();
	private String jarPath;
	private File cfgFile;

	public Config() {
	}

	public void setJarPath(String path) {
		this.jarPath = path;
	}

	public void load(String path) {
		if (cfgFile == null) {
			cfgFile = new File(jarPath + path);
		}

		InputStream in = null;

		try {
			in = new FileInputStream(cfgFile);
			Yaml yaml = new Yaml();
			this.map = yaml.load(in);
		} catch (FileNotFoundException e1) {
			System.out.print("配置文件未找到。");
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				System.exit(1);
			} catch (NullPointerException ignored){}
		}
	}

	public void save() {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);

		Yaml yaml = new Yaml(options);
		Util.createFile(cfgFile);

		try {
			FileWriter writer = new FileWriter(cfgFile);
			yaml.dump(this.map, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDefaultConfig(String path) {
		if (cfgFile == null)
			cfgFile = new File(jarPath + path);
		if (cfgFile.exists())
			return;

		InputStream in = Config.class.getClassLoader().getResourceAsStream(path);
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(cfgFile);
			byte[] bytes = new byte[1024];
			int count;
			while (true) {
				assert in != null;
				if ((count = in.read(bytes)) == -1) break;
				out.write(bytes, 0, count);
			}
			System.out.println("未找到配置文件，已自动为您生成，请将其配置好后重新运行此程序。");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException ignored){}
		}
	}

	public void Save() {
		this.save();
	}

	@SuppressWarnings("unchecked")
	public Object getValue(String path) {
		Object value;

		if (path.equals(".")) {
			value = this.map;
		} else if (path.contains(".")) {
			String[] keys = path.split("\\.");
			Map<String, Object> map;
			map = (Map<String, Object>) this.map.get(keys[0]);

			for (int i = 1; i + 1 <= keys.length - 1; i = i + 1) {
				map = (Map<String, Object>) map.get(keys[i]);
			}

			value = map.get(keys[keys.length - 1]);
		} else {
			value = this.map.get(path);
		}
		return value;
	}

	public String getString(String path) {
		String value;
		value = this.getValue(path).toString();
		return value;
	}

	@SuppressWarnings("unchecked")
	public void setValue(String path, Object value) {
		if (path.equals(".")) {
			this.map = (Map<String, Object>) value;
		} else if (path.contains(".")) {
			String[] keys = path.split("\\.");
			Map<String, Object> cache = this.map;

			for (int i = 0; i < keys.length - 1; i++) {
				cache = (Map<String, Object>) cache.get(keys[i]);
			}

			cache.put(keys[keys.length - 1], value);
		} else {
			map.put(path, value);
		}
	}
}
