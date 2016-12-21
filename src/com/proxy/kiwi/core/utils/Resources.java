package com.proxy.kiwi.core.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * Helper class for reading compiled files. The compiled files resides inside
 * the src directory.
 * 
 * @author Daniel
 *
 */
public class Resources {
	public static final String RES_FOLDER = "com/proxy/kiwi/res/";

	public static URL get(String path) {

		return Resources.class.getClassLoader().getResource(RES_FOLDER + path);
	}

	public static ArrayList<String> getAll(String... paths) {
		ArrayList<String> strings = new ArrayList<>();
		for (String path : paths) {
			strings.add(Resources.get(path).toExternalForm());
		}
		return strings;
	}

	public static File getFile(String path, String name) {
		try {
			File temp = File.createTempFile(name, ".kiwitemp");
			temp.deleteOnExit();
			InputStream in = get(path).openStream();
			FileOutputStream out = new FileOutputStream(temp);
			IOUtils.copy(in, out);
			out.close();
			in.close();

			return temp;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String getContent(String path) {
		URL url = Resources.class.getClassLoader().getResource(RES_FOLDER + path);
		StringBuilder out = new StringBuilder();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return out.toString();
	}
}
