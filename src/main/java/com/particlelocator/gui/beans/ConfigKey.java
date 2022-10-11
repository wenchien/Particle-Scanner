package com.particlelocator.gui.beans;

public enum ConfigKey {
    CUSTOMFOLDER ("CustomProject"),
    GAMEINFO("GameInfoTxt"),
    MAPNAME("MapName"),
    PARTICLEFILE("ParticleFile");

    private final String key;

    ConfigKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
