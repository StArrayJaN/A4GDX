package starray.adofai.Event.LevelEvent;

import starray.adofai.Event.AngleOffset;
import starray.adofai.Event.BasicEvent;
import starray.adofai.Event.EventTag;
import starray.adofai.Event.EventType;

public class EmitParticle implements BasicEvent, AngleOffset, EventTag{

    private String eventTag;
    private int count;
    private double angleOffset;

    private String tag;
    private int floor;


    public EmitParticle(String tag, int floor, String eventTag, int count, double angleOffset) {
        this.tag = tag;
        this.floor = floor;
        this.eventTag = eventTag;
        this.count = count;
        this.angleOffset = angleOffset;
    }

    public String getEventTag() {
        return eventTag;
    }

    public int getCount() {
        return count;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public EventType getEventType() {
        return EventType.EmitParticle;
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
