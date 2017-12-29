package svmod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

public class Checksum {
    
    private String checksum;
    private Path file;
    
    public Checksum(Path fullpath) {
        file = fullpath;
        if (Files.notExists(file)) {
            System.out.println("Error: Cannot generate checksum of " + file.toString() + " because does not exist");
            checksum = "does not exist";
            file = null;
            return;
        }
        try {
            byte[] fileData = Files.readAllBytes(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(fileData);
            checksum = DatatypeConverter.printHexBinary(digest);
        } catch (NoSuchAlgorithmException | IOException ex) {
            Logger.getLogger(Checksum.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public boolean equals(Checksum c) {
        return (file != null && c.getFile() != null && checksum.equals(c.getChecksum()));
    }
    
    public String getChecksum() {
        return checksum;   
    }
    
    public Path getFile() {
        return file;   
    }

    
}
