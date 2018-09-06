package com.drumpads.drumpad.musicmaker;

public class InitRecordItem {
    private String name, path;
    private long duration, creationTime;

    public InitRecordItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getDuration() {
        return duration;
    }

    public String getPath () {
        return path;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
}
