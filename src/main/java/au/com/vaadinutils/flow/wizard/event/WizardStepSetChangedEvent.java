package au.com.vaadinutils.flow.wizard.event;

import au.com.vaadinutils.flow.wizard.Wizard;

public class WizardStepSetChangedEvent extends AbstractWizardEvent {

    private static final long serialVersionUID = 8327572270357966942L;

    public WizardStepSetChangedEvent(Wizard source, boolean fromClient) {
        super(source, fromClient);
    }
}
