package au.com.vaadinutils.flow.jasper;

public enum AttachmentType {
    PDF("application/pdf", ".pdf"), CSV("text/csv", ".csv"), HTML("text/html", ".html"), EML("application/eml", ".eml"),
    ZIP("application/zip", ".zip"), TXT("text/plain", "txt"), GIF("image/gif", ".gif"), JPG("image/jpg", "jpg"),
    JPEG("image/jpeg", "jpeg"), PNG("image/png", "png"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");

    private final String type;
    private final String extension;

    AttachmentType(final String type, final String fileExtension) {
        this.type = type;
        extension = fileExtension;
    }

    @Override
    public String toString() {
        return type;
    }

    public String getFileExtension() {
        return extension;
    }

    public String getMIMETypeString() {
        return type;
    }
}