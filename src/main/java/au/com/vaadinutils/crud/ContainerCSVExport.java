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
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;

import au.com.vaadinutils.fields.ClickableLabel;
import au.com.vaadinutils.flow.helper.VaadinHelper;
import au.com.vaadinutils.flow.helper.VaadinHelper.NotificationType;
import au.com.vaadinutils.flow.jasper.AttachmentType;
import au.com.vaadinutils.util.PipedOutputStreamWrapper;

/**
 * @deprecated Replaced in V14 migration.
 */
public class ContainerCSVExport<E> {
    PipedOutputStreamWrapper stream = new PipedOutputStreamWrapper();
    Logger logger = org.apache.logging.log4j.LogManager.getLogger();
    private HeadingPropertySet headingsSet;
    private Table table;
    private LinkedHashMap<String, Object> extraColumnHeadersAndPropertyIds;

    public ContainerCSVExport(final String fileName, final Table table, final HeadingPropertySet headingsSet) {
        this.table = table;
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

                    export(table, bufferedWriter, headingsSet);
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

        final StreamResource resource = new StreamResource(source, fileName + AttachmentType.CSV.getFileExtension());
        resource.setMIMEType(AttachmentType.CSV.getMIMETypeString());

        final FileDownloader fileDownloader = new FileDownloader(resource);
        fileDownloader.setOverrideContentType(false);
        fileDownloader.extend(downloadButton);
        return downloadButton;
    }

    public void export(Table table, Writer stream, HeadingPropertySet headingsSet) throws IOException {

        final CSVWriter writer = new CSVWriter(stream);

        final Map<String, Object> headerPropertyMap = new LinkedHashMap<>();

        final  List<HeadingToPropertyId> cols = headingsSet.getColumns();
        for (HeadingToPropertyId col : cols) {
            headerPropertyMap.put(col.getHeader(), col.getPropertyId());
        }

        final  List<String> headerList = new LinkedList<>();
        headerList.addAll(headerPropertyMap.keySet());
        extraColumnHeadersAndPropertyIds = getExtraColumnHeadersAndPropertyIds();
        headerList.addAll(extraColumnHeadersAndPropertyIds.keySet());

        writeHeaders(writer, headerList);

        final  Set<Object> properties = new LinkedHashSet<>();
        properties.addAll(headerPropertyMap.values());

        int ctr = 0;
        for (Object id : table.getContainerDataSource().getItemIds()) {
            writeRow(writer, table, id, properties);
            ctr++;
            if (ctr > 100000) {
                writer.writeNext(new String[] { "Export stopped at 100,000 lines." });
                break;
            }
        }

        writer.flush();

    }

    private void writeRow(CSVWriter writer, Table table, Object id, Set<Object> properties) {
        final  Item item = table.getItem(id);
        final  String[] values = new String[properties.size() + extraColumnHeadersAndPropertyIds.size()];
        int i = 0;
        for (Object propertyId : properties) {
            @SuppressWarnings("rawtypes")
            final Property itemProperty = item.getItemProperty(propertyId);
            if (itemProperty != null && itemProperty.getValue() != null) {
                ColumnGenerator generator = table.getColumnGenerator(propertyId);

                // added handling for generated Boolean columns, - just using
                // the default property toString()

                if (generator != null && itemProperty.getType() != Boolean.class) {
                    Object value = generator.generateCell(table, id, propertyId);
                    if (value instanceof Label) {
                        value = new HtmlToPlainText().getPlainText(Jsoup.parse(((Label) value).getValue()));
                    }
                    if (value instanceof AbstractLayout) {
                        value = new HtmlToPlainText().getPlainText(Jsoup.parse(itemProperty.getValue().toString()));
                    }
                    if (value instanceof Link) {
                        value = new HtmlToPlainText().getPlainText(Jsoup.parse(itemProperty.getValue().toString()));
                    }
                    if (value != null) {
                        values[i++] = value.toString();
                    }
                } else {
                    values[i++] = itemProperty.getValue().toString();
                }
            } else {
                ColumnGenerator generator = table.getColumnGenerator(propertyId);
                if (generator != null) {
                    Object value = generator.generateCell(table, id, propertyId);
                    if (value != null) {
                        if (value instanceof ClickableLabel) {
                            value = new HtmlToPlainText()
                                    .getPlainText(Jsoup.parse(((ClickableLabel) value).getValue()));
                        }

                        if (value instanceof Label) {
                            value = new HtmlToPlainText().getPlainText(Jsoup.parse(((Label) value).getValue()));
                            // value = ((Label) value).getValue();
                        }

                        if (value instanceof AbstractLayout) {

                            // if you want your generated field to be exported,
                            // set a string using setData() on the layout.
                            if (((AbstractLayout) value).getData() instanceof ContainerCSVExportData) {
                                value = ((AbstractLayout) value).getData().toString();
                            } else {
                                value = "";
                            }
                        }
                        if (value instanceof Link) {
                            value = new HtmlToPlainText().getPlainText(Jsoup.parse(((Link) value).getCaption()));
                        }
                    }
                    if (value == null) {
                        value = "";
                    }
                    values[i++] = value.toString();

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
