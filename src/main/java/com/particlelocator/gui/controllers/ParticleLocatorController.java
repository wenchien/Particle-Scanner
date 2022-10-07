package com.particlelocator.gui.controllers;

import com.particlelocator.gui.util.FileBrowsingSupplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParticleLocatorController {

    private FileBrowsingSupplier fbSupplier = FileBrowsingSupplier.getInstance();

    private ObservableList<String> pcfMaterialsList = FXCollections.observableArrayList();

    private ObservableList<String> gameinfoPathList = FXCollections.observableArrayList();

    private Path dmxConverter = Path.of(this.getClass().getResource("/dmxconvertutil/dmxconvert.exe").toURI());

    @FXML
    private CheckBox autoMoveCheckbox;

    @FXML
    private Button customProjectBrowse;

    @FXML
    private TextField customProjectFolderTextField;

    @FXML
    private Button gameInfoBrowse;

    @FXML
    private ListView<?> gameInfoListView;

    @FXML
    private TextField gameInfoTextField;

    @FXML
    private CheckBox generatePcfManifest;

    @FXML
    private ListView<?> materialListView;

    @FXML
    private Button pcfBrowse;

    @FXML
    private TextField pcfTextField;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button runBtn;

    @FXML
    private MenuItem saveToConfig;

    @FXML
    private TextField mapVersionTextField;

    public ParticleLocatorController() throws URISyntaxException {
    }

    @FXML
    private void initialize() {
        progressBar.setProgress(0);
        // run every "initFunc" methods to add extra functionality to each Javafx component for this class
        List.of(this.getClass().getDeclaredMethods()).stream().forEach(m -> {
            if (m.getName().startsWith("initFunc")) {
                try {
                    m.invoke(this, null);
                } catch (IllegalAccessException ex) {
                    //LOGGER.error(ex.getMessage(), ex);
                } catch (InvocationTargetException ex) {
                    //LOGGER.error(ex.getMessage(), ex);
                }
            }
        });
    }

    // Convert binary DMX to Tex DMX
    // .pcf file by default is binary DMX  format
    @FXML
    private void runTask() {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                try {
                    int materialCount = pcfMaterialsList.size();
                    for (String materialPathStr : pcfMaterialsList) {
                        if (gameInfoTextField.getText() != null && gameInfoTextField.getText() != "") {
                            String gameInfoText = gameInfoTextField.getText();
                            String rootFolder = gameInfoText.substring(0, gameInfoText.lastIndexOf("\\"));
                            String customPorjectFolderStr = customProjectFolderTextField.getText();
                            // check if material already exists in the projectFolder path
                            if (new File(customPorjectFolderStr + materialPathStr).exists()) {
                                continue;
                            } else {
                                // then check if material can be found anywhere in the game search path
                                boolean isFound = false;
                                for (String gameInfoSearchPath : gameinfoPathList) {
                                    File searchFile = new File(rootFolder + "\\" + gameInfoSearchPath + "\\" + materialPathStr);
                                    if (searchFile.exists()) {
                                        // move it under project folder
                                        isFound = true;
                                        File searchFileDestination = new File(customPorjectFolderStr + "\\" + materialPathStr);
                                        Files.copy(searchFile.toPath(), searchFileDestination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        break;
                                    }
                                }

                                if (!isFound) {
                                    // report back as not found
                                }
                            }

                            // if material not found, add it to a list and report that list later. Ignore that material for now
                        }
                    }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

        };
        progressBar.progressProperty().bind(task.progressProperty());
    }

    private int executeDmxConverter(File sourceFile) throws URISyntaxException, IOException, InterruptedException {
        String commandLine = String.format("%s -i %s -ie binary -o %s -of tex", dmxConverter.toFile().getAbsolutePath(),
                            "\"" + sourceFile.getAbsolutePath() + "\"", "\"converted.dmx\""); // output to temp file
        Process process = Runtime.getRuntime().exec(commandLine);
        return process.waitFor();
    }

    private void initFuncPcfBrowse() {
        // LOGGER.info("Initializing Application Console menu");
        pcfBrowse.setOnAction(e -> {
            File selectedFile = fbSupplier.fileBrowsingConsumerFileReturn(pcfBrowse, pcfTextField, new FileChooser.ExtensionFilter("PCF Files", "*.pcf"));
            if (selectedFile.exists()) {
                // Populate
                pcfMaterialsList.clear();

                try {
                    int returnCode = executeDmxConverter(selectedFile);
                    Set<String> materialsPathSets = parseDmxForMaterial(new File(selectedFile.getPath() + "\\converted.dmx"));
                    pcfMaterialsList.addAll(materialsPathSets);
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    private void initFuncGameInfoBrowse() {
        // LOGGER.info("Initializing Application Console menu");
        gameInfoBrowse.setOnAction(e -> {
            File selectedFile = fbSupplier.fileBrowsingConsumerFileReturn(gameInfoBrowse, gameInfoTextField, new FileChooser.ExtensionFilter("TXT Files", "*.txt"));
            gameinfoPathList.clear();
            gameinfoPathList.addAll(populateListOfGamePath(selectedFile));
        });
    }

    private void initFuncCustomProjectFolderBrowse() {
        // LOGGER.info("Initializing Application Console menu");
        customProjectBrowse.setOnAction(e -> {
            fbSupplier.directoryBrowsingConsumer(customProjectBrowse, customProjectFolderTextField);
        });
    }


    private Set<String> parseDmxForMaterial(File file) {
        Set<String> materialsPathSets = Collections.emptySet();
        Path dmxFilePath = Paths.get(file.getAbsolutePath());
        try (Stream<String> dmxLines = Files.lines(dmxFilePath)) {
            materialsPathSets = dmxLines.filter(s -> s.contains("material"))
                    .map(s -> s.replaceAll("\t", "")
                            .replace("\"material\" \"string\"", "").trim())
                    .collect(Collectors.toSet());
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return materialsPathSets;
    }


    private Set<String> populateListOfGamePath(File file){
        Set<String> gameinfoPathSets = Collections.emptySet();
        try (FileInputStream fis = new FileInputStream(file); DataInputStream dis = new DataInputStream(fis)) {
            gameinfoPathSets = new HashSet<String>();
            String inputGameInfo = new String(dis.readAllBytes());
            int searchPathsIndex = inputGameInfo.indexOf("SearchPaths");
            String searchPathInfo = inputGameInfo.substring(inputGameInfo.indexOf("{", searchPathsIndex) + 1, inputGameInfo.indexOf("}", searchPathsIndex));
            // System.out.println(searchPathInfo);
            for (String gameInfoString : searchPathInfo.split("\t")){
                String path = gameInfoString.replaceAll("\n", "").replaceAll("\s", "");
                if(path.startsWith("csgo")) {
                    gameinfoPathSets.add(path);
                }
            }

        } catch(FileNotFoundException ex) {

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return gameinfoPathSets;
    }

}
