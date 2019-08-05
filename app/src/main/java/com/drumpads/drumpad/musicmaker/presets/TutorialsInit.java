package com.drumpads.drumpad.musicmaker.presets;

import java.util.ArrayList;

public class TutorialsInit {
    private ArrayList<TutorialInfo> tutorialsInfo;

    public TutorialsInit(ArrayList<TutorialInfo> tutorialsInfo) {
        this.tutorialsInfo = tutorialsInfo;
    }

    public ArrayList<TutorialInfo> getTutorialsInfo() {
        return tutorialsInfo;
    }

    public void setTutorialsInfo(ArrayList<TutorialInfo> tutorialsInfo) {
        this.tutorialsInfo = tutorialsInfo;
    }
}
