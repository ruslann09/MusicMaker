package com.drumpads.drumpad.musicmaker;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class SoundLoaderInitRow {
    private String name, executorName, reference;
    private boolean isPremium;
    private Bitmap icon;

    public SoundLoaderInitRow(String name, String executorName, String reference, boolean isPremium) {
        this.name = name;
        this.executorName = executorName;
        this.reference = reference;
        this.isPremium = isPremium;
    }

    public SoundLoaderInitRow(String name, String executorName, String reference, boolean isPremium, Bitmap icon) {
        this.name = name;
        this.executorName = executorName;
        this.reference = reference;
        this.isPremium = isPremium;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getReference() {
        return reference;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }
}
