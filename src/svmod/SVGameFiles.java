package svmod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SVGameFiles {

    private static SVGameFiles instance = null;

    private static final Path HOME_PATH = Paths.get(System.getProperty("user.home"));
    private static Path SV_PATH = Paths.get(HOME_PATH.toString(), "AppData", "LocalLow", "Cygames", "Shadowverse");
    private static final String[] GAME_FOLDERS = {"a", "b", "m", "s", "v"};

    protected SVGameFiles() {
    }

    public static SVGameFiles getInstance() {
        if (instance == null) {
            instance = new SVGameFiles();
        }
        return instance;
    }

    public String installFile(Path targetFile) {
        Path destinationFolder = targetFile.getParent();
        Path destination = Paths.get(SV_PATH.toString(), destinationFolder.getFileName().toString());
        if (Files.notExists(destination)) {
            return "Cannot find destination folder: Does not exist. " + destination.toString();
        }
        if (Files.notExists(targetFile)) {
            return "Cannot find target file: Does not exist. " + targetFile.toString();
        }
        Path destinationFile = Paths.get(destination.toString(), targetFile.getFileName().toString());
        if (Files.notExists(destinationFile)) {
            return "Cannot find destination file: Does not exist. " + destinationFile.toString();
        }
        try {
            Files.deleteIfExists(destinationFile);
        } catch (IOException ex) {
            Logger.getLogger(SVGameFiles.class.getName()).log(Level.SEVERE, null, ex);
            return "Unknown error";
        }
        try {
            Files.copy(targetFile, destinationFile);
        } catch (IOException ex) {
            Logger.getLogger(SVGameFiles.class.getName()).log(Level.SEVERE, null, ex);
            return "Unknown error";
        }
        return "Installed " + targetFile.toString();
    }

    public String deleteFile(Path targetFile) {
        Path destinationFolder = targetFile.getParent();
        Path destination = Paths.get(SV_PATH.toString(), destinationFolder.getFileName().toString());
        if (Files.notExists(destination)) {
            return "Cannot find destination folder: Does not exist. " + destination.toString();
        }
        Path destinationFile = Paths.get(destination.toString(), targetFile.getFileName().toString());
        try {
            Files.deleteIfExists(destinationFile);
        } catch (IOException ex) {
            Logger.getLogger(SVGameFiles.class.getName()).log(Level.SEVERE, null, ex);
            return "Unknown error";
        }
        return "Deleted " + targetFile.toString();
    }

    public String createBackup(Path targetFile, Path backupPath) {
        Path fullTargetPath = Paths.get(SV_PATH.toString(), targetFile.toString());
        if (!Files.exists(fullTargetPath)) {
            return "File to backup does not exist";
        }
        Path targetFolder = targetFile.getParent();
        Path backupSubfolder = Paths.get(backupPath.toString(), targetFolder.getFileName().toString());
        try {
            Files.createDirectories(backupSubfolder);
        } catch (IOException ex) {
            Logger.getLogger(SVGameFiles.class.getName()).log(Level.SEVERE, null, ex);
            return "Unknown error";
        }
        Path backupFile = Paths.get(backupSubfolder.toString(), targetFile.getFileName().toString());
        if (Files.exists(backupFile)) {
            return "Backup already exists";
        }
        try {
            Files.copy(fullTargetPath, backupFile);
        } catch (IOException ex) {
            Logger.getLogger(SVGameFiles.class.getName()).log(Level.SEVERE, null, ex);
            return "Unknown error";
        }
        return "Created backup of " + targetFile.toString();
    }

    public Path getGameFileLocation() {
        return SV_PATH;
    }

    public void setGameFileLocation(Path s) {
        SV_PATH = s;
    }

    public void setGameFileLocation(String s) {
        SV_PATH = Paths.get(s).normalize();
    }

    public String findGameFile(Path p) {
        for (String GAME_FOLDERS1 : GAME_FOLDERS) {
            Path test = Paths.get(SV_PATH.toString(), GAME_FOLDERS1, p.getFileName().toString());
            if (Files.exists(test)) {
                return GAME_FOLDERS1;
            }
        }
        return null;
    }
    
}
