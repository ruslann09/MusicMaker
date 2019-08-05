package com.drumpads.drumpad.musicmaker.presets;

public class TutorialRecord {
    private int[] step, prev, prevNext;
    private long timeToPress, factTime;
    private String soundPath, color;

    public TutorialRecord() {
    }

    public TutorialRecord(int[] step, int[] prev, int[] prevNext, long timeToPress, String soundPath, String color) {
        this.step = step;
        this.prev = prev;
        this.prevNext = prevNext;
        this.timeToPress = timeToPress;
        this.soundPath = soundPath;
        this.color = color;
    }

    public int[] getStep() {
        return step;
    }

    public void setStep(int[] step) {
        this.step = step;
    }

    public long getTimeToPress() {
        return timeToPress;
    }

    public void setTimeToPress(long timeToPress) {
        this.timeToPress = timeToPress;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSoundPath() {
        return soundPath;
    }

    public void setSoundPath(String soundPath) {
        this.soundPath = soundPath;
    }

    public int[] getPrevNext() {
        return prevNext;
    }

    public void setPrevNext(int[] last) {
        this.prevNext = last;
    }

    public int[] getPrev() {
        return prev;
    }

    public void setPrev(int[] prev) {
        this.prev = prev;
    }

    public long getFactTime() {
        return factTime;
    }

    public void setFactTime(long factTime) {
        this.factTime = factTime;
    }
}
