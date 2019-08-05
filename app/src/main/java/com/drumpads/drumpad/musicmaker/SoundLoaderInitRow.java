package com.drumpads.drumpad.musicmaker;

public class SoundLoaderInitRow {
    private String name, executorName, reference, path;
    private boolean isPremium, isDefault, isCustom, isDownloading;
    private String icon;

    public SoundLoaderInitRow(String path) {
        this.path = path;
    }

    public SoundLoaderInitRow(String name, String executorName, String reference, boolean isPremium) {
        this.name = name;
        this.executorName = executorName;
        this.reference = reference;
        this.isPremium = isPremium;
    }

    public SoundLoaderInitRow(String name, String executorName, String reference, boolean isPremium, String icon) {
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

    public String getIcon() {
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

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public void setPath (String path) {
        this.path = path;
    }

    public String getPath () {
        return path;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }
}
