package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;

public class Hold implements BasicEvent {

    private int duration;

    private int floor;
    private int distanceMultiplier;
    private boolean landingAnimation;

    public Hold(int duration, int floor, int distanceMultiplier, boolean landingAnimation) {
        this.duration = duration;
        this.floor = floor;
        this.distanceMultiplier = distanceMultiplier;
        this.landingAnimation = landingAnimation;
    }

    public int getDuration() {
        return duration;
    }

    public EventType getEventType() {
        return EventType.Hold;
    }

    public int getFloor() {
        return floor;
    }

    public int getDistanceMultiplier() {
        return distanceMultiplier;
    }

    public boolean getLandingAnimation() {
        return landingAnimation;
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
