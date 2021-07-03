/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

/**
 *
 * @author azrul
 */

public class Date2StringFieldBridge implements TwoWayFieldBridge {

    //private Date dateValue;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void set(final String name, final Object value, final Document document, final LuceneOptions luceneOptions) {
        if (value==null){
            return;
        }
        //luceneOptions.
        LocalDate localdate = (LocalDate)value;
        Date dateValue = convertToDate(localdate);
        if (value != null) {
            
            String dateString = sdf.format(dateValue);
            if (luceneOptions != null && document != null) {
                final Field field = new Field(name, dateString, luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getTermVector());
                field.setBoost(luceneOptions.getBoost());
                document.add(field);
            }

        }
    }

   

    @Override
    public Object get(final String arg0, final Document arg1) {
        if (arg0==null){
            return null;
        }
        try {
            
            Date dateValue =  sdf.parse(arg0);
            return convertToLocalDate(dateValue);
        } catch (ParseException ex) {
            Logger.getLogger(Date2StringFieldBridge.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String objectToString(final Object arg0) {
        return arg0 == null ? "" : sdf.format(arg0);
    }
    
    public Date convertToDate(LocalDate dateToConvert) {
    return java.util.Date.from(dateToConvert.atStartOfDay()
      .atZone(ZoneId.systemDefault())
      .toInstant());
}
    public LocalDate convertToLocalDate(Date dateToConvert) {
    return dateToConvert.toInstant()
      .atZone(ZoneId.systemDefault())
      .toLocalDate();
    }

}
