package au.com.vaadinutils.audit;

import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.events.CrudEventType;

/**
 * @deprecated Will be removed once dependent classes are removed.
 */
public class AuditorLoggingImpl implements Auditor {
    Logger logger = org.apache.logging.log4j.LogManager.getLogger();

    @Override
    public void audit(CrudEventType event, CrudEntity entity) {
        logger.info("{} {} {} {}", event.toString(), entity.getClass().getSimpleName(), entity.getName(),
                entity.getId());

    }
    // Logger logger = org.apache.logging.log4j.LogManager.getLogger();
}
