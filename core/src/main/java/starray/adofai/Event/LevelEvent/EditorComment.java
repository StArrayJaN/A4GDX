package starray.adofai.Event.LevelEvent;

import starray.adofai.Event.BasicEvent;
import starray.adofai.Event.EventType;

public class EditorComment implements BasicEvent {

    private String comment;

    private int floor;

    public EditorComment(String comment, int floor) {
        this.comment = comment;
        this.floor = floor;
    }

    public String getComment() {
        return comment;
    }

    public EventType getEventType() {
        return EventType.EditorComment;
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
