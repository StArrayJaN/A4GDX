package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;

public class AutoPlayTiles implements BasicEvent {

    private boolean showStatusText;
    private boolean safetyTiles;
    private int floor;
    private boolean enabled;

    public AutoPlayTiles(boolean showStatusText, boolean safetyTiles, int floor, boolean enabled) {
        this.showStatusText = showStatusText;
        this.safetyTiles = safetyTiles;
        this.floor = floor;
        this.enabled = enabled;
    }

    public boolean getShowStatusText() {
        return showStatusText;
    }

    public boolean getSafetyTiles() {
        return safetyTiles;
    }

    public EventType getEventType() {
        return EventType.AutoPlayTiles;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        return;
    }
}
