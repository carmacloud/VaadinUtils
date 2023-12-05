package au.com.vaadinutils.flow.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

import au.com.vaadinutils.flow.wizard.event.WizardCancelledEvent;
import au.com.vaadinutils.flow.wizard.event.WizardCompletedEvent;
import au.com.vaadinutils.flow.wizard.event.WizardProgressListener;
import au.com.vaadinutils.flow.wizard.event.WizardStepActivationEvent;
import au.com.vaadinutils.flow.wizard.event.WizardStepSetChangedEvent;

/**
 * This class implements a wizard style functionality that allows multiple
 * screens to be shown, one at a time.<br>
 * This class and all the other classes within this package have been adapted
 * from the original Wizards for Vaadin project, found here; <a href=
 * "https://vaadin.com/directory/component/wizards-for-vaadin">https://vaadin.com/directory/component/wizards-for-vaadin</a>
 */
public class Wizard extends VerticalLayout {

    private static final long serialVersionUID = 1752128905167662825L;
    protected final List<WizardStep> steps = new ArrayList<WizardStep>();
    protected final Map<String, WizardStep> idMap = new HashMap<String, WizardStep>(6);

    protected WizardStep currentStep;
    protected WizardStep lastCompletedStep;

    private int stepIndex = 1;

    protected HorizontalLayout footer;
    private VerticalLayout contentPanel;

    private Button nextButton;
    private Button backButton;
    private Button finishButton;
    private Button cancelButton;

    private WizardProgressBar header;

    public Wizard() {
        setSizeFull();
        setPadding(true);
        setMargin(false);
        contentPanel = new VerticalLayout();
        contentPanel.getStyle().set("border", "1px solid #E1E3E6");
        contentPanel.setSizeFull();

        initControlButtons();

        footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.add(cancelButton, backButton, nextButton, finishButton);
        footer.setJustifyContentMode(JustifyContentMode.END);
    
        this.add(contentPanel);

        add(footer);
        setAlignSelf(Alignment.END, footer);

        initDefaultHeader();
    }

    private void initControlButtons() {
        nextButton = new Button("Next");
        nextButton.setWidth("60px");
        nextButton.addClickListener(event -> {
            next();
        });

        backButton = new Button("Back");
        backButton.setWidth("60px");
        backButton.addClickListener(event -> {
            back();
        });

        finishButton = new Button("Finish");
        finishButton.setWidth("60px");
        finishButton.addClickListener(event -> {
            finish();
        });
        finishButton.setEnabled(false);

        cancelButton = new Button("Cancel");
        cancelButton.setWidth("60px");
        cancelButton.addClickListener(event -> {
            cancel();
        });
    }

    private void initDefaultHeader() {
        final WizardProgressBar progressBar = new WizardProgressBar(this);
        setHeader(progressBar);
    }

    /**
     * Sets a {@link Component} that is displayed on top of the actual content. Set
     * to {@code null} to remove the header altogether.
     * 
     * @param newHeader {@link Component} to be displayed on top of the actual
     *                  content or {@code null} to remove the header.
     */
    private void setHeader(final WizardProgressBar newHeader) {
        if (header != null) {
            if (newHeader == null) {
                remove(header);
            } else {
                remove(header);
                add(newHeader);
            }
        } else {
            if (newHeader != null) {
                addComponentAsFirst(newHeader);
            }
        }
        this.header = newHeader;
    }

    /**
     * Returns a {@link Component} that is displayed on top of the actual content or
     * {@code null} if no header is specified.
     * 
     * <p>
     * By default the header is a {@link WizardProgressBar} component that is also
     * registered as a {@link WizardProgressListener} to this Wizard.
     * </p>
     * 
     * @return {@link Component} that is displayed on top of the actual content or
     *         {@code null}.
     */
    public WizardProgressBar getHeader() {
        return header;
    }

    /**
     * Inserts the WizardStep at the specified position in this list. Shifts the
     * step currently at that position (if any) and any subsequent steps to the
     * right (adds one to their indices).
     */
    public void addStep(WizardStep step, int index) {
        String id = "wizard-step-" + step.hashCode();
        addStep(step, id, index);
    }

    /**
     * Inserts the WizardStep at the specified position in this list. Shifts the
     * step currently at that position (if any) and any subsequent steps to the
     * right (adds one to their indices).
     */
    public void addStep(WizardStep step, String id, int index) {
        if (idMap.containsKey(id)) {
            throw new IllegalArgumentException(String.format(
                    "A step with given id %s already exists. You must use unique identifiers for the steps.", id));
        }

        steps.add(index, step);
        idMap.put(id, step);

        updateButtons();

        // notify listeners
        fireEvent(new WizardStepSetChangedEvent(this, false));

        // activate the first step immediately
        if (currentStep == null) {
            activateStep(step);
        }

    }

