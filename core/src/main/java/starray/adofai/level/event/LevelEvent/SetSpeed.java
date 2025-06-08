package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.AngleOffset;
import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;

public class SetSpeed implements BasicEvent, AngleOffset {

    private int beatsPerMinute;
    private String speedType;
    private double angleOffset;
    private int bpmMultiplier;

    private int floor;

    public SetSpeed(int beatsPerMinute, String speedType, double angleOffset, int bpmMultiplier, int floor) {
        this.beatsPerMinute = beatsPerMinute;
        this.speedType = speedType;
        this.angleOffset = angleOffset;
        this.bpmMultiplier = bpmMultiplier;
        this.floor = floor;
    }

    public int getBeatsPerMinute() {
        return beatsPerMinute;
    }

    public String getSpeedType() {
        return speedType;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public int getBpmMultiplier() {
        return bpmMultiplier;
    }

    public EventType getEventType() {
        return EventType.SetSpeed;
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
