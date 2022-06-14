package au.com.vaadinutils.flow.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.textfield.TextField;

public class AutoCompleteTextField<E> extends TextField {
    private static final long serialVersionUID = 1L;

    private AutoCompleteQueryListener<E> listener;
    private Map<E, String> options = new LinkedHashMap<>();
    private AutoCompleteOptionSelected<E> optionListener;
    private ContextMenu contextMenu;
    private boolean isContextMenuOpen = false;

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

    public AutoCompleteTextField(final String caption) {
        this();
        setLabel(caption);
    }

    public AutoCompleteTextField() {
//        setTextChangeEventMode(TextChangeEventMode.LAZY);
//        setImmediate(true);
        addValueChangeListener(valueChangeListener -> {
                options.clear();

                if (listener != null) {
                    listener.handleQuery(AutoCompleteTextField.this, valueChangeListener.getValue());
                }

                if (!options.isEmpty()) {
                    createContextMenu();
                    showOptionMenu();
                }
        });
    }

    private void createContextMenu() {
        // Don't create a new context menu if the existing one is still open
        if (isContextMenuOpen) {
            return;
        }

        // Create a new ContextMenu as each instance can only be added and
        // removed from a parent once
        contextMenu = new ContextMenu(this);
//        contextMenu.setAsContextMenuOf(this);
//        contextMenu.setOpenAutomatically(false);

        contextMenu.addOpenedChangeListener(listener -> {
                isContextMenuOpen = listener.isOpened();
        });
    }

    private void showOptionMenu() {
//        contextMenu.removeAllItems();
//        contextMenu.open();
        isContextMenuOpen = true;

        for (final Entry<E, String> option : options.entrySet()) {
            final MenuItem menuItem = contextMenu.addItem(option.getValue());
            menuItem.addClickListener(itemClickListener -> {
                    optionListener.optionSelected(AutoCompleteTextField.this, option.getKey());

                    // Remove the context menu when it gets closed to allow the
                    // default context menu to still be available
                    contextMenu.remove();
            });
        }
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
        options.put(option, optionLabel);
    }

    public void hideAutoComplete() {
        if (contextMenu != null) {
            contextMenu.close();
        }
    }
}
