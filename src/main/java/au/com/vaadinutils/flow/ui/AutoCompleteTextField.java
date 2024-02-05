package au.com.vaadinutils.flow.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.vaadin.componentfactory.Popup;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.BlurNotifier.BlurEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import au.com.vaadinutils.flow.helper.VaadinHelper;

public class AutoCompleteTextField<E> extends Div {

    private static final long serialVersionUID = -6634513296678504250L;
    private final TextField field = new TextField();
    private final Popup popup = new Popup();
    private final Map<String, E> options = new LinkedHashMap<>();
    private AutoCompleteQueryListener<E> listener;
    private AutoCompleteOptionSelected<E> optionListener;

    private long dropDownWidth = 120;

    /**
     * <pre>
     * {@code
     * sample usage
     * 
     * 	AutoCompleteTextField<PostCode> suburb = new AutoCompleteTextField<>();
     * 
     * suburb.setQueryListener(new AutoCompleteQueryListener<PostCode>()
     * {
     * 
     * 	    &#64;Override
     * 	    public void handleQuery(AutoCompleteTextField<PostCode> field,String queryText)
     * 	    {
     * 		    field.addOption(new PostCode(3241),"Title");
     * 	    }
     * 	});
     * 
     * 	suburb.setOptionSelectionListener(new AutoCompleteOptionSelected<PostCode>()
     * 	{
     * 	    
     * 	    &#64;Override
     * 	    public void optionSelected(AutoCompleteTextField<PostCode> field, PostCode option)
     * 	    {
     * 		field.setValue(option.getSuburb());
     * 	    }
     * 	});
     * }
     * </pre>
     * 
     * The ContextMenu is removed from the parent after each interaction to allow
     * the default context menu to still work for copy/paste. However there is an
     * issue with the ContextMenuClosedEvent firing before the
     * ContextMenuItemClickEvent. This means that the ContextMenu can't be removed
     * from the parent in onContextMenuClosed, otherwise contextMenuItemClicked is
     * never entered. The effect of this is that the default context menu stops
     * working if the user clicks out of the auto complete context menu without
     * selecting anything.
     */

    public AutoCompleteTextField(final String fieldCaption, final String listCaption) {
        Preconditions.checkNotNull(listCaption, "List Caption is required to link the popup to the field.");
        Preconditions.checkArgument(listCaption.length() > 0,
                "List Caption is required to link the popup to the field.");
        field.setClassName(listCaption);
        field.setId(listCaption);
        field.setLabel(fieldCaption);
        field.setClearButtonVisible(true);
        popup.setFor(listCaption);

        add(field, popup);

        // Set as Lazy and if also set, there can be a timeout value.
        field.setValueChangeMode(ValueChangeMode.LAZY);
        field.addValueChangeListener(valueChangeListener -> {
            if (listener != null) {
                options.clear();
                listener.handleQuery(AutoCompleteTextField.this, valueChangeListener.getValue());
            }

            if (valueChangeListener.isFromClient()) {
                showOptionMenu();
            }
        });
    }

    private void showOptionMenu() {
        final List<String> listItems = new ArrayList<String>();
        for (final Entry<String, E> option : options.entrySet()) {
            listItems.add(option.getKey());
        }

        popup.show();
        popup.removeAll();
        final VerticalLayout layout = new VerticalLayout();
        layout.setWidth(dropDownWidth, Unit.PIXELS);
        for (final String string : listItems) {
            final Span span = new Span(
                    new Html("<font color='" + VaadinHelper.CARMA_DARK_BLACK + "'>" + string + "</font>"));
            span.setId(string);
            layout.add(span);
            span.addClickListener(e -> {
                optionListener.optionSelected(AutoCompleteTextField.this, options.get(string));
                // Clear list and hide
                popup.removeAll();
                popup.hide();
            });
        }

        popup.add(layout);
    }

    public long getDropDownWidth() {
        return dropDownWidth;
    }

    public void setDropDownWidth(final long dropDownWidth) {
        this.dropDownWidth = dropDownWidth;
    }

    public void setOptionSelectionListener(final AutoCompleteOptionSelected<E> listener) {
        this.optionListener = listener;
    }

    public void removeOptionSelectionListener() {
        optionListener = null;
    }

    public void setQueryListener(final AutoCompleteQueryListener<E> listener) {
        this.listener = listener;
    }

    public void removeQueryListener() {
        listener = null;
    }

    public void addOption(final E option, final String optionLabel) {
        options.put(optionLabel, option);
    }

    public TextField getField() {
        return this.field;
    }

    public void addValueChangeListener(
            final ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> event) {
        field.addValueChangeListener(event);
    }

    public void addBlurListener(final ComponentEventListener<BlurEvent<TextField>> event) {
        field.addBlurListener(event);
    }

    public void setTextChangeTimeout(final int timeout) {
        field.setValueChangeTimeout(timeout);
    }

    public void hideAutoComplete() {
        popup.hide();
    }
}
