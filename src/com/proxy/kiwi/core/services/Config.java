package com.proxy.kiwi.core.services;

import com.google.gson.*;
import com.proxy.kiwi.core.folder.Folder;
import com.proxy.kiwi.core.utils.Command;
import com.proxy.kiwi.core.utils.FileListener;
import com.proxy.kiwi.core.utils.Log;
import com.proxy.kiwi.core.utils.Resources;

import javafx.scene.input.KeyCode;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

public class Config {

	private static JsonObject config;

	private static String config_path, default_config;

	private static boolean create_flag = false;

	private static FileListener listener;

	public static void init() {
		String OS = (System.getProperty("os.name")).toUpperCase();
		String workingDirectory;
		String defaultPath;

		/* If on windows, use the AppData folder. */
		if (OS.contains("WIN")) {
			workingDirectory = System.getenv("AppData") + "\\Kiwi\\";
			defaultPath = Paths.get("C:", "Users", System.getProperty("user.name"), "Pictures").toString();

		}
		/* Else, use the home directory. */
		else {
			workingDirectory = System.getProperty("user.home") + "/Kiwi/";
			defaultPath = System.getProperty("user.home") + "/Pictures/";
			// TODO On Macs, add System.getProperty("user.home") +
			// "/Library/Application Support/Kiwi/"
		}

		File folder = new File(workingDirectory);
		folder.mkdir();

		config_path = workingDirectory + "config.json";

		default_config = Resources.getContent("default_config.txt").replaceAll("_PATH_", defaultPath);

		JsonParser parser = new JsonParser();

		try {
			config = parser.parse(new FileReader(config_path)).getAsJsonObject();
		} catch (JsonIOException | JsonSyntaxException e) {
			// TODO error message
			e.printStackTrace();
		} catch (FileNotFoundException e) {

			config = parser.parse(default_config).getAsJsonObject();
			setOption("path", defaultPath);
			addLibrary(defaultPath);

		}

		addMissingHotkeys();

		if (listener == null) {
			listener = FileListener.create(config_path, () -> {
				Log.print(Log.IO, "Detected change in config file");
				init();
			});			
		}
		

	}

