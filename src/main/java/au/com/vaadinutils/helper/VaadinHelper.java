package au.com.vaadinutils.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VaadinHelper {

    private final static Logger logger = LogManager.getLogger();

    public static byte[] getResourceBytes(final String filePath) {
        final File file = new File(filePath);
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return fileContent;
        } catch (IOException e) {
            logger.error("File not found...");
            return null;
        }
    }
}
