package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.AngleOffset;
import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventTag;
import starray.adofai.level.event.EventType;
import starray.adofai.level.event.LevelEventEnum.Vector2;

public class MoveCamera implements BasicEvent, AngleOffset, EventTag {

    private int duration;
    private String ease;
    private boolean dontDisable;
    private String eventTag;
    private String relativeTo;
    private double angleOffset;

    private Vector2 position;
    private int floor;
    private boolean minVfxOnly;

    public MoveCamera(int duration, String ease, boolean dontDisable, String eventTag, String relativeTo, double angleOffset, Vector2 position, int floor, boolean minVfxOnly) {
        this.duration = duration;
        this.ease = ease;
        this.dontDisable = dontDisable;
        this.eventTag = eventTag;
        this.relativeTo = relativeTo;
        this.angleOffset = angleOffset;
        this.position = position;
        this.floor = floor;
        this.minVfxOnly = minVfxOnly;
    }

    public int getDuration() {
        return duration;
    }

    public String getEase() {
        return ease;
    }

    public boolean getDontDisable() {
        return dontDisable;
    }

    public String getEventTag() {
        return eventTag;
    }

    public String getRelativeTo() {
        return relativeTo;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public EventType getEventType() {
        return EventType.MoveCamera;
    }

    public Vector2 getPosition() {
        return position;
    }

    public int getFloor() {
        return floor;
    }

    public boolean getMinVfxOnly() {
        return minVfxOnly;
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
