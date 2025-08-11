package au.com.vaadinutils.flow.helper;

import java.util.Arrays;

import com.vaadin.flow.component.upload.UploadI18N;

public class CarmaI18NUpload extends UploadI18N {

    private static final long serialVersionUID = -8477528918272809218L;

    /**
     * Provides a default I18N configuration for the Upload.
     *
     * Use as a basis for customising individual texts.
     */
    public CarmaI18NUpload() {
        setDropFiles(new DropFiles().setOne("Drop file here").setMany("Drop files here"));
        setAddFiles(new AddFiles().setOne("Upload File...").setMany("Upload Files..."));
        setCancel("Cancel");
        setError(new Error().setTooManyFiles("Too Many Files.").setFileIsTooBig("File is Too Big.")
                .setIncorrectFileType("Incorrect File Type."));
        setUploading(new Uploading()
                .setStatus(new Uploading.Status().setConnecting("Connecting...").setStalled("Stalled")
                        .setProcessing("Processing File...").setHeld("Queued"))
                .setRemainingTime(new Uploading.RemainingTime().setPrefix("remaining time: ")
                        .setUnknown("unknown remaining time"))
                .setError(new Uploading.Error().setServerUnavailable("Upload failed, please try again later")
                        .setUnexpectedServerError("Upload failed due to server error")
                        .setForbidden("Upload forbidden")));
        setUnits(new Units().setSize(Arrays.asList("B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")));
    }
}