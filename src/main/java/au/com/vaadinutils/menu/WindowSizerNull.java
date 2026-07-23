package au.com.vaadinutils.menu;

/**
 * Will be removed once dependent classes are removed.
 */
public class WindowSizerNull implements WindowSizer {

    @Override
    public int width() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int height() {
        throw new RuntimeException("Not implemented");
    }
}
