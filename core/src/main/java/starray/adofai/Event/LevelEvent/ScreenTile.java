package starray.adofai.Event.LevelEvent;

import starray.adofai.Event.AngleOffset;
import starray.adofai.Event.BasicEvent;
import starray.adofai.Event.EventTag;
import starray.adofai.Event.EventType;
import starray.adofai.Event.LevelEventEnum.Vector2;

public class ScreenTile implements BasicEvent, AngleOffset, EventTag {

    private int duration;
    private String ease;
    private String eventTag;
    private Vector2 tile;
    private double angleOffset;

    private int floor;

    public ScreenTile(int duration, String ease, String eventTag, Vector2 tile, double angleOffset, int floor) {
        this.duration = duration;
        this.ease = ease;
        this.eventTag = eventTag;
        this.tile = tile;
        this.angleOffset = angleOffset;
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

    public Vector2 getTile() {
        return tile;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public EventType getEventType() {
        return EventType.ScreenTile;
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
