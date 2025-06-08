package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;

public class ScaleRadius implements BasicEvent {

    private int scale;

    private int floor;

    public ScaleRadius(int scale, int floor) {
        this.scale = scale;
        this.floor = floor;
    }

    public int getScale() {
        return scale;
    }

    public EventType getEventType() {
        return EventType.ScaleRadius;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public boolean getEnabled(){
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        return;
    }

}
