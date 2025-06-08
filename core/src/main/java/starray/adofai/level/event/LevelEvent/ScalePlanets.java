package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.AngleOffset;
import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventTag;
import starray.adofai.level.event.EventType;

public class ScalePlanets implements BasicEvent, AngleOffset, EventTag {

    private int duration;
    private String ease;
    private String eventTag;
    private String targetPlanet;
    private int scale;
    private double angleOffset;

    private int floor;

    public ScalePlanets(int duration, String ease, String eventTag, String targetPlanet, int scale, double angleOffset, int floor) {
        this.duration = duration;
        this.ease = ease;
        this.eventTag = eventTag;
        this.targetPlanet = targetPlanet;
        this.scale = scale;
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

    public String getTargetPlanet() {
        return targetPlanet;
    }

    public int getScale() {
        return scale;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public EventType getEventType() {
        return EventType.ScalePlanets;
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
