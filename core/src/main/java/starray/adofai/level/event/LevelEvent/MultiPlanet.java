package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;

public class MultiPlanet implements BasicEvent {

    private String planets;

    private int floor;

    public MultiPlanet(String planets, int floor) {
        this.planets = planets;
        this.floor = floor;
    }

    public String getPlanets() {
        return planets;
    }

    public EventType getEventType() {
        return EventType.MultiPlanet;
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
