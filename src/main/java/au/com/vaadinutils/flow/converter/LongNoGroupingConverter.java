package au.com.vaadinutils.flow.converter;

import java.text.NumberFormat;
import java.util.Locale;

import com.vaadin.flow.data.converter.StringToLongConverter;

/**
 * Extend the default String to Long Converter to allow an {@link Integer}
 * format without grouping (i.e., no comma)
 */
public class LongNoGroupingConverter extends StringToLongConverter {

    private static final long serialVersionUID = -5880695178732918209L;
    private final NumberFormat format = NumberFormat.getIntegerInstance();

    public LongNoGroupingConverter(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected NumberFormat getFormat(Locale locale) {
        format.setGroupingUsed(false);
        return format;
    }

}
