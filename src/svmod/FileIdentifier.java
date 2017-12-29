package svmod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileIdentifier {

    private static FileIdentifier instance = null;
    private static final Path WORKING_DIR = Paths.get(".").toAbsolutePath();
    private final static Gson gson = new Gson();
    private JsonObject metadata = null;

    protected FileIdentifier() {
        loadIdentifierFile(null);
    }

    public static FileIdentifier getInstance() {
        if (instance == null) {
            instance = new FileIdentifier();
        }
        return instance;
    }

    public void loadIdentifierFile(Path p) {
        if (p == null) {
            p = Paths.get(WORKING_DIR.toString(), "filenames.json");
        }
        if (Files.notExists(p)) {
            return;
        }
        String fileContents = "";
        List<String> contents = null;
        try {
            contents = Files.readAllLines(p);
        } catch (IOException ex) {
            Logger.getLogger(FileIdentifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (String l : contents) {
            fileContents += l;
        }
        try {
            metadata = gson.fromJson(fileContents, JsonObject.class);
        } catch (Exception e) {
        }
    }

    public String identify(Path s) {
        if (metadata == null) {
            return null;
        }
        String fileName = s.getFileName().toString();
        if (metadata.has(fileName)) {
            return metadata.get(fileName).getAsString();
        }
        return null;
    }

}
