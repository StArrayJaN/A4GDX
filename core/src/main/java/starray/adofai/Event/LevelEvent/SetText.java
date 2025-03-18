package starray.adofai.Event.LevelEvent;

import starray.adofai.Event.AngleOffset;
import starray.adofai.Event.BasicEvent;
import starray.adofai.Event.EventTag;
import starray.adofai.Event.EventType;

public class SetText implements BasicEvent, AngleOffset, EventTag {

    private String eventTag;
    private double angleOffset;

    private String tag;
    private int floor;
    private String decText;

    public SetText(String eventTag, double angleOffset, String tag, int floor, String decText) {
        this.eventTag = eventTag;
        this.angleOffset = angleOffset;
        this.tag = tag;
        this.floor = floor;
        this.decText = decText;
    }

    public String getEventTag() {
        return eventTag;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public EventType getEventType() {
        return EventType.SetText;
    }

    public String getTag() {
        return tag;
    }

    public int getFloor() {
        return floor;
    }

    public String getDecText() {
        return decText;
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
