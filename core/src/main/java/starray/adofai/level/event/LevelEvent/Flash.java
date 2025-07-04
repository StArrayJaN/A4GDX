package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.AngleOffset;
import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventTag;
import starray.adofai.level.event.EventType;

public class Flash implements BasicEvent, AngleOffset, EventTag{

    private int duration;
    private String plane;
    private String ease;
    private String eventTag;
    private String endColor;
    private int endOpacity;
    private double angleOffset;

    private int floor;
    private int startOpacity;
    private String startColor;

    public Flash(int floor, int duration, String plane, String ease, String eventTag, String startColor, int startOpacity, String endColor, int endOpacity, double angleOffset) {
        this.floor = floor;
        this.duration = duration;
        this.plane = plane;
        this.ease = ease;
        this.eventTag = eventTag;
        this.startColor = startColor;
        this.endColor = endColor;
        this.endOpacity = endOpacity;
        this.angleOffset = angleOffset;
        this.startOpacity = startOpacity;
    }

    public int getDuration() {
        return duration;
    }

    public String getPlane() {
        return plane;
    }

    public String getEase() {
        return ease;
    }

    public String getEventTag() {
        return eventTag;
    }

    public String getEndColor() {
        return endColor;
    }

    public int getEndOpacity() {
        return endOpacity;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public EventType getEventType() {
        return EventType.Flash;
    }

    public int getFloor() {
        return floor;
    }

    public int getStartOpacity() {
        return startOpacity;
    }

    public String getStartColor() {
        return startColor;
    }

    public boolean getEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        return;
    }
}
