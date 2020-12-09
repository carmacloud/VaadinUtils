package au.com.vaadinutils.crud.security;

import au.com.vaadinutils.crud.CrudSecurityManager;

public class DefaultSecurityManagerFactory implements SecurityManagerFactory {

    @Override
    public CrudSecurityManager buildSecurityManager(Class<?> baseCrudView) {
        return new AllowAllSecurityManager();
    }
}
