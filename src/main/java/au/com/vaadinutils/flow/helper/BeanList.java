package au.com.vaadinutils.flow.helper;

import java.util.List;

import com.vaadin.flow.data.binder.Binder;

/**
 * For using in a simple {@link Binder} implementations when a {@link List} of
 * objects is needed.
 *
 * @param <E>
 */
public class BeanList<E> {

    private List<E> beanList;

    public List<E> getBeanList() {
        return beanList;
    }

    public void setBeanList(List<E> beanList) {
        this.beanList = beanList;
    }
}
