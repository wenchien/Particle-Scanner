package com.particlelocator.gui.beans;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

public class Configuration {

    public static final Gson jsonConfigReader = new GsonBuilder().setPrettyPrinting().create();

    public static Map<String, String> configMap = null;

    public static String configFileLoc;

    static {
        try {
            configFileLoc = Configuration.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
                        .getPath() + "config.json";
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println(configFileLoc);
        if (new File(configFileLoc).exists()) {
            System.out.println(configFileLoc);

            configMap = jsonConfigReader.fromJson(configFileLoc, new TypeToken<Map<String, String>>() {}.getType());
        }
    }
}
