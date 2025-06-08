package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.AngleOffset;
import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventTag;
import starray.adofai.level.event.EventType;
import starray.adofai.level.event.LevelEventEnum.Vector2;

public class MoveDecorations implements BasicEvent, AngleOffset, EventTag {

    private int duration;
    private String ease;
    private String eventTag;
    private double angleOffset;

    private String tag;
    private Vector2 positionOffset;
    private int floor;
    private Vector2 parallaxOffset;

    public MoveDecorations(int duration, String ease, String eventTag, double angleOffset, String tag, Vector2 positionOffset, int floor, Vector2 parallaxOffset) {
        this.duration = duration;
        this.ease = ease;
        this.eventTag = eventTag;
        this.angleOffset = angleOffset;
        this.tag = tag;
        this.positionOffset = positionOffset;
        this.floor = floor;
        this.parallaxOffset = parallaxOffset;
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
        return EventType.MoveDecorations;
    }

    public String getTag() {
        return tag;
    }

    public Vector2 getPositionOffset() {
        return positionOffset;
    }

    public int getFloor() {
        return floor;
    }

    public Vector2 getParallaxOffset() {
        return parallaxOffset;
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
