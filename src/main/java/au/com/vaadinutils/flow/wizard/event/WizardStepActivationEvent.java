package au.com.vaadinutils.flow.wizard.event;

import au.com.vaadinutils.flow.wizard.Wizard;
import au.com.vaadinutils.flow.wizard.WizardStep;

public class WizardStepActivationEvent extends AbstractWizardEvent {

    private static final long serialVersionUID = 5298014833826937000L;
    private final WizardStep activatedStep;

    public WizardStepActivationEvent(Wizard source, boolean fromClient, WizardStep activatedStep) {
        super(source, fromClient);
        this.activatedStep = activatedStep;
    }

    /**
     * Returns the {@link WizardStep} that was the activated.
     * 
     * @return the activated {@link WizardStep}.
     */
    public WizardStep getActivatedStep() {
        return activatedStep;
    }
}
