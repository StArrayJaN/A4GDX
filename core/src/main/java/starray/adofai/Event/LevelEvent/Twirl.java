package starray.adofai.Event.LevelEvent;

import starray.adofai.Event.BasicEvent;
import starray.adofai.Event.EventType;

public class Twirl implements BasicEvent {


    private int floor;

    public Twirl(int floor) {
        this.floor = floor;
    }

    public EventType getEventType() {
        return EventType.Twirl;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public boolean getEnabled(){
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        return;
    }
}
