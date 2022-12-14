package com.particlelocator.gui.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.particlelocator.gui.beans.ConfigKey;
import com.particlelocator.gui.beans.Configuration;
import com.particlelocator.gui.beans.ParticleManifest;
import com.particlelocator.gui.util.FileBrowsingSupplier;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "unchecked"})
public class ParticleLocatorController {

    private FileBrowsingSupplier fbSupplier = FileBrowsingSupplier.getInstance();

    private ObservableList<String> pcfMaterialsList = FXCollections.observableArrayList();

    private ObservableList<String> gameinfoPathList = FXCollections.observableArrayList();

    private final Path dmxConverter;

    @FXML
    private CheckBox autoMoveCheckbox;

    @FXML
    private Button customProjectBrowse;

    @FXML
    private TextField customProjectFolderTextField;

    @FXML
    private Button gameInfoBrowse;

    @FXML
    private ListView<String> gameInfoListView;

    @FXML
    private TextField gameInfoTextField;

    @FXML
    private CheckBox generatePcfManifest;

    @FXML
    private ListView<String> materialListView;

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

    @FXML
    private MenuItem openConfig;

    @FXML
    private MenuItem clearAllFields;

    public ParticleLocatorController() throws URISyntaxException {
        dmxConverter = Path.of(this.getClass().getResource(String.valueOf(ConfigKey.DMXCONVERTER)).toURI());
    }