    /**
     * Adds a step after an existing step
     * 
     * @param newStep      - new step to add
     * @param newId        - id for the new step
     * @param existingStep - an existing step after which the step will be inserted
     */
    public void addStepAfterStep(WizardStep newStep, String newId, WizardStep existingStep) {
        int idx = steps.indexOf(existingStep) + 1;
        if (idx < 0) {
            throw new IllegalArgumentException("Can not insert " + newStep + " after the step " + existingStep + " as "
                    + existingStep + " is not currently a step in the wizard");
        }
        addStep(newStep, newId, idx);
    }

    /**
     * Adds a step after an existing step
     * 
     * @param newStep      - new step to add
     * @param newId        - id for the new step
     * @param existingStep - an existing step after which the step will be inserted
     */
    public void addStepAfterStep(WizardStep newStep, WizardStep existingStep) {
        String id = "wizard-step-" + "-" + (stepIndex++);
        addStepAfterStep(newStep, id, existingStep);
    }

    /**
     * Adds a step to this Wizard with the given identifier. The used {@code id}
     * must be unique or an {@link IllegalArgumentException} is thrown. If you don't
     * wish to explicitly provide an identifier, you can use the
     * {@link #addStep(WizardStep)} method.
     * 
     * @param step
     * @param id
     * @throws IllegalStateException if the given {@code id} already exists.
     */
    public void addStep(WizardStep step, String id) {
        addStep(step, id, steps.size());
    }

    /**
     * Adds a step to this Wizard. The WizardStep will be assigned an identifier
     * automatically. If you wish to provide an explicit identifier for your
     * WizardStep, you can use the {@link #addStep(WizardStep, String)} method
     * instead.
     * 
     * @param step
     */
    public void addStep(WizardStep step) {
        addStep(step, "wizard-step-" + stepIndex++);
    }

    public List<WizardStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Add a listener for events that track when steps have been added or removed.
     * 
     * @param listener A {@link ComponentEventListener} for type
     *                 {@link WizardStepSetChangedEvent}
     * @return A {@link Registration} so that it can be removed if needed.
     */
    public Registration addStepChangeListener(ComponentEventListener<WizardStepSetChangedEvent> listener) {
        return addListener(WizardStepSetChangedEvent.class, listener);
    }

    /**
     * Add a listener for events that track when steps have been activated (i.e. as
     * the Wizard moves backwards and forwards.).
     * 
     * @param listener A {@link ComponentEventListener} for type
     *                 {@link WizardStepActivationEvent}
     * @return A {@link Registration} so that it can be removed if needed.
     */
    public Registration addStepActivationListener(ComponentEventListener<WizardStepActivationEvent> listener) {
        return addListener(WizardStepActivationEvent.class, listener);
    }

    /**
     * Add a listener for events that track when the 'Finish' button is clicked
     * (i.e., the wizard is completed).
     * 
     * @param listener A {@link ComponentEventListener} for type
     *                 {@link WizardCompletedEvent}
     * @return A {@link Registration} so that it can be removed if needed.
     */
    public Registration addCompletedChangeListener(ComponentEventListener<WizardCompletedEvent> listener) {
        return addListener(WizardCompletedEvent.class, listener);
    }

    /**
     * Add a listener for events that track when the 'Cancel' button is clicked
     * (i.e., the wizard has been interrupted by the user).
     * 
     * @param listener A {@link ComponentEventListener} for type
     *                 {@link WizardCancelledEvent}
     * @return A {@link Registration} so that it can be removed if needed.
     */
    public Registration addCancelledChangeListener(ComponentEventListener<WizardCancelledEvent> listener) {
        return addListener(WizardCancelledEvent.class, listener);
    }

    /**
     * Returns {@code true} if the given step is already completed by the user.
     * 
     * @param step step to check for completion.
     * @return {@code true} if the given step is already completed.
     */
    public boolean isCompleted(WizardStep step) {
        return steps.indexOf(step) < steps.indexOf(currentStep);
    }

    /**
     * Returns {@code true} if the given step is the currently active step.
     * 
     * @param step step to check for.
     * @return {@code true} if the given step is the currently active step.
     */
    public boolean isActive(WizardStep step) {
        return (step == currentStep);
    }

    private void updateButtons() {
        backButton.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
        if (isLastStep(currentStep)) {
            finishButton.setEnabled(true);
            finishButton.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
            nextButton.setEnabled(false);
            nextButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        } else {
            finishButton.setEnabled(false);
            finishButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            nextButton.setEnabled(true);
            nextButton.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
        }
        if (!isFirstStep(currentStep)) {
            backButton.setEnabled(true);
            backButton.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
        } else {
            backButton.setEnabled(false);
            backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        }
    }

