package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;

public class Bookmark implements BasicEvent {

    private int floor;

    public Bookmark(int floor) {
        this.floor = floor;
    }

    public EventType getEventType() {
        return EventType.Bookmark;
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
