package au.com.vaadinutils.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VaadinHelper {

    private final static Logger logger = LogManager.getLogger();

    // File helper
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
    
    // Dates
    public static Date convertToDate(final LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime convertToLocalDateTime(final Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
