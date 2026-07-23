package au.com.vaadinutils.flow.wizard.event;

import au.com.vaadinutils.flow.wizard.Wizard;

public class WizardCancelledEvent extends AbstractWizardEvent {

    private static final long serialVersionUID = 6021180872252472173L;

    public WizardCancelledEvent(Wizard source, boolean fromClient) {
        super(source, fromClient);
    }
}
