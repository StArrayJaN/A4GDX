package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;

public class Checkpoint implements BasicEvent{

    private int tileOffset;
    private int floor;


    public Checkpoint(int tileOffset, int floor) {
        this.tileOffset = tileOffset;
        this.floor = floor;
    }
    public int getTileOffset() {
        return tileOffset;
    }

    public EventType getEventType() {
        return EventType.Checkpoint;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }
    @Override
    public void setEnabled(boolean enabled) {
        return;
    }
}