    @FXML
    private void initialize() {
        // Check if config exists
        if (null != Configuration.configMap) {
            // Pre-populate stuff
            Map<String, String> localConfigMap = Configuration.configMap;
            populateTextFieldsByConfig(localConfigMap);
        }

        progressBar.setProgress(0);
        gameInfoListView.setItems(gameinfoPathList);
        materialListView.setItems(pcfMaterialsList);

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

    private void initFuncLoadConfig() {
        openConfig.setOnAction(e -> {
            File selectedFile = fbSupplier.fileBrowsingConsumerFileReturn(openConfig, new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            Map<String, String> localConfigMap = Configuration.jsonConfigReader.fromJson(selectedFile.getAbsolutePath(), new TypeToken<Map<String, String>>() {}.getType());
            populateTextFieldsByConfig(localConfigMap);
        });
    }

    private void initFuncSaveConfig() {
        saveToConfig.setOnAction(e -> {
            Map<String, String> configToSave = new HashMap<>();
            configToSave.put(ConfigKey.CUSTOMFOLDER.toString(), Objects.requireNonNull(customProjectFolderTextField.getText()));
            configToSave.put(ConfigKey.GAMEINFO.toString(), Objects.requireNonNull(gameInfoTextField.getText()));
            configToSave.put(ConfigKey.MAPNAME.toString(), Objects.requireNonNull(mapVersionTextField.getText()));
            configToSave.put(ConfigKey.PARTICLEFILE.toString(), Objects.requireNonNull(pcfTextField.getText()));
            String jsonString = Configuration.jsonConfigReader.toJson(configToSave);
            try {
                System.out.println(Configuration.configFileLoc);
                FileOutputStream fos = new FileOutputStream(new File(Configuration.configFileLoc));
                fos.write(jsonString.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {

            }

        });
    }

    private void initFuncRunButton() {
        runBtn.disableProperty().bind(
                Bindings.isEmpty(customProjectFolderTextField.textProperty())
                        .or(Bindings.isEmpty(gameInfoTextField.textProperty()))
                        .or(Bindings.isEmpty(pcfTextField.textProperty()))
                        .or(Bindings.isEmpty(mapVersionTextField.textProperty()))
        );

        runBtn.setOnAction(e -> {
            runTask();
        });
    }

    private void initFuncPcfBrowse() {
        // LOGGER.info("Initializing Application Console menu");
        pcfBrowse.setOnAction(e -> {
            File selectedFile = fbSupplier.fileBrowsingConsumerFileReturn(pcfBrowse, pcfTextField, new FileChooser.ExtensionFilter("PCF Files", "*.pcf"));
            if(null == selectedFile) {
                return;
            }
            if (selectedFile.exists()) {
                // Populate
                pcfMaterialsList.clear();

                try {
                    int returnCode = executeDmxConverter(selectedFile);
                    Set<String> materialsPathSets = parseDmxForMaterial(new File(selectedFile.getPath() + ".dmx"));
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
            if(null == selectedFile) {
                return;
            }
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

    @FXML
    private void runTask() {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                try {
                    int materialCount = pcfMaterialsList.size();
                    String gameInfoText = gameInfoTextField.getText();
                    String rootFolder = gameInfoText.substring(0, gameInfoText.lastIndexOf("\\"));
                    String customPorjectFolderStr = customProjectFolderTextField.getText();

                    if (generatePcfManifest.isSelected()) {
                        generatePcfManifest(new File(customPorjectFolderStr + "\\particles"),new File(customPorjectFolderStr), mapVersionTextField.getText());
                    }

                    if(!autoMoveCheckbox.isSelected()) {
                        return null;
                    }

                    for (String materialPathStr : pcfMaterialsList) {
                        if (null != gameInfoTextField.getText() && "" != gameInfoTextField.getText()) {
                            // check if material already exists in the projectFolder path
                            if (new File(customPorjectFolderStr + materialPathStr).exists()) {
                                // assuming vtfs already exists
                                continue;
                            } else {
                                // then check if material's vmt AND vtf can be found anywhere in the game search path
                                boolean isFound = false;
                                for (String gameInfoSearchPath : gameinfoPathList) {
                                    String searchPathStr = rootFolder + "\\" + gameInfoSearchPath + "\\materials\\";
                                    File searchFile = new File(searchPathStr + materialPathStr);
                                    List<String> vtfTexturesLists = parseVmtForVtf(searchFile);
                                    File searchVtfFile = new File(searchPathStr + vtfTexturesLists.get(0));
                                    if (searchFile.exists() && searchVtfFile.exists()) {
                                        // move it under project folder
                                        isFound = true;
                                        File searchFileDestination = new File(customPorjectFolderStr + "\\materials\\" + materialPathStr);
                                        File searchVtfFileDestination = new File(customPorjectFolderStr + "\\materials\\" + vtfTexturesLists.get(0));

                                        // Copy .vmt and .vtf to custom project folder
                                        Files.copy(searchFile.toPath(), searchFileDestination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        Files.copy(searchVtfFile.toPath(), searchFileDestination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        break;
                                    }
                                }

                                if (!isFound) {
                                    // report back as not found
                                    System.out.println(materialPathStr + " : not found");
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
                "\"" + sourceFile.getAbsolutePath() + "\"", "\"" + sourceFile.getAbsolutePath() + ".dmx\"");
        Process process = Runtime.getRuntime().exec(commandLine);
        return process.waitFor();
    }

    private List<String> parseVmtForVtf(File file) throws IOException {
        List<Function<String, String>> mapper = new ArrayList<>();
        mapper.add(s -> s.replaceAll("\t", ""));
        mapper.add(s -> s.replaceAll("\s", ""));
        mapper.add(s -> s.replace("$basetexture", ""));
        mapper.add(s -> s.replaceAll("\"", ""));
        mapper.add(String::toLowerCase);

        List<String> materialsVtfSets = new ArrayList<>((Collection<? extends String>) parseLinesToSet(file, s -> s.contains("$basetexture"), mapper, List.class));
        if (materialsVtfSets.isEmpty()) {
            throw new IOException("Cannot find any VTF given the VMT list");
        }
        return materialsVtfSets;
    }


    private Set<String> parseDmxForMaterial(File file) throws IOException {

        List<Function<String, String>> mapper = new ArrayList<>();
        mapper.add(s -> s.replaceAll("\t", ""));
        mapper.add(s -> s.replaceAll("\"material\" \"string\"", "").trim());

        Set<String> materialsPathSets = new HashSet<>((Collection<? extends String>) parseLinesToSet(file, s -> s.contains("material"), mapper, Set.class));

        if (materialsPathSets.isEmpty()) {
            throw new IOException("Invalid DMX. Regenerating the DMX file may resolve this issue");
        }

        return materialsPathSets;
    }

    private void generatePcfManifest(File particlesDirectory, File customProjectFolder, String fullMapName) {
        Set<String> particleFileSets = new HashSet<>();

        for (File particleFile : particlesDirectory.listFiles()) {
            String pcfPath = particleFile.getAbsolutePath();
            particleFileSets.add(pcfPath.substring(pcfPath.indexOf("particles")));
        }

        if (!particleFileSets.isEmpty()) {
            ParticleManifest pm = new ParticleManifest();
            pm.setParticleFileMap(particleFileSets);
            try(OutputStream out = new FileOutputStream(customProjectFolder.getAbsolutePath() + "maps\\" + fullMapName + "_particles.txt")) {
                out.write(pm.toString().getBytes(StandardCharsets.UTF_8));
            }catch(Exception ex) {

            }
        }
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

    private <T extends String> void populateTextFieldsByConfig(Map<T, T> configMap) {
        customProjectFolderTextField.setText(configMap.get(ConfigKey.CUSTOMFOLDER.toString()));
        gameInfoTextField.setText(configMap.get(ConfigKey.GAMEINFO.toString()));
        mapVersionTextField.setText(configMap.get(ConfigKey.MAPNAME.toString()));
        pcfTextField.setText(configMap.get(ConfigKey.PARTICLEFILE.toString()));
    }

    private <T> Collection<?> parseLinesToSet(File source, Predicate<String> stringFilterPredicate
            , List<Function<String, String>> mappers, Class<T> collection) {
        Path sourcePath = Path.of(source.toURI());
        Collection<?> collectedResults = null;
        try (Stream<String> lines = Files.lines(sourcePath)) {
            Stream<String> processedLines =  lines
                    .filter(stringFilterPredicate)
                    .map(s -> {
                        String result = s;
                        for(Function<String, String> mapper : mappers) {
                            result = mapper.apply(result);
                        }
                        return result;
                    });
                    if (collection.equals(List.class)) {
                        collectedResults = processedLines.collect(Collectors.toList());
                        return collectedResults;
                    }
                    if (collection.equals(Set.class)) {
                        collectedResults = processedLines.collect(Collectors.toSet());
                        return collectedResults;
                    }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
