package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.AngleOffset;
import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;
import starray.adofai.level.event.EventTag;

public class Bloom implements BasicEvent, EventTag, AngleOffset {

    private int intensity;
    private int duration;
    private String ease;
    private String eventTag;
    private String color;
    private double angleOffset;
    private int threshold;
    private int floor;


    public Bloom(int intensity, int duration, String ease, String eventTag, String color, int angleOffset, int threshold, int floor) {
        this.intensity = intensity;
        this.duration = duration;
        this.ease = ease;
        this.eventTag = eventTag;
        this.color = color;
        this.angleOffset = angleOffset;
        this.threshold = threshold;
        this.floor = floor;
    }

    public int getIntensity() {
        return intensity;
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

    public String getColor() {
        return color;
    }

    @Override
    public double getAngleOffset() {
        return angleOffset;
    }

    public int getThreshold() {
        return threshold;
    }

    public EventType getEventType() {
        return EventType.Bloom;
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
