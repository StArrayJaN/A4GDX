package starray.adofai.level;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import starray.adofai.State;
import starray.adofai.libgdx.Tools;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class LevelUtils {

    public static ProcessListener processListener;
    public static List<Double> delayList = new ArrayList<>();

    public interface ProcessListener {
        void onProcessDone(String message, State state);
        void onProcessChange(String message, int progress);
    }

    public static List<Double> getNoteTimes(Level l)  {
        List<Float> angleDataList = l.getCharts();
        JSONArray levelEvents = l.events;
        int n = angleDataList.size() + 1;
        for (int i = 0; i < angleDataList.size(); i++) {
            if (angleDataList.get(i) == 999) {
                if (l.hasEvent(i, "SetSpeed")) {
                    JSONObject a = levelEvents.getJSONObject(l.getEventIndex(i, "SetSpeed"));
                    a.put("floor", a.getIntValue("floor") + 1);
                } else if (l.hasEvent(i, "Twirl") && l.hasEvent(i + 1, "Twirl"))
                {
                    int index = l.getEventIndex(i, "Twirl");
                    levelEvents.remove(index);
                    levelEvents.remove(index);
                } else if (l.hasEvent(i, "Twirl")&& !l.hasEvent(i + 1, "Twirl"))
                {
                    int index = l.getEventIndex(i, "Twirl");
                    JSONObject a = levelEvents.getJSONObject(index);
                    if (a != null)
                    {
                        a.put("floor",a.getIntValue("floor") + 1);
                    }
                }
            }
        }
        // 1. 定义结构体
        class NoteInfo {
            double angle;
            double bpm;
            int direction = 0; // 0:未定，1/-1:已定
            double extraHold = 0;
            boolean midr = false;
            int multiPlanet = -1;
        }

        List<NoteInfo> parsedChart = new ArrayList<>();
        int midrCount = 0;
        List<Integer> midrId = new ArrayList<>();
        // 2. 初步处理，获取轨道角度和中旋
        for (int i = 0; i < angleDataList.size(); i++) {
            double angleData = angleDataList.get(i);
            if (angleData == 999) {
                midrCount++;
                NoteInfo temp = parsedChart.get(i - midrCount);
                temp.midr = true;
                parsedChart.set(i - midrCount, temp);
                midrId.add(i - 1);
            } else {
                NoteInfo temp = new NoteInfo();
                temp.angle = fmod(angleData, 360);
                temp.bpm = Double.NaN; // unSet
                temp.direction = 0;
                temp.extraHold = 0;
                temp.midr = false;
                temp.multiPlanet = -1;
                parsedChart.add(temp);
            }
        }
        // 3. 事件应用
        double bpm = l.getBPM();
        double pitch = l.getPitch() / 100;
        for (int i = 0; i < levelEvents.size(); i++) {
            JSONObject o = levelEvents.getJSONObject(i);
            int tile = o.getIntValue("floor");
            String event = o.get("eventType").toString();
            tile -= upperBound(midrId.toArray(new Integer[0]), tile);
            if (tile < 0 || tile >= parsedChart.size()) continue;
            NoteInfo note = parsedChart.get(tile);
            if (event.equals("SetSpeed")) {
                if (o.get("speedType").equals("Multiplier")) {
                    bpm = o.getDouble("bpmMultiplier") * bpm;
                } else if (o.get("speedType").equals("Bpm")) {
                    bpm = o.getDouble("beatsPerMinute") * pitch;
                }
                note.bpm = bpm;
                parsedChart.set(tile, note);
            }
            if (event.equals("Twirl")) {
                note.direction = -1;
                parsedChart.set(tile, note);
            }
            if (event.equals("Pause")) {
                note.extraHold = o.getDouble("duration") / 2;
                parsedChart.set(tile, note);
            }
            if (event.equals("Hold")) {
                note.extraHold = o.getDouble("duration");
                parsedChart.set(tile, note);
            }
            if (event.equals("MultiPlanet")) {
                if (o.get("planets").equals("ThreePlanets")) {
                    note.multiPlanet = 1;
                } else {
                    note.multiPlanet = 0;
                }
                parsedChart.set(tile, note);
            }
        }
        // 4. 方向和bpm归一化
        double BPM = l.getBPM() * pitch;
        int direction = 1;
        for (int i = 0; i < parsedChart.size(); i++) {
            NoteInfo note = parsedChart.get(i);
            if (note.direction == -1) direction = -direction;
            note.direction = direction;
            if (Double.isNaN(note.bpm)) {
                note.bpm = BPM;
            } else {
                BPM = note.bpm;
            }
            parsedChart.set(i, note);
        }
        // 5. 计算noteTime
        List<Double> noteTime = new ArrayList<>();
        double curAngle = 0;
        double curBPM = l.getBPM();
        double curTime = 0;
        boolean isMultiPlanet = false;
        for (int i = 0; i < parsedChart.size(); i++) {
            NoteInfo o = parsedChart.get(i);
            curAngle = fmod(curAngle - 180, 360);
            curBPM = o.bpm;
            double destAngle = o.angle;
            double pAngle = 0;
            if (Math.abs(destAngle - curAngle) <= 0.001) {
                pAngle = 360;
            } else {
                pAngle = fmod((curAngle - destAngle) * o.direction, 360);
            }
            pAngle += o.extraHold * 360;
            double angleTemp = pAngle;
            if (isMultiPlanet) {
                if (pAngle > 60) pAngle -= 60;
                else pAngle += 300;
            }
            if (o.multiPlanet != -1) {
                if (o.multiPlanet == 1) {
                    isMultiPlanet = true;
                    if (pAngle > 60) pAngle -= 60;
                    else pAngle += 300;
                } else {
                    isMultiPlanet = false;
                    pAngle = angleTemp;
                }
            }
            curTime += angleToTime(pAngle, curBPM);
            curAngle = destAngle;
            if (o.midr) {
                curAngle = curAngle + 180;
            }
            noteTime.add(curTime);
        }
        return noteTime;
    }
/*
    public static void runMacro(List<Double> noteTime, String keyList) throws IOException {
        if (processListener != null) {
            SwingUtilities.invokeLater(() -> {
                processListener.onProcessChange("处理完成", 100);
                processListener.onProcessDone("""
                        处理完成，按W开始
                        按←和→来调整偏移
                        按Q退出""", State.FINISHED);
            });
        }
        StartMacro start = new StartMacro(noteTime.toArray(new Double[0]));
        start.setKeyList(keyList);
        try {
            start.startHook();
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }
    }*/

        private static int upperBound(Integer[] arr, int value) {
        int left = 0;
        int right = arr.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] >= value) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }

    //伪模运算
    public static double fmod(double a, double b) {
        double t = Math.floor(a / b);
        return a - b * t;
    }

    //看什么注释，看方法名字↓
    public static double angleToTime(double angle, double bpm) {
        return (angle / 180) * (60 / bpm) * 1000;
    }

    /*public static class StartMacro implements NativeKeyListener {

        Double[] bpmList;
        Robot bot;
        double offset = 0;
        String keyList = "A";
        Thread thread;
        boolean breaked;
        List<Integer> keys;

        public StartMacro(Double[] list) {
            bpmList = list;
            thread = getThread();
            try {
                bot = new Robot();
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
        }

        public void setKeyList(String keyList) {
            this.keyList = keyList;
            char[] keyChars = keyList.toCharArray();
            keys = new ArrayList<>();
            for (char c : keyChars) {
                keys.add(KeyEvent.getExtendedKeyCodeForChar(c));
            }
        }

        public void startHook() throws NativeHookException {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        }

        public void stopHook() {
            breaked = true;
            GlobalScreen.removeNativeKeyListener(this);
        }

        public Thread getThread() {
            return new Thread(() -> {
                int keyIndex = 0;
                double start = currentTime();
                int events = 1;
                while (events < bpmList.length) {
                    double cur = currentTime();
                    double timeMilliseconds = (cur - start) + bpmList[0];
                    while (events < bpmList.length && bpmList[events] + offset <= timeMilliseconds) {
                        //根据bpm计算延迟
                        if (keyIndex >= keys.size()) keyIndex = 0;
                        bot.keyPress(keys.get(keyIndex));
                        bot.keyRelease(keys.get(keyIndex));
                        if (Main.enableConsole) {
                            System.out.printf("进度:%d/%d,BPM:%f,延迟:%fms,偏移:%f,键位:%s\n",events,
                                    bpmList.length,
                                    60000 / (bpmList[events] - bpmList[events -1]),
                                    bpmList[events] - bpmList[events -1],
                                    offset,
                                    KeyEvent.getKeyText(keys.get(keyIndex)));
                        }
                        events++;
                        keyIndex++;
                    }
                    if (breaked) break;
                }
                if (processListener != null) {
                    processListener.onProcessDone("已结束",State.FINISHED);
                }
            });
        }

        @Override
        public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
            switch (nativeEvent.getKeyCode()) {
                case NativeKeyEvent.VC_W:
                    thread.start();
                    break;
                case NativeKeyEvent.VC_LEFT:
                    offset -= 5;
                    break;
                case NativeKeyEvent.VC_RIGHT:
                    offset += 5;
                    break;
                case NativeKeyEvent.VC_Q:
                    breaked = true;
                    thread.interrupt();
                    if (processListener != null) {
                        processListener.onProcessDone("已退出",State.STOPPED);
                    }
                    break;
            }
        }
    }
*/
    public static List<Double> genericDelayTable(Double[] bpmList) {
        List<Double> delayTable = new ArrayList<>();
        double start = Tools.currentTime();
        int events = 1;
        while (events < bpmList.length) {
            double cur = Tools.currentTime();
            double timeMilliseconds = (cur - start) + bpmList[0];
            System.out.println(bpmList[events]);
            delayTable.add(timeMilliseconds);
            events++;
        }
        return delayTable;
    }

    private static void removeEffects(Level l)  {
        String[] effectEvents = {
                "MoveCamera",
                "MoveTrack",
                "AddDecoration",
                "CustomBackground",
                "Flash",
                "SetFilter",
                "HallOfMirrors",
                "ShakeScreen",
                "MoveDecorations",
                "ScaleRadius",
                "RepeatEvents",
                "Bloom",
                "ScreenScroll"
        };

        for (int i = 0; i < effectEvents.length; ++i) {
            l.removeAllEvent(effectEvents[i], true);
            l.removeAllEvent(effectEvents[i], false);
        }
    }

    public static void convertToOld(Level level) {
        String[] removeSettings = {"speedTrialAim",
                "trackTexture",
                "trackTextureScale",
                "showDefaultBGTile",
                "defaultBGTileColor",
                "defaultBGShapeType",
                "defaultBGShapeColor",
                "defaultTextColor",
                "defaultTextShadowColor",
                "congratsText",
                "perfectText",
                "imageSmoothing",
                "scalingRatio"};

        String[] newSettingValue = {"showDefaultBGIfNoImage",
                "separateCountdownTime",
                "separateCountdownTime",
                "seizureWarning",
                "lockRot",
                "loopBG",
                "scalingRatio",
                "pulseOnFloor",
                "startCamLowVFX",
                "loopVideo",
                "floorIconOutlines",
                "stickToFloors"};
        String ntrue = "Enabled";
        String nfalse = "Disabled";
        for (int i = 0; i < newSettingValue.length; i++) {
            if (level.hasSetting(newSettingValue[i])) {
                if (newSettingValue[i].equals("scalingRatio")) {
                    level.setLevelSetting("unscaledSize", level.settings.getIntValue("scalingRatio"));
                }
                if (!newSettingValue[i].equals("scalingRatio") && level.getSetting(newSettingValue[i]).equals(true)) {
                    level.setLevelSetting(newSettingValue[i], ntrue);


                }
                if (!newSettingValue[i].equals("scalingRatio") && level.getSetting(newSettingValue[i]).equals(false)) {
                    level.setLevelSetting(newSettingValue[i], nfalse);
                }
            }
        }

        for (String reomveSetting : removeSettings) {
            level.removeLevelSetting(reomveSetting);
        }

        for (int i = 0; i < level.events.size(); ++i) {
            JSONObject o = level.events.getJSONObject(i);
            for (String key : o.keySet()) {
                try {
                    if (o.getBoolean(key)) {
                        o.put(key, "Enabled");
                    } else if (!o.getBoolean(key)) {
                        o.put(key, "Disabled");
                    }
                } catch (Exception err) {
                }
            }
            if (o.get("eventType").equals("ScalePlanets")) {
                o.remove("targetPlanet");
            }
        }

        for (int i = 0; i < level.decorations.size(); ++i) {
            JSONObject o = level.decorations.getJSONObject(i);
            for (String key : o.keySet()) {
                try {
                    if (o.getBoolean(key)) {
                        o.put(key, "Enabled");
                    } else if (!o.getBoolean(key)) {
                        o.put(key, "Disabled");
                    }
                } catch (Exception err) {
                }
            }
        }

        level.removeAllEvent("setFloorIcon", false);
        level.removeAllEvent("AddObject", false);
        level.removeAllEvent("MoveObject", false);
        level.setLevelSetting("version", 12);
    }
}

