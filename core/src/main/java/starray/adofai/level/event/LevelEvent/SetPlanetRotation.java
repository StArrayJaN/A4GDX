package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;

public class SetPlanetRotation implements BasicEvent {

    private String ease;

    private int floor;
    private String easePartBehavior;
    private int easeParts;

    public SetPlanetRotation(String ease, int floor, String easePartBehavior, int easeParts) {
        this.ease = ease;
        this.floor = floor;
        this.easePartBehavior = easePartBehavior;
        this.easeParts = easeParts;
    }

    public String getEase() {
        return ease;
    }

    public EventType getEventType() {
        return EventType.SetPlanetRotation;
    }

    public int getFloor() {
        return floor;
    }

    public String getEasePartBehavior() {
        return easePartBehavior;
    }

    public int getEaseParts() {
        return easeParts;
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
