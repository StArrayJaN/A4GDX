package starray.adofai.Event.LevelEvent;

import starray.adofai.Event.AngleOffset;
import starray.adofai.Event.BasicEvent;
import starray.adofai.Event.EventTag;
import starray.adofai.Event.EventType;

public class SetObject implements BasicEvent, AngleOffset, EventTag {

    private int duration;
    private String ease;
    private String eventTag;
    private double angleOffset;

    private String tag;
    private int floor;

    public SetObject(int duration, String ease, String eventTag, double angleOffset, String tag, int floor) {
        this.duration = duration;
        this.ease = ease;
        this.eventTag = eventTag;
        this.angleOffset = angleOffset;
        this.tag = tag;
        this.floor = floor;
    }

    public int getDuration() {
        return duration;
    }

    public String getEase() {
        return ease;
    }

    public String getEventTag() {
        return eventTag;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public EventType getEventType() {
        return EventType.SetObject;
    }

    public String getTag() {
        return tag;
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
