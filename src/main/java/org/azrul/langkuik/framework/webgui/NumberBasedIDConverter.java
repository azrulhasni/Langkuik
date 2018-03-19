/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.framework.webgui;

import com.vaadin.data.util.converter.AbstractStringToNumberConverter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author azrulm
 */
public class NumberBasedIDConverter extends AbstractStringToNumberConverter<Number> {
    private final Class<? extends Number> numberClass;
    
    public NumberBasedIDConverter(Class<? extends Number> numberClass) {
        this.numberClass =numberClass;
    }

    @Override
    protected NumberFormat getFormat(Locale locale) {
        //NumberFormat numberFormat = super.getFormat(locale);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        if (numberClass.equals(BigDecimal.class)){
            decimalFormat.setParseBigDecimal(true);
        }
        
        return decimalFormat;
    }

    @Override
    public Number convertToModel(String value,
            Class<? extends Number> targetType, Locale locale)
            throws com.vaadin.data.util.converter.Converter.ConversionException {
        return (Number) convertToNumber(value, numberClass, locale);
    }

    @Override
    public Class<Number> getModelType() {
        return (Class<Number>) numberClass;
    }
}

