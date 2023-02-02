package au.com.vaadinutils.crud;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;

import com.opencsv.CSVWriter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.mpr.LegacyWrapper;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import au.com.vaadinutils.flow.helper.VaadinHelper;
import au.com.vaadinutils.flow.helper.VaadinHelper.NotificationType;
import au.com.vaadinutils.flow.jasper.AttachmentType;
import au.com.vaadinutils.util.PipedOutputStreamWrapper;

/**
 * @deprecated Replaced in V14 migration.
 */
public class GridContainerCSVExport<E> {
    PipedOutputStreamWrapper stream = new PipedOutputStreamWrapper();
    Logger logger = org.apache.logging.log4j.LogManager.getLogger();
    private GridHeadingPropertySet<E> headingsSet;
    private Grid grid;
    private LinkedHashMap<String, Object> extraColumnHeadersAndPropertyIds;

    public GridContainerCSVExport(final String fileName, final Grid grid, final GridHeadingPropertySet<E> headingsSet) {

        this.grid = grid;
        this.headingsSet = headingsSet;
        final Dialog window = new Dialog();
        window.setHeight("100px");
        window.setWidth("300px");
        window.setModal(true);

        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);

        window.setCloseOnEsc(false);
        window.setCloseOnOutsideClick(false);

        window.add(new LegacyWrapper(layout));
        window.open();
        window.setVisible(true);

        final Button downloadButton = createDownloadButton(fileName, window);

        layout.addComponent(downloadButton);
        layout.setComponentAlignment(downloadButton, Alignment.MIDDLE_CENTER);
    }

    private Button createDownloadButton(final String fileName, final Dialog window) {
        final Button downloadButton = new Button("Download CSV Data");
        downloadButton.setDisableOnClick(true);

        @SuppressWarnings("serial")
        StreamSource source = new StreamSource() {

            @Override
            public InputStream getStream() {

                try {
                    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(arrayOutputStream));

                    export(grid, bufferedWriter, headingsSet);
                    return new ByteArrayInputStream(arrayOutputStream.toByteArray());
                } catch (Throwable e) {
                    logger.error(e, e);
                    VaadinHelper.notificationDialog(e.getMessage(), NotificationType.ERROR);
                } finally {
                    final UI ui = UI.getCurrent();
                    try {
                        Thread.sleep(500);
                        ui.access(() -> {
                            window.close();
                        });
                    } catch (InterruptedException e1) {
                        logger.error(e1, e1);
                    }
                }
                return null;
            }
        };

        StreamResource resource = new StreamResource(source, fileName + AttachmentType.CSV.getFileExtension());
        resource.setMIMEType(AttachmentType.CSV.getMIMETypeString());

        FileDownloader fileDownloader = new FileDownloader(resource);
        fileDownloader.setOverrideContentType(false);
        fileDownloader.extend(downloadButton);
        return downloadButton;
    }

    public void export(Grid grid, Writer stream, GridHeadingPropertySet<E> headingsSet) throws IOException {

        CSVWriter writer = new CSVWriter(stream);

        Map<String, Object> headerPropertyMap = new LinkedHashMap<>();

        List<GridHeadingToPropertyId> cols = headingsSet.getColumns();
        for (GridHeadingToPropertyId col : cols) {
            headerPropertyMap.put(col.getHeader(), col.getPropertyId());
        }

        List<String> headerList = new LinkedList<>();
        headerList.addAll(headerPropertyMap.keySet());
        extraColumnHeadersAndPropertyIds = getExtraColumnHeadersAndPropertyIds();
        headerList.addAll(extraColumnHeadersAndPropertyIds.keySet());

        writeHeaders(writer, headerList);

        Set<Object> properties = new LinkedHashSet<>();
        properties.addAll(headerPropertyMap.values());

        for (Object id : grid.getContainerDataSource().getItemIds()) {
            writeRow(writer, grid, id, properties);
        }

        writer.flush();

    }

    private void writeRow(CSVWriter writer, Grid grid, Object id, Set<Object> properties) {
        Item item = grid.getContainerDataSource().getItem(id);
        String[] values = new String[properties.size() + extraColumnHeadersAndPropertyIds.size()];
        int i = 0;
        for (Object propertyId : properties) {
            @SuppressWarnings("rawtypes")
            final Property itemProperty = item.getItemProperty(propertyId);
            if (itemProperty != null) {
                Object value = itemProperty.getValue();
                if (value != null) {
                    final Object convertedValue = convert(value);
                    if (convertedValue != null) {
                        values[i++] = sanitiseValue(convertedValue);
                    } else {
                        values[i++] = "";
                    }
                } else {
                    values[i++] = "";
                }
            }
        }

        for (Object columnId : extraColumnHeadersAndPropertyIds.values()) {
            String value = getValueForExtraColumn(item, columnId);
            if (value == null) {
                value = "";
            }
            values[i++] = value;
        }
        writer.writeNext(values);

    }

    public Object convert(Object value) {
        return value;
    }

    public String sanitiseValue(final Object value) {
        String sanitisedValue;

        if (value instanceof String) {
            sanitisedValue = new HtmlToPlainText().getPlainText(Jsoup.parse(value.toString()));
        } else {
            sanitisedValue = value.toString();
        }

        if (sanitisedValue == null) {
            sanitisedValue = "";
        }

        return sanitisedValue;
    }

    private void writeHeaders(CSVWriter writer, List<String> headers) {
        writer.writeNext(headers.toArray(new String[] {}));
    }

    /**
     * propertyId's will later be passed to getValueForExtraColumn so it can
     * generate the data for a column
     * 
     * @return - an ordered map where key=heading string and value = a unique
     *         propertyId
     */
    protected LinkedHashMap<String, Object> getExtraColumnHeadersAndPropertyIds() {
        return new LinkedHashMap<>();
    }

    /**
     * 
     * @param item
     * @param columnId - as specified in the map returned from
     *                 getExtraColumnHeadersAndPropertyIds()
     * @return
     */
    protected String getValueForExtraColumn(Item item, Object columnId) {
        return null;
    }

}