    public Button getNextButton() {
        return nextButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public Button getFinishButton() {
        return finishButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    protected void activateStep(WizardStep step) {
        if (step == null) {
            return;
        }

        if (currentStep != null) {
            if (currentStep.equals(step)) {
                // already active
                return;
            }

            // ask if we're allowed to move
            boolean advancing = steps.indexOf(step) > steps.indexOf(currentStep);
            if (advancing) {
                if (!currentStep.onAdvance()) {
                    // not allowed to advance
                    return;
                }
            } else {
                if (!currentStep.onBack()) {
                    // not allowed to go back
                    return;
                }
            }

            // Keep track of the last step that was completed
            int currentIndex = steps.indexOf(currentStep);
            if (lastCompletedStep == null || steps.indexOf(lastCompletedStep) < currentIndex) {
                lastCompletedStep = currentStep;
            }
            // Note: using component.replace(old component, new component) does not work.
            contentPanel.removeAll();
        } 
        
        final Scroller scroller = new Scroller(step.getContent(), Scroller.ScrollDirection.VERTICAL);
        scroller.setSizeFull();
        contentPanel.add(scroller);

        currentStep = step;

        updateButtons();
        fireEvent(new WizardStepActivationEvent(this, false, step));
    }

    protected void activateStep(String id) {
        final WizardStep step = idMap.get(id);
        if (step != null) {
            // check that we don't go past the lastCompletedStep by using the id
            int lastCompletedIndex = lastCompletedStep == null ? -1 : steps.indexOf(lastCompletedStep);
            int stepIndex = steps.indexOf(step);

            if (lastCompletedIndex < stepIndex) {
                activateStep(lastCompletedStep);
            } else {
                activateStep(step);
            }
        }
    }

    protected String getId(WizardStep step) {
        for (Map.Entry<String, WizardStep> entry : idMap.entrySet()) {
            if (entry.getValue().equals(step)) {
                return entry.getKey();
            }
        }
        return null;
    }

    protected boolean isFirstStep(WizardStep step) {
        if (step != null) {
            return steps.indexOf(step) == 0;
        }
        return false;
    }

    protected boolean isLastStep(WizardStep step) {
        if (step != null && !steps.isEmpty()) {
            return steps.indexOf(step) == (steps.size() - 1);
        }
        return false;
    }

    /**
     * Cancels this Wizard triggering a {@link WizardCancelledEvent}. This method is
     * called when user clicks the cancel button.
     */
    public void cancel() {
        fireEvent(new WizardCancelledEvent(this, false));
    }

    /**
     * Triggers a {@link WizardCompletedEvent} if the current step is the last step
     * and it allows advancing (see {@link WizardStep#onAdvance()}). This method is
     * called when user clicks the finish button.
     */
    public void finish() {
        if (isLastStep(currentStep) && currentStep.onAdvance()) {
            // next (finish) allowed -> fire complete event
            fireEvent(new WizardCompletedEvent(this, false));
        }
    }

    /**
     * Activates the next {@link WizardStep} if the current step allows advancing
     * (see {@link WizardStep#onAdvance()}) or calls the {@link #finish()} method
     * the current step is the last step. This method is called when user clicks the
     * next button.
     */
    public void next() {
        if (isLastStep(currentStep)) {
            finish();
        } else {
            int currentIndex = steps.indexOf(currentStep);
            activateStep(steps.get(currentIndex + 1));
        }
    }

    /**
     * Activates the previous {@link WizardStep} if the current step allows going
     * back (see {@link WizardStep#onBack()}) and the current step is not the first
     * step. This method is called when user clicks the back button.
     */
    public void back() {
        int currentIndex = steps.indexOf(currentStep);
        if (currentIndex > 0) {
            activateStep(steps.get(currentIndex - 1));
        }
    }

    /**
     * Removes the supplied step from this Wizard. An {@link IllegalStateException}
     * is thrown if the step is already completed or is the currently active step.
     * 
     * @param stepToRemove The {@link WizardStep} to be removed.
     */
    public void removeStep(WizardStep stepToRemove) {
        if (idMap.containsValue(stepToRemove)) {
            for (Map.Entry<String, WizardStep> entry : idMap.entrySet()) {
                if (entry.getValue().equals(stepToRemove)) {
                    // delegate the actual removal to the overloaded method
                    removeStep(entry.getKey());
                    return;
                }
            }
        }
    }

    /**
     * Removes the step with given id from this Wizard. An
     * {@link IllegalStateException} is thrown if the given step is already
     * completed or is the currently active step.
     * 
     * @param id identifier of the step to remove.
     * @see #isCompleted(WizardStep)
     * @see #isActive(WizardStep)
     */
    public void removeStep(String id) {
        if (idMap.containsKey(id)) {
            WizardStep stepToRemove = idMap.get(id);
            if (isCompleted(stepToRemove)) {
                throw new IllegalStateException("Already completed step cannot be removed.");
            }
            if (isActive(stepToRemove)) {
                throw new IllegalStateException("Currently active step cannot be removed.");
            }

            idMap.remove(id);
            steps.remove(stepToRemove);

            // notify listeners
            fireEvent(new WizardStepSetChangedEvent(this, false));
        }
    }
}
