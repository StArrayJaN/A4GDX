package starray.adofai.Event.LevelEvent;

import starray.adofai.Event.EventType;
import starray.adofai.Event.BasicEvent;

public class AnimateTrack implements BasicEvent{

    private int beatsAhead;
    private EventType eventType;
    private int floor;
    private int beatsBehind;
    private boolean enabled;

    public AnimateTrack(int beatsAhead, int floor, int beatsBehind) {
        this.beatsAhead = beatsAhead;
        this.floor = floor;
        this.beatsBehind = beatsBehind;
    }
    public int getBeatsAhead() {
        return beatsAhead;
    }

    public EventType getEventType() {
        return EventType.AnimateTrack;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public int getBeatsBehind() {
        return beatsBehind;
    }
}
