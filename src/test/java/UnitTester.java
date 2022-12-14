import com.particlelocator.gui.GuiDriver;
import com.particlelocator.gui.beans.ParticleManifest;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnitTester {

    @Test
    public void testManifestGenerate() {
        ParticleManifest pm = new ParticleManifest();
        Set<String> particleManiList = new HashSet<>();
        particleManiList.add("particles/seima.pcf");
        particleManiList.add("particles/seima2.pcf");
        particleManiList.add("particles/seima3.pcf");
        particleManiList.add("particles/seima4.pcf");
        pm.setParticleFileMap(particleManiList);
        // System.out.println(pm);
        try(OutputStream out = new FileOutputStream(new File("D:\\Steam\\steamapps\\common\\Counter-Strike Global Offensive\\csgo\\custom\\ze_fe8_sacred_stones\\maps\\test.txt"))) {
            out.write(pm.toString().getBytes(StandardCharsets.UTF_8));
        }catch(Exception ex) {

        }
    }

    @Test
    public void testParseVmt() throws IOException {
        File file = new File("D:\\Steam\\steamapps\\common\\Alien Swarm\\swarm\\materials\\particle\\particle_glow_06.vmt");
        Path vmtPath = Path.of(file.toURI());
        try (Stream<String> vmtLines = Files.lines(vmtPath)) {
            Set<String> vmts = vmtLines
                    .filter(s -> s.contains("$basetexture"))
                    .map(s -> s.replaceAll("\t", "")
                            .replaceAll("\s", "")
                            .replace("$basetexture", "")
                            .replaceAll("\"", "")
                            .toLowerCase())
                    .collect(Collectors.toSet());
            System.out.println(vmts);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    @Test
    public void testConverter() throws URISyntaxException, IOException, InterruptedException {
        //"C:\Users\88696\Desktop\dmxconvert.exe" -i %1 -ie binary -o "%~dpn1_Keyvalues2.dmx" -of tex
        File file = new File("D:\\Steam\\steamapps\\common\\Counter-Strike Global Offensive\\csgo\\custom\\ze_fe8_sacred_stones\\particles\\seima_particle_v2_copy.pcf");
        Path dmxConverter = Path.of(this.getClass().getResource("/dmxconvertutil/dmxconvert.exe").toURI());
        String commandLine = String.format("%s -i %s -ie binary -o %s -of tex", dmxConverter.toFile().getAbsolutePath(), "\"" + file.getAbsolutePath() + "\"", "\"" + file.getAbsolutePath() + ".dmx" + "\"");
        System.out.println(commandLine);
        Process process = Runtime.getRuntime().exec(commandLine);
        int returnCode = process.waitFor();
        System.out.println(returnCode);
    }

    @Test
    public void testDmxParsing() {
        File file = new File("D:\\Steam\\steamapps\\common\\Counter-Strike Global Offensive\\csgo\\custom\\ze_fe8_sacred_stones\\particles\\seima_pcf_2.dmx");
        Set<String> materialsPathSets = Collections.emptySet();
        Path dmxFilePath = Paths.get(file.getAbsolutePath());
        try (Stream<String> dmxLines = Files.lines(dmxFilePath)) {
            Set<String> dmxMaterialsLineList = dmxLines.filter(s -> s.contains("material"))
                                                        .map(s -> s.replaceAll("\t", "")
                                                                .replace("\"material\" \"string\"", "").trim())
                                                        .collect(Collectors.toSet());
            System.out.println(dmxMaterialsLineList);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    @Test
    public void testGameInfoParsing() {
        File file = new File("D:\\Steam\\steamapps\\common\\Counter-Strike Global Offensive\\csgo\\gameinfo.txt");
        Set<String> gameinfoPathSets = new HashSet<String>();
        try (FileInputStream fis = new FileInputStream(file); DataInputStream dis = new DataInputStream(fis)) {
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

    }
}
