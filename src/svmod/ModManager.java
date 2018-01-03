package svmod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ModManager {

    private static ModManager instance = null;
    private static final Path WORKING_DIR = Paths.get(".").toAbsolutePath();
    private static Path modsDir = Paths.get(WORKING_DIR.toString(), "mods");
    private static Path backupDir = Paths.get(WORKING_DIR.toString(), "backup");

    private static ObservableList<Mod> modsList = FXCollections.observableArrayList();

    protected ModManager() {
        if (Files.notExists(modsDir)) {
            modsDir = Paths.get(".");
        }
        if (Files.notExists(backupDir)) {
            backupDir = Paths.get(".");
        }
        modsDir = modsDir.toAbsolutePath().normalize();
        backupDir = backupDir.toAbsolutePath().normalize();
        loadMods();
    }

    public static ModManager getInstance() {
        if (instance == null) {
            instance = new ModManager();
        }
        return instance;
    }

    public String loadMods() {
        modsList.clear();
        if (Files.notExists(modsDir)) {
            return "Error: Mods directory does not exist";
        }
        List<Path> modFolders = null;
        try {
            modFolders = Files.walk(modsDir, 1).filter(Files::isDirectory).collect(Collectors.toList());
        } catch (IOException ex) {
            Logger.getLogger(ModManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        modFolders.remove(0);
        if (modFolders.isEmpty()) {
            return "Error: no mods found";
        }
        for (Path nextMod : modFolders) {
            Mod m = new Mod(nextMod);
            modsList.add(m);
        }
        return "";
    }

    public Path getModDirectory() {
        return modsDir;
    }

    public Path getBackupDirectory() {
        return backupDir;
    }

    public void setModDirectory(String absPath) {
        modsDir = Paths.get(absPath).normalize();
    }

    public void setBackupDirectory(String absPath) {
        backupDir = Paths.get(absPath).normalize();
    }

    public ObservableList<Mod> getModList() {
        return modsList;
    }

    public String installFileFromMod(Path filepath, Mod mod) {
        String output = "";
        SVGameFiles svgf = SVGameFiles.getInstance();
        Path folder = mod.getFolderPath();
        Path fullpath = Paths.get(folder.toString(), filepath.toString());
        output += svgf.createBackup(filepath, backupDir) + "\n";
        output += svgf.installFile(fullpath) + "\n";
        return output;
    }

    public String installMod(Mod mod) {
        String output = "";
        SVGameFiles svgf = SVGameFiles.getInstance();
        LinkedList<Path> modFiles = mod.getModFiles();
        Path folder = mod.getFolderPath();
        for (Path filepath : modFiles) {
            Path fullpath = Paths.get(folder.toString(), filepath.toString());
            output += svgf.createBackup(filepath, backupDir) + "\n";
            output += svgf.installFile(fullpath) + "\n";
        }
        output += "Installed " + mod.getName();
        return output;
    }

    public String uninstallFileFromMod(Path filepath, Mod mod) {
        String output = "";
        SVGameFiles svgf = SVGameFiles.getInstance();
        Path backupPath = Paths.get(backupDir.toString(), filepath.toString());
        output += svgf.installFile(backupPath) + "\n";
        output += "Uninstalled " + filepath.toString();
        return output;
    }

    public String uninstallMod(Mod mod) {
        String output = "";
        SVGameFiles svgf = SVGameFiles.getInstance();
        LinkedList<Path> modFiles = mod.getModFiles();
        for (Path filepath : modFiles) {
            Path backupPath = Paths.get(backupDir.toString(), filepath.toString());
            output += svgf.installFile(backupPath) + "\n";
        }
        output += "Uninstalled " + mod.getName();
        return output;
    }

}
