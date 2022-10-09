package com.particlelocator.gui.beans;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParticleManifest {

    private static final String header = "particles_manifest";

    private Set<String> particleFileMap = new HashSet<>();

    public String getHeader() {
        return header;
    }

    public Set<String> getParticleFileMap() {
        return particleFileMap;
    }

    public void setParticleFileMap(Set<String> particleFileMap) {
        this.particleFileMap = particleFileMap;
    }

    public ParticleManifest() {

    }

    public ParticleManifest(Set<String> particleFileMap) {
        this.particleFileMap = particleFileMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for(String fileStr : particleFileMap) {
            sb.append("\t\tfile\t" + "\"" + fileStr + "\"\n");
        }

        return header + "\n"
                + "{\n"
                + sb.toString()
                + "}";
    }

}
