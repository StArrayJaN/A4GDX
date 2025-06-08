package starray.adofai.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

/**
 * 模仿 Unity 的 AudioSource API，专为短音效设计
 * 支持延迟播放、定时播放和快速单次播放
 */
public class AudioSource {
    private final Sound sound;
    private Task scheduledTask;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private float pan = 0.0f;
    private boolean isLooping = false;

    // 初始化音效
    public AudioSource(String soundPath) {
        this.sound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
    }

    public AudioSource(Sound sound) {
        this.sound = sound;
    }

    //=== 核心播放控制 ===//
    public long play() {
        long soundId = sound.play(volume, pitch, pan);
        sound.setLooping(soundId, isLooping);
        return soundId;
    }

    public void stop() {
        sound.stop();
        cancelScheduledPlay();
    }

    //=== 延迟和定时播放 ===//
    public void playDelayed(float delay) {
        final float delaySeconds = delay / 1000;
        cancelScheduledPlay();
        scheduledTask = Timer.schedule(new Task() {
            @Override
            public void run() {
                play();
            }
        }, delaySeconds);
    }

    public void playScheduled(double scheduledTimeInSeconds) {
        double currentTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000.0;
        double delay = Math.max(0, scheduledTimeInSeconds - currentTime);
        playDelayed((float) delay);
    }

    private void cancelScheduledPlay() {
        if (scheduledTask != null && !scheduledTask.isScheduled()) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
    }

    //=== 参数控制 ===//
    public void setVolume(float volume) {
        this.volume = MathUtils.clamp(volume, 0f, 1f);
    }

    public void setPitch(float pitch) {
        this.pitch = MathUtils.clamp(pitch, 0.5f, 2f);
    }

    public void setPan(float pan) {
        this.pan = MathUtils.clamp(pan, -1f, 1f);
    }

    public void setLooping(boolean looping) {
        this.isLooping = looping;
    }

    //=== 静态快捷方法 ===//
    public static void playOneShot(Sound sound) {
        sound.play();
    }

    public static void playOneShot(Sound sound, float volume) {
        sound.play(volume);
    }

    public static void playOneShot(Sound sound, float volume, float pitch, float pan) {
        sound.play(volume, pitch, pan);
    }

    //=== 资源清理 ===//
    public void dispose() {
        stop();
        sound.dispose();
    }
}
