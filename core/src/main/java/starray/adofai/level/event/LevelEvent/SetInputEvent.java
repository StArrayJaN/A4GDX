package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.AngleOffset;
import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventTag;
import starray.adofai.level.event.EventType;

public class SetInputEvent implements BasicEvent, AngleOffset, EventTag {

    private String eventTag;
    private boolean ignoreInput;
    private double angleOffset;

    private String state;
    private int floor;
    private String target;
    private String targetEventTag;
    private boolean enabled;

    public SetInputEvent(String eventTag, boolean ignoreInput, double angleOffset, String state, int floor, String target, String targetEventTag, boolean enabled) {
        this.eventTag = eventTag;
        this.ignoreInput = ignoreInput;
        this.angleOffset = angleOffset;
        this.state = state;
        this.floor = floor;
        this.target = target;
        this.targetEventTag = targetEventTag;
        this.enabled = enabled;
    }

    public String getEventTag() {
        return eventTag;
    }

    public boolean getIgnoreInput() {
        return ignoreInput;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public EventType getEventType() {
        return EventType.SetInputEvent;
    }

    public String getState() {
        return state;
    }

    public int getFloor() {
        return floor;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetEventTag() {
        return targetEventTag;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
