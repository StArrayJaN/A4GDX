package starray.adofai.Event.LevelEvent;

import starray.adofai.Event.AngleOffset;
import starray.adofai.Event.BasicEvent;
import starray.adofai.Event.EventType;

public class SetFrameRate implements BasicEvent, AngleOffset{

    private final int frameRate;
    private final double angleOffset;
    private int floor;
    private boolean enabled;

    public SetFrameRate(int frameRate, double angleOffset, int floor, boolean enabled) {
        this.frameRate = frameRate;
        this.angleOffset = angleOffset;
        this.floor = floor;
        this.enabled = enabled;
    }

    public int getFrameRate() {
        return frameRate;
    }

    @Override
    public double getAngleOffset() {
        return angleOffset;
    }

    @Override
    public EventType getEventType() {
        return EventType.SetFrameRate;
    }

    @Override
    public int getFloor() {
        return floor;
    }

    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
