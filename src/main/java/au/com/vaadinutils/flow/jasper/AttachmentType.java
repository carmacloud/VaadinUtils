package au.com.vaadinutils.flow.jasper;

public enum AttachmentType {
    PDF("application/pdf", ".pdf"), CSV("text/csv", ".csv"), HTML("text/html", ".html"), EML("application/eml", ".eml"),
    ZIP("application/zip", ".zip"), TXT("text/plain", "txt");

    private String type;
    private String extension;

    AttachmentType(String type, String fileExtension) {
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