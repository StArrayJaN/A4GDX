package starray.adofai.level;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import starray.adofai.libgdx.SimpleCallback;

@SuppressWarnings("All")
public class Level {
    JSONObject level;
    JSONObject settings;
    JSONArray events;
    List<String> eventList;
    JSONArray decorations;
    private String currentLevelFile;
    private String currenttLevelDir;

    public static double generalizeAngle(double angle) {
        angle = angle - ((int) (angle / 360)) * 360;
        return angle < 0 ? angle + 360 : angle;
    }

    public Level(JSONObject level) {
        this.level = level;
        this.settings = level.getJSONObject("settings");
        this.events = level.getJSONArray("actions");
        this.eventList = events.toList(String.class);
        if (settings.getIntValue("version") >= 10) {
            this.decorations = level.getJSONArray("decorations");
        }
    }

    public String getCurrentLevelFile() {
        return currentLevelFile;
    }

    public String getCurrentLevelDir() {
        return currenttLevelDir;
    }

    public JSONArray getEvents() {
        return events;
    }

    public static Level readLevelFile(String filePath) throws LevelNotFoundException {
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)))
                .replaceAll("\"Enabled\"", "true")
                .replaceAll("\"Disabled\"", "false")
                .replaceAll(",,",",");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int i = content.indexOf("{");
        String newJSONStr = content.substring(i);
        try {
            JSONObject.parse(newJSONStr);
        } catch (Exception e) {
            if (e instanceof JSONException) {
                String str = e.getMessage();
                int start = 0;
                while (start < str.length() && !Character.isDigit(str.charAt(start))) {
                    start++;
                }

                int end = start;
                while (end < str.length() && Character.isDigit(str.charAt(end))) {
                    end++;
                }

                String numberStr = str.substring(start, end);
                int number = Integer.parseInt(numberStr) - 2;
                StringBuilder sb = new StringBuilder(newJSONStr);
                sb.insert(number, ",");
                newJSONStr = sb.toString();
            }
        }
        Level level = new Level(JSONObject.parse(newJSONStr));
        level.currentLevelFile = filePath;
        level.currenttLevelDir = new File(filePath).getParent();
        return level;
    }

    public JSONObject toJSONObject() {
        JSONArray jsonArray = new JSONArray(getCharts());
        level.put("angleData", jsonArray);
        level.put("settings", JSONObject.toJSONString(settings));
        if (settings.getIntValue("version") >= 10) {
            level.put("decorations", decorations);
        }
        return level;
    }

    public String toString() {
        return this.toJSONObject().toString();
    }

    public List<Float> getCharts() throws JSONException {
        //预处理，有需要则在main方法进行更多处理
        JSONArray charts = null;
        String pathData = "RRRRRRRRRR";
        if (level.containsKey("angleData")) {
            return level.getJSONArray("angleData").toJavaList(Float.class);
        }else {
            pathData = level.getString("pathData");
        }

        List<Float> chartArray = new ArrayList<>();

        if (charts == null) {
            List<TileAngle> parsedPathData = new ArrayList<>();
            for (int i = 0; i < pathData.length(); i++) {
                char c = pathData.charAt(i);
                parsedPathData.add(TileAngle.angleCharMap.get(c));
            }
            float staticAngle = 0;

            for (TileAngle angle : parsedPathData) {
                if (angle == TileAngle.NONE) {
                    chartArray.add(angle.angle);
                    continue;
                } else {
                    if (angle.relative) {
                        staticAngle = (float) generalizeAngle(staticAngle + 180 - angle.angle);
                    } else staticAngle = angle.angle;
                }
                chartArray.add(staticAngle);
            }

            return chartArray;
        }

        for (int i = 0; i < charts.size(); i++) {
            float chart = Float.parseFloat(charts.get(i).toString());
            chartArray.add(chart);
        }
        return chartArray;
    }

    public double getBPM() throws JSONException {
        return Double.parseDouble(settings.get("bpm").toString());
    }

    public int getOffset() throws JSONException {
        return settings.getIntValue("offset");
    }

    public String getMusicPath() {
        String songFilename = settings.getString("songFilename");
        File music = new File(new File(currentLevelFile).getParentFile(), songFilename);
        if (songFilename == null || !music.exists()) {
            return null;
        }
        return new File(currentLevelFile).getParent() + File.separator + songFilename;
    }

    public float getPitch() throws JSONException {
        return settings.getIntValue("pitch");
    }

    public Object getSetting(String setting) {
        return settings.get(setting);
    }

    public void setLevelSetting(String key, Object value) throws JSONException {
        settings.put(key, value);
    }

    public void removeLevelSetting(String key) {
        settings.remove(key);
    }

    public boolean hasSetting(String key) {
        try {
            settings.get(key);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public double[] getSpeedList(SimpleCallback simpleCallback) throws JSONException, InterruptedException, ExecutionException {
        double bpm = getBPM();
        int chartSize = getCharts().size();
        double[] speedList = new double[chartSize];

        return speedList;
    }

    public String[] getSpeed(int chart) throws JSONException {
        JSONObject event;
        for (int a = 0; a < events.size(); a++) {
            event = (JSONObject) events.get(a);
            if ((int) event.get("floor") == chart && event.get("eventType").equals("SetSpeed")) {
                String isMultiplier = "false";
                if (event.get("speedType").equals("Multiplier")) {
                    isMultiplier = "true";
                    return new String[]{String.valueOf(chart), isMultiplier, event.get("bpmMultiplier").toString()};
                } else {
                    return new String[]{String.valueOf(chart), isMultiplier, event.get("beatsPerMinute").toString()};
                }
            }
        }
        return null;
    }

    public List<JSONObject> getAllEvents(String event) throws JSONException {
        List<JSONObject> eventList = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            if (events.getJSONObject(i).get("eventType").equals(event)) {
            eventList.add(events.getJSONObject(i));
            }
        }
        return eventList;
    }

    public List<JSONObject> getChartEvents(int chart) throws JSONException {
        JSONObject eventObject;
        List<JSONObject> chartEvents = new ArrayList<>();
        for (int a = 0; a < events.size(); a++) {
            eventObject = (JSONObject) events.get(a);
            if (eventObject.getIntValue("floor") == chart) {
                chartEvents.add(eventObject);
            }
        }
        return chartEvents;
    }

    public List<JSONObject> getEvents(int chart, String event) throws JSONException {
        List<JSONObject> eventList = new ArrayList<>();
        for (JSONObject jsonObject : getChartEvents(chart)) {
            if (jsonObject.get("eventType").equals(event)) {
                if (hasEvent(chart, jsonObject.get("eventType").toString())) {
                    eventList.add(jsonObject);
                }
            }
        }
        return eventList;
    }

    public boolean hasEvent(int chart, String event) throws JSONException {
        JSONObject eventObject;
        for (int a = 0; a < events.size(); a++) {
            eventObject = events.getJSONObject(a);
            if (eventObject.getIntValue("floor") == chart && eventObject.getString("eventType").equals(event)) {
                return true;
            }
        }
        return false;
    }

    public void removeAllEvent(String event, boolean isDecoration) throws JSONException {
        JSONObject eventObject;
        if (isDecoration) {
            for (int i = 0; i < decorations.size(); i++) {
                eventObject = (JSONObject) decorations.get(i);
                if (eventObject.get("eventType").equals(event)) {
                    decorations.remove(i);
                    i--;
                }
            }
        } else {
            for (int i = 0; i < events.size(); i++) {
                eventObject = (JSONObject) events.get(i);
                if (eventObject.get("eventType").equals(event)) {
                    events.remove(i);
                    i--;
                }
            }
        }
    }

    public int getEventIndex(int chart, String event) {
        JSONObject eventObject;
        for (int a = 0; a < events.size(); a++) {
            eventObject = events.getJSONObject(a);
            if (eventObject.getIntValue("floor") == chart && eventObject.get("eventType").equals(event)) {
                return a;
            }
        }
        return 0;
    }

    public void saveFile(String filePath) throws JSONException, IOException {
        File file = new File(currentLevelFile.replace(".adofai", "-mod.adofai"));
        if (filePath != null) {
            file = new File(filePath);
        }
        writeJSONToFile(level, file);
    }

    public static void writeJSONToFile(JSONObject JSONString, File filePath) throws IOException, JSONException {
        FileWriter writer = new FileWriter(filePath);
        writer.write(JSONString.toString());
        writer.close();
    }

    public static void writeJSONToFile(JSONArray JSONString, File filePath) throws IOException, JSONException {
        FileWriter writer = new FileWriter(filePath);
        writer.write(JSONString.toString());
        writer.close();
    }

    enum TileAngle {

        _0('R', 0, false),
        _15('p', 15, false),
        _30('J', 30, false),
        _45('E', 45, false),
        _60('T', 60, false),
        _75('o', 75, false),
        _90('U', 90, false),
        _105('q', 105, false),
        _120('G', 120, false),
        _135('Q', 135, false),
        _150('H', 150, false),
        _165('W', 165, false),
        _180('L', 180, false),
        _195('x', 195, false),
        _210('N', 210, false),
        _225('Z', 225, false),
        _240('F', 240, false),
        _255('V', 255, false),
        _270('D', 270, false),
        _285('Y', 285, false),
        _300('B', 300, false),
        _315('C', 315, false),
        _330('M', 330, false),
        _345('A', 345, false),
        _5('5', 108, true),
        _6('6', 252, true),
        _7('7', 900.0f / 7.0f, true),
        _8('8', 360 - 900.0f / 7.0f, true),
        R60('t', 60, true),
        R120('h', 120, true),
        R240('j', 240, true),
        R300('y', 300, true),
        NONE('!', 999, true);

        public final char charCode;
        public final float angle;
        public final boolean relative;

        public static final Map<Character, TileAngle> angleCharMap = new HashMap<>();

        static {
            for (TileAngle value : TileAngle.values()) angleCharMap.put(value.charCode, value);
        }

        TileAngle(char charCode, float angle, boolean relative) {
            this.charCode = charCode;
            this.angle = angle;
            this.relative = relative;
        }
    }
}
