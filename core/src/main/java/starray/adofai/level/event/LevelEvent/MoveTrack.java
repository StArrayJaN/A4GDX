package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.AngleOffset;
import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventTag;
import starray.adofai.level.event.EventType;
import starray.adofai.level.event.LevelEventEnum.Vector2;

public class MoveTrack implements BasicEvent, AngleOffset, EventTag {

    private int duration;
    private String ease;
    private String eventTag;
    private Vector2 startTile;
    private boolean maxVfxOnly;
    private double angleOffset;
    private Vector2 endTile;

    private int gapLength;
    private Vector2 positionOffset;
    private int floor;

    public MoveTrack(int duration, String ease, String eventTag, Vector2 startTile, boolean maxVfxOnly, double angleOffset, Vector2 endTile, int gapLength, Vector2 positionOffset, int floor) {
        this.duration = duration;
        this.ease = ease;
        this.eventTag = eventTag;
        this.startTile = startTile;
        this.maxVfxOnly = maxVfxOnly;
        this.angleOffset = angleOffset;
        this.endTile = endTile;
        this.gapLength = gapLength;
        this.positionOffset = positionOffset;
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

    public Vector2 getStartTile() {
        return startTile;
    }

    public boolean getMaxVfxOnly() {
        return maxVfxOnly;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public Vector2 getEndTile() {
        return endTile;
    }

    public EventType getEventType() {
        return EventType.MoveTrack;
    }

    public int getGapLength() {
        return gapLength;
    }

    public Vector2 getPositionOffset() {
        return positionOffset;
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
