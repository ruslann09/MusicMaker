package com.drumpads.drumpad.musicmaker.presets;

public class TutorialInfo {
    private int best, last;
    private int startPos, endPos;

    public TutorialInfo(int best, int last, int startPos, int endPos) {
        this.best = best;
        this.last = last;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public int getBest() {
        return best;
    }

    public void setBest(int best) {
        this.best = best;
    }

    public int getLast() {
        return last;
    }

    public void setLast(int last) {
        this.last = last;
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }
}