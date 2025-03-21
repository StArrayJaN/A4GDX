package starray.adofai.libgdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Objects;

public class Tools {
    public static Vector2 getScreenCenter() {
        return new Vector2(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
    }

    public static Matrix4 getCen() {
        return new Matrix4().setToOrtho2D(getScreenCenter().x / 2, getScreenCenter().y / 2, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public static float calculateSpeed(float bpm) {
        if (bpm > 7200) {
            return 0.99f; // 超过15000直接返回最大速度
        } else {
            // 将bpm映射到0.01~1.0之间
            float speed = (0.01f + (bpm / 7200) * 0.99f);
            // 确保速度不低于最小值0.01
            return Math.max(0.01f, speed);
        }
    }

    public static String getOrExportResources(String path) throws IOException {
        String runtimePath = getRuntimePath();
        File file = new File(runtimePath, path);
        if (!file.exists()) {
            Files.write(file.toPath(), Tools.class.getResourceAsStream("/" + path).readAllBytes());
        }
        return file.getAbsolutePath();
    }


    public static String getRuntimePath() {
        String path = Objects.requireNonNull(Tools.class.getResource(Tools.class.getSimpleName() + ".class")).getPath();
        return new File(path.split("!")[0].replace("file:/", "")).getParent();
    }

    public static double currentTime() {
        return System.nanoTime() / 1E6;
    }

    public static void sleepRun(double ms, Runnable runnable, double offset) {
        double currentTime = currentTime();
        while (true) {
            if (currentTime + ms + offset < currentTime()) {
                runnable.run();
                break;
            }
        }
    }

    public static String getStackTrace(Throwable throwable) {
        var stackTrace = throwable.getStackTrace();
        var sb = new StringBuilder();
        sb.append(throwable).append("\n");
        for (var e : stackTrace) {
            sb.append("\t").append(e.toString()).append("\n");
        }
        return sb.toString();
    }

    public static void log(Object message) {
        if (message instanceof Throwable) {
            message = getStackTrace((Throwable) message);
        }
        if (Objects.requireNonNull(Gdx.app.getType()) == Application.ApplicationType.Android) {
            try {
                Method method = Class.forName("android.util.Log").getDeclaredMethod("e", String.class, String.class);
                method.invoke(null, "A4GDX", message.toString());
            } catch (Exception e) {
                log(e);
            }
        } else {
            System.err.println(message);
        }
    }

    public static boolean isAndroid() {
        return Objects.requireNonNull(Gdx.app.getType()) == Application.ApplicationType.Android;
    }

    public static void writeStreamToFile(File file, InputStream inputStream) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > -1) {
            fileOutputStream.write(buffer, 0, len);
        }
        fileOutputStream.flush();
        fileOutputStream.close();
    }
}
