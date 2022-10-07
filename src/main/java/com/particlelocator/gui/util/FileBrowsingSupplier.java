package com.particlelocator.gui.util;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;

public class FileBrowsingSupplier {

    // private static Logger LOGGER = LoggerFactory.getLogger(FileBrowsingSupplier.class);

    private static final FileBrowsingSupplier INSTANCE = new FileBrowsingSupplier();

    private FileBrowsingSupplier() {}

    public static FileBrowsingSupplier getInstance() {
        return INSTANCE;
    }

    public void fileBrowsingConsumer(Node browseBtn, Node directoryTextField, @Nullable FileChooser.ExtensionFilter... filters) {
        Button btn = (Button)browseBtn;
        TextField txtField = (TextField) directoryTextField;
        FileChooser fileChooser = new FileChooser();
        Arrays.stream(filters).forEach(f -> fileChooser.getExtensionFilters().add(f));
        File selectedFile = fileChooser.showOpenDialog((Stage)btn.getScene().getWindow());
        //LOGGER.info(selectedFile != null && selectedFile.exists() ? "Selected file: " + selectedFile.getAbsolutePath() : "undefined");
        txtField.setText(selectedFile != null && selectedFile.exists() ? selectedFile.getAbsolutePath() : "undefined");
    }

    public File fileBrowsingConsumerFileReturn(Node browseBtn, Node directoryTextField, @Nullable FileChooser.ExtensionFilter... filters) {
        Button btn = (Button)browseBtn;
        TextField txtField = (TextField) directoryTextField;
        FileChooser fileChooser = new FileChooser();
        Arrays.stream(filters).forEach(f -> fileChooser.getExtensionFilters().add(f));
        File selectedFile = fileChooser.showOpenDialog((Stage)btn.getScene().getWindow());
        //LOGGER.info(selectedFile != null && selectedFile.exists() ? "Selected file: " + selectedFile.getAbsolutePath() : "undefined");
        txtField.setText(selectedFile != null && selectedFile.exists() ? selectedFile.getAbsolutePath() : "undefined");
        return selectedFile;
    }

    public void directoryBrowsingConsumer(Node browseBtn, Node directoryTextField) {
        Button btn = (Button)browseBtn;
        TextField txtField = (TextField) directoryTextField;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog((Stage)btn.getScene().getWindow());
        //LOGGER.info(selectedDirectory != null && selectedDirectory.exists() ? "Selected directory: " + selectedDirectory.getAbsolutePath() : "undefined");
        txtField.setText(selectedDirectory != null && selectedDirectory.exists() ? selectedDirectory.getAbsolutePath() : "undefined");
    }

}
