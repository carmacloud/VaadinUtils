package au.com.vaadinutils.flow.errorhandling;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.DescriptorException;

public class ViolationConstraintHandler {
    private final static Logger logger = LogManager.getLogger();

    /**
     * Logs the initial error and calls the recursive version of itself. Always
     * throws a runtime exception.
     *
     * @param e
     */
    static void expandException(final Throwable e) {
        if (e instanceof PersistenceException) {
            handlePersistenceException((PersistenceException) e);
        }
        logger.error(e, e);
        handleConstraintViolationException(e, 5);
        throw new RuntimeException(e);
    }

    /**
     * Digs down looking for a useful exception, it will throw a runtime exception
     * if it finds a useful exception.
     *
     * @param e
     * @param nestLimit
     */

    private static void handleConstraintViolationException(final Throwable e, int nestLimit) {
        if (nestLimit > 0 && e != null) {
            nestLimit--;
            if (e instanceof DescriptorException) {
                final DescriptorException desc = (DescriptorException) e;

                throw new RuntimeException(desc.getMessage());
            }
            if (e instanceof ConstraintViolationException) {
                String groupedViolationMessage = e.getClass().getSimpleName() + " ";
                for (final ConstraintViolation<?> violation : ((ConstraintViolationException) e)
                        .getConstraintViolations()) {
                    logger.error("{}", violation.getLeafBean().getClass().getCanonicalName());
                    final String violationMessage = violation.getLeafBean().getClass().getSimpleName() + " "
                            + violation.getPropertyPath() + " " + violation.getMessage() + ", the value was "
                            + violation.getInvalidValue();
                    logger.error(violationMessage);
                    groupedViolationMessage += violationMessage + "\n";
                }
                throw new RuntimeException(groupedViolationMessage);
            }

            handleConstraintViolationException(e.getCause(), nestLimit);
        }
    }

    static private void handlePersistenceException(final Exception e) {
        if (e.getCause() instanceof PersistenceException) {
            String tmp = e.getMessage();
            final PersistenceException pex = (PersistenceException) e.getCause();
            if (pex.getCause() instanceof DatabaseException) {
                final DatabaseException dex = (DatabaseException) pex.getCause();
                tmp = dex.getMessage();
                if (tmp.indexOf("Query being") > 0) {
                    // strip of the query
                    tmp = tmp.substring(0, tmp.indexOf("Query being"));

                    if (tmp.contains("MySQL")) {
                        tmp = tmp.substring(tmp.indexOf("MySQL") + 5);
                    }
                }
            }
            logger.error(e, e);
            throw new RuntimeException(tmp);
        }
    }
}