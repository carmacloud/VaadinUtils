package au.com.vaadinutils.crud;

/**
 * @deprecated Replaced in V14 migration.
 */
public class ContainerCSVExportData {
    final String value;

    public ContainerCSVExportData(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
