package starray.adofai.libgdx;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import java.util.LinkedList;
import java.util.Queue;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import java.util.LinkedList;
import java.util.Queue;

public class SoundManager {
    private static final int MAX_CONCURRENT_SOUNDS = 20000;
    private static final float BASE_FREQUENCY = 100.0f; // 基准频率（Hz）
    private static final float MIN_PITCH = 1f;        // 最小音高倍数
    private static final float MAX_PITCH = 40000.0f;        // 最大音高倍数

    private final Queue<ActiveSound> soundQueue = new LinkedList<>();
    private long lastNanoTime; // 上次播放的纳秒时间戳

    private static class ActiveSound {
        final Sound sound;
        final long soundId;

        ActiveSound(Sound sound, long soundId) {
            this.sound = sound;
            this.soundId = soundId;
        }
    }

    public void play(Sound sound) {
        // 获取当前纳秒时间
        final long currentNano = System.nanoTime();

        // 计算瞬时频率（Hz）
        float currentFreq = calculateFrequency(currentNano);

        // 计算动态音高（线性比例）
        float pitch = MathUtils.clamp(
            currentFreq / BASE_FREQUENCY,
            MIN_PITCH,
            MAX_PITCH
        );

        // 播放并设置音效参数
        long soundId = sound.play();
        sound.setPitch(soundId, pitch);

        // 管理音效队列
        manageQueue(new ActiveSound(sound, soundId));
        lastNanoTime = currentNano;
    }

    private float calculateFrequency(long currentNano) {
        // 首次播放返回基准频率
        if (lastNanoTime == 0) return BASE_FREQUENCY;

        // 计算纳秒时间差
        long deltaNanos = currentNano - lastNanoTime;

        // 处理时间差为0的特殊情况（1纳秒保底）
        if (deltaNanos <= 0) deltaNanos = 1;

        // 转换为秒并计算频率：频率 = 1 / 间隔时间(秒)
        return (float) (1e9 / deltaNanos); // 1e9 = 1秒的纳秒数
    }

    private void manageQueue(ActiveSound newSound) {
        soundQueue.add(newSound);

        // 移除超限的最旧音效
        while (soundQueue.size() > MAX_CONCURRENT_SOUNDS) {
            ActiveSound oldest = soundQueue.poll();
            oldest.sound.stop(oldest.soundId);
        }
    }

    public void dispose() {
        while (!soundQueue.isEmpty()) {
            ActiveSound sound = soundQueue.poll();
            sound.sound.stop(sound.soundId);
        }
    }
}
