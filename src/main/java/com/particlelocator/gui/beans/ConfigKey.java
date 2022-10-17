package com.particlelocator.gui.beans;

// Configuration string for internal Java class uses
public enum ConfigKey {
    CUSTOMFOLDER ("CustomProject"),
    GAMEINFO("GameInfoTxt"),
    MAPNAME("MapName"),
    PARTICLEFILE("ParticleFile"),
    ICONNAME("icons/icon.jpg"),
    PARTICLECONTROLLER("particle-locator-gui.fxml"),
    DARKTHEME("css/dark-theme.css"),
    DMXCONVERTER("/dmxconvertutil/dmxconvert.exe");


    private final String key;

    ConfigKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
