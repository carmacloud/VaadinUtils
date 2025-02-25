package au.com.vaadinutils.flow.helper;

import java.util.List;

import com.vaadin.flow.data.binder.Binder;

/**
 * For using in a simple {@link Binder} implementations when a {@link List} of
 * objects is needed.<br>
 * TODO LC: CAR-5635. This could be removed and a more direct use of methods in
 * the components using this class.
 *
 * @param <E>
 */
public class BeanList<E> {

    private List<E> beanList;

    public List<E> getBeanList() {
        return beanList;
    }

    public void setBeanList(final List<E> beanList) {
        this.beanList = beanList;
    }
}