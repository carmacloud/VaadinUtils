package au.com.vaadinutils.fields;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;

import au.com.vaadinutils.listener.ListenerManager;
import au.com.vaadinutils.listener.ListenerManagerFactory;

/**
 * Replaced in V14 migration.
 */
public class DataBoundButton<T> extends Button implements Field<T> {
    private static final long serialVersionUID = 2137449474336770169L;
    private Property<T> dataSource;
    private boolean required = false;
    private String requiredMessage = "";
    private T value;

    final Class<T> type;
    private boolean invalidCommitted = false;
    private boolean buffered = false;

    Set<Validator> validators = new HashSet<>();
    private boolean invalidAllowed = true;

    transient Logger logger = LogManager.getLogger(DataBoundButton.class);

    ListenerManager<ValueChangeListener> listeners = ListenerManagerFactory.createListenerManager("DataBoundButton",
            10);

    DataBoundButton(final Class<T> type) {
        this.type = type;
    }

    public DataBoundButton(final String fieldLabel, final Class<T> type2) {
        super(fieldLabel);
        type = type2;
    }

    @Override
    public boolean isInvalidCommitted() {
        return invalidCommitted;
    }

    @Override
    public void setInvalidCommitted(final boolean isCommitted) {
        invalidCommitted = isCommitted;

    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        dataSource.setValue(value);

    }

    @Override
    public void discard() throws SourceException {

        value = dataSource.getValue();

    }

    @Override
    public void setBuffered(final boolean buffered) {
        this.buffered = buffered;

    }

    @Override
    public boolean isBuffered() {

        return buffered;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void addValidator(final Validator validator) {
        validators.add(validator);

    }

    @Override
    public void removeValidator(final Validator validator) {
        validators.remove(validator);

    }

    @Override
    public void removeAllValidators() {
        validators.clear();

    }

    @Override
    public Collection<Validator> getValidators() {
        return validators;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void validate() throws InvalidValueException {

    }

    @Override
    public boolean isInvalidAllowed() {
        return invalidAllowed;
    }

    @Override
    public void setInvalidAllowed(final boolean invalidValueAllowed) throws UnsupportedOperationException {
        invalidAllowed = invalidValueAllowed;
    }

    @Override
    public T getValue() {
        logger.info(value);
        return value;
    }

    @Override
    public void setValue(final T newValue) throws ReadOnlyException {
        logger.info(value);
        value = newValue;

    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public void addValueChangeListener(final ValueChangeListener listener) {
        listeners.addListener(listener);

    }

    @Override
    public void addListener(final ValueChangeListener listener) {
        listeners.addListener(listener);

    }

    @Override
    public void removeValueChangeListener(final ValueChangeListener listener) {
        listeners.removeListener(listener);

    }

    @Override
    public void removeListener(final ValueChangeListener listener) {
        listeners.removeListener(listener);

    }

    @Override
    public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
        logger.info("Value changed");

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void setPropertyDataSource(final Property newDataSource) {
        logger.info("data source set");
        this.dataSource = newDataSource;
        value = dataSource.getValue();

    }

    @Override
    public Property<T> getPropertyDataSource() {
        return dataSource;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public void setRequired(final boolean required) {
        this.required = required;

    }

    @Override
    public void setRequiredError(final String requiredMessage) {
        this.requiredMessage = requiredMessage;

    }

    @Override
    public String getRequiredError() {
        return requiredMessage;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

}
