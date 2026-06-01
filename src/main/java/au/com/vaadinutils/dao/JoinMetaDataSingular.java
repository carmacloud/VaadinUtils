package au.com.vaadinutils.dao;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;

public class JoinMetaDataSingular<E, K> implements JoinMetaData<E, K> {
    final SingularAttribute<E, K> attribute;
    final JoinType type;
    final boolean fetch;

    public JoinMetaDataSingular(final SingularAttribute<E, K> attribute, final JoinType type, final boolean fetch) {
        this.attribute = attribute;
        this.type = type;
        this.fetch = fetch;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        final JoinMetaDataSingular other = (JoinMetaDataSingular) obj;
        return type == other.type && attribute == other.attribute && fetch == other.fetch;
    }

    @Override
    public int hashCode() {
        return type.hashCode() + attribute.hashCode() + Boolean.valueOf(fetch).hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Join<E, K> getJoin(final Root<E> root) {
        for (final Fetch<E, ?> join : root.getFetches()) {
            if (join.getAttribute().equals(attribute) && join.getJoinType().equals(type)) {
                return (Join<E, K>) join;
            }
        }

        if (fetch) {
            return (Join<E, K>) root.fetch(attribute, type);
        }

        for (final Join<E, ?> join : root.getJoins()) {
            if (join.getAttribute().equals(attribute) && join.getJoinType().equals(type)) {
                return (Join<E, K>) join;
            }
        }

        return root.join(attribute, type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Join<E, K> getJoin(final Join<?, E> join) {
        for (final Fetch<E, ?> existingJoin : join.getFetches()) {
            if (existingJoin.getAttribute().equals(attribute) && existingJoin.getJoinType().equals(type)) {
                return (Join<E, K>) existingJoin;
            }
        }

        if (fetch) {
            return (Join<E, K>) join.fetch(attribute, type);
        }

        for (final Join<E, ?> existingJoin : join.getJoins()) {
            if (existingJoin.getAttribute().equals(attribute) && existingJoin.getJoinType().equals(type)) {
                return (Join<E, K>) existingJoin;
            }
        }

        return join.join(attribute, type);
    }

    @Override
    public String toString() {
        return attribute.getDeclaringType().getJavaType().getSimpleName() + "->" + attribute.getName() + ":"
                + type.toString();
    }
}