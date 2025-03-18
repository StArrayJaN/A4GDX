package starray.adofai.Event.LevelEvent;

import starray.adofai.Event.AngleOffset;
import starray.adofai.Event.BasicEvent;
import starray.adofai.Event.EventTag;
import starray.adofai.Event.EventType;

public class HallOfMirrors implements BasicEvent, AngleOffset, EventTag {

    private String eventTag;
    private double angleOffset;

    private int floor;
    private boolean enabled;

    public HallOfMirrors(String eventTag, double angleOffset, int floor, boolean enabled) {
        this.eventTag = eventTag;
        this.angleOffset = angleOffset;
        this.floor = floor;
        this.enabled = enabled;
    }

    public String getEventTag() {
        return eventTag;
    }

    @Override
    public double getAngleOffset() {
        return angleOffset;
    }

    @Override
    public EventType getEventType() {
        return EventType.HallOfMirrors;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
