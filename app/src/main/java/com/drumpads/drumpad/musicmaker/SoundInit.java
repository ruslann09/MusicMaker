package com.drumpads.drumpad.musicmaker;

public class SoundInit {
    private int id;
    private long time;

    public SoundInit (int id, long time) {
        this.id = id;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
