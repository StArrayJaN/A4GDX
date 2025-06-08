package starray.adofai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Sounds {
    List<Double> noteTimes;
    List<Task> tasks = new ArrayList<>();
    /*Thread soundThread;*/
    public Sounds(List<Double> noteTimes) {
        this.noteTimes = noteTimes;
        prepare();
    }

    public void prepare() {
        for (int i = 0; i < noteTimes.size(); i++) {
            Task task = new Task();
            task.delay = noteTimes.get(i);
            task.sound = Gdx.audio.newSound(Gdx.files.internal("kick.wav"));
            tasks.add(task);
        }
    }

    public void start() {
        for (Task task: tasks) {
            task.run(true);
        }
    }

    private class Task {
        private double delay;
        private Sound sound;
        private void run(boolean background) {
            if (background) {
                new Thread(() -> {
                        double currentTime = System.nanoTime() / 1E6;
                        while (true) {
                            if((currentTime + delay) <= System.nanoTime() / 1E6) {
                                sound.play();
                                break;
                            }
                        }
                }).start();
            } else {
                double currentTime = System.nanoTime() / 1E6;
                while (true) {
                    if ((currentTime + delay) <= System.nanoTime() / 1E6) {
                        sound.play();
                        break;
                    }
                }
            }
        }
    }

}
