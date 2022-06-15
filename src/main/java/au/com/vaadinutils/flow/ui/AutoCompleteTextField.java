package au.com.vaadinutils.flow.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class AutoCompleteTextField<E> extends TextField {

    private static final long serialVersionUID = -6634513296678504250L;
    private final Dialog dialog = new Dialog();
    private final ListBox<String> list = new ListBox<String>();
    private final Map<String, E> options = new LinkedHashMap<>();
    private AutoCompleteQueryListener<E> listener;
    private AutoCompleteOptionSelected<E> optionListener;


    /**
     * <pre>
     * {@code
     * sample usage
     * 
     * 	AutoCompleteTextFieldV2<PostCode> suburb = new AutoCompleteTextFieldV2<>();
     * 
     * suburb.setQueryListener(new AutoCompleteQueryListener<PostCode>()
     * {
     * 
     * 	    &#64;Override
     * 	    public void handleQuery(AutoCompleteTextFieldV2<PostCode> field,String queryText)
     * 	    {
     * 		    field.addOption(new PostCode(3241),"Title");
     * 	    }
     * 	});
     * 
     * 	suburb.setOptionSelectionListener(new AutoCompleteOptionSelected<PostCode>()
     * 	{
     * 	    
     * 	    &#64;Override
     * 	    public void optionSelected(AutoCompleteTextFieldV2<PostCode> field, PostCode option)
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
        setClassName(fieldCaption);
        dialog.add(list);

        setValueChangeMode(ValueChangeMode.LAZY);
        addValueChangeListener(valueChangeListener -> {
            options.clear();

            if (listener != null) {
                listener.handleQuery(AutoCompleteTextField.this, valueChangeListener.getValue());
            }

            if (!options.isEmpty()) {
                if (valueChangeListener.isFromClient()) {
                    showOptionMenu();
                }
            }
        });
    }

    private void showOptionMenu() {
        final List<String> listItems = new ArrayList<String>();
        for (final Entry<String, E> option : options.entrySet()) {
            listItems.add(option.getKey());
        }

        list.setItems(listItems);
        dialog.open();
        list.addValueChangeListener(value -> {
            if (value.isFromClient()) {
                final E key = options.get(value.getValue());
                if (key != null) {
                    optionListener.optionSelected(AutoCompleteTextField.this, key);
                }
                dialog.close();
            }
        });
    }

    public void setOptionSelectionListener(AutoCompleteOptionSelected<E> listener) {
        this.optionListener = listener;
    }

    public void removeOptionSelectionListener() {
        optionListener = null;
    }

    public void setQueryListener(AutoCompleteQueryListener<E> listener) {
        this.listener = listener;
    }

    public void removeQueryListener() {
        listener = null;
    }

    public void addOption(E option, String optionLabel) {
        options.put(optionLabel, option);
    }

    public ListBox<String> getList() {
        return list;
    }

    public void hideAutoComplete() {
        dialog.close();
    }
}
