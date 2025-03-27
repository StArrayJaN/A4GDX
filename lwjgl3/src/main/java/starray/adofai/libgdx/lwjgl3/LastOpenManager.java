package starray.adofai.libgdx.lwjgl3;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LastOpenManager {
    private static LastOpenManager instance;
    static {
        try {
            instance = new LastOpenManager();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private File lastOpenFileJson;
    private JSONObject info = new JSONObject();;
    public static String PATH_KEY = "path";
    public static String KEY_LIST_KEY = "keyList";

    private LastOpenManager() throws IOException {
        init();
    }

    public static LastOpenManager getInstance() {
        return instance;
    }

    private void init() throws IOException {
        lastOpenFileJson = new File("lastOpenFile.json");
        if (!lastOpenFileJson.exists()) {
            lastOpenFileJson.createNewFile();
        } else {
            String fileContent = new String(Files.readAllBytes(lastOpenFileJson.toPath()));
            if (!fileContent.isEmpty()) {
                info = new JSONObject(fileContent);
            }
        }
    }

    public void putData(String key, String value) {
        info.put(key, value);
    }
    public void putDouble(String key, double value) {
        info.put(key, value);
    }
    public void putBoolean(String key, boolean value) {
        info.put(key, value);
    }

    public String getData(String key, String defaultValue) {
        return info.optString(key, defaultValue);
    }
    public double getDouble(String key, double defaultValue) {
        return info.optDouble(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return info.optBoolean(key, defaultValue);
    }

    public JSONObject getInfo() {
        return info;
    }

    public void setLastOpenFile(String path) {
        info.put(PATH_KEY, path);
    }

    public String getLastOpenFile() {
        return info.optString(PATH_KEY,"");
    }

    public String getKeyList() {
        return info.optString(KEY_LIST_KEY,"");
    }

    public void setKeyList(String keys) {
        info.put(KEY_LIST_KEY, keys);
    }

    public void save() {
        if (info != null) {
            try {
                Files.write(lastOpenFileJson.toPath(), info.toString(2).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
