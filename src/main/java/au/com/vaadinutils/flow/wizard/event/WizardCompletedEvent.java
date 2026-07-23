package au.com.vaadinutils.flow.wizard.event;

import au.com.vaadinutils.flow.wizard.Wizard;

public class WizardCompletedEvent extends AbstractWizardEvent {

    private static final long serialVersionUID = 431597612721013346L;

    public WizardCompletedEvent(Wizard source, boolean fromClient) {
        super(source, fromClient);
    }

}
