package starray.adofai.level.event.LevelEvent;

import starray.adofai.level.event.BasicEvent;
import starray.adofai.level.event.EventType;
import starray.adofai.level.event.LevelEventEnum.HitSound;

public class SetHitSound implements BasicEvent {

    private final int hitSoundVolume;
    private final int floor;
    private final HitSound hitSound;
    private final String gameSound;
    private boolean enabled;

    public SetHitSound(int hitSoundVolume, String eventType, int floor, HitSound hitSound, String gameSound,boolean enabled) {
        this.hitSoundVolume = hitSoundVolume;
        this.floor = floor;
        this.hitSound = hitSound;
        this.gameSound = gameSound;
        this.enabled = enabled;
    }

    public int getHitSoundVolume() {
        return hitSoundVolume;
    }

    @Override
    public EventType getEventType() {
        return EventType.SetHitSound;
    }

    @Override
    public int getFloor() {
        return floor;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    public String getGameSound() {
        return gameSound;
    }

    public HitSound getHitSound() {
        return hitSound;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
