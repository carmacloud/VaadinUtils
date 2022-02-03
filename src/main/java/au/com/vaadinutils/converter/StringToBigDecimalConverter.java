/*
 * Copyright 2000-2013 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package au.com.vaadinutils.converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.vaadin.data.util.converter.AbstractStringToNumberConverter;

/**
 * A converter that converts from {@link String} to {@link BigDecimal} and back.
 * Uses the given locale and a {@link NumberFormat} instance for formatting and
 * parsing.
 * <p>
 * Leading and trailing white spaces are ignored when converting from a String.
 * </p>
 * <p>
 * Override and overwrite {@link #getFormat(Locale)} to use a different format.
 * </p>
 * 
 * @author Vaadin Ltd
 * @since 7.2
 */
public class StringToBigDecimalConverter extends AbstractStringToNumberConverter<BigDecimal> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected NumberFormat getFormat(Locale locale) {
        NumberFormat numberFormat = super.getFormat(locale);
        if (numberFormat instanceof DecimalFormat) {
            ((DecimalFormat) numberFormat).setParseBigDecimal(true);
        }
        // Force this to 4, default is 3 and not sufficient for fields using values with
        // scale of 4 in DB.
        numberFormat.setMaximumFractionDigits(4);
        return numberFormat;
    }

    @Override
    public BigDecimal convertToModel(String value, Class<? extends BigDecimal> targetType, Locale locale)
            throws com.vaadin.data.util.converter.Converter.ConversionException {
        return (BigDecimal) convertToNumber(value, BigDecimal.class, locale);
    }

    @Override
    public Class<BigDecimal> getModelType() {
        return BigDecimal.class;
    }
}