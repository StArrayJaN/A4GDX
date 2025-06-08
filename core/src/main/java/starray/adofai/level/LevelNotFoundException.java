package starray.adofai.level;

import java.io.IOException;

public class LevelNotFoundException extends IOException {
    public LevelNotFoundException(String message,Throwable cause) {
        super(message,cause);
    }
    public LevelNotFoundException(String message) {
        super(message);
    }
}
