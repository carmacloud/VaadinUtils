package au.com.vaadinutils.dao;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

public interface JoinMetaData<E, K> {

    Join<E, K> getJoin(Root<E> root);

    Join<E, K> getJoin(Join<?, E> join);
}