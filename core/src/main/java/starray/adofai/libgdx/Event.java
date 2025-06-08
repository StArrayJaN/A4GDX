package starray.adofai.libgdx;

import starray.adofai.level.Level;

public interface Event {
    void onLoadDone(Level level);
    void onPlay(Level level);
}
