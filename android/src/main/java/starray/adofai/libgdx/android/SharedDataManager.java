package starray.adofai.libgdx.android;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedDataManager {
    private static SharedDataManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static SharedDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedDataManager();
            instance.sharedPreferences = context.getSharedPreferences("shared_data", Context.MODE_PRIVATE);
            instance.editor = instance.sharedPreferences.edit();
        }
        return instance;
    }

    public void putData(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public void putBoolData(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void putNumberData(String key, Number value) {
        editor.putFloat(key, value.floatValue());
        editor.apply();
    }

    public String getData(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean getBoolData(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public Number getNumberData(String key, Number defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue.floatValue());
    }
}
