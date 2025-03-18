package starray.adofai.Event.LevelEvent;

import starray.adofai.Event.BasicEvent;
import starray.adofai.Event.EventType;

public class ScaleMargin implements BasicEvent {

    private int scale;

    private int floor;

    public ScaleMargin(int scale, int floor) {
        this.scale = scale;
        this.floor = floor;
    }

    public int getScale() {
        return scale;
    }

    public EventType getEventType() {
        return EventType.ScaleMargin;
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
