package au.com.vaadinutils.flow.wizard.event;

import com.vaadin.flow.component.ComponentEvent;

import au.com.vaadinutils.flow.wizard.Wizard;

public class AbstractWizardEvent extends ComponentEvent<Wizard> {

    private static final long serialVersionUID = 5593764289204456275L;

    public AbstractWizardEvent(Wizard source, boolean fromClient) {
        super(source, fromClient);
    }
    
    /**
     * Returns the {@link Wizard} component that was the source of this event.
     * 
     * @return the source {@link Wizard} of this event.
     */
    public Wizard getWizard() {
        return getSource();
    }
}
