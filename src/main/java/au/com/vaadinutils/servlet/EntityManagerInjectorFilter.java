package au.com.vaadinutils.servlet;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.EntityWorker;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class EntityManagerInjectorFilter implements Filter {
    // private static transient Logger logger =
    private final Logger logger = LogManager.getLogger();

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {
        try {
            EntityManagerProvider.setThreadLocalEntityManager(new EntityWorker<Void>() {

                @Override
                public Void exec() throws Exception {
                    filterChain.doFilter(servletRequest, servletResponse);
                    return null;
                }
            });
        } catch (final Exception e1) {
            // CAR-5548: temporary fix to suppress this error from clogging up the log
            // files.
            if ("Unregistered node was not found based on its id. The tree is most likely corrupted."
                    .equalsIgnoreCase(e1.getMessage())) {
                logger.error("Error: " + e1.getMessage());
            } else {
                logger.error(e1, e1);
            }
        }
    }

    @Override
    public void destroy() {
        // entityManagerFactory = null;
    }
}