	public static void save() {
		clean();
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			FileWriter fOut = new FileWriter(config_path);
			fOut.write(gson.toJson(jp.parse(config.toString())));
			fOut.close();
		} catch (IOException e) {
			// TODO error message
			e.printStackTrace();
		}
	}

	private static void clean() {
		JsonObject folders = config.get("folders").getAsJsonObject();
		for (Iterator<Entry<String, JsonElement>> iterator = folders.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, JsonElement> entry = iterator.next();
			if (entry.getValue().getAsJsonObject().entrySet().size() == 0) {
				iterator.remove();
			}
		}
	}

	public static String getOption(String key) {
		return config.get(key).getAsString();
	}

	public static int getIntOption(String key) {
		return config.get(key).getAsInt();
	}

	public static boolean getBoolOption(String key) {
		return config.get(key).getAsBoolean();
	}

	public static void setOption(String key, String value) {
		config.addProperty(key, value);
	}

	public static void setIntOption(String key, int value) {
		config.addProperty(key, value);
	}

	public static void setBoolOption(String key, boolean value) {
		config.addProperty(key, value);
	}

	public static String getFolderImage(String folder) {
		if (!propertyExists(folder, "image")) {
			return null;
		}
		return getFolder(folder).get("image").getAsString();
	}

	public static void setFolderImage(String folder, String path) {
		checkFolderExists(folder);
		getFolder(folder).addProperty("image", path);
	}

	public static int getFolderXOffset(String folder) {
		if (!propertyExists(folder, "xOff")) {
			return 0;
		}
		return getFolder(folder).get("xOff").getAsInt();
	}

	public static void setFolderXOffset(String folder, int off) {
		checkFolderExists(folder);
		getFolder(folder).addProperty("xOff", off);
	}

	public static int getFolderYOffset(String folder) {
		if (!propertyExists(folder, "yOff")) {
			return 0;
		}
		return getFolder(folder).get("yOff").getAsInt();
	}

	public static void setFolderYOffset(String folder, int off) {
		checkFolderExists(folder);
		getFolder(folder).addProperty("yOff", off);
	}

	public static void addFolderChapter(String folder, int page) {
		checkFolderExists(folder);
		if (propertyExists(folder, "chapters")) {
			String[] chapters = getChapters(folder);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < chapters.length; i++) {
				sb.append(chapters[i]);

				if (page > Integer.parseInt(chapters[i].trim())
						&& ((i + 1 < chapters.length && page < Integer.parseInt(chapters[i + 1].trim()))
								|| i + 1 == chapters.length)) {
					sb.append(",");
					sb.append(page);
				}
				if (i != chapters.length - 1) {
					sb.append(",");
				}
			}
			getFolder(folder).addProperty("chapters", sb.toString());
		} else {
			if (page == 1) {
				getFolder(folder).addProperty("chapters", "" + 1);
			} else {
				getFolder(folder).addProperty("chapters", "1," + page);
			}
		}
		save();
	}

	public static void removeFolderChapter(String folder, int page) {
		checkFolderExists(folder);
		if (propertyExists(folder, "chapters")) {
			StringBuilder sb = new StringBuilder();
			String[] chapters = getChapters(folder);
			for (int i = 0; i < chapters.length; i++) {
				if (Integer.parseInt(chapters[i].trim()) != page) {
					sb.append(chapters[i]);
					if (i != chapters.length - 1) {
						sb.append(",");
					}
				}
			}
			if (sb.toString().length() == 0) {
				getFolder(folder).remove("chapers");
			} else {
				getFolder(folder).addProperty("chapters", sb.toString());
			}
		}
		save();
	}

	public static int getNextChapter(String folder, int page) {
		String[] chapters = getChapters(folder);
		int[] array = new int[chapters.length];
		for (int i = 0; i < array.length; i++) {
			try {
				array[i] = Integer.parseInt(chapters[i].trim());
			} catch (NumberFormatException e) {

			}
		}
		for (int i = 0; i < array.length; i++) {
			if (page >= array[i] && i + 1 < array.length && page < array[i + 1]) {
				return array[i + 1];
			}
		}
		return 1;
	}

	public static int getPreviousChapter(String folder, int page) {
		String[] chapters = getChapters(folder);
		int[] array = new int[chapters.length];
		for (int i = 0; i < array.length; i++) {
			try {
				array[i] = Integer.parseInt(chapters[i].trim());
			} catch (NumberFormatException e) {

			}
		}
		for (int i = 0; i < array.length; i++) {
			if (page > array[i] && i + 1 < array.length && page <= array[i + 1]) {
				return array[i];
			}
		}
		return array[array.length - 1];
	}

	public static String[] getChapters(String folder) {
		checkFolderExists(folder);
		if (propertyExists(folder, "chapters")) {
			return getFolder(folder).get("chapters").getAsString().split(",");
		} else {
			return null;
		}
	}

	private static void checkFolderExists(String folder) {
		JsonElement je;
		je = config.get("folders");
		if (je == null) {
			config.add("folders", new JsonObject());
		}
		je = config.get("folders").getAsJsonObject().get(folder);
		if (je == null) {
			config.get("folders").getAsJsonObject().add(folder, new JsonObject());
		}
	}

	private static boolean propertyExists(String folder, String property) {
		return (config.get("folders") != null && config.get("folders").getAsJsonObject().get(folder) != null
				&& getFolder(folder).get(property) != null);
	}

	private static JsonObject getFolder(String folder) {
		return config.get("folders").getAsJsonObject().get(folder).getAsJsonObject();
	}

	public static JsonArray getHotkeysFor(String command) {
		return config.getAsJsonObject("hotkeys").getAsJsonArray(command);
	}

	private static void addMissingHotkeys() {
		Gson gson = new Gson();
		JsonObject hotkeys = config.getAsJsonObject("hotkeys");

		for (Command command : Command.values()) {
			if (!hotkeys.entrySet().contains(command.getName())) {
				if (command.default_hotkeys.length > 0) {
					JsonArray array = new JsonArray();
					for (String hotkey : command.default_hotkeys) {
						JsonElement el = gson.fromJson(hotkey, JsonElement.class);
						array.add(el);
					}
					hotkeys.add(command.getName(), array);
				}
			}
		}
	}

	public static Command getCommandFor(KeyCode key) {
		for (Entry<String, JsonElement> entry : config.getAsJsonObject("hotkeys").entrySet()) {
			JsonArray arr = entry.getValue().getAsJsonArray();
			for (JsonElement hotkey : arr) {
				if (hotkey != null) {
					if (KeyCode.getKeyCode(hotkey.getAsString()).equals(key)) {
						return Command.get(entry.getKey());
					}
				}
			}
		}
		;

		return Command.UNDEFINED;
	}

	public static Set<Entry<String, JsonElement>> getHotkeys() {
		return config.getAsJsonObject("hotkeys").entrySet();
	}

	public static Set<Entry<String, JsonElement>> getLibraries() {
		return config.getAsJsonObject("libraries").entrySet();
	}

	public static void addLibrary(String path) {
		Gson gson = new Gson();
		JsonElement el = gson.fromJson("false", JsonElement.class);
		JsonElement el2 = gson.fromJson("true", JsonElement.class);

		config.getAsJsonObject("libraries").entrySet().forEach((entry) -> {
			if (entry.getKey().equals(path)) {
				entry.setValue(el2);
			} else {
				entry.setValue(el);
			}

		});

		if (!config.getAsJsonObject("libraries").has(path)) {
			config.getAsJsonObject("libraries").add(path, el);
		}
		save();
	}

	public static Set<String> getArtists() {
		Set<String> artists = new HashSet<>();
		for (Entry<String, JsonElement> folder : config.get("folders").getAsJsonObject().entrySet()) {
			JsonElement folderArtists = folder.getValue().getAsJsonObject().get("artists");
			if (folderArtists != null) {
				folderArtists.getAsJsonArray().forEach((el) -> {
					artists.add(el.getAsString());
				});
			}
		}

		return artists;
	}

	public static Set<String> getArtists(Folder folder) {
		Set<String> artists = new HashSet<>();
		checkFolderExists(folder.getName());
		if (config.get("folders").getAsJsonObject().get(folder.getName()) != null) {
			JsonElement folderArtists = getFolder(folder.getName()).getAsJsonObject().get("tags");
			if (folderArtists != null) {
				folderArtists.getAsJsonArray().forEach((el) -> {
					artists.add(el.getAsString());
				});
			}
		}
		return artists;
	}

	public static Set<String> getTags() {
		Set<String> tags = new HashSet<>();
		for (Entry<String, JsonElement> folder : config.get("folders").getAsJsonObject().entrySet()) {
			JsonElement folderTags = folder.getValue().getAsJsonObject().get("tags");
			if (folderTags != null) {
				folderTags.getAsJsonArray().forEach((el) -> {
					tags.add(el.getAsString());
				});
			}
		}

		return tags;
	}

	public static Set<String> getTags(Folder folder) {
		Set<String> tags = new HashSet<>();
		if (config.get("folders").getAsJsonObject().get(folder.getName()) != null) {
			JsonElement folderTags = getFolder(folder.getName()).getAsJsonObject().get("tags");
			if (folderTags != null) {
				folderTags.getAsJsonArray().forEach((el) -> {
					tags.add(el.getAsString());
				});
			}
		}
		return tags;
	}

	public static void setTags(Folder folder, Set<String> tags) {
		Gson gson = new Gson();
		checkFolderExists(folder.getName());
		JsonObject f = getFolder(folder.getName());
		JsonElement folderTags = f.get("tags");
		if (folderTags != null) {
			f.remove("tags");
		}
		f.add("tags", new JsonArray());
		folderTags = f.get("tags");
		for (String tag : tags) {
			folderTags.getAsJsonArray().add(gson.fromJson(tag, JsonElement.class));
		}
		save();
	}

	public static void setArtists(Folder folder, Set<String> artists) {
		Gson gson = new Gson();
		checkFolderExists(folder.getName());
		JsonObject f = getFolder(folder.getName());

		JsonElement folderArtists = f.get("artists");
		if (folderArtists != null) {
			f.remove("artists");
		}
		f.add("artists", new JsonArray());
		folderArtists = f.get("artists");

		for (String artist : artists) {
			folderArtists.getAsJsonArray().add(gson.fromJson(artist, JsonElement.class));
		}

	}

	// New API

	public static Set<String> aggregateStringArrays(String key, String... keys) {
		Set<String> set = new HashSet<>();

		JsonElement obj = get(keys);
		if (obj != null && obj instanceof JsonObject) {
			strFullMap(set, obj.getAsJsonObject(), key);
		}

		return set;
	}

	private static void strFullMap(Set<String> set, JsonObject obj, String key) {
		obj.entrySet().forEach((entry) -> {
			if (entry.getKey().equals(key) && entry.getValue() instanceof JsonArray) {
				entry.getValue().getAsJsonArray().forEach((el) -> {
					set.add(el.getAsString());
				});
			} else if (entry.getValue() instanceof JsonObject) {
				strFullMap(set, entry.getValue().getAsJsonObject(), key);
			}
		});
	}

	public static Map<String, int[]> intArrayMap(String... keys) {
		Map<String, int[]> map = new HashMap<>();
		JsonElement obj = get(keys);
		if (obj == null) {
			return map;
		}
		obj.getAsJsonObject().entrySet().forEach((entry) -> {
			map.put(entry.getKey(), toIntArray(entry.getValue().getAsJsonArray()));
		});

		return map;
	}

	public static Map<String, String[]> arrayMap(String... keys) {
		Map<String, String[]> map = new HashMap<String, String[]>();
		JsonElement obj = get(keys);
		if (obj == null) {
			return map;
		}
		obj.getAsJsonObject().entrySet().forEach((entry) -> {
			map.put(entry.getKey(), toArray(entry.getValue().getAsJsonArray()));
		});

		return map;
	}

	public static Map<String, String> stringMap(String... keys) {
		Map<String, String> map = new HashMap<String, String>();
		JsonElement obj = get(keys);
		if (obj == null) {
			return map;
		}
		obj.getAsJsonObject().entrySet().forEach((entry) -> {
			map.put(entry.getKey(), entry.getValue().getAsString());
		});

		return map;
	}

	public static Set<String> stringSet(String... keys) {
		Set<String> res = new HashSet<>();
		JsonArray arr = get(keys).getAsJsonArray();

		if (arr == null) {
			return null;
		}
		arr.forEach((el) -> {
			res.add(el.getAsString());
		});

		return res;
	}

	public static int[] intArray(String... keys) {
		return toIntArray(get(keys).getAsJsonArray());
	}

	public static String[] array(String... keys) {
		return toArray(get(keys).getAsJsonArray());
	}

	public static boolean bool(String... keys) {
		return toBool(get(keys));
	}

	public static int integer(String... keys) {
		return toInt(get(keys));
	}

	public static String string(String... keys) {
		return toString(get(keys));
	}

	private static JsonElement get(String... keys) {
		JsonElement obj = config;
		for (String key : keys) {
			JsonElement el = obj.getAsJsonObject().get(key);
			if (el == null) {
				if (create_flag) {
					obj.getAsJsonObject().add(key, new JsonObject());
					el = obj.getAsJsonObject().get(key);
				} else {
					return null;
				}
			}
			obj = el;
		}
		return obj;
	}

	public static void add(String key, Object value, String... keys) {
		create_flag = true;
		JsonElement obj = get(keys);
		if (obj != null && obj instanceof JsonObject) {
			JsonObject prop = obj.getAsJsonObject();
			if (value instanceof String) {
				prop.addProperty(key, (String) value);
			} else if (value instanceof Integer) {
				prop.addProperty(key, (Integer) value);
			} else if (value instanceof Boolean) {
				prop.addProperty(key, (Boolean) value);
			}
		}
		create_flag = false;
	}

	private static boolean toBool(JsonElement jsonElement) {
		return (jsonElement == null ? false : jsonElement.getAsBoolean());
	}

	private static int toInt(JsonElement jsonElement) {
		return (jsonElement == null ? 0 : jsonElement.getAsInt());
	}

	private static String toString(JsonElement jsonElement) {
		return (jsonElement == null ? "" : jsonElement.getAsString());
	}

	private static String[] toArray(JsonArray jsonarray) {
		String[] arr = new String[jsonarray.size()];
		for (int i = 0; i < jsonarray.size(); i++) {
			arr[i] = jsonarray.get(i).getAsString();
		}
		return arr;
	}

	private static int[] toIntArray(JsonArray jsonarray) {
		int[] arr = new int[jsonarray.size()];
		for (int i = 0; i < jsonarray.size(); i++) {
			try {
				arr[i] = Integer.parseInt(jsonarray.get(i).getAsString());
			} catch (NumberFormatException e) {
				arr[i] = 0;
			}
		}
		return arr;
	}
}
