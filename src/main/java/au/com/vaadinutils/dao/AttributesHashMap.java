package au.com.vaadinutils.dao;

import java.util.HashMap;

import jakarta.persistence.metamodel.SingularAttribute;

/**
 * This class is to be used with the find*By*Attributes methods in JpaBaseDao.
 * It is used for type checking of the singular attribute and the value to
 * search on.
 * 
 * @param <T> the class of the entity
 */
@SuppressWarnings("serial")
public class AttributesHashMap<T> extends HashMap<SingularAttribute<T, Object>, Object> {

    public AttributesHashMap() {
        this(5);
    }

    public AttributesHashMap(final int initialSize) {
        super(initialSize);
    }

    @SuppressWarnings("unchecked")
    public <K> void safePut(final SingularAttribute<T, K> key, final K value) {
        super.put((SingularAttribute<T, Object>) key, value);
    }

    @Override
    public Object put(final SingularAttribute<T, Object> key, final Object value) {
        throw new RuntimeException("Use safePut method!");
    }
}