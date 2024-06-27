package au.com.vaadinutils.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
// Make this annotation accessible at runtime via reflection.
@Target({ ElementType.TYPE })
// This annotation can only be applied to classes.
// Will be removed once dependent classes are removed.
public @interface Menus {
    Menu[] menus();
}
