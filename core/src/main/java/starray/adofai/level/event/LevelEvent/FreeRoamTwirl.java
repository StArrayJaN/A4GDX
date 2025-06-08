package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;
import starray.adofai.level.event.LevelEventEnum.Vector2;

public class FreeRoamTwirl implements BasicEvent{

    private Vector2 position;
    private int floor;

    public FreeRoamTwirl(Vector2 position, int floor) {
        this.position = position;
        this.floor = floor;
    }

    public EventType getEventType() {
        return EventType.FreeRoamTwirl;
    }

    public Vector2 getPosition() {
        return position;
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
