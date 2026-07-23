package au.com.vaadinutils.flow.errorhandling;

import java.io.ByteArrayOutputStream;

public interface ErrorSettings {

    String getSupportCompanyName();

    String getSystemName();

    String getUserName();

    String getTargetEmailAddress();

    public void sendEmail(String emailAddress, String subject, String bodyText, ByteArrayOutputStream attachment,
            String filename, String MIMEType);

    String getBuildVersion();

    String getUserEmail();

    String getCustomHashString(String stackTraceAsString);
}
