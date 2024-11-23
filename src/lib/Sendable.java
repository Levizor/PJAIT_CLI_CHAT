package lib;

import java.time.LocalTime;

public abstract class Sendable {
    String timestamp;
    static int idCounter = 0;

    public Sendable(){
        setTime();
    }

    public void setTime() {
        LocalTime now = LocalTime.now();
        timestamp = now.getHour() + ":" + now.getMinute();
    }

    public String getTimestamp(){
        return timestamp;
    }
}
