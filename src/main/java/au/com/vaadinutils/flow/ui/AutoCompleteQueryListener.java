package au.com.vaadinutils.flow.ui;

public interface AutoCompleteQueryListener<E> {
    void handleQuery(AutoCompleteTextField<E> field, String queryText);
}
