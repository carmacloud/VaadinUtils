package au.com.vaadinutils.audit;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.events.CrudEventType;

/**
 * @deprecated Will be removed once dependent classes are removed.
 */
public interface Auditor {

    void audit(CrudEventType event, CrudEntity entity);

}
