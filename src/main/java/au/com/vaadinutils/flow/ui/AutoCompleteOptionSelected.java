package au.com.vaadinutils.flow.ui;

public interface AutoCompleteOptionSelected<E> {
    public void optionSelected(AutoCompleteTextField<E> field, E option);
}
