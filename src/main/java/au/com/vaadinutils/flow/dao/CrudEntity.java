package au.com.vaadinutils.flow.dao;

import java.io.Serializable;

public interface CrudEntity extends Serializable {

    Long getId();

    void setId(Long id);

    /**
     * Used when displaying messages that need to identify an individual entity to
     * the user
     * 
     * @return
     */
    String getName();
}