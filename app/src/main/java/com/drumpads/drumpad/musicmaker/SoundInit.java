package com.drumpads.drumpad.musicmaker;

public class SoundInit {
    private int id;
    private long time;
    private String src;

    public SoundInit (String src, long time) {
        this.id = id;
        this.time = time;
        this.src = src;
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

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }
}
