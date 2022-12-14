package com.particlelocator.gui;

import com.particlelocator.gui.beans.ConfigKey;
import com.particlelocator.gui.controllers.ParticleLocatorController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;

public class GuiDriver extends Application {
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        FXMLLoader fxmlLoader = new FXMLLoader(GuiDriver.class.getResource(String.valueOf(ConfigKey.PARTICLECONTROLLER)));
        fxmlLoader.setController(new ParticleLocatorController());

        Stage pcfStage = fxmlLoader.load();
        pcfStage.getIcons().add(new Image(GuiDriver.class.getResourceAsStream(String.valueOf(ConfigKey.ICONNAME))));
        pcfStage.getScene().getStylesheets().add(GuiDriver.class.getResource(String.valueOf(ConfigKey.DARKTHEME)).toString());
        pcfStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}