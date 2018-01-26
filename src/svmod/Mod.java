package svmod;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mod {

    private final static String MODJSON = "mod.json";
    private final static Gson gson = new Gson();

    private JsonObject metadata = null;
    private String name = null;
    private String version = null;
    private LinkedList<String> authors = null;
    private String description = null;
    private Path folderPath = null;
    private LinkedList<Path> modFiles = null;
    private HashMap<Path, Path> previewFilesMap = null;
    private boolean valid = false;
    private int installStatus = 0; //0 = not installed, 1 = partially installed, 2 = fully installed

    public Mod(Path modFolder) {
        folderPath = modFolder;
        valid = false;
        load();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public LinkedList getAuthors() {
        return authors;
    }

    public String getDescription() {
        return description;
    }

    public Path getFolderPath() {
        return folderPath;
    }

    public LinkedList<Path> getModFiles() {
        return modFiles;
    }

    public HashMap<Path, Path> getPreviewFiles() {
        return previewFilesMap;
    }

    public boolean isValid() {
        return valid;
    }

    public int getInstallStatus() {
        updateInstallStatus();
        return installStatus;
    }

    @Override
    public String toString() {
        if (!this.isValid()) {
            return this.name + " (NEEDS FORMATTING)";
        }
        if (this.description.equals("")) {
            return this.name;
        }
        return this.name + ": " + this.description;
    }

    private void load() {
        Path modjsonFile = Paths.get(folderPath.toString(), MODJSON);
        if (Files.notExists(modjsonFile)) {
            name = folderPath.getFileName().toString();
            System.out.println("Cannot find " + MODJSON + " metadata file: Does not exist " + folderPath.toString());
            return;
        }
        String modJsonContents = "";

        try {
            List<String> contents = Files.readAllLines(modjsonFile);
            for (String l : contents) {
                modJsonContents += l;
            }
        } catch (IOException ex) {
            Logger.getLogger(Mod.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            metadata = gson.fromJson(modJsonContents, JsonObject.class);
        } catch (Exception e) {
            return;
        }

        if (metadata.has("name")) {
            name = metadata.get("name").getAsString();
        } else {
            name = "Untitled Mod";
        }
        if (metadata.has("version")) {
            version = metadata.get("version").getAsString();
        } else {
            version = "1.0.0";
        }
        if (metadata.has("description")) {
            description = metadata.get("description").getAsString();
        } else {
            description = "";
        }

        authors = new LinkedList<String>();
        Iterator authorsIter = null;
        try {
            authorsIter = metadata.get("authors").getAsJsonArray().iterator();
            while (authorsIter.hasNext()) {
                authors.add(((JsonPrimitive) authorsIter.next()).getAsString());
            }
        } catch (Exception e) {
            authors.add("Anonymous");
        }

        modFiles = new LinkedList<Path>();
        JsonObject modFilesJson = null;
        try {
            modFilesJson = metadata.get("copy_files").getAsJsonObject();
        } catch (Exception e) {
            return;
        }
        Set<String> modFileFolders = modFilesJson.keySet();
        Iterator modFileIter = modFileFolders.iterator();
        gatherPaths(modFileIter, modFiles, "copy_files");
        if (modFiles.isEmpty()) {
            return;
        }
        previewFilesMap = new HashMap<>();
        if (metadata.has("preview")) {
            try {
                LinkedList<Path> previewFiles = new LinkedList<Path>();
                JsonObject previewFilesJson = metadata.get("preview").getAsJsonObject();
                Set<String> previewFileFolders = previewFilesJson.keySet();
                Iterator previewFileIter = previewFileFolders.iterator();
                gatherPaths(previewFileIter, previewFiles, "preview");
                for (Path mp : modFiles) {
                    for (Path pp : previewFiles) {
                        if (removeExtension(mp.toString()).equals(removeExtension(pp.toString()))) {
                            previewFilesMap.put(mp, pp);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        valid = true;
    }

    private void updateInstallStatus() {
        if (!isValid()) {
            installStatus = 0;
            return;
        }
        Path svpath = SVGameFiles.getInstance().getGameFileLocation();
        int matches = 0;
        int total = 0;
        for (Path p : modFiles) {
            Path svFile = Paths.get(svpath.toString(), p.toString());
            Path modFile = Paths.get(folderPath.toString(), p.toString());
            Checksum svCheck = new Checksum(svFile);
            Checksum modCheck = new Checksum(modFile);
            if (svCheck.equals(modCheck)) {
                matches++;
            }
            total++;
            if (matches > 0 && total > matches) {
                break;
            }
        }
        if (matches == 0) {
            installStatus = 0;
        } else if (matches == total) {
            installStatus = 2;
        } else {
            installStatus = 1;
        }
    }

    private void gatherPaths(Iterator iter, LinkedList lst, String tag) {
        while (iter.hasNext()) {
            String folder = (String) iter.next();
            JsonArray filesToCopy = metadata.get(tag).getAsJsonObject().get(folder).getAsJsonArray();
            Iterator filesIterator = filesToCopy.iterator();
            while (filesIterator.hasNext()) {
                Path nextFile = Paths.get(folder, ((JsonPrimitive) filesIterator.next()).getAsString());
                if (Files.exists(Paths.get(folderPath.toString(), nextFile.toString()))) {
                    lst.add(nextFile);
                }
            }
        }
    }

    private String removeExtension(String file) {
        return file.replaceAll("\\.[^.]*$", "");
    }

    public static String processMalformedMod(Path p, String modName, String modAuthors) {
        p = p.toAbsolutePath();
        JsonObject modJsonFile = new JsonObject();

        String name = (modName.length() > 0) ? modName : p.getFileName().toString();
        modJsonFile.addProperty("name", name);
        modJsonFile.addProperty("version", "1.0.0");
        modJsonFile.addProperty("description", "Formatted by Ghoulean's SVModManager");
        JsonArray authors = new JsonArray();
        String[] allModAuthors = modAuthors.split(",");
        if (allModAuthors.length == 0) {
            authors.add("Anonymous");
        } else {
            for (String s : allModAuthors) {
                authors.add(s);
            }
        }
        modJsonFile.add("authors", authors);

        JsonObject copyFiles = new JsonObject();

        boolean bad = true;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(p)) {
            for (Path path : directoryStream) {
                path = path.getFileName();
                String destinedFolder = SVGameFiles.getInstance().findGameFile(path);
                if (destinedFolder != null) {
                    bad = false;
                    Path original = Paths.get(p.toString(), path.toString());
                    Path moveLoc = Paths.get(p.toString(), destinedFolder, path.toString());
                    Files.createDirectories(Paths.get(p.toString(), destinedFolder));
                    Files.copy(original, moveLoc);
                    Files.deleteIfExists(original);
                    if (!copyFiles.has(destinedFolder)) {
                        copyFiles.add(destinedFolder, new JsonArray());
                    }
                    copyFiles.getAsJsonArray(destinedFolder).add(path.toString());
                }
            }
        } catch (IOException ex) {
            return null;
        }
        if (bad) {
            return null;
        }
        modJsonFile.add("copy_files", copyFiles);
        try {
            Files.write(Paths.get(p.toString(), "mod.json"),
                    new Gson().toJson(modJsonFile).getBytes("utf-8"),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Mod.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Mod.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "yes";
    }

}
