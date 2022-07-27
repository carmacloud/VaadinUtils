package au.com.vaadinutils.flow.errorhandling;

import java.io.ByteArrayOutputStream;

public class DefaultErrorSettings implements ErrorSettings {

    @Override
    public String getSupportCompanyName() {
        return "Support company name not set, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
    }

    @Override
    public String getSystemName() {
        return "System name not set, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
    }

    @Override
    public String getUserName() {
        return "User not known, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
    }

    @Override
    public String getTargetEmailAddress() {
        return "Target email address not set, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
    }

    @Override
    public void sendEmail(String emailAddress, String subject, String bodyText, ByteArrayOutputStream attachment,
            String filename, String MIMEType) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getBuildVersion() {
        return "Target email address not set, call ErrorSettingsFactory.setErrorSettings and implement appropriate settings";
    }

    @Override
    public String getUserEmail() {
        return "User email address not set, call ErrorSettingsFactory.getUserEmail and implement appropriate settings";

    }

    @Override
    public String getCustomHashString(String stackTraceAsString) {
        return stackTraceAsString;
    }

}
